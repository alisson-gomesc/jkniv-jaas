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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

class HttpRequest
{
    private static final Logger          LOG                              = MyLoggerFactory.getLogger(HttpRequest.class);
    public enum Method {GET, POST, PUT, DELETE};
    private String            url;
    private String            body;
    private HttpURLConnection conn = null;
    private Method method;
    private Map<String, String> headers;

    public HttpRequest(String url)
    {
        this(url, Method.GET);
    }

    public HttpRequest(String url, Method method)
    {
        super();
        this.method = method;
        this.url = url;
        this.headers = new HashMap<String,String>();
    }

    public HttpResponse send() throws LoginException
    {
        return send(this.body);
    }
    
    public void addHeader(String key, String value)
    {
        this.headers.put(key, value);
    }
    
    public HttpResponse send(String body) throws LoginException
    {
        StringBuilder response = new StringBuilder();
        OutputStreamWriter wr = null;
        BufferedReader br = null;
        HttpResponse httpResponse = null;
        conn = openHttpConnection();
        try
        {
            if (Method.POST == method || Method.PUT == method)
            {
                wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(body);            
                wr.flush();
            }
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                response.append(line + "\n");
            }
            httpResponse = new HttpResponse(conn.getResponseCode(), response.toString(), conn.getHeaderFields());            
        }
        catch (Exception ex)
        {
            LOG.log(Level.SEVERE, "Error to submit ["+method+"] HTTP request", ex);
            throw new LoginException("Cannot connect to COUCHDB using url [" + url + "]");
        }
        finally
        {
            if (conn != null)
                conn.disconnect();
            
            if (wr != null)
            {
                try
                {
                    wr.close();
                }
                catch (IOException ignore)
                {
                }
            }
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException ignore)
                {
                }
            }
        }
        return httpResponse;
    }
    
    
    private HttpURLConnection openHttpConnection() throws LoginException
    {
        HttpURLConnection httpURLConnection = null;
        try
        {
            URL url = new URL(this.url);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod(method.name());
            for (Entry<String, String> entry : headers.entrySet())
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            httpURLConnection.connect();
        }
        catch (IOException e)
        {
            throw new LoginException("Cannot connect to COUCHDB using url [" + url + "]");
        }
        return httpURLConnection;
    }
    
}
