/* $Id: MICRData.java,v 1.37 2001/10/02 17:06:39 mattm Exp $ */
package net.paymate.jpos.data;
import net.paymate.util.*;

public class MICRData implements jpos.MICRConst, isEasy {
  public static final ErrorLogStream dbg= new ErrorLogStream(MICRData.class.getName());

  public boolean isPresent;

  public final static int SignalLevelMin=60;
  public final static int SignalLevelMax=200;

  public final static String BadTransit="?????????";
  public final static String BadAccount="????????????";
  public final static String BadSerial ="??????";


  public String RawTrack; //getRawData()
  //+++ make a class for transit so we can enusre missing data is marked BAD
  public String Transit ; //getTransitNumber()
  //  public String Bank    ; //getBankNumber()
  public String Bank(){
    return Transit.substring(0,4); //PERSONAL ONLY +++ ???
  }
  public String Account ; //getAccountNumber()
  public String Serial  ; //getSerialNumber()

  public String Amount  ; //getAmount()
  public String EPC     ; //getEPC() //"" or 0..9
  public int checktype  ; //JPOS enumeration,!=ISO8583's
  public int country    ; //JPOS enumeration,!=ISO8583's

  //propertynames
  protected final static String TransitKey ="Transit";
  protected final static String AccountKey ="Account";
  protected final static String SerialKey  ="Serial";

  //parsing markers
  protected final static byte TransitMarker=(byte)'t';//0x74
  protected final static byte OnUsMarker=(byte)'o'; //0x6F
  protected final static byte DashMarker=(byte)'-';//0x2D
  protected final static byte AmountMarker =(byte)'a' ;//0x61   there is one more not yet important to us.
  ////////////////////////////////////////////////
  protected boolean checkField(String value,String key){
    key="Incomplete"+key;
    if(value==null){
      dbg.WARNING(key+":null");
      return false;
    }
    if(badCharCount(value)>0){//+_+ crude
      dbg.WARNING(key+":illegible chars:"+badCharCount(value));
      return false;
    }
    return true;
  }

  public boolean TransitOk(){
    if(!checkField(Transit,"Transit")){
      return false;//already bitched
    }
    if(Transit.indexOf(DashMarker)==4){
      return true; //to the best of our knowledge
    }
    return  true; //+++ +_+ something is wrong with Mod10.zerosum(Transit);
  }

  public boolean AccountOk(){
    return checkField(Account,"Account");
  }

  public boolean SerialOk(){
     return checkField(Serial,"Serial");
  }

  public boolean isComplete(){
    return TransitOk()&& AccountOk()&&SerialOk();
  }

  public static final boolean Cool(MICRData check){
    return check!=null&&check.isPresent&&check.isComplete();
  }

  ////////////////////////////////////////////////
  public void Clear(){
    isPresent=false;
    RawTrack  ="Not MICR'ed" ;
    Transit   =BadTransit ;
    //    Bank      ="" ;
    Account   =BadAccount ;
    Serial    =BadSerial ;
    Amount    ="0" ;
    EPC       ="" ;
  }

  public MICRData(){
    Clear();
  }

  public MICRData(MICRData rhs){
    RawTrack   =new String(rhs. RawTrack) ;
    Transit    =new String(rhs. Transit ) ;
    //    Bank       =rhs. Bank     ;
    Account    =new String(rhs. Account ) ;
    Serial     =new String(rhs. Serial  ) ;
    Amount     =new String(rhs. Amount  ) ;
    EPC        =new String(rhs. EPC     ) ;
  }

  public void save(EasyCursor ezp){
    ezp.setString(TransitKey, Transit);
    ezp.setString(AccountKey, Account);
    ezp.setString(SerialKey,  Serial);
  }

  public void load(EasyCursor ezp){
    Transit= ezp.getString(TransitKey);
    Account= ezp.getString(AccountKey);
    Serial=  ezp.getString(SerialKey );
  }
  ////////////////////////////////////////////////

  public static final int badCharCount(String micrfield){
    int count=0;
    for(int i=micrfield.length();i-->0;){//surely Java must have this somewhere +_+
      if (micrfield.charAt(i)=='?') {
        ++count;
      }
    }
    return count;
  }
  ////////////////////////////////////////////////

