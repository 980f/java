package net.paymate.serial;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/serial/Parameters.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.18 $
 */

import net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;

public class Parameters implements isEasy, Key {

  final static String VersionInfo= "$Revision: 1.18 $";
  String portName;
  int baudRate=9600;
  int databits=8;
  int stopbits=1;
  int niceness=1000000000; //millis per false eof

/**
 * @return the greater of the actual niceness or the parameter given.
 * the rationale is this is the amount of time someone else should wait
 * before deciding that the wait implied by our niceness has failed.
 */
  int niceness(int minniceness){
    return Math.max(niceness,minniceness);
  }

  boolean haveparity=false;
  boolean evenparity=false;

  int flowControl;

  public boolean hasrx=true; //true if device providesd data stream, false ==output only
  public boolean initialRTS=true;
  public boolean initialDTR=true;

  public int obufsize;//+++ protect
  public int priority=Thread.NORM_PRIORITY;//+1;//+_+ protect

    // Fields
  public static final int FLOWCONTROL_NONE = 0;
  public static final int FLOWCONTROL_RTSCTS_IN = 1;
  public static final int FLOWCONTROL_RTSCTS_OUT = 2;
  public static final int FLOWCONTROL_XONXOFF_IN = 4;
  public static final int FLOWCONTROL_XONXOFF_OUT = 8;

  public int Wordsize(){
    return databits+stopbits+ Bool.asInt(haveparity);//---ignores 1.5 stopbits
  }

  public double CharTime(){
    return MathX.ratio(Wordsize(),baudRate) ;
  }

  /**
   * need to wrap the horribly ancient technique of pacling bitsinto an int for flow control options.
   */
  public Parameters setFlow(int bitmask){
    flowControl=bitmask;
    return this;
  }

  public boolean FlowIs(int bitpat){
    return (flowControl&bitpat) !=0;
  }

  public boolean FlowIs(String modern){
    return FlowIs(parseFlow(modern));
  }


  public static int parseFlow(String enum){
    if(StringX.NonTrivial(enum)){
      if(enum.equalsIgnoreCase("hard")) return FLOWCONTROL_RTSCTS_OUT+FLOWCONTROL_RTSCTS_IN;
      if(enum.equalsIgnoreCase("soft")) return FLOWCONTROL_XONXOFF_OUT+FLOWCONTROL_XONXOFF_IN;
    }
    return FLOWCONTROL_NONE;
  }

  public Parameters(String s) {
    portName = s;
  }



  public void save(EasyCursor cfg){
    cfg.setString(nameKey,portName);
    cfg.setInt(baudRateKey,baudRate);
    cfg.setChar(parityKey,parityCode());
    cfg.setInt(databitsKey,databits);
    cfg.setInt(stopbitsKey,stopbits);
    cfg.setString(flowcontrolKey,"None");   //+_+ +++
    cfg.setInt(bufsizeKey,obufsize);
    cfg.setInt(nicenessKey,niceness);
  }

  public void load(EasyCursor cfg){
    portName=cfg.getString(nameKey,portName);//legacy, node carried portName
    baudRate = cfg.getInt(baudRateKey,9600);
    setParity(cfg.getChar(parityKey,'N')) ;
    databits = cfg.getInt(databitsKey,8);
    stopbits = cfg.getInt(stopbitsKey,1);
    setFlow( parseFlow(cfg.getString(flowcontrolKey,"None")));
    obufsize= cfg.getInt(bufsizeKey,8192);
    niceness= cfg.getInt(nicenessKey,1000000000);
    setProtocol(cfg.getString(protocolKey));//late addition for testser usage
  }

  public Parameters(String s, EasyCursor cfg) {
    this(s);
    load(cfg);
  }

  public int getBaudRate(){
    return baudRate;
  }

  public String getBaudRateString(){
    return Integer.toString(baudRate);
  }

  public int getDatabits(){
    return databits;
  }

  public int getStopbits(){
    return stopbits;
  }

  public Parameters setBaudRate(int i){
    baudRate = i;
    return this;
  }

  public Parameters setBaudRate(String s){
    return setBaudRate(StringX.parseInt(s));
  }

  public Parameters setDatabits(int i){
    databits = i;
    return this;
  }

  public Parameters setDatabits(String s){
    int index="5678".indexOf(s.charAt(0));
    return setDatabits( (index>=0)? (index+5):8);
  }

  public Parameters setParity(String s){
    return setParity( StringX.firstChar(s));
  }

  public Parameters setParity(char c){
    switch(c){
    case 'E':{
      haveparity=true;
      evenparity=true;
    } break;
    case 'O':{
      haveparity=true;
      evenparity=false;
    } break;
    default:
      haveparity=false;
    }
    return this;
  }

  public String getPortName() {
    return portName;
  }

  public char parityCode(){
    return haveparity?( evenparity ? 'E' : 'O') : 'N';
  }

  public Parameters setProtocol(String n81){
    if(StringX.NonTrivial(n81)){
      setParity(StringX.charAt(n81,0,'N'));
      databits = StringX.charAt(n81,1,'8')-'0';
      stopbits = StringX.charAt(n81,2,'1')-'0';
//someday    setFlow( );
    }
    return this;
  }

  public String toSpam(){//4debug
    return getPortName()+':'+this.baudRate+parityCode()+databits+stopbits;
  }

  public String fullSpam(){//4moreDebug
    EasyCursor ezc = new EasyCursor();
    this.save(ezc);
    return ezc.toString();
  }

  public Parameters(){
  //for traditional() and EasyCursor.getObject();
  }

  public static Parameters Traditional(String yada){
    Parameters newone=new Parameters();
    TextListIterator args=TextListIterator.New(TextList.CreateFrom(yada));
    newone.portName=args.next();
    newone.setBaudRate(args.next());
    newone.setProtocol(args.next());
    return newone;
  }

  public static Parameters CommandLine(TextListIterator arg,int defbaud,String defprotocol){
    Parameters sp=new Parameters(StringX.OnTrivial(arg.next(),"/dev/ttyS0"));
    sp.setBaudRate(StringX.OnTrivial(arg.next(),defbaud));
    sp.setProtocol(StringX.OnTrivial(arg.next(),defprotocol));
    return sp;
 }

}
//$Id: Parameters.java,v 1.18 2003/07/27 05:35:13 mattm Exp $
