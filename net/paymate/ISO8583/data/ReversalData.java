/* $Id: ReversalData.java,v 1.7 2000/10/17 21:55:16 andyh Exp $ */

package net.paymate.ISO8583.data;

import net.paymate.ISO8583.factory.SubDecimal;
import net.paymate.ISO8583.factory.SubString;

public  class ReversalData {
  int MessageType; //of message to be reversed 800,200,100
  SubDecimal mt=new SubDecimal(0,4);

  int STAN; //field 11
  SubDecimal stan=new SubDecimal(4,6); //jesus: sometimes 5 sometimes 6 chars

  String Time; //10 char time, as per iso field7

  SubString time=new SubString(10,10); //PRECERT:reversals

  SubDecimal padding=new SubDecimal(20,22,0L);

  public String Value(){
    StringBuffer packed= new StringBuffer();
    packed.setLength(4+6+10+22);

    mt.setto(MessageType);
    mt.insertInto(packed);

    stan.setto(STAN);
    stan.insertInto(packed);

    time.setto(Time);
    time.insertInto(packed);

    padding.insertInto(packed); //two 11 digit fields of all zero
    return packed.toString();
  }

  public ReversalData(int msgtype, int stan, String oldField7){
    MessageType=msgtype;
    STAN=stan;
    Time=oldField7;
  }

}
//$Id: ReversalData.java,v 1.7 2000/10/17 21:55:16 andyh Exp $
