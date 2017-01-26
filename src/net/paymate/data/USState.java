// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/USState.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class USState extends TrueEnum {
  public final static int AK=0;
  public final static int AL=1;
  public final static int AR=2;
  public final static int AZ=3;
  public final static int CA=4;
  public final static int CO=5;
  public final static int CT=6;
  public final static int DC=7;
  public final static int DE=8;
  public final static int FL=9;
  public final static int GA=10;
  public final static int HI=11;
  public final static int IA=12;
  public final static int ID=13;
  public final static int IL=14;
  public final static int IN=15;
  public final static int KS=16;
  public final static int KY=17;
  public final static int LA=18;
  public final static int MA=19;
  public final static int MD=20;
  public final static int ME=21;
  public final static int MI=22;
  public final static int MN=23;
  public final static int MO=24;
  public final static int MS=25;
  public final static int MT=26;
  public final static int NC=27;
  public final static int ND=28;
  public final static int NE=29;
  public final static int NH=30;
  public final static int NJ=31;
  public final static int NM=32;
  public final static int NV=33;
  public final static int NY=34;
  public final static int OH=35;
  public final static int OK=36;
  public final static int OR=37;
  public final static int PA=38;
  public final static int RI=39;
  public final static int SC=40;
  public final static int SD=41;
  public final static int TN=42;
  public final static int TX=43;
  public final static int UT=44;
  public final static int VA=45;
  public final static int VT=46;
  public final static int WA=47;
  public final static int WI=48;
  public final static int WV=49;
  public final static int WY=50;

  public int numValues(){ return 51; }
  static final TextList myText = TrueEnum.nameVector(USState.class);
  protected final TextList getMyText() {
    return myText;
  }
  static USState Prop=new USState();
  public USState(){
    super();
  }
  public USState(int rawValue){
    super(rawValue);
  }
  public USState(String textValue){
    super(textValue);
  }
  public USState(USState rhs){
    this(rhs.Value());
  }

}
