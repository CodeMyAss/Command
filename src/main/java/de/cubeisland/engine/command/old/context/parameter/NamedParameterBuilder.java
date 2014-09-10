package de.cubeisland.engine.command.old.context.parameter;

public abstract class NamedParameterBuilder<ParamT extends NamedParameter, SourceT> extends BaseParameterBuilder<ParamT, SourceT>
{
    protected NamedParameterBuilder(Class<ParamT> clazz)
    {
        super(clazz);
    }

    /**
     * Sets the name of the named parameter
     *
     * @param name the name
     */
    public void setName(String name)
    {
        this.param().name = name;
    }

    /**
     * Adds an alias that can be used as alternative name.
     *
     * @param name the alias
     */
    public void addAlias(String name)
    {
        this.param().aliases.add(name);
    }
}