package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/TermBatchReportTermInfoList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import java.util.Hashtable;

public class TermBatchReportTermInfoList {
  private Hashtable list = new Hashtable();

  public void add(Terminalid terminalid, String terminalname, String authtermid, TermAuthid termauthid) {
    add(new TermBatchReportTermInfo(terminalid, terminalname, authtermid, termauthid));
  }

  private void add(TermBatchReportTermInfo info) {
    list.put(info.terminalid.toString(), info);
  }

  public TermBatchReportTermInfo find(Terminalid terminalid) {
    return (TermBatchReportTermInfo) list.get(terminalid.toString());
  }
}

