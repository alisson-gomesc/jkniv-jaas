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
 */package net.sf.jkniv.jaas;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.jkniv.jaas.Cipher;
import net.sf.jkniv.jaas.MD5;
import net.sf.jkniv.jaas.SHA256;
import net.sf.jkniv.jaas.Hashing.HashingResult;

public class CipherContractValidatorTest
{
    Cipher cipher;
    String password = "123456";
    
    @Before
    public void setup() throws UnsupportedEncodingException 
    {
        this.cipher = mock(Cipher.class);
        given(cipher.checkCredential(password)).willReturn(true);
        given(cipher.decode(password)).willReturn("1");
        given(cipher.encode(password)).willReturn("1");
        given(cipher.encodeWithSalt(password)).willReturn(new String[]{"1", "2"});
        given(cipher.getAlgorithm()).willReturn("SHA256");
        given(cipher.getCharset()).willReturn(Charset.forName("UTF8"));
        given(cipher.supportDecode()).willReturn(false);
    }
    
    @Test
    public void whenValidateCipherContract() throws UnsupportedEncodingException 
    {
        
        assertThat(cipher.checkCredential(password), is(true));
        assertThat(cipher.decode(password), is("1"));
        assertThat(cipher.encode(password), is("1"));
        assertThat(cipher.encodeWithSalt(password), is(new String[]{"1", "2"}));
        assertThat(cipher.getAlgorithm(), is("SHA256"));
        assertThat(cipher.getCharset(), is(Charset.forName("UTF8")));
        assertThat(cipher.supportDecode(), is(false));

        verify(cipher).checkCredential(password);
        verify(cipher).decode(password);
        verify(cipher).encode(password);
        verify(cipher).encodeWithSalt(password);
        verify(cipher).getAlgorithm();
        verify(cipher).getCharset();
        verify(cipher).supportDecode();
    }
}
