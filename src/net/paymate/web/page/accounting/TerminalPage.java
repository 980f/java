package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/TerminalPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.TerminalPendingRow;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.TerminalsFormat;
import net.paymate.web.table.query.TermAuthsFormat;
import net.paymate.jpos.data.*;
import net.paymate.connection.*;
import net.paymate.data.sinet.business.*;

public class TerminalPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TerminalPage.class);

  public TerminalPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties req) {
    this(linfo, opcodeused, req, null);
  }
  public TerminalPage(LoginInfo linfo, AdminOpCode opcodeused, Terminalid termid) {
    this(linfo, opcodeused, null, termid);
  }

  public TerminalPage(LoginInfo linfo, AdminOpCode opcodeused,
                      EasyProperties req, Terminalid termid) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(req == null) {
      req = new EasyProperties();
    }
    if(!Terminalid.isValid(termid)) {
      termid = new Terminalid(req.getInt(TerminalsFormat.TERMID));
    }


    String TRANSFERTYPE = "tt";
    String CARDNUMBER = "card";
    String EXPIRY = "exp";
    String AMOUNT = "amt";
    String STANNER = "stan";
    String TXNPWD = "txnpwd";
    String AVSADDR = "avsaddr";
    String AVSZIP = "avszip";
    // try to do any attempted txns:
    TransferType tt = new TransferType(req.getString(TRANSFERTYPE));
    String txnpwd = req.getString(TXNPWD);
    String card = req.getString(CARDNUMBER);
    String expiry = req.getString(EXPIRY);
    String amount = req.getString(AMOUNT);
    String stanner = req.getString(STANNER);
    STAN stan = STAN.NewFrom(stanner);
    String avsaddr = req.getString(AVSADDR);
    String avszip = req.getString(AVSZIP);
    if(StringX.NonTrivial(txnpwd)) {
      if(linfo.assoc.passes(txnpwd)) {
        // transfertype check
        if(!tt.isLegal()) {
          ec.addElement("Invalid transfer type.");
        } else {
          // make sure the card is not trivial
          if(!StringX.NonTrivial(card)) {//@todo: use card number class sooner rather than later!
            ec.addElement("You must enter a card number.");
          } else {
            // make sure the expiry is not trivial
            if(!StringX.NonTrivial(expiry)) {
              ec.addElement("You must enter an expiry.");
            } // might be alright if the card is swiped
            // make sure the amount is > 0
            if(!StringX.NonTrivial(amount) || amount.indexOf('.') == -1) {
              ec.addElement("Invalid amount.");
            } else {
              // create a stan if there isn't one
              if(!stan.isValid()) {
                long ticks = UTC.Now().getTime();
                ticks /= 1000; //drop millis
                ticks %= 1000000; //keep just six digits, may later be 9 digits.
                stan = STAN.NewFrom((int)ticks);
              }
              // do the txn
              MSRData cardinfo = new MSRData();
              boolean swiped = false;
              if(card.indexOf("=") > 0) {
                cardinfo.setTrack(MSRData.T2, card);
                swiped = cardinfo.ParseTrack2();
              } else {
                cardinfo.accountNumber = new CardNumber(card);
                cardinfo.expirationDate=new ExpirationDate();
                cardinfo.expirationDate.parsemmYY(expiry);
              }
              if(StringX.NonTrivial(avsaddr)) {
                cardinfo.setAVSAddress(avsaddr);
              }
              if(StringX.NonTrivial(avszip)) {
                cardinfo.setAVSZip(avszip);
              }
              BinEntry guess =  BinEntry.Guess(cardinfo);
              if(guess!=null){//+_+ see CardIssuer.isIssuer()
                PayType pt=new PayType(guess.act);
                SaleInfo sale = SaleInfo.New(pt.Value(),tt.Value(),true,stan.value());
                sale.type.setto(new EntrySource(swiped ? EntrySource.Machine : EntrySource.Manual));
                sale.setMoney(new net.paymate.awtx.RealMoney(amount));
                // +++ eventually create a paytype pulldown, too, or:
                // CreditSale
                // CreditAuthOnly
                // CreditForce
                // CreditReturn
                // GiftCardSale
                // GiftCardReturn
                // ...
                PaymentRequest cr = PaymentRequest.CreditRequest(sale, cardinfo);
                cr.terminalid = termid;
                cr.requestInitiationTime=UTC.Now();
                if(tt.is(TransferType.Force)) {
                  cr.sale.preapproval = stanner;
                }
                ActionReply ar = ConnectionServer.THE().
                    generateReply(cr, linfo, false, null, null, true, null);
                ec.addElement(TransactionPage.dumpReplyWithTxnLink(ar));
              } else {
                ec.addElement("card not in internal Bin data.");
              }
            }
          }
        }
      } else {
        ec.addElement("Could not perform txn due to invalid password.");
      }
      ec.addElement(BRLF);
    }
    dbg.WARNING("config detail for terminal "+termid);
    String terminalname = db.getTerminalName(termid);
    Storeid storeid = db.getStoreForTerminal(termid);
    ec.addElement(new TerminalsFormat(linfo, db.getTerminal(termid), "Terminal " + terminalname, null));
    // txn entry form
    Form f = NewPostForm("./" + key() + "?" + AdminOp.t1pg.url() + "&" + TerminalsFormat.TERMID + "=" + termid);
    Table t = new Table().setCellSpacing(4).setCellPadding(0).setBorder(0);
    t.addElement(new Caption().addElement(new H3("Transaction")));
    f.addElement(t);
    TR tr2 = null;
    // transfertype
    int [] inttypes = {
      TransferType.Sale,
      TransferType.Authonly,
      TransferType.Force,
      TransferType.Return,
    };
    Option [] transfertypes = new Option[inttypes.length];
    for(int i = inttypes.length; i-->0;) {
      TransferType tttemp = new TransferType(inttypes[i]);
      transfertypes[i] = new Option(tttemp.Image()).addElement(tttemp.Image());
      if(tt.is(inttypes[i])) {
        transfertypes[i].setSelected(true);
      }
    }
    tr2 = new TR();
    tr2.addElement(new TD("Transfer Type: ")).addElement(new TD().addElement(new Select(TRANSFERTYPE, transfertypes)));
    t.addElement(tr2);
    // card #
    tr2 = new TR();
    tr2.addElement(new TD("Card: ")).addElement(new TD().addElement(new Input(Input.TEXT, CARDNUMBER, StringX.TrivialDefault(card, "1234567890123456"))));
    t.addElement(tr2);
    // expiration date
    tr2 = new TR();
    tr2.addElement(new TD("Exp. Date: ")).addElement(new TD().addElement(new Input(Input.TEXT, EXPIRY, StringX.TrivialDefault(expiry, "MMYY"))));
    t.addElement(tr2);
    // amount
    tr2 = new TR();
    tr2.addElement(new TD("Amount [abs]: ")).addElement(new TD().addElement(new Input(Input.TEXT, AMOUNT, StringX.TrivialDefault(amount, "0.00"))));
    t.addElement(tr2);
    // STAN
    tr2 = new TR();
    tr2.addElement(new TD("Stan [opt]: ")).addElement(new TD().addElement(new Input(Input.TEXT, STANNER, STAN.isValid(stan) ? stan.toString() : "")));
    t.addElement(tr2);
    // AVS
    tr2 = new TR();
    tr2.addElement(new TD("AVS Address [opt]: ")).
        addElement(new TD().addElement(new Input(Input.TEXT, AVSADDR, avsaddr)));
    t.addElement(tr2);
    tr2 = new TR();
    tr2.addElement(new TD("AVS zip [opt]: ")).
        addElement(new TD().addElement(new Input(Input.TEXT, AVSZIP, avszip)));
    t.addElement(tr2);
    // submission
    tr2 = new TR();
    tr2.addElement(new TD("Submission Password: ")).addElement(new TD().addElement(new Input(Input.PASSWORD, TXNPWD, "").addElement(new Input().setType(Input.SUBMIT).setValue("Transact"))));
    t.addElement(tr2);
    // add it to the page, centered
    ec.addElement(BRLF);
    ec.addElement(new Center().addElement(f));
    ec.addElement(BRLF);
    // terminal edit stuff
    ec.addElement(RecordEditPage.editRecord(db.terminal, termid, false/*write*/, null, linfo));
    // a button to create a new termauth for gawds
    if(linfo.isaGod()) {
      TD td1 = new TD().addElement("Enter Password to create a new termauth: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, newtermauthpw, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Create termauth"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t2 = new Table().addElement(tr1);
      Form f2 = NewPostForm("./" + Acct.key() + "?" + AdminOp.newTermauth.url()).
          addElement(t2);
      f2.addElement(new Input(Input.HIDDEN, TerminalsFormat.TERMID, String.valueOf(termid)));
      ec.addElement(f2);
    }
    ec.addElement(new TermAuthsFormat(linfo, db.getTermauths(termid),"TermAuths", null));
    fillBody(ec);
  }

}