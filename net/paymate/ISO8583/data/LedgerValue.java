package net.paymate.ISO8583.data;

import net.paymate.util.*;
import net.paymate.awtx.RealMoney;
import java.text.*;

/**
 *  Description: money that is always positive
 *
 * @title        $Source: /cvs/src/net/paymate/ISO8583/data/LedgerValue.java,v $
 * @copyright    (C) 2000, Paymate.net
 * @version      $Revision: 1.19 $
 */

 public class LedgerValue implements Comparable {
//  private static final String defaultFormat = "#0.00";

  /**
   *  all real activity is in whole cents
   */
  protected long cents;
  /**
   *  true for negative values
   */
  protected boolean debt;
  /**
   *  number of items that went into this sum.
   */
  protected int itemcount = 0;

  private DecimalFormat asmoney = new DecimalFormat();


  /**
   *  Constructor for LedgerValue objects
   * @param  len      ...
   * @param  incents  ...
   */
  public LedgerValue(int len, long incents) {
    setFormat("C" + len + ";D" + len);
    setto(incents);
  }

  /**
   *  Constructor for LedgerValue objects
   * @param  len  ...
   */
  public LedgerValue(int len) {
    this(len, 0L);
  }

  /**
   *  Constructor for LedgerValue objects
   *
   * @param  frmt   ...
   * @param  image  ...
   */
  public LedgerValue(String frmt, String image) {
    setFormat(frmt);
    parse(image);
  }

  /**
   *  Constructor for LedgerValue objects
   *
   * @param  frmt  ...
   */
  public LedgerValue(String frmt) {
    setFormat(frmt);
    setto(0L);
  }

  /**
   *  Constructor for LedgerValue objects
   */
  public LedgerValue() {
    setto(0L);
    //format left at default
  }

  public static final LedgerValue New(RealMoney absolute, boolean negate){
    LedgerValue newone=new LedgerValue();
    newone.setto(absolute);
    newone.changeSignIf(negate);
    return newone;
  }

  public static final LedgerValue New(RealMoney absolute){
    return New(absolute,false);
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
   * @param  incents  ...
   * @return          ...
   */
  public LedgerValue setto(long incents) {
    debt = incents < 0;
    cents = debt ? -incents : incents;
    if (itemcount < 1) {
      itemcount = 1;
    }
    return this;
  }


  /**
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue setto(RealMoney rhs) {
    debt = false;
    setto(rhs.Value());
    return this;
  }


  //
  /**
   * @return    the cents value, signed ...
   */
  public long Value() {
    return debt ? -cents : cents;
  }


  /**
   * @param  obj  ...
   * @return      ...
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
      //presume it is string from tranjour!
      rhs = parseImage((String) obj);
    }
    else {
      //as speced by sun:
      throw new ClassCastException();
    }
    return net.paymate.jpos.awt.Math.signum(this.Value() - rhs);
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
    asmoney.format(amt, sbsnm, new FieldPosition(NumberFormat.INTEGER_FIELD));
    return sbsnm.toString();
  }


  /**
   * @return    ...
   */
  public LedgerValue changeSign() {
    debt = !debt;
    return this;
  }


  /**
   * @param  negate  ...
   * @return         ...
   */
  public LedgerValue changeSignIf(boolean negate) {
    if (negate) {
      debt = !debt;
    }
    return this;
  }


  /**
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue plus(RealMoney rhs) {
    return new LedgerValue().setto(Value() + RealMoney.Value(rhs));
  }


  /**
   * @param  rhs  increment to add to running total
   * @return      ...
   */
  public LedgerValue add(RealMoney rhs) {
    if (rhs != null) {
      ++itemcount;
    }
    return setto(Value() + RealMoney.Value(rhs));
  }


  /**
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue subtract(RealMoney rhs) {
    if (rhs != null) {
      --itemcount;
    }
    return setto(Value() - RealMoney.Value(rhs));
  }


  /**
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue plus(LedgerValue rhs) {
    return new LedgerValue().setto(Value() + Value(rhs));
  }


  /**
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue add(LedgerValue rhs) {
    if (rhs != null) {
      itemcount += rhs.itemcount;
    }
    return setto(Value() + Value(rhs));
  }


  /**
   * @param  rhs  ...
   * @return      ...
   */
  public LedgerValue subtract(LedgerValue rhs) {
    if (rhs != null) {
      itemcount -= rhs.itemcount;
      //this is well thought out.
    }
    return setto(Value() - Value(rhs));
  }


  /**
   *  Method
   *
   * @return    ...
   * @see ObjectRange before changing this
   */
  public String toString() {
    return Image();
  }


  /**
   * @param  image  ...
   * @return        ...
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
   *  Method
   *
   * @param  image  ...
   * @return        signed cents
   */
  public static final long parseImage(String image) {
    long change = 0;
    if (Safe.NonTrivial(image)) {
      int dp = image.indexOf('.');
      if (dp < 0) {
        //no dp means no dollar tolerate either
        change = Safe.parseLong(image);
        //
      }
      else {
        int comma;
        while ((comma = image.indexOf(',')) >= 0) {
          //need Safe.excise(String s, int position );
          image = image.substring(comma - 1) + image.substring(comma + 1, image.length());
        }

        int sign = image.indexOf('$');
        int firstdigit = sign >= 0 ? (sign + 1) : 0;
        change = Safe.parseLong(image.substring(firstdigit, dp)) * 100L;
        change += Safe.parseInt(image.substring(dp + 1));
      }
    }
    return change;
  }

}
//$Id: LedgerValue.java,v 1.19 2001/07/19 01:06:44 mattm Exp $
