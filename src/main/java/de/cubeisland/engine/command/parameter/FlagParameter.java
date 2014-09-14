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
package de.cubeisland.engine.command.parameter;

import de.cubeisland.engine.command.CommandCall;
import de.cubeisland.engine.command.parameter.property.FixedValues;

public class FlagParameter extends Parameter
{
    public FlagParameter(String name, String longName)
    {
        this.setProperty(new FixedValues(new String[]{name, longName}));
    }

    public final String name()
    {
        return this.propertyValue(FixedValues.class)[0];
    }

    public final String longName()
    {
        return this.propertyValue(FixedValues.class)[1];
    }

    @Override
    protected boolean accepts(CommandCall call)
    {
        String token = call.currentToken();
        if (token.startsWith("-"))
        {
            token = token.substring(1);
            for (String name : this.propertyValue(FixedValues.class))
            {
                if (name.equalsIgnoreCase(token))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean parse(CommandCall call)
    {
        String token = call.currentToken();
        call.consume(1);
        call.propertyValue(ParsedParameters.class).add(ParsedParameter.of(this, token, token));
        return true;
    }
}