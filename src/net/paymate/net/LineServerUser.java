package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/LineServerUser.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

public interface LineServerUser {
  public static final byte[] NullResponse= new byte[0];
  public byte[] onReception(byte[] line);//return response to "line"
  public byte[] onConnect();//return something to send when a connection has been made
  public boolean onePerConnect();
}
//$Id: LineServerUser.java,v 1.6 2003/04/03 21:40:04 andyh Exp $