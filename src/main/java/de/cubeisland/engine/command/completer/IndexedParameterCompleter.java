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
package de.cubeisland.engine.command.completer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.command.BaseCommandContext;

import static de.cubeisland.engine.command.StringUtils.startsWithIgnoreCase;

public class IndexedParameterCompleter implements Completer
{
    private final Completer completer;
    private final Set<String> staticLabels;

    public IndexedParameterCompleter(Completer completer, Set<String> staticLabels)
    {
        this.completer = completer;
        this.staticLabels = staticLabels;
    }

    @Override
    public List<String> complete(BaseCommandContext context, String token)
    {
        List<String> result = new ArrayList<String>();
        if (this.completer != null)
        {
            result.addAll(this.completer.complete(context, token));
        }
        for (String staticLabel : staticLabels)
        {
            if (startsWithIgnoreCase(staticLabel, token))
            {
                result.add(staticLabel);
            }
        }
        return result;
    }


}
