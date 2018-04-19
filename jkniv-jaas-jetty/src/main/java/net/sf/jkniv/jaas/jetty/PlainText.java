/* 
 * JKNIV JAAS,
 * Copyright (C) 2017, the original author or authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.sf.jkniv.jaas.jetty;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class PlainText implements Cipher
{
    private static final String ALGO = Cipher.PLAIN_TEXT;
    private Charset charset;
    
    public PlainText()
    {
        this(Charset.forName("UTF-8"));
    }

    public PlainText(Charset charset)
    {
        this.charset = charset;
    }

    public String encode(String phrase) throws UnsupportedEncodingException
    {
        return phrase;
    }

    public String decode(String phrase)
    {
        return phrase;
    }
    
    public Charset getCharset()
    {
        return charset;
    }

    public String getAlgorithm()
    {
        return ALGO;
    }
    
    public boolean supportDecode()
    {
        return true;
    }

}
