package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/Timeout.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.util.*;

public class Timeout implements isEasy{
  private int value[];
  /**
   * @returns millis to wait for the given type of operation
   * 0 if invalid input
   */
  public int ticksFor(SinetTimeout stout){
    return (int)Ticks.forSeconds(stout.isLegal()?value[stout.Value()]:0);
  }
  /**
   * @returns timeout proportioned to traditional connection timeout.
   */
  private int multiplier(int actiontype){
    switch (actiontype) {
      case SinetTimeout.multiple:        return 3;
      case SinetTimeout.configuration:   return 2;
      case SinetTimeout.connection:      return 1;
      case SinetTimeout.holdoff:         return 3;
      case SinetTimeout.single:          return 2;
      default:                           return 1; //should never happen...
    }
  }
  /*package*/ void validateTimeouts(int baseTimeout){
    for (int tipe = SinetTimeout.Prop.numValues(); tipe-- > 0; ) {
      if (value[tipe] <= 0) {
        value[tipe] = multiplier(tipe) * baseTimeout;
      }
    }
  }

  public void save(EasyCursor ezc){
    for (int tipe = SinetTimeout.Prop.numValues(); tipe-- > 0; ) {
      ezc.setInt(SinetTimeout.Prop.TextFor(tipe),value[tipe]);
    }
  }

  public void load(EasyCursor ezc){
    for (int tipe = SinetTimeout.Prop.numValues(); tipe-- > 0; ) {
      value[tipe]=ezc.getInt(SinetTimeout.Prop.TextFor(tipe));
    }
  }

  public Timeout() {
    value=new int[SinetTimeout.Prop.numValues()];
  }

  public Timeout(int legacy) {
    this();
    validateTimeouts(legacy);
  }
}
//$Id: Timeout.java,v 1.2 2004/02/23 17:47:24 andyh Exp $
