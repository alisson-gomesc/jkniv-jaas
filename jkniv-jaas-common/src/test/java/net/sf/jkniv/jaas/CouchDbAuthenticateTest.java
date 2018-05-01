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
        schema = "whinstone-author";
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
    public void whenGetDocumentoUsinGetMethod() throws LoginException
    {
        CouchDbAuthenticate auth = new CouchDbAuthenticate(url, user, passwd);
        String cookie = auth.authenticate();
        HttpRequest request = new HttpRequest(url+"/"+schema+"/1");
        HttpResponse response = request.send();
       
        assertThat(response, notNullValue());
        assertThat(response.getStatus(), is(200));
        assertThat(response.getBody(), notNullValue());
        System.out.println(response.getBody());
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
    public void whenParserJsonPassword()
    {
        //(?:"asin":")(.*?)(?:")
        Pattern pattern = Pattern.compile("(?:\"passwd\":\")(.*?)(?:\")");
        
        String body = "{\"passwd\":\"DA5AA25B5DBAF93379DC603BD0A27AD27206DAFCA9E71E73\",\"passsalt\":\"C8B3B1BD1A28363EC80CD1D15FACB0EE8565F9340DC86194\",\"status\":\"ACTIVE\",\"email\":\"35marcilio@gmail.com\"}";

        Matcher matcher = pattern.matcher(body);
        assertThat(matcher.find(), is(true));
        
        String pass= matcher.group().split(":")[1].replaceAll("\"","");
        assertThat(pass, is("DA5AA25B5DBAF93379DC603BD0A27AD27206DAFCA9E71E73"));
    }
    
    @Test
    public void whenParserJsonRoles()
    {
        Pattern pattern = Pattern.compile("(?:\"roles\":)(?:\\[)(.*)(?:\\])");        
        String body = "{\"status\":\"ACTIVE\",\"email\":\"35marcilio@gmail.com\", \"roles\":[\"ADMIN\",\"OPERATOR\"] }";
        Matcher matcher = pattern.matcher(body);
        assertThat(matcher.find(), is(true));
        String[] roles = matcher.group(1).replaceAll("\"","").split(",");
        
        assertThat(roles.length, is(2));
        assertThat(roles[0], is("ADMIN"));
        assertThat(roles[1], is("OPERATOR"));
    }

    @Test
    public void whenExtractJsonArray()
    {
        Pattern pattern = Pattern.compile("(?:\"category\":)(?:\\[)(.*)(?:\"\\])");        
        String body = "{\"device_types\":[\"smartphone\"],\"isps\":[\"a\",\"B\"],\"network_types\":[],\"countries\":[],\"category\":[\"Jebb\",\"Bush\"],\"carriers\":[],\"exclude_carriers\":[]}";
        Matcher matcher = pattern.matcher(body);
        assertThat(matcher.find(), is(true));
        String[] categories = matcher.group(1).replaceAll("\"","").split(",");
        
        assertThat(categories.length, is(2));
        assertThat(categories[0], is("Jebb"));
        assertThat(categories[1], is("Bush"));
    }
    
}
