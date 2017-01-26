/* $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/data/TerminalInfo.java,v $ */
package net.paymate.data;  //+_+ bad place for this. try terminalClient.

import net.paymate.Main;
import net.paymate.hypercom.IceEasyKey;
import net.paymate.ivicm.IviTrio;
import net.paymate.jpos.common.JposWrapper;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.serial.Parameters;
import net.paymate.terminalClient.PosSocket.Formatter;
import net.paymate.util.*;

/**
 * todo: remove dotted(), use easyCursor properly so that we can xml it.
 * todo: modify our linux bootup scripts to make all ports have uniform naming.
 * todo: replace HyperKey etc. with Class instances and then use class.getName();
 */

public class TerminalInfo implements isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TerminalInfo.class);


  public StoreInfo  si=new StoreInfo(); //legacy data path

  String nickName="dbMissingName";
  public Terminalid id = new Terminalid(); //the permanently globally unique id.

  private String equipmenthack; //will disappear when we have an equipment table.
  private boolean requiresLogin = true;//this setting is important!  to deal with invalid Terminal configuration in logins.
  private boolean force2ndcopy=false;
  private boolean ask4avs=false;

  private boolean canStandinModifies=false;
  public boolean canStandinModify(){
    return canStandinModifies;
  }
  public boolean setCanStandinModify(boolean allowed){
    return canStandinModifies=allowed;
  }

  public boolean askforAvs(){
    return ask4avs;
  }
  public void setAskforAvs(boolean doit){
    ask4avs=doit;
  }

  public boolean force2ndcopy(){
    return force2ndcopy;
  }
  public TerminalInfo setTwoCopies(boolean dotwo){
    force2ndcopy=dotwo;
    return this;
  }
  /**
   * allow the pos terminal to use sigcapture, independent of whether the hardware for it exists.
   */
  private boolean allowSigCapture=true;//legacy default

  public boolean allowSigCap(){
    return allowSigCapture;
  }
  public TerminalInfo setAllowSigCap(boolean capture){
    allowSigCapture=capture;
    return this;
  }

  private ModelHack billthecat=null;
  /**
   * @return whether this terminal is a gateway style terminal
   */
  public boolean isGateway() {
    return (billthecat != null) && billthecat.isGateway();
  }
  public static boolean IsGateway(TerminalInfo ti){
    return (ti != null) && ti.isGateway();
  }

  /**
   * @return true if this is an SHack (jumpware)
   */
  public boolean prefersNoBatchReport() {
    return (billthecat != null) && (billthecat instanceof SHack);
  }
  /**
   * @return whether transaction order is to be maintained despite/during standin operation.
   */
  public boolean strictlyOrder(){
    return (billthecat != null) && billthecat.strictlyOrder();
  }

  public boolean requiresLogin() {
    return requiresLogin;
  }
//  private boolean enOfflineManual=true;//legacy was false.
//  public final static String enOfflineManualKey="enOfflineManual";
/**
 * @return whether manual card entry is allowed while offline
 */
//  public boolean allowManualInStandin(){
//    return enOfflineManual;
//  }
//  public boolean allowManualInStandin(boolean enable){
//    enOfflineManual=enable;
//    return allowManualInStandin();
//  }

  public String className (){
    return equipmentlist.getString(TerminalInfoKey.CLASSPROPERTY);
  }

  public EasyCursor equipmentlist;

  public Formatter getFormatter() {
    return Formatter.New(equipmentlist.getString(TerminalInfoKey.FormatterKey));
  }

  public boolean compliesWith(TerminalInfo newone){//npe check newoone +_+
    return
    StringX.equalStrings(newone.equipmenthack,equipmenthack)&& //gotta have the same hardware!
        id.equals(newone.id) &&             //gotta be the same terminal
        StringX.equalStrings(newone.nickName,nickName)&& //gotta have the same name for listings
        si.equals(newone.si); //this turned out to be trivial, maybe someday it won't be
  }

  public Terminalid id(){
    return id;
  }

  public TerminalInfo setId(Terminalid uniqueID){
    id=uniqueID;
    return this;
  }

  public TerminalInfo (Terminalid uniqueID){
    setId(uniqueID);
  }

  // this is for the BatchReply.load() (reflective)
  public TerminalInfo (){
  }

  ////////////////////////////////////////////
  // remove this old stuff?
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
    ezp.setBoolean(TerminalInfoKey.ask4avs,ask4avs);
    ezp.setString(TerminalInfoKey.nickNameKey ,nickName );
    ezp.setBoolean(TerminalInfoKey.twoCopiesKey,force2ndcopy);
    ezp.setBoolean(TerminalInfoKey.allowSigCapKey,allowSigCapture);

