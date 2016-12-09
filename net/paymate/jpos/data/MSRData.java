/* $Id: MSRData.java,v 1.60 2001/11/17 00:38:35 andyh Exp $ */
package net.paymate.jpos.data;

import net.paymate.util.*;
import net.paymate.ISO8583.data.*;

public class MSRData implements isEasy {//Magnetic Stripe Reader data
  static final ErrorLogStream dbg=new ErrorLogStream(MSRData.class.getName());
  private boolean isPresent=false;
  static public boolean legacy=false;//will remove when mainsail all gone.

  public static final int T1=0;
  public static final int T2=1;
  public static final int T3=2;

  public CardNumber       accountNumber;
  public ExpirationDate   expirationDate;
  public Person           person;
//  public long swipetime;
  public boolean equals(MSRData rhs){
    return rhs!=null&& rhs.expirationDate.equals(expirationDate)&&rhs.accountNumber.equals(accountNumber);
  }

  public static final int NumTracks=3;
  private TrackData [] track= new TrackData[NumTracks];

  public TextList errors= new TextList();


  public MSRData addError(String errmsg){
    if(errors==null){
      errors=new TextList();
    }
    errors.add(errmsg);
    return this;
  }
  /**
  * beenParsed is 4debug at present
  */
  private boolean [] beenParsed= new boolean[NumTracks];

  public TrackData track(int which){//psuedo array
    return track[which];
  }

  public MSRData setTrack(int which,String rawdata){
//    swipetime=Safe.utcNow();
    track(which).setto(rawdata,"");
    return this;
  }

  public String ServiceCode; //not a jpos item as far as I can tell...
  //but important to debit/credit stuff

  //parsing tokens:
  private final static int CARET=94;
  private final static int EQUALSIGN=61;
  private final static int SLASH=47;
  private final static int SPACE=32;

  ////////////////////////////////////////////////////////
  public void clearTracks(){
    for(int i=NumTracks;i-->0;){
      clearTrack(i);
    }
  }

  public void clearTrack(int which){
    if(track[which]==null){
      track[which]=new TrackData();
    }
    track[which].Clear();
    beenParsed[which]=false;
  }


  public void Clear(){//physically erase data
    isPresent=false;
    accountNumber.Clear();
    expirationDate.Clear();
    person.Clear();
    clearTracks();
    errors.clear();
    ServiceCode="";
//    swipetime=0;
  }

  public MSRData(){
    accountNumber=new CardNumber();
    expirationDate=new ExpirationDate();
    person=new Person();
    track=new TrackData[NumTracks];
    ServiceCode=new String();
    Clear();
  }

  public boolean okAccount(){
    return accountNumber.isValid(); //cuts out some of our cards!
  }

  public boolean okExpiry(){
    return expirationDate.isLegit(); //cuts out some of our cards!
  }

  public boolean isComplete(){
    return okAccount()&& okExpiry();//don't care if person is realistic && person.isReasonable();
  }

  public boolean beenPresented(boolean setme){
    return isPresent=setme;
  }

  public static final boolean beenPresented(MSRData card){
    return card!=null && card.isPresent;
  }

  public long age(){
    return Safe.utcNow()-Safe.parseLong(track(T3).Data());
  }

  public boolean looksSwiped(){
    if(legacy){//old stuff fubar'd if only have a track2
      return TrackData.NonTrivial(track[T1]) && TrackData.NonTrivial(track[T2]);
    } else {
      return TrackData.NonTrivial(track[T1]) || TrackData.NonTrivial(track[T2]);
    }
  }

  /**
  * @return true if card appears to be a credit/debit card
  */

  public static final void spammarama(MSRData card,boolean wasSwiped){
    dbg.VERBOSE("Investigating "+(wasSwiped?"swiped card":" manual card"));
    if(card==null){
      dbg.VERBOSE("Card is null!");
      return ;
    }
    dbg.VERBOSE("isPresent:"+card.isPresent);
    for(int ti=NumTracks;ti-->0;){
      TrackData track=card.track(ti);
      dbg.VERBOSE(TrackData.Name(ti)+track.toSpam());
    }
    dbg.VERBOSE(MSRData.SpamFrom(card).toString());
    dbg.VERBOSE("beenpresented:"+beenPresented(card));
    dbg.VERBOSE("isComplete:"+card.isComplete());
    dbg.VERBOSE("looksSwiped:"+card.looksSwiped());
    dbg.VERBOSE("is Cool:"+Cool(card,wasSwiped));
    dbg.VERBOSE(card.track(T3).Data());
  }

