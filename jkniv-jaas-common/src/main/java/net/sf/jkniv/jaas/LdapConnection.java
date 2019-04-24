package net.sf.jkniv.jaas;

import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

interface LdapConnection
{
    DirContext openDir(Properties env) throws NamingException;

}