//    ezp.setBoolean(enOfflineManualKey,enOfflineManual);
    ezp.setString("eqhack",equipmenthack);
    ezp.addMore(moreProperties);
    ezp.setBoolean(TerminalInfoKey.canStandinModifiesKey, canStandinModifies);
  }

  public boolean setHack(String eqhack){
    this.equipmenthack= eqhack;
    dbg.VERBOSE("Load:"+toSpam());
    if(StringX.NonTrivial(equipmenthack)){
      equipmentlist= fromHack(equipmenthack);
      return true;
    } else {
      return false;
    }
  }

  public void load(EasyCursor ezp){
    nickName =ezp.getString(TerminalInfoKey.nickNameKey );
    force2ndcopy=ezp.getBoolean(TerminalInfoKey.twoCopiesKey,false);

    if(!setHack(ezp.getString("eqhack"))){
      equipmentlist= ezp.EasyExtract(null);
      equipmentlist.Assert(TerminalInfoKey.CLASSPROPERTY,net.paymate.terminalClient.BaseTerminal.class.getName());//#want full name
    }
    allowSigCapture=equipmentlist.getBoolean(TerminalInfoKey.allowSigCapKey,true);//legacy value
    canStandinModifies=ezp.getBoolean(TerminalInfoKey.canStandinModifiesKey,false);//overriddenwhen terminal object is instantiated
    ask4avs=ezp.getBoolean(TerminalInfoKey.ask4avs,false);
  }
  ////////////////

  static ModelHack [] hackers={
    new JposHack(dbg), //JposWrapper
    new PHack(dbg),   //IceWrapper for PosTerminal
    new TwoHack(dbg),   //ingenico w/sigcap
    new ThreeHack(dbg), //ingenico w/o sigcap
    new SHack(dbg),    //jumpware format on socket
    new AHack(dbg),   //ascii format on socket
    new HHack(dbg),   //hypercom format on socket
    new XHack(dbg),  //serial port with human readable framing,
    new VHack(dbg),  //serial port with visalike framing, verifone flavor thereof
    new IHack(dbg),  //ascii series formatter with extensions for regression testing
  };

  static EasyCursor localhackfile;

  static EasyCursor localHack(String nick){
    if(localhackfile == null){
      localhackfile=EasyCursor.FromDisk(Main.LocalFile("eqhack","properties"));
    }
    return localhackfile.EasyExtract(nick);
  }

  public EasyProperties moreProperties = new EasyProperties();

  /* package */ EasyCursor fromHack(String eqhack) {

   EasyCursor ezc=localHack(id().toString());//changed to id rather than nickname. harder on the debugger but way easier for real system config.
    if(!force2ndcopy){//semi-legacy  hack
      force2ndcopy=ezc.getBoolean(TerminalInfoKey.twoCopiesKey,false);
    }

    try {
      //hardware components
      //hack until we have equipment table:
      if(StringX.NonTrivial(eqhack)){
        dbg.VERBOSE("eqhack: "+eqhack);
        for (int i=hackers.length;i-->0;){
          /*ModelHack*/ billthecat=hackers[i];
           dbg.VERBOSE("Is it hack: "+billthecat.prefix);
           if(eqhack.startsWith(billthecat.prefix)){
             dbg.VERBOSE("It is "+billthecat.prefix);
             try {
               requiresLogin = billthecat.requiresLogin();
               billthecat.hackthis(eqhack,ezc);
               ezc.Assert(TerminalInfoKey.CLASSPROPERTY,billthecat.hacksClass().getName());
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
    dbg.VERBOSE(ezc.asParagraph());
    //else the server must have been updated to give us what we need.
    return ezc;
  }

  // used on the service dump (to printer) on the terminal
  public String toSpam(){
    return getNickName()+"/"+id()+"/eq:"+equipmenthack;
  }

//  /**
//   * @deprecated do not use directly. Used only by reflection
//   */
//  public TerminalInfo (){
//    //for actionReply loading
//  }

  public static TerminalInfo fake(){
    TerminalInfo fake=new TerminalInfo(new Terminalid(42)).setNickName("no name");
    fake.setHack("Fake");
    return fake;
  }

}//end of terminalINfo class

////////////////////////////////
// classes that locally generate configuration that will
// eventually come from a table.
abstract class ModelHack implements TerminalFormatterKey {
  String prefix;
  ErrorLogStream dbg=null;
  abstract Class hacksClass();
  abstract void hackthis(String eqhack, EasyCursor ezc);
  public ModelHack(ErrorLogStream dbg) {
    this.dbg=dbg;
  }
  void tell(String name, String value, EasyCursor ezc) {
    dbg.VERBOSE("Setting "+name+"="+value+" for " + ezc.preFix());//$$$always blank prefix.
  }
  protected boolean isGateway = false;
  public boolean isGateway() {
    return isGateway;
  }
  public boolean strictlyOrder(){
    return true;
  }
  // I dream of templates
  void setString(String name, String value, EasyCursor ezc) {
    tell(name,value,ezc);
    ezc.setString(name, value);
  }
  void setBoolean(String name, boolean value, EasyCursor ezc) {
    tell(name,String.valueOf(value),ezc);
    ezc.setBoolean(name, value);
  }
  boolean assertInt(String name, int value, EasyCursor ezc) {
    tell(name,String.valueOf(value),ezc);
    return ezc.Assert(name, value);
  }
// P612 doesn't always have CTDS connected:
/**
 * either pad the string, or use rcb timing from net.payamte.testpos.properties, but not both.
 */
  void p612Pad(EasyCursor ezc, boolean pad){
    ezc.push(IviTrio.Scribe612key);
    setBoolean("present",true,ezc);
    assertInt("textPad",  pad ? 40 : 0,ezc);//30 worked, added margin
    assertInt("rasterPad",pad ? 45 : 0,ezc);//WAG.
    ezc.pop();
  }

//com port selection:
  static final String COMS = "0123456789";
  static final String USBS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  static final String ALL  = COMS+USBS;
//  static final boolean iswindows = !OS.isUnish();    // first of all, are we on windows or not?
//  static final String comprefix = iswindows ? "COM" : "/dev/ttyS";
//  static final String USBprefix = iswindows ? "COM" : "/dev/ttyUSB"; // this may not work for USB on windows, anyway; use a different config in the terminal table when using windows (show as just coms; no usbs)

  protected static String comIndex(char encoded) {
    int index = USBS.indexOf(encoded);
    boolean isUsb= index>=0;
    if( ! isUsb ){
      index=COMS.indexOf(encoded);
    }
    return OS.comPath(isUsb,index);
  }

  public boolean requiresLogin() {
    return true;
  }

}

abstract class SocketHack extends ModelHack {

  SocketHack(ErrorLogStream dbg,String formattername,String prefix){
    super(dbg);
    this.formattername=formattername;
    this.prefix=prefix;
  }

  Class hacksClass(){
    return net.paymate.terminalClient.PosSocketTerminal.class;//@possocket@
  }

  String formattername;

  void hackthis(String eqhack, EasyCursor ezc){
    ezc.setInt("port2serve",StringX.parseInt(StringX.subString(eqhack,1)));
    ezc.Assert("idleTimeoutMs",0);
    ezc.setString(TerminalInfoKey.FormatterKey,formattername);
  }

  public boolean requiresLogin() {
    return false;
  }
}

class SHack extends SocketHack {
  SHack(ErrorLogStream dbg){
    super(dbg,JumpwareKey,"S");
  }
}

class AHack extends SocketHack {
  AHack(ErrorLogStream dbg){
    super(dbg,AsciiKey,"A");
  }
}

class HHack extends SocketHack {
  HHack(ErrorLogStream dbg){
    super(dbg,HyperKey,"H");
  }
}

class IHack extends SocketHack {
  IHack(ErrorLogStream dbg){
    super(dbg,BIAsciiKey,"I");
  }
}

class XHack extends ModelHack {
  XHack(ErrorLogStream dbg){
    super(dbg);
    super.prefix="X";
  }
  Class hacksClass(){
    return net.paymate.terminalClient.SerialTerminal.class;
  }

  void hackthis(String eqhack, EasyCursor ezc){
    ezc.setString("port2serve",comIndex(StringX.charAt(eqhack,1,'-')));
    switch (StringX.charAt(eqhack,2,'H')) {
      case 'H': ezc.setString(TerminalInfoKey.FormatterKey,HyperKey);     break;
      case 'I': ezc.setString(TerminalInfoKey.FormatterKey,BIAsciiKey);   break;
      case 'A': ezc.setString(TerminalInfoKey.FormatterKey,AsciiKey);     break;
      case 'J': ezc.setString(TerminalInfoKey.FormatterKey,JumpwareKey);  break;
      case 'P': ezc.setString(TerminalInfoKey.FormatterKey,VerifoneKey);  break;
      case 'G': {
        ezc.setString(TerminalInfoKey.FormatterKey,PTGatewayKey);
        isGateway = true;
        break;
      }
    }
  }

  public boolean requiresLogin() {
    return false;
  }
}

class VHack extends XHack {//verifone hack
  VHack(ErrorLogStream dbg){
    super(dbg);
    super.prefix="V";
  }
  Class hacksClass(){
    return net.paymate.terminalClient.Techulator.class;
  }
}

class PHack extends ModelHack {
  PHack(ErrorLogStream dbg){
    super(dbg);
    super.prefix="P";
  }
  Class hacksClass(){
    return net.paymate.terminalClient.PosTerminal.class;
  }
  void hackthis(String eqhack, EasyCursor ezc){
    ezc.setClass(TerminalInfoKey.WrapperClass,net.paymate.hypercom.IceWrapper.class);
    net.paymate.serial.Parameters parms;
    String portName="/dev/null/";//if this fails under windows we will have a human watching.

    BufferParser bp=BufferParser.Slack().Start(eqhack);
    bp.getByte();//P
    while(bp.remaining()>0){
      switch(bp.getByte()){//component type
        case 'T':{
          //add a port section:
          portName=comIndex((char)bp.getByte());
        } break;
        case 'S':{
          ezc.assertBoolean(TerminalInfoKey.allowSigCapKey,bp.getByte()=='1');//legacy value
        } break;
        case 'P':{
          ezc.push(IceEasyKey.printer);
          try {
            switch (bp.getByte()) {
              default: //legacy is 5500 plain
              case '0': {
                ezc.setBoolean(IceEasyKey.hasCutter, true);
                ezc.setInt(IceEasyKey.textWidth, 44);
              } break;
              case '1' :{//added 5500 plus
                ezc.setBoolean(IceEasyKey.hasCutter, false);
                ezc.setInt(IceEasyKey.textWidth, 42);
              } break;
            }
          }
          finally {
            ezc.pop();
          }
        } break;
      }

      parms=new net.paymate.serial.Parameters(portName);
      parms.setBaudRate(19200);
      parms.setProtocol("E71");
      ezc.setBlock(parms,TerminalInfoKey.port);
    }
  }
}

abstract class IviHack extends ModelHack {
  IviHack(ErrorLogStream dbg,String prefix){
    super(dbg);
    this.prefix=prefix;
  }
  Class hacksClass(){
    return net.paymate.terminalClient.PosTerminal.class;
  }

  protected String dotted(String device,String paramname){
    if(StringX.NonTrivial(device)){
      return device+"."+paramname;//!must match EasyCursor! ---
    } else {
      return paramname;
    }
  }

  protected void setPort(String device,String portname, EasyCursor ezc){
    setString(dotted(device,Parameters.nameKey),portname,ezc);
  }

  protected void setPresent(String device,EasyCursor ezc){
    setBoolean(dotted(device,"present"),true,ezc);
  }
  void hackthis(String eqhack, EasyCursor ezc){
    ezc.setClass(TerminalInfoKey.WrapperClass,net.paymate.jpos.Terminal.IviWrapper.class);
    // +_+ add code that makes sure it is exactly 4 characters wide!!!! (just wait for equipment table)
    char pref = StringX.charAt(eqhack,0,'2');
    char et1k = StringX.charAt(eqhack,1,'-');
    char ec3k = StringX.charAt(eqhack,2,'-');
    char p612 = StringX.charAt(eqhack,3,'-');
    char pads = StringX.charAt(eqhack,4,'C');//legacy default to check manager
    dbg.VERBOSE("echo: "+pref+et1k+ec3k+p612+pads);
    boolean allowSigCap = pref=='2';
    ezc.setBoolean(TerminalInfoKey.allowSigCapKey,allowSigCap);
    boolean et1kpresent = Bool.flagPresent(et1k,ALL);
    boolean ec3kpresent = Bool.flagPresent(ec3k,ALL);
    boolean p612present = Bool.flagPresent(p612,ALL);
    boolean rcb = et1kpresent && p612present && et1k==p612;

    dbg.VERBOSE("bits SECPRX: "+allowSigCap+et1kpresent+ec3kpresent+p612present+rcb+pads);

    if(et1kpresent) {
      setPresent(IviTrio.ET1Kkey,ezc);
      setPort(IviTrio.ET1Kkey,comIndex(et1k),ezc);
    }

    if(ec3kpresent) {
      switch (pads) {
        case 'C': ezc.push(IviTrio.EC3Kkey); break;
        case 'P': ezc.push(IviTrio.encrypt100Key); break;
        case 'N': ezc.push(IviTrio.NC50Key);break;
        default:  ezc.push("badpad");break;
      }
      try {
        setPresent("",ezc);
        setPort(null,comIndex(ec3k),ezc);
      }
      finally {
        ezc.pop();
      }
    }

    if(p612present) {
      p612Pad(ezc, !rcb);
      if(!rcb) {
        setPort(IviTrio.Scribe612key,comIndex(p612),ezc);
      }
    }
    dbg.VERBOSE("props: "+ezc.asParagraph(", "));
  }


}

class ThreeHack extends IviHack {
  ThreeHack(ErrorLogStream dbg){
    super(dbg,"3");
  }

}

class TwoHack extends IviHack {
  TwoHack(ErrorLogStream dbg){
    super(dbg,"2");
  }
}

class JposHack extends ModelHack {
  JposHack(ErrorLogStream dbg){
    super(dbg);
    super.prefix="Z";
  }
  Class hacksClass(){
    return net.paymate.terminalClient.PosTerminal.class;
  }
  void hackthis(String eqhack, EasyCursor ezc){
    ezc.setClass(TerminalInfoKey.WrapperClass,JposWrapper.class);
    ezc.setString(JposWrapper.opts.root,eqhack.substring(1+eqhack.indexOf('.')));
    ezc.setString(Parameters.protocolKey, "E71");
    ezc.setString(Parameters.nameKey, comIndex(eqhack.charAt(1)));

  }

}

//$Id: TerminalInfo.java,v 1.13 2005/03/17 06:45:04 andyh Exp $
