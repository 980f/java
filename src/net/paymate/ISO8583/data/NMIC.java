/* $Id: NMIC.java,v 1.5 2000/06/03 09:46:57 alien Exp $ */
package net.paymate.ISO8583.data;

/*
70 Network Management Information Code
Attributes: n 3
Description: A three-digit code indicating the purpose of a network management request message.

*/

public class NMIC {
  public final static int LogonRequest=  1;      //800 Logon request
  public final static int NewCardRequest=100;    //380 New Card request
  public final static int KeyChangeRequest=101;  //800 key change request
  public final static int PINChangeRequest=101;  //380 PIN change request(same value as above!)
  public final static int PINChange=103;         //382 ACH PIN change
  public final static int EchoRequest=301;       //800 Echo request
}
//$Id: NMIC.java,v 1.5 2000/06/03 09:46:57 alien Exp $
