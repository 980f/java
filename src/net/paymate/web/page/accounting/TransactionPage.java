package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/TransactionPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.25 $
 */

import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.UnsettledTransactionFormat;
import net.paymate.connection.*;
import net.paymate.awtx.*;
import net.paymate.jpos.data.*;
import net.paymate.data.sinet.business.*;

public class TransactionPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TransactionPage.class);

  public TransactionPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties req) {
    this(linfo, opcodeused, req, null, false, null);
  }

  private static final String DRPWD = "drpwd";
  private static final String DID = "did";
  private static final String VPW = "vpw";
  private static final String MPW = "mpw";
  private static final String AMT = "amt";
  private static final String RPW = "rpw";

  public TransactionPage(LoginInfo linfo, AdminOpCode opcodeused,
                         EasyProperties req, Txnid txnid, boolean archive, TxnRow rec) {
    super(linfo, opcodeused, archive);
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(!Txnid.isValid(txnid)) {
      txnid = new Txnid(req.getInt(TransID));
    }

    // +++ turn this into a formal PayMatePage !!MUST  ADD BUTTON TO PRINT CLEANLY if you do(alh)
    // +++ add amount change feature? (void or refund the txn.  if that works, do a new one?  ask how to do this)
    // here, a transaction id is passed in so that the details can be displayed
    // then, a full report of the transaction is displayed,
    // including a link to the voiding or voided transaction, if appropriate
    // and the receipt is displayed, or a link to it is, along with a reprint option.


// +++++++++++++++++++ @@@ %%% Clean this up, combine code into functions, etc ...

    // +++ Check that the txnid is one for one of OUR terminals! SECURITY! [only AFTER we get multistore going!]
    if(!Txnid.isValid(txnid)) {
      ec.addElement(BRLF+"<CENTER><B>Error in page format.  No transactions selected.</B></CENTER>"+BRLF);
    } else {
      if(rec == null) {
        rec = db.getTxnRecordfromTID(txnid);
      }
      if(rec == null) {
        ec.addElement(BRLF+"<CENTER><B>Error locating transaction # " + rec.refNum() + ".</B></CENTER>"+BRLF);
      } else {
        Terminalid terminalid = rec.terminalid();// db.getTerminalForTxnid(txnid);
        boolean justGatewayed = rec.hasAuthRequest() && rec.hasAuthResponse() && !rec.stoodin();
        boolean voidable   = rec.canVoid() && !justGatewayed;
        boolean enmodify = (linfo.store != null) ? linfo.store.enmodify : false;
        boolean modifiable = rec.canModify() && enmodify;
        boolean returnable = rec.canRefund();
        if(!archive) {
          // these should return a boolean of true IFF they changed something, so the refetch of rec can be conditional on it
          attemptVoid(req.getString(VPW), voidable, linfo, txnid, ec, rec, terminalid);
          attemptModify(req.getString(MPW), modifiable, linfo, txnid, rec, req.getString(AMT), ec);
          attemptRefund(linfo, req.getString(RPW), returnable, rec, ec);
          attemptDrawerChange(req.getString(DRPWD), req.getInt(DID), rec, txnid,
                              ec, linfo);
        }
        // get the txn info again, since it could have changed based on the above actions!
        // also because of archiver, need a separate copy of the record, else it closes the query in UTF, below
        rec = db.getTxnRecordfromTID(txnid);
        boolean hasDrawer = (StringX.parseInt(rec.drawerid) > 0);
        // output the transaction info (receipt) graphically (always)
        UnsettledTransactionFormat utf  = new UnsettledTransactionFormat(linfo,
            rec, null, null,
            "<a name=\"title\"/>Transaction #" + rec.refNum() + " [Trace #: " + rec.txnid + "] Details", null,
            true /* countLosses */, archive);
        String pather = null;
        if(archive) {
          pather = ReceiptArchiver.receiptForTransaction(txnid.toString());
        } else {
          pather = ReceiptRequestor + "?" + TransID + "=" + txnid;
        }
        ec.addElement(utf).
            addElement(PayMatePage.BRLF).addElement(PayMatePage.BRLF).
            addElement(new IMG(pather).setAlt("Receipt Image:" + txnid)).
            addElement(PayMatePage.BRLF);
        if(!archive) {
          drawerForm(hasDrawer, ec, txnid, linfo.isaGod());
          voidForm(voidable, ec, txnid, justGatewayed, rec);
          modifyForm(modifiable, ec, txnid, justGatewayed, rec, enmodify);
          refundForm(returnable, ec, txnid, justGatewayed, rec, linfo.isaGod());
        }
        EasyCursor ezp2 = txn2Properties(rec, linfo, archive);
        Element tgr = EasyCursorTableGen.output(
            "Transaction Detail for # " + rec.refNum() + ":",
            linfo.colors(), ezp2, TXNINFOHEADERS);
        ec.addElement(tgr).addElement(PayMatePage.BRLF);
        // additional details
        if(!archive && linfo.isaGod()) {
          ec.addElement(RecordEditPage.editRecord(db.txn, txnid, false /*write*/, null, linfo));
        }
      }
    }
    fillBody(ec);
  }

  private static final void attemptVoid(String voidpw, boolean voidable,
                                        LoginInfo linfo, Txnid txnid,
                                        ElementContainer ec, TxnRow rec,
                                        Terminalid terminalid) {
    //////////////////
    // void
    if(StringX.NonTrivial(voidpw)) {
      // this means they wanted to void the txn!
      if(voidable) {
        if(linfo.assoc.passes(voidpw)) {
          if(linfo.permits(AssociatePermissions.PermitVoid)) {
            // try to void the record
            PaymentRequest request = PaymentRequest.Void(txnid); // DO MORE HERE ???
            request.terminalid = terminalid; // get the terminalid that the txn was done on, NOT linfo.terminalID;
            ActionReply reply = ConnectionServer.THE().
                generateReply(request, linfo, false, null, null, true, null);//web voids
            if (reply!=null&& reply instanceof PaymentReply) {
              PaymentReply frep=((PaymentReply)reply);
              boolean voided = frep.auth().isApproved();
              if(voided) {
                // +++ put a link to the VOID txn (not the voided, as we are already looking at it)
                ec.addElement(BRLF+"<CENTER><B>Transaction " + rec.stan() + " voided.</B></CENTER>"+BRLF);
              } else {
                ec.addElement(BRLF+"<CENTER><B>Unable to void transaction [" + frep.auth().message() + ": " + frep.Errors.asParagraph() + "].</B></CENTER>"+BRLF);
                if(rec.drawerid().isValid()) {
                  // since it has already been drawered, we must now recalc the drawer
                  PayMateDB db = PayMateDBDispenser.getPayMateDB();
                  db.retotalClosedDrawer(rec.drawerid());
                }
              }
            } else {
              ec.addElement(BRLF+"<CENTER><B>Unable to void transaction.</B></CENTER>"+BRLF);
            }
          } else {
            ec.addElement(BRLF+"<CENTER><B>Unable to void transaction - user not allowed.</B></CENTER>"+BRLF);
            // log as a security "exception"
            dbg.ERROR("Txn void attempt via web by user " + linfo.assoc + " unsuccessful due to lack of permissions.");
          }
        } else {
          ec.addElement(BRLF+"<CENTER><B>Unable to void transaction due to incorrect password.</B></CENTER>"+BRLF);
          // log as a security "exception"
          dbg.ERROR("Txn void attempt via web by user " + linfo.assoc + " unsuccessful due to incorrect password " + voidpw);
        }
      } else {
        ec.addElement(BRLF+"<CENTER><B>Unable to void transaction.  Transaction is no longer voidable.</B></CENTER>"+BRLF);
      }
    }
  }

  private static final void attemptModify(String modpw, boolean modifiable,
                                   LoginInfo linfo, Txnid txnid,
                                   TxnRow rec, String amtstr, ElementContainer ec) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    /////////////////////
    // modify
    if(StringX.NonTrivial(modpw)) { // then we want to modify the txn
      if(modifiable) {
        if(linfo.assoc.passes(modpw)) {
          if (linfo.permits(AssociatePermissions.PermitSale)) {
            // try to modify the record
            TxnReference tref = TxnReference.New(txnid);
            if(!StringX.NonTrivial(amtstr) &&
               rec.isAuthOnly() &&
               !StringX.NonTrivial(rec.settleamount)) {
              // assume we about to set settlement = authamount
              amtstr = rec.amount;//@authorsettle? auth amount.
            }
            RealMoney newAmount = new RealMoney(amtstr);
            PaymentRequest request = PaymentRequest.Modify(tref, newAmount); // DO MORE HERE ???
            request.terminalid = db.getTerminalForTxnid(txnid); // get the terminalid that the txn was done on, NOT linfo.terminalID;
            ActionReply reply = ConnectionServer.THE().
                generateReply(request, linfo, false, null, null, true, null); //web mods
            if (reply != null && reply instanceof PaymentReply) {
              PaymentReply frep = ( (PaymentReply) reply);
              boolean modified = frep.auth().isApproved();
              if (modified) {
                if(linfo.isaGod()) { // only gawds can do this advanced stuff (for now; until it is debugged)
                  ec.addElement(TransactionPage.dumpReplyWithTxnLink(frep));
                }
                ec.addElement(BRLF + "<CENTER><B>Transaction " + rec.stan() +
                              " modified.</B></CENTER>" + BRLF);
              } else {
                ec.addElement(BRLF +
                              "<CENTER><B>Unable to modify transaction [" +
                              frep.auth().message() + " \\ " +
                              frep.Errors.asParagraph() +
                              "].</B></CENTER>" + BRLF);
              }
            } else {
              ec.addElement(BRLF +
                            "<CENTER><B>Unable to void transaction.</B></CENTER>" +
                            BRLF);
            }
          } else {
            ec.addElement(BRLF+"<CENTER><B>Unable to modify transaction - user not allowed.</B></CENTER>"+BRLF);
            // log as a security "exception"
            dbg.ERROR("Txn modify attempt via web by user " + linfo.assoc + " unsuccessful due to lack of permissions.");
          }
        } else {
          ec.addElement(BRLF+"<CENTER><B>Unable to modify transaction due to incorrect password.</B></CENTER>"+BRLF);
          // log as a security "exception"
          dbg.ERROR("Txn modify attempt via web by user " + linfo.assoc + " unsuccessful due to incorrect password " + modpw);
        }
      } else {
        ec.addElement(BRLF+"<CENTER><B>Unable to modify transaction.  Transaction is no longer modifiable.</B></CENTER>"+BRLF);
      }
    }
  }

  private static final void attemptRefund(LoginInfo linfo, String retpw,
                                   boolean returnable, TxnRow rec,
                                   ElementContainer ec) {
    if(linfo.isaGod()) { // only gawds can do this advanced stuff (for now; until it is debugged)
      //////////////////
      // REFUND
      if(StringX.NonTrivial(retpw)) { // then we want to modify the txn
        if(returnable) {
          if(linfo.assoc.passes(retpw)) {
            // +++ determine which permissions to check & when! @refundbutton
            if (linfo.permits(AssociatePermissions.PermitSale)) {
              // try to generate a refund record
              // +++ put this code into TxnRow?
              // only do this for sale, force, and settling authonly.
              if(rec.settle()) {
                int tt = rec.transfertype().Value();
                if(tt == TransferType.Authonly || tt == TransferType.Force || tt == TransferType.Sale) {
                  // SaleInfo
                  RealMoney setamt = rec.rawSettleAmount();
                  RealMoney autamt = rec.rawAuthAmount();
                  RealMoney newAmount = null;
                  if (rec.isAuthOnly() || (setamt.isZero() && !autamt.isZero())) {
                    newAmount = autamt;
                  } else {
                    newAmount = setamt;
                  }
                  SaleInfo sale = SaleInfo.New(rec.paytype().Value(),
                                               TransferType.Return, true
                                               /*manual*/, rec.stan().value());
                  sale.setMoney(newAmount);
                  // MSRData [+++ put parts in the MSRData class] - NO TRACKS!
                  MSRData card = new MSRData();
                  MSRData oldcard = rec.card();
                  card.accountNumber = oldcard.accountNumber;
                  card.expirationDate = oldcard.expirationDate;
                  card.ServiceCode = oldcard.ServiceCode;
                  // Request
                  PaymentRequest request = null;
                  if (rec.isGiftCard()) {
                    request = PaymentRequest.GiftCardRequest(sale, card);
                  } else if (rec.isCredit()) {
                    request = PaymentRequest.CreditRequest(sale, card);
                  }
                  if (request != null) {
                    request.setTerminalInfo(rec.terminalid());
                    ActionReply reply = ConnectionServer.THE().
                        generateReply(request, linfo, false, null, null, true, null); //web mods
                    if (reply != null && reply instanceof PaymentReply) {
                      PaymentReply frep = ( (PaymentReply) reply);
                      boolean refunded = frep.auth().isApproved();
                      if (refunded) {
                        ec.addElement(BRLF + "<CENTER><B>Transaction " +
                                      rec.stan() +
                                      " refunded.</B></CENTER>" + BRLF);
                        ec.addElement(TransactionPage.dumpReplyWithTxnLink(frep));
                      } else {
                        ec.addElement(BRLF +
                            "<CENTER><B>Unable to refund transaction [" +
                            frep.auth().message() + " \\ " +
                            frep.Errors.asParagraph() +
                            "].</B></CENTER>" + BRLF);
                      }
                    }
                  } else {
                    ec.addElement(BRLF + "<CENTER><B>Unable to refund transaction.  Original txn cannot be refunded [wrong type].</B></CENTER>" +
                                  BRLF);
                  }
                } else {
                  ec.addElement(BRLF + "<CENTER><B>Unable to refund transaction.  Original txn is not a sale-type txn.</B></CENTER>" +
                                BRLF);
                }
              } else {
                ec.addElement(BRLF + "<CENTER><B>Unable to refund transaction.  Original txn did not or would not settle.</B></CENTER>" +
                              BRLF);
              }
            } else {
              ec.addElement(BRLF+"<CENTER><B>Unable to refund transaction - user not allowed.</B></CENTER>"+BRLF);
              // log as a security "exception"
              dbg.ERROR("Txn refund attempt via web by user " + linfo.assoc + " unsuccessful due to lack of permissions.");
            }
          } else {
            ec.addElement(BRLF+"<CENTER><B>Unable to generate a refund transaction due to incorrect password.</B></CENTER>"+BRLF);
            // log as a security "exception"
            dbg.ERROR("Txn refund attempt via web by user " + linfo.assoc + " unsuccessful due to incorrect password " + retpw);
          }
        } else {
          ec.addElement(BRLF+"<CENTER><B>Unable to generate a refund for transaction.  Transaction is no longer refundable.</B></CENTER>"+BRLF);
        }
      }
    }
  }

  private static final void attemptDrawerChange(String drpwd, int toSet /* drawerid */,
                                         TxnRow rec, Txnid txnid,
                                         ElementContainer ec, LoginInfo linfo) {
    //////////////////
    // DRAWERS
    // deal with drawers (only gawds)
    // want to change the drawer assignment?
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(StringX.NonTrivial(drpwd)) { // change the drawer assignment
      if(linfo.assoc.passes(drpwd)) {
        boolean hasDrawer = (StringX.parseInt(rec.drawerid) > 0);
        if(hasDrawer && toSet > 0) {
          String msg = "Unable to change drawer to " + toSet + " from " + rec.drawerid + " for txn " + txnid + " since it already has one (release first).";
          ec.addElement(BRLF+"<CENTER><B>" + msg + "</B></CENTER>"+BRLF);
          dbg.ERROR(msg);
        } else if(!hasDrawer && toSet < 1) {
          String msg = "Unable to release drawer for txn " + txnid + " since it is not assigned to a drawer.";
          ec.addElement(BRLF+"<CENTER><B>" + msg + "</B></CENTER>"+BRLF);
          dbg.ERROR(msg);
        } else { // do it
          String msg = "";
          if(!hasDrawer) {  // assign
            if(db.setDrawerid(txnid, new Drawerid(toSet))) {
              msg = "Transaction " + txnid + " has drawer set to " + toSet + ".";
            } else {
              msg = "Unable to assign transaction " + txnid + " to drawer " + toSet + ".";
            }
          } else { // release
            if(db.releaseDrawerid(txnid)) {
              msg = "Transaction " + txnid + " has drawer released.";
            } else {
              msg = "Unable to release transaction " + txnid + " from drawer " + rec.drawerid + ".";
            }
          }
          ec.addElement(BRLF+"<CENTER><B>" + msg + "</B></CENTER>"+BRLF);
          dbg.ERROR(msg);
        }
      } else {
        dbg.ERROR("Invalid password!");
        ec.addElement(BRLF+"<CENTER><B>Unable to change drawer due to incorrect password.</B></CENTER>"+BRLF);
        // log as a security "exception"
        dbg.ERROR("Txn drawer change attempted via web by user " + linfo.assoc + " unsuccessful to do incorrect password " + drpwd);
      }
    }
  }

  private static final void drawerForm(boolean hasDrawer, ElementContainer ec, Txnid txnid, boolean isagawd) {
    if(isagawd) {
// drawer form
      // has a drawer already been assigned? +++ this needs to be mutexed with the terminal's drawer closing stuff.
      // +++ %%% @@@ We MUST have a per-terminal mutex to prevent ANY two things from happening at the terminal at the same time
      TR tr1 = new TR();
      if(hasDrawer) {
        // form for releasing txns from a drawer (back into pending): Password, button.
        TD td1 = new TD().addElement("Enter Password to release from drawer: ");
        TD td2 = new TD().addElement(new Input(Input.PASSWORD, DRPWD, ""));
        TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Release from drawer"));
        tr1.addElement(td1).addElement(td2).addElement(td3);
      } else { // null
        // form for assigning a txn to an existing drawer: Text-entry field of the drawerid, password, button.  Eventually use a dropdown?
        TD td1 = new TD().addElement("Enter DrawerID for txn assignment: ");
        TD td2 = new TD().addElement(new Input(Input.TEXT, DID, ""));
        TD td3 = new TD().addElement("Enter Password: ");
        TD td4 = new TD().addElement(new Input(Input.PASSWORD, DRPWD, ""));
        TD td5 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Assign to drawer"));
        tr1.addElement(td1).addElement(td2).addElement(td3).addElement(td4).addElement(td5);
      }
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./"+Acct.key()+"?"+AdminOp.DetailsAdminOp.url()).
        addElement(new Input(Input.HIDDEN, "tid", String.valueOf(txnid))).
        addElement(t);
      ec.addElement(f);
    }
  }


  private static final void refundForm(boolean returnable, ElementContainer ec, Txnid txnid, boolean justGatewayed, TxnRow rec, boolean isagawd) {
    if(isagawd) {
// REFUND stuff
      if(returnable) {
        TD td1 = new TD().addElement("Enter Password to refund: ");
        TD td2 = new TD().addElement(new Input(Input.PASSWORD, RPW, ""));
        TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("REFUND Txn"));
        TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
        Table t = new Table().addElement(tr1);
        Form f = NewPostForm("./"+Acct.key()+"?"+AdminOp.DetailsAdminOp.url()).
            addElement(new Input(Input.HIDDEN, "tid", String.valueOf(txnid))).
            addElement(t);
        ec.addElement(f);
      } else {
        // why is this not returnable?
        String why = "Unknown reason.";
        if (justGatewayed) {
          why = "Cannot refund gatewayed transaction.";
        } else {
          why = rec.whyCannotRefund();
        }
        ec.addElement("Not refundable. " + why).addElement(BRLF);
      }
    }
  }

  private static final void modifyForm(boolean modifiable, ElementContainer ec, Txnid txnid, boolean justGatewayed, TxnRow rec, boolean enmodify) {
    if(modifiable) {
      TD td1 = new TD().addElement("Enter new amount: ");
      TD td2 = new TD().addElement(new Input(Input.TEXT, AMT, rec.rawSettleAmount().Image(UnsettledTransactionFormat.moneyformat)));
      TD td3 = new TD().addElement("Enter Password to modify: ");
      TD td4 = new TD().addElement(new Input(Input.PASSWORD, MPW, ""));
      TD td5 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Modify Txn Amount"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3).
          addElement(td4).addElement(td5);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./"+Acct.key()+"?"+AdminOp.DetailsAdminOp.url()).
          addElement(new Input(Input.HIDDEN, "tid", String.valueOf(txnid))).
          addElement(t);
      ec.addElement(f);
    } else {
      if(enmodify) {
        // why is this not modifiable?
        String why = "Unknown reason.";
        if (justGatewayed) {
          why = "Cannot modify gatewayed transaction.";
        }
        else {
          why = rec.whyCannotModify();
        }
        ec.addElement("Not modifiable. " + why).addElement(BRLF);
      }
    }
  }

  private static final void voidForm(boolean voidable, ElementContainer ec, Txnid txnid, boolean justGatewayed, TxnRow rec) {
    // don't give a void option if it is already voided
    // +++ if the record is not pending, automatically do a refund instead?  (otherwise their old drawer will get changed!)
    if(voidable) {
      TD td1 = new TD().addElement("Enter Password to void: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, VPW, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("VOID Txn"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./"+Acct.key()+"?"+AdminOp.DetailsAdminOp.url()).
          addElement(new Input(Input.HIDDEN, "tid", String.valueOf(txnid))).
          addElement(t);
      ec.addElement(f);
    } else {
      // why is this not voidable?
      String why = "Unknown reason.";
      if (justGatewayed) {
        why = "Cannot void gatewayed transaction.";
      } else {
        why = rec.whyCannotVoid();
      }
      ec.addElement("Not voidable. " + why).addElement(BRLF);
    }
    if(rec.isVoided()) {
      ec.addElement("Voided.");
    }
  }

  private static final HeaderDef TXNINFOHEADERS [] = {
    new HeaderDef(AlignType.LEFT,  "Attribute"),
    new HeaderDef(AlignType.RIGHT, "Value"),
  };

  /**
   *  this needs to have different data depending if it is a god or not [show the card numbers?]
   */
  private final EasyCursor txn2Properties(TxnRow rec, LoginInfo linfo, boolean archive) {
    EasyCursor ezc = new EasyCursor();
    try {
      Associateid associd = rec.associateid();
      Associate assoc = AssociateHome.Get(associd);
      if(Associateid.isValid(associd)) {
        ezc.setString("Associate", (linfo.isaGod() ? "[" + associd + "] " : "") +
                      ( (assoc != null) ? assoc.firstMiddleLast() : ""));
      }
      Authid authid = rec.authid();
      Authid settleid = rec.settleid();
      if( ! archive) { // the ConnectionServer and AuthMgr are probably non-existent when doing an archive!
        if(Authid.isValid(authid)) {
          ezc.setString("Authorizer", (linfo.isaGod() ? "[" + authid + "] " : "") + ConnectionServer.THE().authmgr.findAuthById(authid).serviceName());
        }
        if(Authid.isValid(settleid)) {
          ezc.setString("Settler", (linfo.isaGod() ? "[" + settleid + "] " : "") + ConnectionServer.THE().authmgr.findAuthById(settleid).serviceName());
        }
      }
      MSRData card = rec.card(true);
      if(linfo.isaGod() && !archive) {
        String authrequest = "";
        String authresponse = "";
        if(rec.hasAuthRequest()) {
          authrequest = new TextList(rec.authattempt.authrequest).asParagraph(BRLF);
        }
        if(rec.hasAuthResponse()) {
          authresponse = new TextList(rec.authattempt.authresponse).asParagraph(BRLF);
        }
        ezc.setString("authrequest", authrequest);
        ezc.setString("authresponse", authresponse);
        ezc.setString("Card and expiry[MMYY]", card.accountNumber.Image() + " " + card.expirationDate.mmYY());
      }
      ezc.setString("Reference#", rec.authrrn);
      ezc.setString("AuthTraceData", rec.authtracedata);
      ezc.setString("Acct Balance", rec.acctBalance().Image());
      ezc.setString("Card Holder",
                    card.person.isReasonable() ? card.person.CompleteName() :
                    "name not available"); //Card Holder:         name
      ezc.setString("AuthTime", TimeRanger(linfo.ltf(), rec.authstarttime, rec.authendtime)); //AuthTime:     authstarttime - authendtime (in store time format), and duration
      ezc.setString("TranTime", TimeRanger(linfo.ltf(), rec.transtarttime, rec.tranendtime)); //TranTime:      transtarttime - tranendtime (in store time format), and duration
      ezc.setString("AVS Response", rec.avsrespcode+":"+AVSDecoder.AVSDecode(rec.institution, rec.avsrespcode));
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return ezc;
    }
  }

  private static final String UNKNOWNTIME = "???";
  private static final String TimeRanger(LocalTimeFormat ltf, String starttime, String endtime) {
    String ret = null;
    String start = UNKNOWNTIME;
    UTC startutc = null;
    UTC endutc = null;
    if(StringX.NonTrivial(starttime)) {
      startutc = UTC.New(starttime);
      start    = ltf.format(startutc);
    }
    String end = UNKNOWNTIME;
    if(StringX.NonTrivial(endtime)) {
      endutc = UTC.New(endtime);
      end    = ltf.format(endutc);
    }
    String diff = UNKNOWNTIME;
    if(ObjectX.NonTrivial(startutc) && ObjectX.NonTrivial(endutc)) {
      diff = DateX.millisToSecsPlus(endutc.getTime() - startutc.getTime());
    }
    return start + " for " + diff + " secs [" + ltf.getZone().getDisplayName() + "]";
  }


  public static final Element dumpReplyWithTxnLink(ActionReply ar) {
    Txnid txnid = new Txnid();
    if(ar instanceof PaymentReply) {
      TxnReference tref = ((PaymentReply)ar).tref();
      if(tref != null) {
        txnid = tref.txnId;
      }
    }
    EasyProperties ezc = ar.toProperties();
    String arstr = ezc.asParagraph(BRLF);
    Object linker = "";
    if(Txnid.isValid(txnid)) {
      linker = new A(Acct.txnUrl(txnid), txnid.toString());
    }
    return new StringElement("Reply: " + linker + BRLF + arstr);
  }

}