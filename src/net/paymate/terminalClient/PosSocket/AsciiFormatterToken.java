package net.paymate.terminalClient.PosSocket;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/terminalClient/PosSocket/AsciiFormatterToken.java,v $</p>
 * <p>Description: fixed text used in ascii interface</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 * do NOT change this text gratuitously, it is an external API of our system.
 * Other relevent text is in the Enum for Paytype and TransferType
 */

public interface AsciiFormatterToken {

  String END = "END";
  String NAK = "NAK";
  String ACK = "ACK";
  //
  String ReceiptStoreToken = "ReceiptStore";
  String SignatureToken =    "Signature";
  String StrokeToken =       "Stroke";
  String HyperpenToken =     "Hyperpen";

  String DrawerOperation =   "Batch"; //confusing legacy
  String DrawerListing =     "Listing";
  String DrawerClosing =     "Close";

  String LogOperation =      "log";
  String SocketOperation =   "Socket";
  String SocketEcho=         "Echo";
  String SocketSystem=       "System";
  String SocketPause=        "Pause";

//deprecated  String DataOperation =     "Data";
  String VoidOperation =     "Reversal";
  String ModifyOperation =   "Modify";

  String StoreOperation =    "Store";
  String StoreDeposit   =    "Deposit";
//  String PaymentOperation=   "Payment";
  String AVSpresent="AVS";
}
//$Id: AsciiFormatterToken.java,v 1.6 2004/02/04 01:04:23 andyh Exp $