package net.sf.jkniv.jaas;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class CouchDbAuthenticateTest
{
    @Rule
    public ExpectedException catcher = ExpectedException.none();  
    private String url, schema, user, passwd; 
            
    @Before
    public void setUp()
    {
        url = "http://127.0.0.1:5984";
        schema = "db3t-useraccess";
        user = "admin";
        passwd = "admin";
    }
    
    @Test
    public void whenOpenHttpConnection() throws LoginException
    {
        CouchDbAuthenticate auth = new CouchDbAuthenticate(url, user, passwd);
        String cookie = auth.authenticate();
        assertThat(cookie, notNullValue());
        assertThat(cookie, not(is("")));
        assertThat(cookie.startsWith("AuthSession"), is(true));
        System.out.println(cookie);
    }
    
    
    @Test
    public void whenDatabaseUnavaliable() throws LoginException
    {
        catcher.expect(LoginException.class);
        catcher.expectMessage("Cannot connect to COUCHDB using url [http://127.0.0.2:5984/_session]");        
        CouchDbAuthenticate auth = new CouchDbAuthenticate("http://127.0.0.2:5984", user, "xxxxx");
        String cookie = auth.authenticate();
    }
    
    @Test
    public void whenExtractUrlFromSchema()
    {
        String fullUrl = this.url + "/" + this.schema;
        int ch = fullUrl.lastIndexOf("/");
        String url = fullUrl.substring(0, ch);
        assertThat(url, is(this.url));
    }

    @Test
    public void whenParseJsonPassord()
    {
        //(?:"asin":")(.*?)(?:")
        Pattern pattern = Pattern.compile("(?:\"passwd\":\")(.*?)(?:\")");
        
        String body = "{\"passwd\":\"DA5AA25B5DBAF93379DC603BD0A27AD27206DAFCA9E71E73\",\"passsalt\":\"C8B3B1BD1A28363EC80CD1D15FACB0EE8565F9340DC86194\",\"status\":\"ACTIVE\",\"email\":\"35marcilio@gmail.com\"}";

        Matcher matcher = pattern.matcher(body);
        assertThat(matcher.find(), is(true));
        
        String pass= matcher.group().split(":")[1].replaceAll("\"","");
        assertThat(pass, is("DA5AA25B5DBAF93379DC603BD0A27AD27206DAFCA9E71E73"));
    }
}
