/* $Id: TerminalInfo.java,v 1.49 2001/11/16 01:34:27 mattm Exp $ */
package net.paymate.ISO8583.data;
import  net.paymate.util.*;

/**
 * @todo: modify our linux bootup scripts to make all ports have uniform naming.
 * we couldn't give a shit if a port is an isa legacy port or a usb or whatever.
 */

public class TerminalInfo implements isEasy {
  private static final ErrorLogStream dbg = new ErrorLogStream(TerminalInfo.class.getName());

  public StoreInfo  si=new StoreInfo(); //legacy data path

  String nickName="dbMissingName";
  int id = 0; //the permanently globally unique id.

  public String equipmenthack; //will disappear when we have an equipment table.
  public EasyCursor equipmentlist;

  public boolean compliesWith(TerminalInfo newone){//npe check newoone +_+
    return
    Safe.equalStrings(newone.equipmenthack,equipmenthack)&& //gotta have the same hardware!
    (id == newone.id) &&             //gotta be the same terminal
    Safe.equalStrings(newone.nickName,nickName)&& //gotta have the same name for listings
    si.equals(newone.si); //this turned out to be trivial, maybe someday it won't be
  }

  public int id(){
    return id;
  }

  public TerminalInfo setId(int uniqueID){
    id=uniqueID;
    return this;
  }

  public TerminalInfo (int uniqueID){
    setId(uniqueID);
  }

  ////////////////////////////////////////////
  // remove this old stuff?

  protected static final String nickNameKey="nickName";
  public String getNickName(){
    return nickName;
  }

  public TerminalInfo setNickName(String termID){
    nickName=new String(termID);
    return this;
  }

  //////////////////////////////////
  // transport
  public void save(EasyCursor ezp){
    ezp.setString(nickNameKey ,nickName );
    ezp.setString("eqhack",equipmenthack);
  }

  public EasyCursor saveas(String key,EasyCursor ezc){
    ezc.push(key);
    save(ezc);
    return ezc.pop();
  }

  public void load(EasyCursor ezp){
    nickName =ezp.getString(nickNameKey );
    equipmenthack= ezp.getString("eqhack");
    dbg.VERBOSE("Load:"+toSpam());
    if(Safe.NonTrivial(equipmenthack)){
      equipmentlist= fromHack(equipmenthack);
    } else {
      equipmentlist= ezp.EasyExtract(null);
    }
    dbg.VERBOSE("eqlist:"+equipmentlist.asParagraph());
  }
  ////////////////

  static ModelHack [] hackers={
    new OneHack(dbg),
    new TwoHack(dbg),
  };

  EasyCursor fromHack(String eqhack){
    EasyCursor ezc=new EasyCursor();
    try {
      //hardware components
      //hack until we have equipment table:
      if(Safe.NonTrivial(eqhack)){
        dbg.VERBOSE("eqhack: "+eqhack);
        for (int i=hackers.length;i-->0;){
          ModelHack billthecat=hackers[i];
          dbg.VERBOSE("Is it hack: "+billthecat.prefix);
          if(eqhack.startsWith(billthecat.prefix)){
            dbg.VERBOSE("It is "+billthecat.prefix);
            try {
              billthecat.hackthis(eqhack,ezc);
            } catch(Exception e) {
              dbg.Caught(e);
            }
            dbg.VERBOSE("yielding:"+ezc.toSpam());
            break;
          }
        }
      }
    } catch(Exception e2) {
      dbg.Caught(e2);
    }
    //else the server must have been updated to give us what we need.
    return ezc;
  }

  public String toSpam(){
    return "termInfo["+id()+"] named:'"+getNickName()+"' modelhack:"+equipmenthack;
  }

}//end of terminalINfo class

