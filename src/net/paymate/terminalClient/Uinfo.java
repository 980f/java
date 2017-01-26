package net.paymate.terminalClient;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/terminalClient/Uinfo.java,v $</p>
 * <p>Description: PosTerminal data to be presented to user interfaces</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.data.SaleInfo;
import net.paymate.data.AuthResponse;
import net.paymate.data.TxnReference;
import net.paymate.jpos.data.MSRData;
public class Uinfo {
  public SaleInfo sale;
  public AuthResponse auth;  //might be null!
  public TxnReference tref;  //suggested one to void, might be null!
  public boolean expectingIDcard; //type of swipe expected
  public MSRData card;       //formally required by some pinpads.
  public boolean debitAllowedHack;//to qualify forms
}
//$Id: Uinfo.java,v 1.4 2003/12/10 02:16:53 mattm Exp $