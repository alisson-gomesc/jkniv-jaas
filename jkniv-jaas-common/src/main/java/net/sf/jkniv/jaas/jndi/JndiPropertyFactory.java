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
package net.sf.jkniv.jaas.jndi;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

/**
 * <p>JNDI object creation factory for <code>Properties</code> instances.  
 * This makes it convenient to configure a properties in the global JNDI 
 * resources associated with web container and then link to that resource 
 * for web applications that administer the contents properties.</p>
 *
 * @author Alisson Gomes
 * @since 0.3.3
 */
public class JndiPropertyFactory implements ObjectFactory
{
    private static final Logger LOG = Logger.getLogger(JndiPropertyFactory.class.getName());
    
    /**
     * <p>Create and return a new {@code Properties} instance
     * that has been configured according to the properties of the
     * specified <code>Reference</code>.  If you instance can be created,
     * return <code>null</code> otherwise.</p>
     *
     * @param obj The possibly null object containing location or
     *  reference information that can be used in creating an object
     * @param name The name of this object relative to <code>nameCtx</code>
     * @param nameCtx The context relative to which the <code>name</code>
     *  parameter is specified, or <code>null</code> if <code>name</code>
     *  is relative to the default initial context
     * @param environment The possibly null environment that is used in
     *  creating this object
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
    {
        if ((obj == null) || !(obj instanceof Reference))
        {
            return (null);
        }
        Reference ref = (Reference) obj;
        if (!"java.util.Properties".equals(ref.getClassName()))
        {
            LOG.warning("Type of resource must be [java.util.Properties] found [" + ref.getClassName() + "]");
            return (null);
        }
        
        Properties properties = new Properties();
        for (Enumeration iter = ref.getAll(); iter.hasMoreElements();)
        {
            StringRefAddr addr = (StringRefAddr) iter.nextElement();
            LOG.finer("add property ["+addr.getType() + "] = " + addr.getContent()+" to Properties resource");
            //System.out.println("add property ["+addr.getType() + "] = " + addr.getContent()+" to Properties resource");
            properties.put(addr.getType(), (addr.getContent() == null) ? "" : addr.getContent());
        }
        return (properties);
    }
}
