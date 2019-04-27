package net.sf.jkniv.jaas;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

class LdapEntryParser
{
    private static final String          URL_LDAP                     = "ldap://";
    private static final String          URL_LDAPS                    = "ldaps://";
    private static final String          PORT_SSL                     = "636";
    private static final String          PORT                         = "389";
        
    /**
     * Build a Domain Component (DC) from domain url.
     * @param url like gmail.com
     * @return domain component dc=gmail,dc=com
     */
    public String dcFrom(String url)
    {
        String dc = "";
        int index = url.length();
        int colon = url.indexOf(":");
        if (colon > 0)
            index = colon;
        
        dc = "dc=" + url.substring(0, index).replaceAll("\\.", ",dc=");
        return dc;
    }
    
    /**
     * Return a user name appended with a domain
     * @param username with or non domain
     * @param defaultDomain must be used when user name doesn't contains a domain
     * @return user name appended with a domain, line algo@jkniv.be
     */
    public String appendDomain(final String username, final String defaultDomain)
    {
        String userdomain = username;
        int at = username.indexOf("@");
        if (at < 0 && defaultDomain != null && !"".equals(defaultDomain.trim()))
            userdomain = username + "@" + defaultDomain;
        
        return userdomain;
    }

    
    /**
     * Take off the domain from an user name
     * @param username user name with a domain
     * @param defaultDomain default value to be used when doesn't a domain
     * @return the domain name
     */
    public String stripDomain(String username, final String defaultDomain)
    {
        String userdomain = appendDomain(username, defaultDomain);
        String domain = defaultDomain;
        int i = userdomain.indexOf("@");
        if (i > 0)
            domain = userdomain.substring(i + 1);
        
        return domain;
    }
    
    /**
     * URLs separate by comma is splitted in array
     * @param urls URL separated by comma
     * @return array of URLs
     * @throws BadRealmException when an URL is malformed
     */
    public URI[] splitUri(String urls) throws BadRealmException
    {
        if (urls == null || "".equals(urls))
            return new URI[0];
        
        String[] uris = urls.split(",");
        URI[] directories = new URI[uris.length];
        for (int i = 0; i < directories.length; i++)
        {
            try
            {
                String uri = uris[i].trim();
                if (uri.startsWith(URL_LDAP) || uri.startsWith(URL_LDAPS))
                    directories[i] = new URI(uri);
                else
                    directories[i] = new URI(URL_LDAP+uri);
                
                if (directories[i].getPort() == -1)
                {
                    if (directories[i].getScheme().equals("ldap"))
                        directories[i] = new URI(directories[i].toString()+":"+ PORT);
                    else
                        directories[i] = new URI(directories[i].toString()+":"+ PORT_SSL);
                }
            }
            catch (URISyntaxException e)
            {
                throw new BadRealmException(e.getMessage());
            }
        }
        return directories;
    }



    /**
     * URLs separate by comma is splitted in array
     * @param urls URL separated by comma
     * @return array of URLs
     * @throws BadRealmException when an URL is malformed
     */
    public String[] splitUrl(String urls) throws BadRealmException
    {
        if (urls == null)
            return new String[0];
        
        String[] directories = urls.split(",");
        
        for (int i = 0; i < directories.length; i++)
        {
            try
            {
                new URL("http://" + directories[i]);
            }
            catch (MalformedURLException e)
            {
                throw new BadRealmException(e.getMessage());
            }
            directories[i] = directories[i].trim();
        }
        return directories;
    }


}
