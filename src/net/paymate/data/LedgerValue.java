package net.paymate.data;

/**
 *
 * @title        $Source: /cvs/src/net/paymate/data/LedgerValue.java,v $
 *  Description: sum of a lot of money items.
 * @copyright    (C) 2000-2002 Paymate.net
 * @version      $Revision: 1.2 $
 * @todo replace with a join of Accumulator and DecimalFormat.
 * the synchronized's were added to functions which update the object to relieve the user's from having to have to lock a monitor
 */
import net.paymate.util.*;
import net.paymate.awtx.RealMoney;
import java.text.*;
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;

public class LedgerValue implements Comparable, isEasy {
  /**
   *  all real activity is in whole cents
   */
  private long cents;
  /**
   *  number of items that went into this sum.
   */
  private int itemcount = 0;
/** deprecated internal formatter
 *  decimal formatter does scientific notation and such.
 *  With the advent of fracitonal penny rates we need a true dedicated-to-money
 *  formatter using appropriate logic to handle miniscule amounts.
 */
  private DecimalFormat asmoney = new DecimalFormat();


  public void save(EasyCursor ezc){//  interface isEasy
    ezc.setLong("value",cents);
    ezc.setInt("count",itemcount);
//    ezc.setString("format",asmoney.toPattern());
  }

  public void load(EasyCursor ezc){//  interface isEasy
    cents=ezc.getLong("value");
    itemcount=ezc.getInt("count");
  //format is not transmitted!
  }

  /**
   * mainsail legacy Constructor for LedgerValue objects
   * @param  len      number of digits to output for image
   * @param  incents  initial value
   */
  public LedgerValue(int len, long incents) {
    setFormat("C" + len + ";D" + len);
    setto(incents);
  }

  /**
   * mainsail legacy Constructor for LedgerValue objects
   * @param  len  number of digits to output for image
   */
  public LedgerValue(int len) {
    this(len, 0L);
  }

  /**
   * Constructor for human view for LedgerValue objects
   *
   * @param  frmt   @see DecimalFormat compliant pattaern for image and parse
   * @param  image  image of initial value
   */
  public LedgerValue(String frmt, String image) {
    setFormat(frmt);
    parse(image);
  }

  /**
   *  Constructor for human view for LedgerValue objects
   *
   * @param  frmt  @see DecimalFormat compliant pattaern for image and parse
   */
  public LedgerValue(String frmt) {
    setFormat(frmt);
    setto(0L);
  }

  /**
   *  Constructor for computational LedgerValue objects
   */
  public LedgerValue() {
    setto(0L);
    //format left at default
  }

/**
 * preferred instantiators, all public constructors may disappear.
 */
/**
 * @return clone of @param rhs
 */
  public static final LedgerValue New(LedgerValue rhs){
    LedgerValue newone=new LedgerValue();
    newone.cents=rhs.cents;
    newone.asmoney=(DecimalFormat)rhs.asmoney.clone();
    return newone;
  }

/**
 * @return new ledgervalue given @param absolute magnitude and @param negate sign
 */
  public static final LedgerValue New(RealMoney absolute, boolean negate){
    LedgerValue newone=new LedgerValue();
    newone.setto(absolute);
    newone.changeSignIf(negate);
    return newone;
  }

/**
 * @return new ledgervalue given @param absolute magnitude, presumed positive
 */
  public static final LedgerValue New(RealMoney absolute){
    return New(absolute,false);//realMoney is supposed to be always positive.
  }

  public static final LedgerValue New(long cents){
    return (new LedgerValue()).setto(cents);
  }
  public static final LedgerValue Zero(){
    return New(0);
  }
  public static final LedgerValue New(long cents,boolean negate){
    return (new LedgerValue()).setto(cents).changeSignIf(negate);
  }

  /**
   *  Sets the Format attribute of the LedgerValue object
   *
   * @param  format  The new Format value
   * @return         the object ...
   */
  public LedgerValue setFormat(String format) {
    asmoney.applyPattern(format);
    return this;
  }


  /**
   * set an initial value from cents
   * @param  incents
   * @return this
   */
  public LedgerValue setto(long incents) {
    cents = incents;
    itemcount = 1;
    return this;
  }


  /** set an initial value from RealMoney
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue setto(RealMoney rhs) {
    setto(rhs.Value());
    return this;
  }

  /**
   * @return    the cents value, signed ...
   */
  public long Value() {
    return cents;
  }


  /**
   * @param  obj  rhs of comparison
   * @return  this compared to object.
   */
  public int compareTo(Object obj) {
    long rhs;
    if (obj instanceof LedgerValue) {
      rhs = ((LedgerValue) obj).Value();
    }
    else if (obj instanceof RealMoney) {
      rhs = ((RealMoney) obj).Value();
    }
    else if (obj instanceof String) {
      rhs = parseImage((String) obj);
    }
    else {
      //as speced by sun:
      throw new ClassCastException();
    }
    return MathX.signum(this.Value() - rhs);
  }


