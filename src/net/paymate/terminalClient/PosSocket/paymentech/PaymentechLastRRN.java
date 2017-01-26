package net.paymate.terminalClient.PosSocket.paymentech;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/PaymentechLastRRN.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class PaymentechLastRRN {

  // +++ @@@ %%% THIS needs to NOT be static, but what should it be a member of?  Probably TermAuth/AuthTerm/whatever, as it is a terminal that calls PT normally.
  public /* package */ static int LASTretrievalReferenceNumber = 0; // for SVC voids

}