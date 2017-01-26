package net.paymate.ivicm.comm;
/* $Id: SerialParameters.java,v 1.11 2001/09/28 02:10:43 andyh Exp $ */
import net.paymate.util.*;

import javax.comm.*;//for interfaces...

public class SerialParameters implements isEasy {

  final static String VersionInfo= "$Revision: 1.11 $";
  String portName;
  int baudRate;
  int databits;
  int stopbits;
  int parity;
  int flowControl;
  public int obufsize;//+++ protect
  public int priority=Thread.NORM_PRIORITY;//+1;//+_+ protect

  public int Wordsize(){
    return databits+stopbits+ (parity!=0?1:0);//---ignores 1.5 stopbits
  }

  public double CharTime(){
    return Safe.ratio(Wordsize(),baudRate) ;
  }

  public SerialParameters setFlow(int bitmask){
    flowControl=bitmask;
    return this;
  }

  public int parseFlow(String ennum){
    if(Safe.NonTrivial(ennum)){
      if(ennum.equalsIgnoreCase("hard")) return SerialPort.FLOWCONTROL_RTSCTS_OUT+SerialPort.FLOWCONTROL_RTSCTS_IN;
      if(ennum.equalsIgnoreCase("soft")) return SerialPort.FLOWCONTROL_XONXOFF_OUT+SerialPort.FLOWCONTROL_XONXOFF_IN;
    }
    return SerialPort.FLOWCONTROL_NONE;
  }

  public SerialParameters(String s) {
    portName = s;
  }

  public void save(EasyCursor cfg){
    cfg.setInt("baud",baudRate);
    cfg.setInt("dataBits",databits);
    cfg.setInt("stopBits",stopbits);
    cfg.setString("parity","None"); //+_+
    cfg.getString("flow","None");   //+_+ +++
    cfg.setInt("obufsize",obufsize);
  }

  public void load(EasyCursor cfg){
    baudRate = cfg.getInt("baud",9600);
    databits = cfg.getInt("dataBits",8);
    stopbits = cfg.getInt("stopBits",1);
    setParity(cfg.getString("parity","None")) ;
    setFlow( parseFlow(cfg.getString("flow","None")));
    obufsize= cfg.getInt("obufsize",8192);
  }

  public SerialParameters(String s, EasyCursor cfg) {
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

  public int getParity(){
    return parity;
  }

  public int getStopbits(){
    return stopbits;
  }

  public SerialParameters setBaudRate(int i){
    baudRate = i;
    return this;
  }

  public SerialParameters setBaudRate(String s){
    return setBaudRate(Safe.parseInt(s));
  }

  public SerialParameters setDatabits(int i){
    databits = i;
    return this;
  }

  public SerialParameters setDatabits(String s){
    int index="5678".indexOf(s.charAt(0));
    return setDatabits( (index>=0)? (index+5):8);
  }

  public SerialParameters setParity(int i){
    parity = i;
    return this;
  }

  public SerialParameters setParity(String s){
    if(s.equals("None")){
      return setParity(SerialPort.PARITY_NONE);
    }
    if(s.equals("Even")){
      return setParity(SerialPort.PARITY_EVEN);
    }
    if(s.equals("Odd")){
      return setParity(SerialPort.PARITY_ODD);
    }
    if(s.equals("Mark")){
      return setParity(SerialPort.PARITY_MARK);
    }
    return this;
  }

  public SerialParameters setStopbits(int i){
    stopbits = i;
    return this;
  }

  public SerialParameters setStopbits(String s){
    if(s.equals("1")){
      return setStopbits(1);
    }
    if(s.equals("1.5")){
      return setStopbits(3);
    }
    if(s.equals("2")){
      return setStopbits(2);
    }
    return this;
  }

  public String getPortName() {
    return portName;
  }

  public String toSpam(){//4debug
    return getPortName()+':'+this.baudRate+"NOE".charAt(parity)+databits+"012h".charAt(stopbits);
  }

}
//$Id: SerialParameters.java,v 1.11 2001/09/28 02:10:43 andyh Exp $
