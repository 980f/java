/* $Id: TrackData.java,v 1.30 2005/03/19 00:36:32 andyh Exp $ */
package net.paymate.jpos.data;

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class TrackData {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(TrackData.class, ErrorLogStream.WARNING);
  boolean isPresent=false;
  String Data;
  String DiscretionaryData; //sourced in jPos, no target in NTN

  public String Data(){
    return Data;
  }

  public String DiscretionaryData(){
    return DiscretionaryData;
  }

  public void Clear(){
    //+++erase content before releasing to heap
    isPresent         =false;
    Data              ="";
    DiscretionaryData ="";
  }


  final static String illegals="!\"&'*+,:;<=>@_";//plain old not allowed.
  final static String specials="#$%()-./?[\\]^"; //framing, restricted use.
  final static String strippedSentinels="%?";//chars that jpos strips
  /**
  * legal content from driver's license spec,
  * 64 character ascii from 0x20 to 0x5F with the following notes:
  1.The 14 characters ! " & ' * + , : ; < = > @ _ are available for hardware control
    purposes and shall not be used for information (data content). Applies to track 1 only.
  2.The 3 characters [ \ ] are reserved for additional national characters when required.
    They shall not be used internationally. Applies to track 1 only.
  3.The character # is reserved for optional additional graphic symbols. Applies to track 1 only.
  4.The 3 characters % ^ ? shall have the following meaning:
  % start sentinel
  ^ field separator
  ? end sentinel
  */


  public boolean isClean(int trackno){
    return isClean(trackno, Data);
  }

  public static boolean isClean(int trackno, String data){
    for(int i=data.length();i-->0;){
      int c=(int) data.charAt(i);
      if(trackno==MSRData.T1){
        if(c<0x20|| c>0x5F || illegals.indexOf(c)>=0 || strippedSentinels.indexOf(c)>=0 ){
          return false;
        }
      } else if(trackno==MSRData.T2){//+_+ only one'=' allowed
        if(!((c>=0x30&&c<=0x39)|| c==0x3D)){
          return false;
        }
      }
    }
    return true;
  }

  public boolean isProper(int trackno){
    return isProper(trackno, Data);
  }

  public static boolean isProper(int trackno, String data){
    if(StringX.NonTrivial(data) && isClean(trackno, data)){
      return true;//+_+ add more tests here.
    } else {
      return false;
    }
  }

  public TrackData setto(String body, String otherStuff){
    dbg.VERBOSE("Setting some track to:["+body+"] and ["+otherStuff+"]");
    Data=body;
    DiscretionaryData=otherStuff;
    isPresent=StringX.NonTrivial(body);
    dbg.VERBOSE("isPresent is:"+isPresent);
    return this;
  }

  public TrackData setto(byte [] body){//for PINPAD idiotic setTrack functions
    Data=new String(body);
    DiscretionaryData=null;
    isPresent=StringX.NonTrivial(Data);
    dbg.VERBOSE("isPresent is:"+isPresent);
    return this;
  }

  public static final String asString(byte[] raw){
    return new String(raw);//turns out that default works for us.
  }

  public TrackData(){
    Clear();
  }

  public TrackData(TrackData old){
    this();
    this.isPresent = old.isPresent;
    if(old.isPresent){
      this.Data              = new String(old.Data);
      //hypercom test card had null discretionary data! sohuld fix this in parser...
      this.DiscretionaryData = new String(old.DiscretionaryData!=null?old.DiscretionaryData:"");
    }
//    this.JposError=old.JposError;
  }

  public static final String Name(int number){
    String Ttext=Integer.toString((1-MSRData.T1)+number);
    if(number>=MSRData.T1 && number<=MSRData.T3){
      return "Track["+Ttext+"]";
    } else {
      return "BadTrackIndex:"+Ttext;
    }
  }

  public static final boolean NonTrivial(TrackData track){
  //+_+ will final rule below screw us someday?? YESSSSS
    return track!=null && track.isPresent && StringX.NonTrivial(track.Data);
  }

  public void save(String which,EasyCursor ezp){
    ezp.setBoolean(which+"isPresent", isPresent);
    ezp.setString (which+"Data", Data);
//    ezp.setString (which+"DiscretionaryData",DiscretionaryData);
  }

  public void load(String which,EasyCursor ezp){
    isPresent=          ezp.getBoolean(which+"isPresent");
    Data=               ezp.getString (which+"Data");
//    DiscretionaryData=  ezp.getString (which+"DiscretionaryData");
  }

  public String toSpam(){
    return "{"+(isPresent? Data:" Not Present")+"}";//+++ add error text
  }

}
//$Id: TrackData.java,v 1.30 2005/03/19 00:36:32 andyh Exp $
