package net.sf.jkniv.jaas;

@SuppressWarnings("restriction")
class Base64
{
    
    public String encode(String value)
    {
        String v64 = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(value.getBytes());
        return v64;
    }
    
    public String encode(byte[] value)
    {
        String v64 = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(value);
        return v64;
    }
    public String decode(String value)
    {
        byte[] b = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(value);
        return new String(b);
    }
}
