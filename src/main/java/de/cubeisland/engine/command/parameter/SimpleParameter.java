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

import java.util.List;

import de.cubeisland.engine.command.CommandCall;
import de.cubeisland.engine.command.parameter.property.FixedValues;
import de.cubeisland.engine.command.parameter.property.Greed;

public class SimpleParameter extends Parameter
{
    public SimpleParameter()
    {
        this.setProperty(Greed.DEFAULT);
    }

    @Override
    protected boolean accepts(CommandCall call)
    {
        // Just checking if the parameter is possible here
        int greed = this.propertyValue(Greed.class); // cannot be null as greed is preset if not set manually
        int remainingTokens = call.tokens().length - call.consumed();
        String[] names = this.propertyValue(FixedValues.class);
        if (names != null)
        {
            remainingTokens--; // the name consumes a token
            String lcToken = call.currentToken().toLowerCase();
            for (String name : names)
            {
                if (name.equals(lcToken))
                {
                    if (greed == 0 || remainingTokens > 1 && (remainingTokens >= greed))
                    {
                        return true;
                    }
                }
            }
            return false; // No match for named
        }
        // Non named:
        if (greed == 0 || remainingTokens >= 1 && (remainingTokens >= greed))
        {
            return true;
        }
        return false;
    }

    @Override
    protected boolean parse(CommandCall call)
    {
        List<ParsedParameter> result = call.propertyValue(ParsedParameters.class);
        String[] names = this.propertyValue(FixedValues.class);
        if (names != null)
        {
            String name = call.currentToken().toLowerCase(); // previously matched in accepts(..)
            if (this.propertyValue(Greed.class) == 0)
            {
                result.add(ParsedParameter.of(this, call.getManager().read(this, call), name));
                return true;
            }
            call.consume(1); // else consume name
            // TODO somehow include the name ?
        }
        ParsedParameter pParam = this.parseValue(call); // TODO handle greedy params better
        if (!result.isEmpty() && result.get(result.size() - 1).getParameter().equals(pParam.getParameter()))
        {
            ParsedParameter last = result.remove(result.size() - 1);
            String joined = last.getParsedValue() + " " + pParam.getParsedValue();
            pParam = ParsedParameter.of(pParam.getParameter(), joined, joined);
        }
        result.add(pParam);
        return true;
    }

    protected ParsedParameter parseValue(CommandCall call)
    {
        int consumed = call.consumed();
        return ParsedParameter.of(this, call.getManager().read(this, call), call.tokensSince(consumed));
    }
}