/**
* Title:        DriversLicense
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DriversLicense.java,v 1.21 2003/07/29 21:22:00 andyh Exp $
*/

package net.paymate.data;
import net.paymate.util.*;
import net.paymate.lang.Value;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;
import net.paymate.lang.TrueEnum;


public class DriversLicense implements isEasy {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(DriversLicense.class);

  protected MajorTaxArea state=new MajorTaxArea();
  protected String Number="NO NUMBER"; //accounting types are so much fun to deal with
  protected boolean wasValid=false;

  public final static String stateKey = "DLstate";
  public final static String NumberKey = "DLnumber";

  public DriversLicense Clear(){
    state.setto(ObjectX.INVALIDINDEX);
    setNumber("");
    return this;
  }

  public MajorTaxArea State(){
    return state!=null? state: new MajorTaxArea();
  }

  public String Image(){
    return wasValid? (state.Abbreviation()+Number) : "DLisInvalid";
  }
//////////////////////////////////
  public boolean isLegal(){//return value cached when stuff was set
    return wasValid;
  }

  public static final boolean NonTrivial(DriversLicense thisone){
    return thisone!=null && thisone.isLegal();
  }

  public void save(EasyCursor ezp){
    ezp.saveEnum(stateKey,state);
    ezp.setString(NumberKey,Number);
  }

  public void load(EasyCursor ezp){
    ezp.loadEnum(stateKey,state);
    Number=ezp.getString(NumberKey);
    isValid();//adding this fixes one error in 870127
  }

  protected boolean isValid(){
    wasValid=false;
    if(MajorTaxArea.isKnown(state)){
      String [] mask= LicenseMask.forState(state);
      dbg.VERBOSE("#of Masks:"+mask.length);
      for(int i=mask.length;i-->0;){//each state can multiple formats
        if(Value.fitsMask(Number,mask[i])){//if any format accepts this number
          dbg.VERBOSE("fits mask");
          return wasValid= true;//then we have a good license
        }
      }
      dbg.VERBOSE("no mask matches");
    } else {
      dbg.VERBOSE("state not legal");
    }
    return wasValid;
  }

  protected boolean setNumber(String fart){
    dbg.VERBOSE("setNumber:"+fart);
    Number=fart;//save it even if bad
    return isValid();
  }

  public boolean unpack(String image){//inverse of image
    dbg.VERBOSE("unpacking from "+image);
    if(state==null){
      state=new MajorTaxArea();
    }
    state.setto(image.substring(0,2));
    return setNumber(image.substring(2));
  }

  /**
  * @param stateabbrev two letters
  * @param whatever else clerk enters by hand
  */
  public DriversLicense (String stateabbrev,String whatever){
    this(new MajorTaxArea(stateabbrev),whatever);
  }

  public DriversLicense (MajorTaxArea mta,String whatever){
    //order below is important to getting validity checked in a coherent fashion:
    dbg.Enter("construct(mta,string)");
    dbg.VERBOSE("before setting state to:"+state.Abbreviation());
    state=mta;
    dbg.VERBOSE("before setting number to:"+whatever);
    setNumber(whatever);
    dbg.VERBOSE("gets us:"+Image());
    isValid();//adding this helps fix bugs in 870127
    dbg.Exit();
  }

/**
 * convert letters coded as two digits back into letters, 'A'=="01"
 * @param whatever is both in put and output
 * @param mask is the best choice of pattern for the given state.
 */
  protected void compress(StringBuffer whatever,String mask){
      int cursor=-1;//v
      while(++cursor<whatever.length()){//!!don't cache length, it will change in loop
        switch(mask.charAt(cursor)){
        case 'A':
        case 'a':{//read two digits and convert to single char
          int code= 64+Integer.parseInt(whatever.substring(cursor,cursor+1));
          whatever.setCharAt(cursor,(char)code);
          whatever.deleteCharAt(cursor+1); //will except if missing last digit
          } break;
        }//end expected type
      }
  }

  /**
   * extracted from swipe based constructor
   * @returns "isValid()"
   */
  public boolean parseSwipe(String asSwiped){
    dbg.Enter("parseSwipe");
    try {
      dbg.VERBOSE("parsing:"+asSwiped);
      int iin= Integer.parseInt(asSwiped.substring(0,6));
      dbg.VERBOSE("IIN extracted:"+iin);
      state=MajorTaxArea.FromIIN(iin);
      dbg.VERBOSE("Found state:"+state.Abbreviation());
      StringBuffer whatever=new StringBuffer(asSwiped.substring(6,StringX.cutPoint(asSwiped,'=')));
      dbg.VERBOSE("raw number:"+whatever);
      String mask= LicenseMask.forState(state)[0];//for now always use first found +_+
      dbg.VERBOSE("pattern:"+mask);
      compress(whatever,mask);
      dbg.VERBOSE("escaped number:"+whatever);
      setNumber(String.valueOf(whatever));
    } catch (Exception caught){
      dbg.Caught(caught);
    } finally {
      dbg.VERBOSE("After parse:"+this.Image());
      dbg.Exit();
      return isValid();
    }
  }

  public DriversLicense (String asSwiped){//track2 preceding '='
    this(); //in case of an exception we want some initialized fields
    parseSwipe(asSwiped);
  }

  public DriversLicense (){
    state=new MajorTaxArea();
    Number="";
    wasValid=false;
  }

  public DriversLicense (DriversLicense old){
    state=new MajorTaxArea(old.state);
    Number=new String(old.Number);
    wasValid=old.wasValid;
  }

  public DriversLicense(EasyCursor ezp){
    this();
    load(ezp);
  }

}
//$Id: DriversLicense.java,v 1.21 2003/07/29 21:22:00 andyh Exp $
