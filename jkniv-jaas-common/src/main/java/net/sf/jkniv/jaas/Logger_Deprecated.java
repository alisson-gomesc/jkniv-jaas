package net.sf.jkniv.jaas;

/**
 * Logger façade for servers
 * 
 * @author alisson gomes
 *
 */
public interface Logger_Deprecated
{
    void info(String msg);
    
    void info(String msg, Throwable e);

    void debug(String msg);
    
    void debug(String msg, Throwable e);

    void warn(String msg);
    
    void warn(String msg, Throwable e);

    void error(String msg);
    
    void error(String msg, Throwable e);

}