  public static final boolean Cool(MSRData card,boolean wasSwiped ){
    dbg.Enter("Cool");
    try {
      boolean trackPart=!wasSwiped || card.looksSwiped();
      return beenPresented(card) && card.isComplete() && trackPart;
    }
    finally {
      dbg.Exit();
    }
  }

  public MSRData(MSRData old){
    this();
    setto(old);
  }

  public void setto(MSRData old){
    isPresent           =old.isPresent;
    accountNumber       =new CardNumber(old.accountNumber);
    expirationDate      =new ExpirationDate(old.expirationDate);
    person=new Person(old.person);
    track[T1]=new TrackData(old.track[T1]);
    track[T2]=new TrackData(old.track[T2]);
    track[T3]=new TrackData(old.track[T3]);
    ServiceCode= new String(old.ServiceCode);//copy for luck.
    errors= new TextList(old.errors.toStringArray());
//    swipetime=old.swipetime;
  }

  public void save(EasyCursor ezp){
    ezp.setString("accountNumber",  accountNumber.Image());
    ezp.setString("expirationDate", expirationDate.YYmm());
    person.save(ezp);
    track[T1].save("track[0]",ezp);
    track[T2].save("track[1]",ezp);
    track[T3].save("track[2]",ezp);
    ezp.setString("ServiceCode",ServiceCode);
//    ezp.setLong("swipetime",swipetime);
  }

  public void load(EasyCursor ezp){
    accountNumber.setto(ezp.getString("accountNumber"));
    expirationDate.parseYYmm(ezp.getString("expirationDate"));
    person.load(ezp);
    track[T1].load("track[0]",ezp);
    track[T2].load("track[1]",ezp);
    track[T3].load("track[2]",ezp);
    isPresent=accountNumber.isValid();
    ServiceCode=ezp.getString("ServiceCode");
//    swipetime=ezp.getLong("swipetime");

    if(!okAccount() || !okExpiry()){
      dbg.ERROR("Bad Cardinfo received");
      if(track[T2].isPresent){
        dbg.ERROR("Attempting recovery via track2");
        ParseTrack2();//attempt recovery in load(ezp)
      } else if (track[T1].isPresent){
        dbg.ERROR("Attempting recovery via track1");
        ParseTrack1();//attempt recovery in load(ezp)
      }
    }
  }

  public MSRData(EasyCursor ezp){
    this();
    if(ezp!=null){
      load(ezp);
    }
  }

  //////////////////////

/**
 * this is used to make the tracks acceptible to the old system.
 */
  public boolean Cleanup(){
    if(!track[T1].isClean(T1)) {
      track[T1].Clear();
    }

    if(!track[T2].isClean(T2)) {
      track[T2].Clear();
    }
    return assertTracks();
  }

  /**
  * @return a safely extracted substring, with a maximum length.
  * @param track a string that might have some data in it.
  * @param start index of first char of field to extract
  * @param length maximum field length, will extract hwatever is present up to this length
  */
  public static final String field(String track,int start,int length){
    int remaining=track.length()- start;
    if(remaining < 0){
      return "";
    }
    return (remaining >= length)? track.substring(start,start+length): track.substring(start);
  }

  /**
  * prepares a track for parsing, doesn't actually look at its content in any way
  * @param ti 0 based track index.
  */
  private String startParse(int ti){
    String track= Safe.trim(track(ti).Data());
    dbg.VERBOSE("Raw "+TrackData.Name(ti)+" {"+track+"}");
    return track;
  }

