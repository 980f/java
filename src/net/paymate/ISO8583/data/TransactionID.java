package net.paymate.ISO8583.data;
/**
* Title:        $Source: /cvs/src/net/paymate/ISO8583/data/TransactionID.java,v $
* Description:  Used for locating a record based on the most unique info we have
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TransactionID.java,v 1.42 2001/11/17 06:16:57 mattm Exp $
*/

import net.paymate.util.*;

public class TransactionID implements isEasy  {
  private static final ErrorLogStream dbg = new ErrorLogStream(TransactionID.class.getName());

  protected static final String STANKEY = "STAN";
  protected static final String TIMEKEY = "TIME";
  protected static final String CAIDKEY = "CAID";
  protected static final String clientKEY = "CLIENT";


  /**
  * Unknown  Client (standin) Server (standin) Real (from tranjour)
  */
  protected boolean isOffline=false; //true=client standin.

  /**
  * @return true if this tid was created by the clientStandin agent
  */
  public boolean isStandin(){
    return isOffline;
  }

  protected static final String BOGUS_STAN = Fstring.fill("",5,'0');
  protected static final String BOGUS_TIME = Fstring.fill("",14,'0');
  protected static final String BOGUS_CAID = Fstring.fill("",16,'0');

  //for human data entry need some partition markers:
  //  protected static final String DIVIDER = "*"; // default one

  public  String time = "";
  public  int    caid = 0;
  private String stan = "";

  public String stan(){
    return stan;
  }

  public TransactionID setTime(String utcText){
    time=utcText;
    return this;
  }

  public static final TransactionID NewOffline(String transStartTime, int stan, int storeid) {
    return New(true,transStartTime, "fakeit", storeid).setStan(stan);
  }

  public static final TransactionID New(String transStartTime, String stan, int storeid) {
    return New(false,transStartTime, stan, storeid);
  }

  public static final TransactionID New(String transStartTime, int stan, int storeid) {
    return New(transStartTime, Integer.toString(stan), storeid);
  }

  private static final TransactionID New(boolean clientStandin,String transStartTime, String stan, int storeid) {
    TransactionID newone=TransactionID.New(clientStandin);
    newone.stan = Safe.TrivialDefault(stan,           BOGUS_STAN).trim();
    newone.time = Safe.TrivialDefault(transStartTime, BOGUS_TIME).trim();
    newone.caid = storeid;
    return newone;
  }

  public static final TransactionID New(String image){
    TransactionID newone=TransactionID.Zero();
    newone.parse(image);//which sometimes can decipher 'offline'
    return newone;
  }

  public static final TransactionID New(EasyCursor ezc){
    TransactionID newone=TransactionID.Zero();
    newone.load(ezc);
    return newone;
  }

  public static final TransactionID NewFrom(String key, EasyCursor ezp){
    ezp.push(key);
    try {
      return TransactionID.New(ezp);
    } finally {
      ezp.pop();
    }
  }

  public static final TransactionID NewCopy(TransactionID old){
    TransactionID tid = TransactionID.Zero();
    tid.caid = old.caid;
    tid.stan = old.stan;
    tid.time = old.time;
    tid.isOffline=old.isOffline;//when this was missing receipts for standin were not recognized at server and worked only because of orphan search upon viewing
//we need a fricking 'copy' via reflection mechanism, i.e. a nice way to wrap 'clone()' of cloneable
    return tid;
  }

  private static final TransactionID New(boolean isOffline){
    TransactionID newone=new TransactionID();
    newone.isOffline=isOffline;
    return newone;
  }

  /**
  * @return an object that is not a valid id.
  */
  public static final TransactionID Zero(){
    return TransactionID.New(false);
  }

  /////////////////
  // queries
  public boolean isComplete(){
    return Safe.NonTrivial(stan)&&!needsCaid()&&!needsTime();
  }

  public boolean needsCaid(){
    return caid < 1;
  }

