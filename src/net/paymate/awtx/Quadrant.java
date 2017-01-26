package net.paymate.awtx;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/awtx/Quadrant.java,v $</p>
 * <p>Description: used for flipping around signature images, reconciling coordinate systems between input and output devices.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.util.isEasy;
import net.paymate.util.EasyCursor;
import net.paymate.lang.Bool;
public class Quadrant implements isEasy {
  private boolean x;
  private boolean y;

  public boolean XisNegative(){
    return x;
  }

  public boolean YisNegative(){
    return y;
  }

  private Quadrant(  boolean x, boolean y) {
    this.x=x;
    this.y=y;
  }

  public static Quadrant Combine(Quadrant one, Quadrant another){
    Quadrant newone=Quadrant.Clone(one);
    //  '!=' conveniently produces the truth table we need.
    // ...Don't worry about how this reads.
    newone.x= newone.x != another.x;
    newone.y= newone.y != another.y;
    return newone;
  }

  public static Quadrant Clone(Quadrant rhs){
    return new Quadrant(rhs.x,rhs.y);
  }

  public static Quadrant First(){
    return new Quadrant(false,false);
  }

  public static Quadrant Second(){
    return new Quadrant(true,false);
  }

  public static Quadrant Third(){
    return new Quadrant(true,true);
  }
  /**
   * popular with many printers and video displays.
   * @return
   */
  public static Quadrant Fourth(){
    return new Quadrant(false,true);
  }
  /**
   * grey coded quadrant
   * @return abstract int
   */
  private int encoded(){
    return (x?1:0)+(y?2:0);
  }

  private void decode(int quad){
    x= Bool.bitpick(quad,0);
    y= Bool.bitpick(quad,1);
  }

  private static final String myKey="quadrant";

  public void save(EasyCursor ezc){
    ezc.setInt(myKey,encoded());
  }

  public void load(EasyCursor ezc){
    decode(ezc.getInt(myKey,0));//default is set for legacy reasons
  }

  public String toString(){
    switch (encoded()) {
      case 0: return "first";
      case 1: return "second";
      case 3: return "third";
      case 2: return "fourth";
      default: return "zed";
    }
  }

}
//$Id: Quadrant.java,v 1.2 2004/02/26 18:40:50 andyh Exp $
