/* $Id: SettlementCode.java,v 1.4 2000/06/03 09:46:58 alien Exp $ */
/* field 66 Settlement Code
Description: A code indicating the result of the reconciliation request. Possible values:
Attributes: n 1

*/
package net.paymate.ISO8583.data;

public class SettlementCode {
  public final static int InBalance=1;
  public final static int OutOfBalance=2;
  public final static int Error=3;

}
//$Id: SettlementCode.java,v 1.4 2000/06/03 09:46:58 alien Exp $
