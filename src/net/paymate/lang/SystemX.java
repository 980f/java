package net.paymate.lang;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/lang/SystemX.java,v $</p>
 * <p>Description: java.lang.* extension stuff </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

// please do not put net.paymate.util.* stuff in here.

public class SystemX {
  public static String gcMessage(String instigator){
    StringBuffer msg=new StringBuffer(100);
    msg.append("gc() by ");
    msg.append(instigator);
    msg.append(" from:"+memused());
    System.gc();
    msg.append(" to:"+memused());
    return msg.toString();
  }

  public static long memused(){
    return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
  }

}