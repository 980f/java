/**
* Title:        MajorTaxArea
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: MajorTaxArea.java,v 1.10 2001/08/14 23:25:29 andyh Exp $
*/

package net.paymate.data;

import net.paymate.data.State;
import net.paymate.util.ErrorLogStream;

public class MajorTaxArea extends State implements Institution {//'State' is a TrueEnum
  static final ErrorLogStream dbg=new ErrorLogStream(MajorTaxArea.class.getName());

  public final static int Base=636000;
  public String Abbreviation(){
    return Image();
  }

  public String FullName(){
    return Image();//+++ NYI
  }

  public static int findInArray(Comparable [] table,Comparable findme){
    int i;
    for(i=table.length;i-->0;){
      try {
        if(findme.compareTo(table[i])==0){
          break;
        }
      }
      catch (Exception ex) {
        continue;
      }
    }
    return i;
  }

  public String IIN(){
    String twochar=this.Abbreviation();
    int offset=findInArray(aamvaIinTable,twochar);
    return offset>=0? Integer.toString(Base+offset) : "999999";
  }

  public MajorTaxArea(State mtacode){
    super(mtacode);
  }

  public MajorTaxArea(String twochar){
    super(twochar);
  }

  public MajorTaxArea(){
    super();
  }

  public static final boolean isKnown(MajorTaxArea mta){
    return mta!=null && mta.isLegal();
  }

  public boolean isYankee(){//includes all us states and territories
    return isLegal()&& Value()<=State.MY; //we will ensure that this is last US entry
  }

  public boolean isCanuck(){//includes all us states and territories
    return isLegal()&& !isYankee() && Value()<=State.YT; //we need to ensure that this is last canadian entry
  }
  //and eventually isChicano()

  protected final static String aamvaIinTable[]={
    "VA", //000
    "NY", //001
    "MA", //002
    "MD", //003
    "NC", //004
    "SC", //005
    "CT", //006
    "LA", //007
    "MT", //008
    "NM", //009
    "FL", //010
    "DE", //011
    "ON", //012
    "NS", //013
    "CA", //014
    "TX", //015
    "NF", //016
    "NB", //017
    "IA", //018
    "GU", //019
    "CO",
    "AR",
    "KS",
    "OH",
    "VT",
    "PA",
    "AZ",
    "MY",
    "BC",
    "OR",
    "MO",
    "WI",
    "MI",
    "AL",
    null, //reserved
    "IL",
  };

  public static final MajorTaxArea FromIIN(int sixdigitcode){
    dbg.Enter("From IIN:"+sixdigitcode);
    try {
      return new MajorTaxArea(aamvaIinTable[sixdigitcode-Base]);
    } catch(Exception any){
      dbg.WARNING("Invalid IIN:"+any.getLocalizedMessage());
      return new MajorTaxArea();
    } finally {
      dbg.Exit();
    }

  }
  /**
  * @gripe yet another rough edge in Java, the return type of an interface method must
  * match exactly, it can't be a derived member of what the interface defines. It is
  * reasonable given other structural problems in java... but annoying.
  */
  public Institution /*MajorTaxArea*/ setFromIIN(int sixdigitcode){
    return FromIIN(sixdigitcode);
  }

}
//$Id: MajorTaxArea.java,v 1.10 2001/08/14 23:25:29 andyh Exp $
