package net.paymate.data;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: LicenseSwipe.java,v 1.10 2003/07/27 05:34:57 mattm Exp $
 */
import net.paymate.jpos.data.MSRData;
import net.paymate.lang.StringX;
import net.paymate.util.ErrorLogStream;

/**
 * parsing ID cards is split randomly between IdMangler, DriversLicense, and this class
 */
public class LicenseSwipe {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(LicenseSwipe.class);
  String state;
  int tracknumber;
  int start;
  int length;

  public static final String getNumber(MajorTaxArea mta, MSRData swipe){
  dbg.Enter("guesser");
    try {
      LicenseSwipe decoder=parser[0]; //texas, our texas, all hail the mighty state.
      String myTrack=swipe.track(1).Data(); //track for this state
      dbg.VERBOSE("rawtrack:<"+myTrack+"> start:"+decoder.start+" len:"+decoder.length);
      return StringX.subString(myTrack,decoder.start,decoder.start+decoder.length);
    } catch (Exception caught){
      dbg.Caught(caught);
      return "DLnotFound";
    }
    finally {
      dbg.Exit();
    }
  }

  protected static final LicenseSwipe[] parser={
    new LicenseSwipe("TX",2,6,8), //from inspection of raw tracks:
    //63 601 5 06788922=001219 561215
    //unknown, height 6'01", unk, license = expires@yynndd, d.o.b. yymmdd
  };

  protected LicenseSwipe(String zero,int one,int two,int three) {
    state=zero;
    tracknumber=one;
    start=two;
    length=three;
  }

}
//$Id: LicenseSwipe.java,v 1.10 2003/07/27 05:34:57 mattm Exp $