////////////////////////////////
// classes that locally generate configuration that will
// eventually come from a table.
abstract class ModelHack {
  String prefix;
  ErrorLogStream dbg=null;
  abstract void hackthis(String eqhack, EasyCursor ezc);
  public ModelHack(ErrorLogStream dbg) {
    this.dbg=dbg;
  }
  /**
   * either pad the string, or use rcb timing from net.payamte.testpos.properties, but not both.
   */
  void p612Pad(EasyCursor ezc, boolean pad){
    assertInt("P612.textPad",  pad ? 40 : 0,ezc);//30 worked, added margin
    assertInt("P612.rasterPad",pad ? 45 : 0,ezc);//WAG.
  }
  void tell(String name, String value, EasyCursor ezc) {
    dbg.VERBOSE("Setting "+name+"="+value+" for " + ezc.preFix());//$$$always blank prefix.
  }
  // I dream of templates
  void setString(String name, String value, EasyCursor ezc) {
    tell(name,value,ezc);
    ezc.setString(name, value);
  }
  void setBoolean(String name, boolean value, EasyCursor ezc) {
    tell(name,""+value,ezc);
    ezc.setBoolean(name, value);
  }
  void assertInt(String name, int value, EasyCursor ezc) {
    tell(name,""+value,ezc);
    ezc.assertInt(name, value);
  }
}


class OneHack extends ModelHack {
  public OneHack(ErrorLogStream dbg){
    super(dbg);
    prefix="1";
  }
  void hackthis(String eqhack, EasyCursor ezc){
    setBoolean("ET1K.present",true,ezc);
    setBoolean("EC3K.present",true,ezc);
    setBoolean("P612.present",true,ezc);
    switch(eqhack.charAt(prefix.length())){
      case '0':{
        setString("ET1K.portName","/dev/ttyS0",ezc);
        setString("EC3K.portName","/dev/ttyS1",ezc);
        p612Pad(ezc, true);
      } break;
      case '1':{
        //these are exchanged from common expectations to keep the enTouches on different interrupt lines.
        setString("ET1K.portName","/dev/ttyS3",ezc);
        setString("EC3K.portName","/dev/ttyS2",ezc);
        p612Pad(ezc, true);
      } break;
      case '2':{
        setString("ET1K.portName","/dev/ttyS0",ezc);
        setString("EC3K.portName","/dev/ttyS1",ezc);
        setString("P612.portName","/dev/ttyS3",ezc);//com4, so as not to share interrupt with entouch.
        p612Pad(ezc, true);
      } break;
    }
  }
}

class TwoHack extends ModelHack {
  TwoHack(ErrorLogStream dbg){
    super(dbg);
    prefix="2";
  }
  static final String COMS = "0123456789";
  static final String USBS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  static final String ALL  = COMS+USBS;
  static final boolean iswindows = !OS.isUnish();    // first of all, are we on windows or not?
  static final String comprefix = iswindows ? "COM" : "/dev/ttyS";
  static final String USBprefix = iswindows ? "COM" : "/dev/ttyUSB"; // this may not work for USB on windows, anyway; use a different config in the terminal table when using windows (show as just coms; no usbs)
  void hackthis(String eqhack, EasyCursor ezc){
    // +_+ add code that makes sure it is exactly 4 characters wide!!!! (just wait for equipment table)
    eqhack = Safe.TrivialDefault(eqhack, "2---").toUpperCase(); // make it uppercase for USBS

    char et1k = eqhack.charAt(1); //""+ is an expensive operator.
    char ec3k = eqhack.charAt(2);
    char p612 = eqhack.charAt(3);
    boolean et1kpresent = (ALL.indexOf(et1k) > -1);
    boolean ec3kpresent = (ALL.indexOf(ec3k) > -1);
    boolean p612present = (ALL.indexOf(p612) > -1);
    boolean rcb = et1kpresent && p612present && et1k==p612;
    //
    setBoolean("ET1K.present",et1kpresent,ezc);
    if(et1kpresent) {
      setString("ET1K.portName",comIndex(et1k),ezc);
    }
    setBoolean("EC3K.present",ec3kpresent,ezc);
    if(ec3kpresent) {
      setString("EC3K.portName",comIndex(ec3k),ezc);
    }
    setBoolean("P612.present",p612present,ezc);
    if(p612present) {
      p612Pad(ezc, !rcb);
      if(!rcb) {
        setString("P612.portName",comIndex(p612),ezc);
      }
    }
  }

  String comIndex(char encoded) {
    // if we are in here, then the index is valid...
    int usb = USBS.indexOf(encoded);
    return (usb > -1) ? USBprefix+usb : comprefix+encoded;
  }

}

////////////////////////////////////////////////

//$Id: TerminalInfo.java,v 1.49 2001/11/16 01:34:27 mattm Exp $
