package net.sf.jkniv.jaas.jetty;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.jetty.util.log.Logger;

public class I18nManager
{
    private static final Logger    LOG = MyLoggerFactory.getLogger(HybridRealm.class);
    public static final Properties i18n;
    static
    {
        i18n = new Properties();
        InputStream input = null;
        try
        {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream("LocalStrings.properties");
            i18n.load(input);
        }
        catch (IOException ex)
        {
            LOG.warn("Cannot load message properties file: " , ex);
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    LOG.debug("Cannot close input stream from property file" , e);
                }
            }
        }
    }

    public static String getString(Object... args)
    {
        String msg = null;
        Object[] params = Arrays.copyOfRange(args, 1, args.length-1);
        msg = i18n.getProperty(String.valueOf(args[0]));
        
        msg = String.format(msg, params);
        
        
        return msg;
    }
}