  /**
   * @return    ...
   */
  public String Image() {
    StringBuffer sbsnm = new StringBuffer();
    sbsnm.setLength(0);
    double amt = Value() / 100.0;
    //IMPRECISION IS POSSIBLE DEPENDING UPON SOMEONE ELSE'S CODE+++
//+++ must read source for the following code to determine if it is precise enough.
//there are 100 possible legitimate fractions, we could generate them all and
//check them as part of validating each JRE.
    asmoney.format(amt, sbsnm, new FieldPosition(NumberFormat.INTEGER_FIELD));
    return String.valueOf(sbsnm);
  }


  /**
   * @return  'this' after changing its sign
   */
  public LedgerValue changeSign() {
    cents=-cents;
    return this;
  }


  /**
   * @return 'this' after changing its sign IFFI @ param negate is true
   */
  public LedgerValue changeSignIf(boolean negate) {
    if (negate) {
      changeSign();
    }
    return this;
  }


  /**
   * @param  rhs is added to this and returned as
   * @return new ledger value
   */
  public LedgerValue plus(RealMoney rhs) {
    return new LedgerValue().setto(Value() + RealMoney.Value(rhs));
  }


  /**
   * @param  rhs  add to running total
   * @return this
   */
  public synchronized LedgerValue add(RealMoney rhs) {
    if (rhs != null) {
      ++itemcount;
    }
    return setto(Value() + RealMoney.Value(rhs));
  }


  /**
   * @param  rhs  is subtracted from running total.
   * @return this with count set to 1 item
   */
  public synchronized LedgerValue subtract(RealMoney rhs) {
    if (rhs != null) {
      --itemcount;
    }
    return setto(Value() - RealMoney.Value(rhs));
  }


  /**
   * @return  new ledgerValue initialized to sum of this and @param rhs,
   * preserves count of both
   *
   * +++ This function is wrong. ??explain how or delete this remark
   */
  public LedgerValue plus(LedgerValue rhs) {
    return LedgerValue.New(rhs).add(rhs);
  }


  /**
   * @return 'this' after adding in value and item count from @param  rhs
   */
  public synchronized LedgerValue add(LedgerValue rhs) {
    if (rhs != null) {
      itemcount += rhs.itemcount;
    }
    return setto(Value() + Value(rhs));
  }


  /**
   * @return 'this' after subtract value <b>and item count</b> from @param  rhs
   * we sub the item count as the count is most often used to form an average.
   * to accumulate items with a negative contribution change the sign of rhs
   * then add it.
   */
  public synchronized LedgerValue subtract(LedgerValue rhs) {
    if (rhs != null) {
      itemcount -= rhs.itemcount;  //this is well thought out.
    }
    return setto(Value() - Value(rhs));
  }


  /**
   * @return  human readable image
   * @see ObjectRange before changing this! ObjectRange is touchy.
   */
  public String toString() {
    return Image();
  }


  /**
   * @return 'this' set to a single item whose image is @param  image
   */
  public LedgerValue parse(String image) {
    setto(parseImage(image));
    return this;
  }


  /**
   * @param  rhs  ...
   * @return      the cents value, signed ...
   */
  public static final long Value(LedgerValue rhs) {
    return rhs != null ? rhs.Value() : 0;
  }


  /**
   *  This *seems* to work better.   --- Any reason why we can't do this?  %%% @@@
   *  This was tested with the MAIN, below.   It worked perfectly.
   *
   * @param  image of cents or dollars.  Dollars must include a decimal point or else will be assumed cents ...
   * @return signed cents
   */
  public static final long parseImage(String image) {
    long change = 0;
    if (StringX.NonTrivial(image)) {
      image = StringX.replace(image, " ", ""); // get rid of spaces
      image = StringX.replace(image, "$", ""); // get rid of '$'//locale.currencySymbol
      image = StringX.replace(image, ",", ""); // get rid of ','//locale.separatorSymbol(or something) IS PERIOD IN EUROPE!!!!  WILL BREAK THIS ALGORITHM!!!!!
      int dp = image.indexOf('.');
      if(dp >=0) {
        change = Math.round(StringX.parseDouble(image) * 100); // let java do it
        // if you ABSOLUTELY HAVE to rewrite this, make it read through the image as a byte array, from rt to left, ignoring undesired characters, and handling the issue of 4.6 -> 4.06 that was in the old code
      } else {
        change = StringX.parseLong(image);
      }
    }
    return change;
  }

}
//$Id: LedgerValue.java,v 1.2 2004/01/09 11:46:04 mattm Exp $
