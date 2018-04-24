/*
 * JKNIV JAAS,
 * Copyright 2017 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.jkniv.jaas.tomcat;


import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * JNDI resource access.
 *  
 * @author Alisson Gomes
 */
class JndiResources
{
    private static final Logger   LOG = MyLoggerFactory.getLogger(JndiResources.class);
    private static InitialContext ctx;
    
    static
    {
        try
        {
            ctx = new InitialContext();
        }
        catch (NamingException e)
        {
            throw new RuntimeException("Cannot initiate JNDI InitialContext", e);
        }
    }
    
    public static Object lookup(String value)
    {
        Object o = null;
        try
        {
            o = ctx.lookup(value);
            LOG.info("lookup successfully properties for jndi [" + value + "]");
        }
        catch (NamingException e)
        {
            LOG.warning("Cannot localize the jndi name [" + value + "]: " + e.getMessage());
        }
        return o;
    }
}
