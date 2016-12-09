package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/Protocol.java,v $
 * Description:  basic iso8483 protocol definition
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class Protocol {
  FieldDef lookup[];

  public FieldDef FieldSpec(int isobit){//look up in packed storage
    return FieldDef.FieldSpec(isobit, lookup);
  }

  public Protocol(FieldDef lookup[]) {
    this.lookup=lookup;
  }
///////////////////////
// should be abstract:
  public int maxMaps(){
    return 1;
  }

}
//$Id: Protocol.java,v 1.1 2001/11/14 13:53:45 andyh Exp $