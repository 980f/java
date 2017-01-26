package net.paymate.data.sinet.financialTxn;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/financialTxn/Terminal.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.data.sinet.EntityBase;
import net.paymate.data.sinet.SinetClass;
import net.paymate.data.sinet.business.*;

public class Terminal extends EntityBase {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Terminal.class);

  public Terminalid terminalid() {
    return new Terminalid(id().value());
  }

  private static final SinetClass mySinetClass = new SinetClass(SinetClass.Terminal);
  public SinetClass getSinetClass() {
    return mySinetClass;
  }
  public void loadFromProps() {
//    myProps +++
  }
  public void storeToProps() {
//    myProps +++
  }

//  public static final Terminal Get(Terminalid id) {
//    return (Terminal)getEntity(id, mySinetClass);
//  }

  // a terminal belongs to a store, so ask the store to make one
  public static final Terminal New(Storeid storeid) {
    if(Storeid.isValid(storeid)) {
      // --- stub
    }
    return null;
  }

}
