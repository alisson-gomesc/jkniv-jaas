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
package net.sf.jkniv.jaas;


/**
 * Exception thrown when a Realm is found to be corrupted for some reason.
 * 
 * @author Alisson Gomes
 */
public class BadRealmException extends RuntimeException
{
    /**
     * Constructs the exception, with descriptive information.
     *
     * @param info describes the problem with the realm
     */
    public BadRealmException (String info) { super (info); }
    
    public BadRealmException() {
        super();
    }
    
    public BadRealmException(Throwable cause) {
        super(cause);
    }
    
    public BadRealmException(String info, Throwable cause) {
        super(info, cause);
    }
}
