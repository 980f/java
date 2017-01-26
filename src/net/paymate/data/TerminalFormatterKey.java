package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/TerminalFormatterKey.java,v $</p>
 * <p>Description: tokens for those classes that may be instantiated as a formatter for PosSocket.ExternalTerminal</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public interface TerminalFormatterKey {//hmmm, should have been called tokens, not keys.
  String HyperKey="terminalClient.PosSocket.HyperFormatter";
  String AsciiKey="terminalClient.PosSocket.AsciiFormatter";
  String BIAsciiKey="terminalClient.PosSocket.BatchIndexAsciiFormatter";
  String JumpwareKey="terminalClient.PosSocket.JumpwareFormatter";
  String VerifoneKey="authorizer.paymentech.PaymentechUTFormatter";
  String PTGatewayKey="authorizer.paymentech.GatewayUTFormatter";
}//$Id: TerminalFormatterKey.java,v 1.3 2004/03/08 17:19:09 andyh Exp $
