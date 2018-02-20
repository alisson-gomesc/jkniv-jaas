/* 
 * JKNIV ,
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

package net.sf.jkniv.jaas.gf;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import com.sun.appserv.security.AppservPasswordLoginModule;

public class HybridLoginModule extends AppservPasswordLoginModule
{
    private static final Logger LOG = MyLoggerFactory.getLogger(HybridLoginModule.class);
    
    @Override
    protected void authenticateUser() throws LoginException
    {
        String[] grpList = null;
        if (!(_currentRealm instanceof HybridRealm))
            throw new LoginException(sm.getString("hybrid.jdbc.badrealm"));
        
        HybridRealm realm = (HybridRealm) _currentRealm;
        
        grpList = realm.authenticate(_username, _passwd);
        
        if (LOG.isLoggable(Level.FINER))
            LOG.finer("Hybrid login succeeded for: " + _username + " groups:" + Arrays.toString(grpList));
        
        // populate grpList with the set of groups to which _username belongs in this realm, if any
        commitUserAuthentication(grpList);
    }
    
}
