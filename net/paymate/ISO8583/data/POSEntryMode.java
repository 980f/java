/* $Id: POSEntryMode.java,v 1.7 2000/09/20 21:55:00 andyh Exp $ */
/** Field 22
This encoding leaves out:
credit   029 = swiped, do not check offline BIN file
noPin is true for:
Check or courtesy card or credit
false for:
debit, ACH, EBT, Frequent Shopper

*/

package net.paymate.ISO8583.data;

public class POSEntryMode {
  EntrySource es;
  public boolean noPIN; //see doc block

  public int Value(){
    return es.Value()*10 + (noPIN ? 2:1);
  }

  public final static boolean havePin=false;
  public final static boolean NoPin=true;

  public POSEntryMode setto(int value, boolean nope){
    es.setto(value);
    noPIN = nope;
    return this;
  }


  /** @param value EntrySource.something
   *
   */
  public POSEntryMode setto(int value){
    es.setto(value);
    return this;
  }

  /**
   * @param nope NO Pin Entry
   */

  public POSEntryMode setto(boolean nope){
    noPIN = nope;
    return this;
  }
////////////////////////////
  public POSEntryMode (int value, boolean nope){
    this();
    setto(value,nope);
  }

  public POSEntryMode (int value){
    this(value,true);
  }

  public POSEntryMode(){
    es = new EntrySource();
    noPIN=true; //see doc block
  }

}
//$Id: POSEntryMode.java,v 1.7 2000/09/20 21:55:00 andyh Exp $
