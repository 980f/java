/**
 * Title:        Acct<p>
 * Description:  Accounting page -- wraps all transactions / browsing <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Acct.java,v 1.10 2004/04/08 19:21:16 mattm Exp $
 */

package net.paymate.web.page;
import net.paymate.web.*;
import net.paymate.data.Txnid;
import org.apache.ecs.html.A;
import org.apache.ecs.*;
import java.io.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import net.paymate.util.ErrorLogStream;

public class Acct extends PayMatePage {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Acct.class);

  public Acct(LoginInfo linfo, AdminOpCode opcodeused, boolean archive) {
    super(name(), linfo.forDisplay(), opcodeused, linfo, archive);
  }

  public static final String name() {
    return ACCOUNTADMINISTRATION;
  }
  public static final String key() {
    return PayMatePage.key(Acct.class);
  }

  public static String serviceUrl() {
    return fullURL(AdminOp.servicepg.url());
  }

  public static String fullURL(String url) {
    return key() + "?" + url;
  }

  public static final String txnUrl(Txnid txnid) {
    return txnUrl(txnid.toString());
  }
  public static final String txnUrl(String txnid) {
    return key()+"?"+AdminOp.DetailsAdminOp.url()+"&tid=" + txnid;
  }

  public static final A TSVDrawerLink(String id) {
    return TSVLink(CSVDid, id);
  }
  public static final A TSVTerminalLink(String id) {
    return TSVLink(CSVTid, id);
  }
  public static final A TSVBatchLink(String id) {
    return TSVLink(CSVBid, id);
  }
  public static final A TSVTermAuthLink(String id) {
    return TSVLink(CSVTaid, id);
  }
  private static final A TSVLink(String key, String value) {
    return new A(CSVDrawerRequestor + "?" + key + "=" + value, "TSV");
  }

  public static final String newassocpw = "assocpw";
  public static final String newtermpw = "termpw";
  public static final String newentpw = "entpw";
  public static final String newapplpw = "applpw";
  public static final String newstoreauthpw = "storeauthpw";
  public static final String newtermauthpw = "termauthpw";
  public static final String newstorepw = "storepw";
  // for receipt fetching
  public static final String ReceiptRequestor = "receipt.png";
  public static final String TransID          = "tid";
  // for csv drawer exporting
  public static final String CSVDrawerRequestor = "txnlist.tsv";
  public static final String CSVDid = "csvdid";
  public static final String CSVTid = "csvtid";
  public static final String CSVBid = "csvbid";
  public static final String CSVTaid = "csvtaid";

  // for authbill and receiptarchiver work
  public static final String writeDocToFile(String filename, Document doc) {
    boolean good = true;
    FileOutputStream fos = null;
    String result = "";
    try {
      IOX.createParentDirs(filename);
      fos = new FileOutputStream(filename);
      String page = null;
      if(doc == null) {
        result += "Doc is null!";
        good = false;
      } else {
        try {
          page = doc.toString(); // this is already optimized with a stringbuffer.
        } catch(Exception ex) {
          result += "Exception converting doc to String: " + ex + "\n";
          ex.printStackTrace(System.out);
          good = false;
        }
        fos.write(page.getBytes());
      }
      fos.flush();
    } catch (Exception ex) {
      result += "Exception writing doc to file: " + ex + "\n";
      ex.printStackTrace(System.out);
      good = false;
    } finally {
      IOX.Close(fos);
    }
    return result + (good ? "Wrote '" + filename + "' successfully." : "Error writing '" + filename + "' !");
  }
  public static final Element legendElement(String code, String description) {
    TR tr = new TR();
    tr.addElement(new TD(code));
    tr.addElement(new TD(description));
    return tr;
  }
}