  public boolean needsTime(){
    return !Safe.NonTrivial(time) || time.equals(BOGUS_TIME);
  }

  private char divchar(){
    return isOffline ? 'X' : '*';
  }

  public String image() {
    return image(divchar());
  }

  public String image(char divider) {
    return time.trim() + divider + caid + divider + stan.trim();
  }

  public String Abbreviated(){
    return Integer.toString(Safe.parseInt(stan));//clips leading zeroes
  }

  private void parse(String image) {
    dbg.Enter("parse");
    try {
      if(Safe.NonTrivial(image)){
        // Had to do it this way cause strings needed to be trimmed when sent over the web (spaces barf).
        // Setting them up as SubDecimal wouldn't work cause the DB stores them as strings
        // and prefixed zeros will screw up the queries!
        // supposed to be:
        int divider=image.indexOf('*');
        if(divider <0) {
          divider=image.indexOf('X');
          isOffline= divider>=0;
        }
        if(divider>=0){
          time = image.substring(0, divider).trim();
          String theRest = image.substring(++divider);
          divider = theRest.indexOf(divchar());
          if(divider >=0) {
            caid = Integer.valueOf(theRest.substring(0, divider).trim()).intValue();
            stan = theRest.substring(++divider).trim();
          } else {//presume it is time and stan, server will insert CAID
            caid=0;
            stan = theRest.trim();
          }
          return;
        }
        time="";
        caid=0;
        stan=image.trim();
      }

    }
    finally {
      stanhack();
      dbg.VERBOSE("parsed " + image + " into time=" + time + ", caid=" + caid + ", stan=" + stan+", offline="+isOffline);
      dbg.Exit();
    }
  }

  private void stanhack(){
    if(stan.length()==STANLENGTH+1&&stan.charAt(0)=='7'){
      isOffline=true;
    }
  }

  ///////////////////
  // transport

  public void save(EasyCursor ezp){
    ezp.setString(STANKEY,stan);
    ezp.setString(TIMEKEY,time);
    ezp.setInt(CAIDKEY,caid);
    ezp.setBoolean(clientKEY,isOffline);
  }

  public void saveas(String key, EasyCursor ezp){
    ezp.push(key);
    save(ezp);
    ezp.pop();
  }

  public EasyCursor asProperties() {
    EasyCursor ezp = new EasyCursor();
    save(ezp);
    return ezp;
  }

  public void load(EasyCursor ezp) {
    stan = ezp.getString(STANKEY,BOGUS_STAN);
    time = ezp.getString(TIMEKEY,BOGUS_TIME);
    caid = ezp.getInt(CAIDKEY);
    isOffline=ezp.getBoolean(clientKEY,false);
  }

  public void loadfrom(String key, EasyCursor ezp){
    ezp.push(key);
    load(ezp);
    ezp.pop();
  }

  ///////////////////////////
  //RetrievalRefNo
  // these are the formatting constants
  public static final int STANLENGTH = 5;

  public int stanValue(){//all integral types of SubDecimal are made long
    return Safe.parseInt(stan);
  }

  /**
   * will silently truncate to five digitsh
   */
  public TransactionID setStan(int fivedigits){
    stan= (isOffline?"7":"")+Fstring.zpdecimal(fivedigits,STANLENGTH);
    return this;
  }


  public String RRN(){
    return justdate()+Fstring.righted(stan,STANLENGTH,'0')+"001";
  }

  public String noyear(){
    return time.substring(4,14);
  }

  public String justdate(){//MMdd
    return time.substring(4,8);
  }

  public String justtime(){//hhmmss
    return time.substring(8,14);
  }

  public boolean equals(TransactionID tid) {
    boolean ret = false;
    try {
      ret = (caid == tid.caid) && stan.equals(tid.stan) && time.equals(tid.time);
    } catch (Exception e) {
      // +++ this function requires that all items are non-null
    } finally {
      return ret;
    }
  }

}
//$Id: TransactionID.java,v 1.42 2001/11/17 06:16:57 mattm Exp $