  int parseTrack(){
    int tstart  =RawTrack.indexOf(TransitMarker);     //usually 0
    int tend    =RawTrack.lastIndexOf(TransitMarker); //usually 10
    int omarker =RawTrack.indexOf(OnUsMarker);        //usually 23
    int endmark =RawTrack.length();                   //usually 30
    //+_+ doesn't deal with business checks atall.
    //there can be many onusmarkers
    int errcount=0;
    if(tstart==tend){//then there must be just one
      tstart=tend-10;//presume the sole marker was an end marker
      ++errcount;
    }
    if(tstart<0){
      tstart=0;
      ++errcount;
    }
    if(tend>tstart+4){ //to keep bank from blowing.
      Transit=RawTrack.substring(tstart+1,tend);//excluding markers
    } else {
      ++errcount;
      Transit=BadTransit; //give up
    }

    //    Bank=Transit.substring(0,4);
    if (tend>=0 && omarker>=0 && tend<omarker){//PERSONAL CHECKS ONLY!+++
      Account=RawTrack.substring(tend+1,omarker).trim();//for ISO8583 f166
    } else {
      Account=BadAccount;
    }

    if (omarker>=0&&omarker<endmark-3) {//PERSONAL CHECKS ONLY!+++
      Serial= RawTrack.substring(omarker+1).trim();
    } else {
      Serial=""; //spence says 'no serial' is acceptible //BadSerial;
    }

    return 0; //someday count the '?''s and return that.
  }

  int parseCountry(char flag){
    switch(flag){
      case '0': return MICR_CC_USA   ;
      case '1': return MICR_CC_CANADA;
      case '2': return MICR_CC_MEXICO;
      default:  return MICR_CC_UNKNOWN;
    }
  }

  int parseCheckType(char tc){
    switch(tc) {
      case 'B': return MICR_CT_BUSINESS;
      case 'P': return MICR_CT_PERSONAL;
      default:  return MICR_CT_UNKNOWN;
    }
  }


  public TextList Parse(StringBuffer packetData){
    TextList errors=new TextList();

    RawTrack = packetData.substring(14);//+_+ push this back into encheck module
    String preparsed=packetData.substring(0,14);//EC3K's interpration of micr data

    dbg.VERBOSE("Raw MICR Track:"+RawTrack);

    switch(preparsed.charAt(0)){
      case '1': errors.add("Bad chars"); break;
      case '2': errors.add("MICR Not Present"); break;
    }

    switch(preparsed.charAt(1)){
      //not guarantee case '0': errors.add("Aux OnUs Present"); break;
      //bug in cm3000      case '1': errors.add("Aux OnUs Bad chars"); break;
      //desired case '2': errors.add("Aux OnUs Not Present"); break;
    }

    switch(preparsed.charAt(2)){
      case '1': errors.add("OnUs Bad chars"); break;
      case '2': errors.add("OnUs Not Present"); break;
    }

    switch(preparsed.charAt(3)){
      case '0': errors.add("EPC Present");     break;
      case '1': errors.add("EPC Bad chars");   break;
      //      case '2': errors.add("EPC Not Present"); break;
    }

    switch(preparsed.charAt(4)){
      case '1': errors.add("ABA Bad chars"); break;
      case '2': errors.add("ABA Not Present"); break;
    }

    switch(preparsed.charAt(5)){
      case '1': errors.add("Account# Bad chars"); break;
      case '2': errors.add("Account# Not Present"); break;
    }

    switch(preparsed.charAt(6)){
      case '1': errors.add("Seq# Bad chars"); break;
      case '2': errors.add("Seq# Not Present"); break;
    }

    switch(preparsed.charAt(7)){
      case '0': errors.add("TPC Present");
      case '1': errors.add("TPC Bad chars"); break;
      //      case '2': errors.add("TPC Not Present"); break;
      //cm3000 bug      case '3': errors.add("TPC Undocumented Error"); break;
    }

    switch(preparsed.charAt(8)){
      case '0': errors.add("Amount Present"); break; //this is bad
      case '1': errors.add("Amount Bad chars"); break;
      //this is Ok      case '2': errors.add("Amount Not Present"); break;
    }

    switch(preparsed.charAt(9)){
      case 'E': /*Ok errors.add("E13B Font");*/ break;
      default: errors.add("Unknown Micr Font Code:"+preparsed.charAt(9)); break;
    }

    country=parseCountry(preparsed.charAt(10));

    switch(preparsed.charAt(11)){
      case '1': errors.add("Transit Checksum failed");  break;
      case '?': errors.add("Transit Not Found");        break;
    }

    checktype=parseCheckType(preparsed.charAt(12));

    int signalLevel= 20*(1+Character.digit(preparsed.charAt(13),10));
    if(signalLevel<=SignalLevelMin || signalLevel >=SignalLevelMax){
      errors.add( "MICR signal out of range:"+signalLevel+"%");
    }

    int pte=parseTrack();
    if(pte>0){
      //NYI
    }

    return errors;
  }

/* symbolic constants to apply to the above code
check.MICR_CT_PERSONAL=1
check.MICR_CT_BUSINESS=2
check.MICR_CT_UNKNOWN=99
check.MICR_CC_USA=1
check.MICR_CC_CANADA=2
check.MICR_CC_MEXICO=3
check.MICR_CC_UNKNOWN=99
check.JPOS_EMICR_NOCHECK=201
check.JPOS_EMICR_CHECK=202
*/
  public String toSpam() {
    return Transit+'.'+Account+'.'+Serial ;
  }

}
//$Id: MICRData.java,v 1.37 2001/10/02 17:06:39 mattm Exp $
