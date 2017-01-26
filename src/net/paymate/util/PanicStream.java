package net.paymate.util;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/PanicStream.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface PanicStream {
  public void PANIC(String re);
  public void PANIC(String re, Object panicky);
}