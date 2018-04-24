package net.sf.jkniv.jaas.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18nManager
{
    private static final Logger    LOG = MyLoggerFactory.getLogger(HybridRealm.class);
    public static final Properties i18n;
    private static final Pattern PATTERN_MSG = Pattern.compile("\\{[\\d]*\\}");
    private static final Pattern PATTERN_NUMBER = Pattern.compile("-?\\d+");
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
            LOG.log(Level.WARNING, "Cannot load message properties file: " , ex);
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
                    LOG.log(Level.CONFIG, "Cannot close input stream from property file" , e);
                }
            }
        }
    }

    public static String getString(Object... args)
    {
        String msg = null;
        Object[] params = Arrays.copyOfRange(args, 1, args.length);
        msg = i18n.getProperty(String.valueOf(args[0]));
        
        //System.out.println(args[0] + " -> "+msg);
        String formatted = formatter(msg);
        msg = String.format(formatted, params);
        
        return msg;
    }
    

    /**
     * Formatter a string from <code>org.slf4j.Logger</code> format to <code>String.format</code>
     * Example:
     * "The user {} cannot make login with password [{}]"
     * "The user %1$s cannot make login with password [%2$s]"
     * 
     * @param format SLF4J formatter message
     * @return Return a String with new formatter.
     */
    private static String formatter(String format)
    {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = PATTERN_MSG.matcher(format);
        int index = 1;
        int initial = 0;
        List<Integer> groups = new ArrayList<Integer>();
        while (matcher.find())
        {
            groups.add(matcher.start());
            groups.add(matcher.end());
            String indexParam = matcher.group();
            sb.append( format.substring(initial, matcher.start()));
            if(!"".equals(indexParam))
            {
                String i = extractNumber(indexParam);
                sb.append("%" + i + "$s"); // string format "%1$s";
                index++;
            }
            else
                sb.append("%" + (index++) + "$s"); // string format "%1$s";
            initial = matcher.end();
        }
        if (initial > 0)
            sb.append( format.substring(initial, format.length()));
        else
            sb.append(format);
        return sb.toString();
    }
    
    private static String extractNumber(String m)
    {
        String ret = "";
        
        Matcher matcher = PATTERN_NUMBER.matcher(m);
        if(matcher.find())
            ret = matcher.group();
        
        return ret;
    }
}
