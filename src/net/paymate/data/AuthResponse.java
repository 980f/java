package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/AuthResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 * @todo: rework AVS response code management throughout the source base. Having it made into a string here and there is expensive!
 */
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.awtx.RealMoney;//for shared error messages
import net.paymate.data.*; // ActionCode

public class AuthResponse implements isEasy {//implementing the ActionCode interface for its constants made some code hard to validate
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthResponse.class);

  public static final String DEFAULTRRN = "99999999"; // this is really PT but we will have to do something else when we are doing gateways for other authorizers
  public static final String DEFAULTAUTHCODE = "STNDIN"; //# never change this, an external API relies upon it (jumpware)
  public String action;
  protected String authcode;
  public String authmsg;
  protected String authrrn;
  protected AuthTraceData authTraceData;
  protected byte addressVerificationResponseCode;
  private static final byte noavscode=' ';

  protected CPSdata cpsdata;
  public EasyUrlString rawresponse = new EasyUrlString();
  /**
   * Clear the data back to its default values
   */

  public static boolean isDefault(String putativerrn){
    return StringX.equalStrings(putativerrn, DEFAULTRRN);
  }

  public boolean hasAVSResponse(){
    return addressVerificationResponseCode>='A';//either space or 0 is no response.
  }

  protected void clear() {
    acctBalance=new LedgerValue();
    action=ActionCode.Unknown; //one of the following, else unknown.
    addressVerificationResponseCode = noavscode;
    authcode=DEFAULTAUTHCODE;//aka approval code
    authmsg="didn't get to authorizer";
    authTraceData=new AuthTraceData();//what you need to modify this transaction later,often called "RRN"
    authrrn = DEFAULTRRN;
    cpsdata = null;//new VisaCPSdata();
    rawresponse = new EasyUrlString();
  }

  /**
   * eg GC, all later
   */
  public LedgerValue acctBalance;

  public boolean authed(){
    return isApproved() || isDeclined();
  }
  public boolean isApproved(){
    return StringX.equalStrings(action, ActionCode.Approved);
  }
  public boolean isDeclined(){
    return StringX.equalStrings(action, ActionCode.Declined);
  }
  public boolean isPending(){
    //second term is legacy, from before addition of ActionCode.Pending.
    return StringX.equalStrings(action,ActionCode.Pending )||statusUnknown() ;
  }
  public boolean Failed(){
    return StringX.equalStrings(action, ActionCode.Failed);
  }
  public boolean statusUnknown() {
    return !Failed() && !authed();
  }
  public String action(){
    return action;
  }
  public String authcode(){
    return authcode;
  }
  public String message(){
    return authmsg;
  }
  public String authrrn(){
    return authrrn;
  }
  public String authtracedata() {
    return authTraceData.fullImage();
  }
  public AuthResponse setRefInfo(String rrn, String s){
    authrrn = rrn;
    authTraceData=new AuthTraceData(s);
    return this;
  }
  public CPSdata getCPSdata() {
    return cpsdata;
  }
  public String getAVS() {
    return String.valueOf((char)addressVerificationResponseCode);
  }
  public int getAvsCode(){
    return 255&addressVerificationResponseCode;
  }
/**
 * @param action is one of constants above
 * @sixchar is teh six char approval code
 * @longer is a message sent by the authorizer describing the reason behind teh action
 * @rrn is the retrieval reference number from the authorizer
 */
  public AuthResponse setTrio(String action,String sixchar,String longer){
    this.action=action;
    this.authcode=sixchar;
    this.authmsg=StringX.OnTrivial(longer, isApproved()?("APPROVED "+authcode):"Declined,no reason");
// clear handles these
//    authTraceData=new AuthTraceData();
//    authrrn = "";
    return this;
  }

  protected AuthResponse() {
  //blanks the fields.
    clear(); // and this calls the extended class's function, if it is extended, so it should get cleared at construction time
  }
