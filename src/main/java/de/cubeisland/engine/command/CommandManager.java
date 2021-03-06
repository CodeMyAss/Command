/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Anselm Brehme, Phillip Schichtel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.cubeisland.engine.command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class CommandManager
{
    private Locale defaultLocale;
    private Map<Class, ResultManager> resultManagers = new HashMap<Class, ResultManager>();

    private final CommandExecutor executor;

    protected CommandManager(CommandExecutor executor)
    {
        this.executor = executor;
    }

    public void setDefaultLocale(Locale defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }

    public void registerCommand(BaseCommand command)
    {
        if (command.isRegistered())
        {
            throw new IllegalArgumentException("The given command is already registered!");
        }
        this.registerCommand0(command);
    }

    protected abstract void registerCommand0(BaseCommand command);

    public Locale getDefaultLocale()
    {
        return defaultLocale;
    }

    public void registerResultManager(ResultManager manager)
    {
        this.resultManagers.put(manager.getClass(), manager);
    }

    public <T extends ResultManager> T getResultManager(Class<T> clazz)
    {
        return (T)this.resultManagers.get(clazz);
    }

    protected abstract void logTabCompletion(BaseCommandSender sender, BaseCommand command, String[] args);
    protected abstract void logExecution(BaseCommandSender sender, BaseCommand command, String[] args);

    public abstract BaseCommand getCommand(String name);
    public abstract boolean runCommand(BaseCommandSender sender, String commandline);

    public CommandExecutor getExecutor()
    {
        return executor;
    }
}
