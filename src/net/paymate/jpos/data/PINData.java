package net.paymate.jpos.data;
/*
$Source: /cvs/src/net/paymate/jpos/data/PINData.java,v $
Description: a pair of 64 bit integers representing a pin and a sequence number
$Revision: 1.28 $
*/

import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;
import net.paymate.util.ByteArray;
import net.paymate.lang.Fstring;

public class PINData implements isEasy  {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(PINData.class);
  private long pinAsInt=0;  //64 bit integer
  private long sequenceNumber=0; //64 bit integer cause that is what the industry does.

  private String errorDetail;

  /**
   * force 16 hex digits, upper case.
   */
  private static String format(long value){
    return Fstring.righted(Long.toHexString(value).toUpperCase(),16,'0');
  }

  private static long parse(String hexlong){
    return StringX.parseLong(hexlong,16);
  }

  public long Value(){
    return pinAsInt;
  }

  public String Image(){
    return format(pinAsInt);
  }

  public PINData incrementForTestGeneration(){
    pinAsInt<<=3;
    pinAsInt+=sequenceNumber&7;
    ++sequenceNumber;
    return this;
  }

  public long sequenceNumber(){
    return sequenceNumber;
  }

  public String ksnImage(){
    return format(sequenceNumber());
  }

  public boolean NonTrivial(){
    return pinAsInt!=0 && sequenceNumber!=0;
  }

  public static boolean NonTrivial(PINData pindatum){
    return pindatum!=null&&pindatum.NonTrivial();
  }

  public void save(EasyCursor ezp){
    ezp.setString("pin",Image());   //using heximage for debuggability.
    ezp.setString("ksn",ksnImage());//... setLong outputs decimal
  }

  public void load(EasyCursor ezp){
    pinAsInt=parse(ezp.getString("pin"));
    sequenceNumber=parse(ezp.getString("ksn"));
  }

  public PINData setto(PINData old){
    pinAsInt=old.pinAsInt;
    sequenceNumber=old.sequenceNumber;
    return this;
  }

  public static PINData Clone(PINData old){
    return Null().setto(old);
  }

  public void Clear(){
    pinAsInt=0;
    sequenceNumber=0;
  }

  public static PINData Null() {
    return new PINData();
  }

  public PINData setError(String detail) {
    errorDetail =detail;
    return this;
  }
  public String errorMessage(){
    return StringX.OnTrivial(errorDetail,"Empty PIN");
  }
  public static PINData Error(String detail) {
    return new PINData().setError(detail);
  }

  public static PINData Dukpt(String hexpin, String hexseq){//for ascii formatters
    dbg.VERBOSE("from strings:"+hexpin+" seq:"+hexseq);
    PINData newone=new PINData();
    newone.pinAsInt=parse(hexpin);
    newone.sequenceNumber=parse(hexseq);
    return newone;
  }

  public static PINData unpack(byte [] packedPin, byte [] packedKsn){//from binary packets
    //make a long out of bytes in high to low order
    PINData newone=new PINData();
    dbg.VERBOSE("from bytes:"+Formatter.hexImage(packedPin)+" seq:"+Formatter.hexImage(packedKsn));
    newone.pinAsInt=ByteArray.unpackLong(packedPin);
    newone.sequenceNumber=ByteArray.unpackLong(packedKsn);
    dbg.VERBOSE("from bytes:"+EasyCursor.spamFrom(newone));
    return newone;
  }

}
//$Id: PINData.java,v 1.28 2003/08/21 17:19:39 andyh Exp $