  public void ParseTrack1() {
    beenParsed[T1] = false; // be proactive
    dbg.Enter("ParseTrack1");
    try {
      String track1=startParse(T1);
      if(Safe.NonTrivial(track1)&&track[T1].isClean(T1)) {
        int firstcaret = track1.indexOf(CARET);
        dbg.VERBOSE("FirstCaret:"+firstcaret);
        if(firstcaret > 0) {
          //first char is the letter 'B', skip it
          if(track1.charAt(0)!='B'){
            dbg.VERBOSE("ProBaBly not financial!");
          }
          accountNumber.setto(track1.substring(1, firstcaret));
          //+++ could compare to track2's acct
          int secondcaret = track1.indexOf(CARET, firstcaret + 1);
          dbg.VERBOSE("Secondcaret:"+secondcaret);

          if(secondcaret>0){//then we have an end to the name
            expirationDate.parseYYmm(field(track1,secondcaret+1,4));
            //+++ could compare to track2's expiration
            ServiceCode= field(track1,secondcaret+1+4,3);
            //+++ and could check service code as well
            track[T1].DiscretionaryData = field(track1,secondcaret +1+4+3,80);//80=T1Max
            String personage=track1.substring(firstcaret+1,secondcaret);//4debug
            dbg.VERBOSE("Personpart:"+personage);
            person.Parse(personage);
            dbg.VERBOSE("Afterparse1:"+toSpam());
          } else {//--- or is it simply an illegal card???
            dbg.VERBOSE("Substandard track1.");
            //let track2 parsing persist
            person.Parse(track1.substring(firstcaret+1));
          }
        }
      }
    } catch(Exception ex){
      dbg.Caught(ex);
    } finally {
      beenParsed[T1]=true; //even if not error free
      dbg.Exit();
    }
  }


  public void ParseTrack2() {
    beenParsed[T2] = false; // be proactive
    dbg.Enter("ParseTrack2");
    String track2=startParse(T2);
    try {
      if(Safe.NonTrivial(track2)){
        int split = track2.indexOf(EQUALSIGN);
        if(split>0){
          dbg.VERBOSE("Splitting at:"+split);
          accountNumber.setto(track2.substring(0, split));
          expirationDate.parseYYmm(field(track2, split+1 ,4));
          ServiceCode = field(track2,split + 5, 3);
          track[T2].DiscretionaryData = field(track2,split + 8,40);//40 should really be 37
          dbg.VERBOSE("Afterparse2:"+toSpam());
        } else {
          dbg.VERBOSE("No expiration etc.");
          accountNumber.setto(track2);//+_+ probably should reject.
        }
      }
    } catch(Exception ex){
      dbg.Caught(ex);
    } finally {
      beenParsed[T2]=true; //regardless of success
      dbg.Exit();
    }
  }

  private boolean assertTrack(int index){
    if(!TrackData.NonTrivial(track[index])){
      setTrack(index,"Missing"); //should blow on just abaout any system
      return false;
    } else {
      return true;
    }
  }

  private boolean assertTracks(){
    return assertTrack(T2) || assertTrack(T1);
  }

  public void ParseFinancial(){
    dbg.Enter("ParseFinancial");
    try {
      //the order matters!
      //some cards have whitespace in the account number (amex) on track1
      ParseTrack1(); //this has redundant data, but more data
      ParseTrack2(); //this is the purer data source
      assertTracks(); //fixup crap.
    } finally {
      dbg.Exit();
    }
  }

  public void setError(int trackno,int jposcode){
    track(trackno).setErrorCode(jposcode);
  }

  public TextList toSpam(){
    TextList tl = new TextList();
    tl.add("accountNumber="+accountNumber.Greeked());
    tl.add("expirationDate [YYMM]="+expirationDate.YYmm());
    tl.add(track(T3).Data());
    return tl;
  }

  public static final TextList SpamFrom(MSRData card){
    TextList spammy=new TextList();
    try {
      if(card==null){
        spammy.add("Null card");
      } else {
        spammy.add(card.toSpam().asParagraph(", "));
        spammy.appendMore(card.errors);
      }
    }
    finally {
      return spammy;
    }
  }

}
//$Id: MSRData.java,v 1.60 2001/11/17 00:38:35 andyh Exp $
