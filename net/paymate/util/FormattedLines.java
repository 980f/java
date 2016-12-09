/**
 * Title:        null<p>
 * Description:  null<p>
 * Copyright:    null<p>
 * Company:      PayMate.net<p>
 * @author PayMate.net
 * @version null
 */
package net.paymate.util;
import  java.util.Vector;

public class FormattedLines extends Vector {//wraps vector, for type checking and transport
  private static final ErrorLogStream dbg = new ErrorLogStream(FormattedLines.class.getName(), ErrorLogStream.VERBOSE);

  protected static final String countKey = "count";

  protected char fillChar=' '; //for converting raw strings

  public boolean add(Object arf){
    if(arf==null){
      return true;//successfully added nothing
    }
    if(arf instanceof FormattedLineItem){
      return super.add(arf);
    }
    if(arf instanceof String){
      return super.add(new FormattedLineItem((String)arf,fillChar));
    }
    if(arf instanceof TextList){//+_+could expedite this frequently used case
      add(((TextList)arf).Vector());
    }
    if(arf instanceof Vector){//this includes FormattedLines itself.
     //failed to call back this class// return super.addAll((Vector)arf);
      Vector victor=(Vector)arf;//cast
      int count=victor.size();
      for(int i=0;i<count;i++){//retain order
        if(!add(victor.elementAt(i))){
          //do we try the rest?
          //no: quit on any error-for debug reasons
          return false;
        }
      }
      return true;
    }
    //could throw unsupportedCast, but we are too nice for that
    return false;//didan't add it!
  }

  public FormattedLineItem itemAt(int index) {
    return (index < size()) ? (FormattedLineItem)this.elementAt(index) : (FormattedLineItem)null;
  }

  public FormattedLines(){
    super();//pro forma
  }

  public FormattedLines(Object arf){
    this();
    add(arf);
  }

  public FormattedLines(int initialCapacity) {
    super(initialCapacity);
  }

////////////////////////////////////////////////////////
//transport related items
  public String toString() {
    EasyCursor ezp = asProperties();
    return ezp.toString();
  }

  protected String lineItemKey (int i){
    return ""+i;
  }

  public EasyCursor asProperties(){
    EasyCursor ezp = new EasyCursor();
    for(int i =size(); i-->0;) {
      ezp.setEasyCursor(lineItemKey(i), itemAt(i).asProperties());
    }
    ezp.setInt(countKey, size());
    return ezp;
  }

  public FormattedLines load(EasyCursor ezp){//was scrambling!
    int sizer=ezp.getInt(countKey);
    super.setSize(sizer);//allocate our elements
    for(int i = sizer; i-->0;) {//put each where it belongs
      super.set(i,new FormattedLineItem(ezp.getEasyCursor(lineItemKey(i))));
    }
    return this;
  }

  public FormattedLines fromString(String from) {
    EasyCursor ezp = new EasyCursor();
    ezp.fromString(from, true);
dbg.VERBOSE("fromString() using: " + ezp.toString());
    load(ezp);
    return this;
  }

  public static final FormattedLines createFromString(String from) {
    FormattedLines fl = new FormattedLines();
    fl.fromString(from);
    return fl;
  }

}
//$Id: FormattedLines.java,v 1.15 2001/07/19 01:06:54 mattm Exp $