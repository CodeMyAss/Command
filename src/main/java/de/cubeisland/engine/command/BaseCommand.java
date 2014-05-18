package de.cubeisland.engine.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.command.completer.CompleterUtils;
import de.cubeisland.engine.old.command.exception.IncorrectUsageException;
import de.cubeisland.engine.old.command.exception.MissingParameterException;
import de.cubeisland.engine.old.command.exception.PermissionDeniedException;

// TODO loggable
// TODO asnyc
public abstract class BaseCommand
{
    private final CommandManager manager;
    private final CommandOwner owner;
    private final String name;
    private final ContextFactory contextFactory;
    private final Map<String, BaseCommand> children = new HashMap<>();
    private final Map<String, AliasCommand> aliases = new HashMap<>();
    private String description;
    private CommandPermission permission;
    private String label;
    private BaseCommand parent;
    private boolean registered = false;

    private Class<? extends BaseCommandSender>[] restrictUsage = null;

    public BaseCommand(CommandManager manager, CommandOwner owner, String name, String description,
                       ContextFactory contextFactory, CommandPermission permission)
    {
        this.manager = manager;
        this.owner = owner;
        this.name = name;
        this.label = name;
        this.description = description;
        this.contextFactory = contextFactory;
        this.permission = permission;
    }

    public CommandOwner getOwner()
    {
        return owner;
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label == null ? this.name : label;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    protected void addAlias(String name, BaseCommand... parents)
    {
        if (parents.length == 0)
        {
            this.addAlias(new AliasCommand(this, name));
        }
        else
        {
            for (BaseCommand parent : parents)
            {
                parent.addAlias(new AliasCommand(this, name));
            }
        }
    }

    private void addAlias(AliasCommand alias)
    {
        this.aliases.put(alias.getName(), alias);
    }

    public void restrictUsage(Class<? extends BaseCommandSender>... to)
    {
        this.restrictUsage = to;
    }

    public boolean isRestricted(BaseCommandSender sender)
    {
        if (this.restrictUsage != null)
        {
            for (Class<? extends BaseCommandSender> clazz : restrictUsage)
            {
                if (clazz.isAssignableFrom(sender.getClass()))
                {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public ContextFactory getContextFactory()
    {
        return contextFactory;
    }

    public BaseCommand getParent()
    {
        return parent;
    }

    public boolean isRegistered()
    {
        return registered;
    }

    public void setRegistered()
    {
        this.registered = true;
    }

    public final String getUsage()
    {
        return "/" + StringUtils.implode(" ", this.getLabels()) + " " + this.getUsage(this.manager.getDefaultLocale(), null);
    }

    public final String getUsage(Locale locale, BaseCommandSender sender)
    {
        return this.getUsage0(locale, sender);
    }

    public String getUsage(BaseCommandSender sender)
    {
        return StringUtils.implode(" ", this.getLabels()) + " " + this.getUsage(sender.getLocale(),
                                                                    sender); // TODO if User append /
    }

    public String getUsage(CommandContext ctx)
    {
        return StringUtils.implode(" ", ctx.getLabels()) + " " + this.getUsage(ctx.getSender().getLocale(),
                                                                   ctx.getSender()); // TODO if User append /
    }

    protected String getUsage0(Locale locale, BaseCommandSender sender)
    {
        // TODO indexed permissions
        StringBuilder sb = new StringBuilder();
        int inGroup = 0;
        for (CommandParameterIndexed indexedParam : this.contextFactory.getIndexedParameters())
        {
            if (indexedParam.getCount() == 1 || indexedParam.getCount() < 0)
            {
                sb.append(convertLabel(indexedParam.isGroupRequired(), StringUtils.implode("|", convertLabels(
                    indexedParam))));
                sb.append(' ');
                inGroup = 0;
            }
            else if (indexedParam.getCount() > 1)
            {
                sb.append(indexedParam.isGroupRequired() ? '<' : '[');
                sb.append(convertLabel(indexedParam.isRequired(), StringUtils.implode("|", convertLabels(indexedParam))));
                sb.append(' ');
                inGroup = indexedParam.getCount() - 1;
            }
            else if (indexedParam.getCount() == 0)
            {
                sb.append(convertLabel(indexedParam.isRequired(), StringUtils.implode("|", convertLabels(indexedParam))));
                inGroup--;
                if (inGroup == 0)
                {
                    sb.append(indexedParam.isGroupRequired() ? '>' : ']');
                }
                sb.append(' ');
            }
        }

        for (CommandParameter param : this.getContextFactory().getParameters().values())
        {
            if (param.checkPermission(sender))
            {
                if (param.isRequired())
                {
                    sb.append('<').append(param.getName()).append(" <").append(param.getLabel()).append(">> ");
                }
                else
                {
                    sb.append('[').append(param.getName()).append(" <").append(param.getLabel()).append(">] ");
                }
            }
        }
        for (CommandFlag flag : this.getContextFactory().getFlags())
        {
            if (flag.checkPermission(sender))
            {
                sb.append("[-").append(flag.getLongName()).append("] ");
            }
        }
        return sb.toString().trim();
    }

    private List<String> convertLabels(CommandParameterIndexed indexedParam)
    {
        String[] labels = indexedParam.getLabels().clone();
        String[] rawLabels = indexedParam.getLabels();
        for (int i = 0; i < rawLabels.length; i++)
        {
            if (rawLabels.length == 1)
            {
                labels[i] = convertLabel(true, "!" + rawLabels[i]);
            }
            else
            {
                labels[i] = convertLabel(true, rawLabels[i]);
            }
        }
        return Arrays.asList(labels);
    }

    private String convertLabel(boolean req, String label)
    {
        if (label.startsWith("!"))
        {
            return label.substring(1);
        }
        else if (req)
        {
            return "<" + label + ">";
        }
        else
        {
            return "[" + label + "]";
        }
    }

    protected final Stack<String> getLabels()
    {
        Stack<String> cmds = new Stack<String>();
        BaseCommand cmd = this;
        do
        {
            cmds.push(cmd.getName());
        }
        while ((cmd = cmd.getParent()) != null);
        return cmds;
    }

    public final BaseCommand getChild(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.children.get(name.toLowerCase(Locale.ENGLISH));
    }

    public final Set<BaseCommand> getChildren()
    {
        return new HashSet<BaseCommand>(children.values());
    }

    public final boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    public final boolean hasChild(String name)
    {
        return name != null && this.children.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    public final void addChild(BaseCommand command)
    {
        if (command == null)
        {
            throw new IllegalArgumentException("The command must not be null!");
        }
        if (this == command)
        {
            throw new IllegalArgumentException("You can't register a command as a child of itself!");
        }
        if (command.isRegistered())
        {
            throw new IllegalArgumentException("The given command is already registered! Use aliases instead!");
        }

        this.children.put(command.getName(), command);
        command.parent = this;
    }

    /**
     * This method handles the command execution
     *
     * @param context The CommandContext containing all the necessary information
     */
    public abstract CommandResult run(CommandContext context);

    public void checkContext(CommandContext ctx)
    {
        if (!ctx.getCommand().permission.isAuthorized(ctx.getSender()))
        {
            throw new PermissionDeniedException(ctx.getCommand().permission);
        }

        ArgBounds bounds = ctx.getCommand().getContextFactory().getArgBounds();
        if (ctx.getIndexedCount() < bounds.getMin())
        {
            throw new IncorrectUsageException(ctx.getSender().getTranslation(
                "You've given too few arguments.")); // TODO COLOR code
        }
        if (bounds.getMax() > ArgBounds.NO_MAX && ctx.getIndexedCount() > bounds.getMax())
        {
            throw new IncorrectUsageException(ctx.getSender().getTranslation(
                "You've given too many arguments.")); // TODO COLOR code
        }
        if (ctx.getCommand().restrictUsage != null)
        {
            for (Class<? extends BaseCommandSender> clazz : ctx.getCommand().restrictUsage)
            {
                if (clazz.isAssignableFrom(ctx.getSender().getClass()))
                {
                    break;
                }
            }
            throw new IncorrectUsageException(ctx.getSender().getTranslation(
                "This command cannot be used by a " + ctx.getSender().getClass().getSimpleName())); // TODO COLOR code
        }
        // TODO permission for indexed
        for (CommandParameter param : this.getContextFactory().getParameters().values())
        {
            if (ctx.hasParam(param.getName()))
            {
                if (!param.checkPermission(ctx.getSender()))
                {
                    throw new PermissionDeniedException(param.getPermission());
                }
            }
            else if (param.isRequired())
            {
                throw new MissingParameterException(param.getName());
            }
        }
        for (CommandFlag flag : this.getContextFactory().getFlags())
        {
            if (ctx.hasFlag(flag.getName()) && !flag.checkPermission(ctx.getSender()))
            {
                throw new PermissionDeniedException(flag.getPermission());
            }
        }
    }

    public List<String> tabComplete(CommandContext context)
    {
        return CompleterUtils.tabComplete(context, context.last);
    }

    public abstract void help(CommandContext ctx);

    public CommandManager getCommandManager()
    {
        return this.manager;
    }

    public boolean isAuthorized(BaseCommandSender sender)
    {
        return this.permission.isAuthorized(sender);
    }
}