/**
 * to prevent null pointer exceptions use this:
 */

  public static AuthResponse mkTrivial(){
    return new AuthResponse();
  }

  protected AuthResponse setAll(String action,String sixchar,String longer,String tracer,String rrn,String avsrespcode){
    action=StringX.OnTrivial(action,ActionCode.Unknown);
    this.setTrio(action,sixchar,longer).
        setRefInfo(tracer, rrn).
        setAVS(avsrespcode);
    return this;
  }

  private AuthResponse setAVS(String onechar) {
    int thebyte= StringX.firstByte(onechar);
    this.addressVerificationResponseCode= thebyte>noavscode? (byte)(255&thebyte):noavscode;
    return this;
  }

  public static AuthResponse mkAuth(String action,String sixchar,String longer,String tracer,String rrn,String avsrespcode){
    return (new AuthResponse()).setAll(action,sixchar,longer,tracer,rrn,avsrespcode);
  }

  public AuthResponse markApproved(String s){
    return setTrio(ActionCode.Approved,s,"Approved");
  }

  public static AuthResponse mkApproved(String s){
    return new AuthResponse().markApproved(s);
  }

  public AuthResponse markDeclined(String reason){
    return setTrio(ActionCode.Declined,"",reason);
  }

  public static AuthResponse mkDeclined(String reason){
    return new AuthResponse().markDeclined(reason);
  }

  public AuthResponse markFailed(String reason){
    return setTrio(ActionCode.Failed,"",reason);
  }

  public static AuthResponse mkFailed(String reason){
    return new AuthResponse().markFailed(reason);
  }

  public static AuthResponse mkNoResponse(){
    return mkFailed("No Response From Authorizer");
  }

  public static String mkOverLimitStr(String amount, RealMoney limit, String nameoflimit) {
//+_+ @todo: move this 'cents clipping' code to RealMoney.
    String roundLimit=limit.Image();
    roundLimit=StringX.left(roundLimit,StringX.cutPoint(roundLimit,'.'));
    return amount+" over "+nameoflimit+" limit:"+roundLimit;
  }

  public static AuthResponse mkOverLimit(String amount, RealMoney limit, String nameoflimit){
    return mkDeclined(mkOverLimitStr(amount, limit, nameoflimit));
  }
  public static AuthResponse mkOverLimit(RealMoney amount, RealMoney limit, String nameoflimit){
    return mkOverLimit(amount.Image(),limit, nameoflimit);
  }
  public static AuthResponse mkOverLimit(LedgerValue amount, RealMoney limit, String nameoflimit){
    return mkOverLimit(amount.Image(),limit, nameoflimit);
  }

  ///////////////
  //isEasy

  private static final String actionKey="action";
  private static final String authKey="auth";
  private static final String messageKey="message";
  private static final String tracerKey="tracerInfo"; // NOT rrn
  private static final String rrnKey="rrn";
  private static final String avsKey="avscode";

  public void save(EasyCursor ezp){
    ezp.setString(messageKey,message());
    ezp.setString(authKey,authcode());
    ezp.setString(actionKey,action);
    ezp.setString(tracerKey,authtracedata());
    ezp.setString(rrnKey,authrrn());
    ezp.setString(avsKey,getAVS());
  }

  public void load(EasyCursor ezp){
    dbg.ERROR("ezp at AuthResponse.load():"+ezp.asParagraph());
    authmsg=ezp.getString(messageKey);
    action=ezp.getString(actionKey);
    authcode=ezp.getString(authKey);
    authTraceData=new AuthTraceData(ezp.getString(tracerKey));
    authrrn=ezp.getString(rrnKey);
    setAVS(ezp.getString(avsKey));
  }

  public TextList toSpam(TextList tl){
    if(tl==null){
      tl=new TextList();
    }
    tl.add("action",action);
    tl.add("authcode",authcode);
    tl.add("authmsg",authmsg);
    tl.add("authTraceData",(authTraceData!=null) ? authTraceData.fullImage() : "");
    tl.add("authrrn", authrrn);
    tl.add("cpsdata", cpsdata);
    return tl;
  }

  public String toString() {
    return String.valueOf(toSpam(null));
  }

  //--- +_+ +++ move this out of this class. need to have authresults separate from generator of it.
  //'til then overload this!
  // set action to Failed if you couldn't parse the packet, even though the packet was complete, so that we don't standin forever
  public void process(Packet toFinish){
    action=ActionCode.Unknown; //one of the following, else unknown.
    authcode="UGABUG";//aka approval code
    authmsg="software error: didn't overload process";
// let Clear() handle these
//    authTraceData=new AuthTraceData();//what you need to modify this transaction later
//    authrrn = "unknown";
  }

  protected void vbnotcomplete() {
    //defective message !!!
    dbg.ERROR("Parse()ed vb is not complete.");
//    action= AuthResponse.Failed;
//    authmsg="timeout/incomplete response";
  }

  public boolean isM4() {
    // this prevents compiling in subpackages
    return false;
  }
}
//$Id: AuthResponse.java,v 1.6 2004/02/25 21:20:30 andyh Exp $