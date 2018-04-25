package net.sf.jkniv.jaas;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemOutLogger implements Logger_Deprecated
{
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public void info(String msg)
    {
        print(msg, null);
    }
    
    @Override
    public void info(String msg, Throwable e)
    {
        print(msg, e);
    }
        
    @Override
    public void error(String msg)
    {
        print(msg, null);
    }
    
    @Override
    public void error(String msg, Throwable e)
    {
        print(msg, e);
    }
    
    @Override
    public void debug(String msg) {}
    
    @Override
    public void debug(String msg, Throwable e) {}
    
    @Override
    public void warn(String msg) {}
    
    @Override
    public void warn(String msg, Throwable e) {}

    
    private void print(String msg, Throwable e)
    {
        System.out.println(sdf.format(new Date()) + " ["+Thread.currentThread().getName()+"] " + msg );
        if(e != null)
            e.printStackTrace(System.out);
    }
    
}
