package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/STAN.java,v $
 * Description:  for client generated (or relayed) txn identifiers - terminal transaction number - sequential terminal accounting number?
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

// +++ actually, this is NOT a uniqueid! but happens to have the same interface...
public class STAN extends UniqueId {

  public STAN() {
    super();
  }

  public STAN(int value) {
    super(value);
  }

  public STAN(String value) {
    super(value);
  }

  public static STAN NewFrom(String humaninput){
    return new STAN(humaninput);
  }

  public static STAN NewFrom(int generator){
    return new STAN(generator);
  }

  public static STAN OnError(){//pick a number out of our asses, that is unlikely to occur
    return new STAN(666);
  }

} //$Id: STAN.java,v 1.8 2004/03/10 00:36:34 andyh Exp $
