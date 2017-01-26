package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/TermBatchReportTermInfo.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class TermBatchReportTermInfo {
  public Terminalid terminalid;
  public String terminalname;
  public String authtermid;
  public TermAuthid termauthid;
  public TermBatchReportTermInfo(Terminalid terminalid, String terminalname, String authtermid, TermAuthid termauthid) {
    this.terminalid = terminalid;
    this.terminalname = terminalname;
    this.authtermid = authtermid;
    this.termauthid = termauthid;
  }
}