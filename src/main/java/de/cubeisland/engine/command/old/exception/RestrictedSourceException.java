package de.cubeisland.engine.command.old.exception;

public class RestrictedSourceException extends CommandException
{
    private final Class<?> clazz;

    public RestrictedSourceException(Class<?> clazz)
    {
        this.clazz = clazz;
    }

    public Class<?> getClazz()
    {
        return clazz;
    }
}