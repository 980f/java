package net.paymate.jpos.common;

import jpos.JposException;
import jpos.LineDisplay;
import jpos.MSR;
import jpos.POSKeyboard;
import jpos.SignatureCapture;
import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.events.DataEvent;
import jpos.events.DataListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;
import jpos.loader.JposServiceManager;
import net.paymate.Main;
import net.paymate.awtx.DisplayHardware;
import net.paymate.awtx.DisplayInterface;
import net.paymate.awtx.RealMoney;
import net.paymate.awtx.XPoint;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.data.StoreInfo;
import net.paymate.ivicm.comm.SerialConnection;
import net.paymate.ivicm.nc50.NC50;
import net.paymate.ivicm.nc50.nc50DisplayPad;
import net.paymate.jpos.ControlNamer;
import net.paymate.jpos.L4100;
import net.paymate.jpos.L4100RegPopulator;
import net.paymate.jpos.awt.Hancock;
import net.paymate.jpos.data.CardNumber;
import net.paymate.jpos.data.MSRData;
import net.paymate.jpos.data.SigData;
import net.paymate.jpos.data.Signature;
import net.paymate.serial.DisplayPad;
import net.paymate.serial.Parameters;
import net.paymate.serial.PortProvider;
import net.paymate.terminalClient.ClerkItem;
import net.paymate.terminalClient.IviForm.Legend;
import net.paymate.terminalClient.OurForm;
import net.paymate.terminalClient.POSForm;
import net.paymate.terminalClient.PeripheralSet;
import net.paymate.terminalClient.SigningOption;
import net.paymate.terminalClient.Uinfo;
import net.paymate.util.EasyCursor;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.QReceiver;

import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;

public class JposWrapper extends PeripheralSet implements DataListener, ErrorListener {

  public static interface opts {
    String root = "root"; //from terminalinfo
    String claimTimeout = "claim";
    String dot = ".";
  }

  public static interface value {
    int claimTimeout = 2000;
  }

  ControlNamer ctrlNamer;
  int claimTimeout;

  boolean haveCashpad = true; //force until L4100 based one works.
  boolean strictJpos = false;

  NC50 cashpad;

  jpos.SignatureCapture sigcap = null;
  jpos.LineDisplay display = null;
  jpos.POSKeyboard keypad = null;

  jpos.MSR swiper = null;
  jpos.PINPad pinpad = null;
  jpos.POSPrinter lpt = null;

  DisplayPad dispad = null;
  DisplayPad clerkface = null;
  DisplayHardware lineEntry = null;

  L4100 iodriverProxy;

  public static JposEntryRegistry jposRegistry() {
    JposServiceManager manager = jpos.loader.JposServiceLoader.getManager();
    return manager.getEntryRegistry();
  }

  public void dataOccurred( DataEvent e ) {
    Object source = e.getSource();
    String srctype = source.getClass().getName();

    if ( srctype.indexOf( "SignatureCapture" ) > 0 ) {
      post( new SigningOption( SigningOption.DoneSigning ) );
      //defer signature acquisition 'til PosTerminal state machine asks for it.
    } else if ( srctype.indexOf( "MSR" ) > 0 ) {

      try {
        MSRData swipe = new MSRData();
        swipe.setTrack( MSRData.T1, swiper.getTrack1Data() );
        swipe.setTrack( MSRData.T2, swiper.getTrack2Data() );
        swipe.ParseTrack1();
        swipe.ParseTrack2();
        post( swipe );
      }
      catch ( JposException e1 ) {
        post( e1 );
      }
    } else if ( srctype.indexOf( "Keypad" ) > 0 ) {
      try {
        for ( int kc = keypad.getDataCount(); kc-- > 0; ) {
          dispad.KeyStroked( keypad.getPOSKeyData() );
        }
      }
      catch ( JposException e1 ) {
        trace( e1 );
      }
    } else {
      System.out.println( "srctype: " + srctype );
    }
  }

  public void errorOccurred( ErrorEvent e ) {
    post( e ); //let PosTerminal debug stream report upon this for now.
  }

  protected void post( Object obj ) {
    if ( posterm != null ) {
      posterm.Post( obj );
    } else {
      System.err.println( String.valueOf( obj ) );
      if ( obj instanceof Throwable ) {
        Throwable error = (Throwable) obj;
        error.printStackTrace( System.err );
      }
    }
  }

  private JposWrapper( String root, EasyCursor ezc ) {
    dbg = ErrorLogStream.getForClass( JposWrapper.class );
    ctrlNamer = new ControlNamer( root );
    iodriverProxy = new L4100( ctrlNamer, ezc );
    //set parameters from properties +++
    iodriverProxy.run();
  }

  private void findSigcap() {
   System.err.println( "trying to attach to sigcap" );
    sigcap = new SignatureCapture();
    for ( int stage = 0; stage++ >= 0; ) {
      System.err.println( "sigcap stage = " + stage );
      try {
        switch ( stage ) {
          case 1:
            sigcap.open( ctrlNamer.sigName() );
            break;
          case 2:
            sigcap.claim( claimTimeout );
            break;
          case 3:
            sigcap.addDataListener( this );
            break;
          case 4:
            sigcap.addErrorListener( this );
            break;
          case 5:
            sigcap.setAutoDisable( false );
            break;
          case 6:
            sigcap.setDeviceEnabled( true );
            break;
          default:
            stage = -1;
            return;
        }
      }
      catch ( JposException e ) {
        trace( e );
        if ( stage == 1 ) {
          System.err.println( "sigcap not opened" );
          sigcap = null;
          stage = -1;
        } else {
          System.err.println( "sigcap attempting to ignore previous exception" );
        }
      }
    }
  }

  private void findSwiper() {
    System.err.println( "trying to attach to sigcap" );
    swiper = new MSR();
    for ( int stage = 0; stage++ >= 0; ) {
      System.err.println( "swiper stage = " + stage );
      try {
        switch ( stage ) {
          case 1:
            swiper.open( ctrlNamer.msrName() );
            break;
          case 2:
            swiper.claim( claimTimeout );
            break;
          case 3:
            swiper.addDataListener( this );
            break;
          case 4:
            swiper.addErrorListener( this );
            break;
          case 5:
            swiper.setAutoDisable( false );
            break;
          case 6:
            swiper.setDeviceEnabled( true );
            break;
          default:
            stage = -1;
            return;
        }
      }
      catch ( JposException e ) {
        trace( e );
        if ( stage == 1 ) {
          System.err.println( "swiper not opened" );
          swiper = null;
          stage = -1;
        } else {
          System.err.println( "swiper attempting to ignore previous exception" );
        }
      }
    }
  }

  private void findDisplay() {
    display = new LineDisplay();
    try {
      display.open( ctrlNamer.dispName() );
      display.claim( claimTimeout );
      display.setDeviceEnabled( true );
    }
    catch ( JposException e ) {
      trace( e );
      display = null;
    }
  }

  private void findKeypad() {
    keypad = new POSKeyboard();
    try {
      keypad.open( ctrlNamer.kbdName() );
      keypad.claim( claimTimeout );
      keypad.addDataListener( this );
      keypad.addErrorListener( this );
      keypad.setDeviceEnabled( true );
      keypad.setDataEventEnabled( true );
      keypad.setAutoDisable( false );
    }
    catch ( JposException e ) {
      trace( e );
      keypad = null;
    }
  }

  public  void findControls() {
    checkRegistry();
    //try twice until 5 sec versus 8 second bug is fixed by hypercom.
    findSigcap();
    if ( sigcap == null ) {
      findSigcap();
    }

    findSwiper();
    if ( swiper == null ) {
      findSwiper();
    }

    if ( !haveCashpad ) {
      findDisplay();
      findKeypad();
      if ( display != null && keypad != null ) {
        lineEntry = new JposDisplayPad( display, keypad, posterm );
      } else {
        dispad = DisplayPad.Null(); //else we get spammed with NPE's and other errors.
      }
    }
  }

  private void checkRegistry() {
    JposEntryRegistry jpr = jposRegistry();
    System.err.println( "regpop class:" + jpr.getRegPopulator().getClass().getName() );

    int numentries = showRegistry( jpr );
    if ( numentries == 0 ) {
      System.err.println( "there were no entries in JposEntryRegistry." );
      L4100RegPopulator.coerce( jpr );
      numentries = showRegistry( jpr );
      System.err.println( "after stuffing:" + numentries );
    }  else {
      System.err.println( "we only stuff it once ;)" );
    }
  }

  private int showRegistry( JposEntryRegistry jpr ) {
    Enumeration jpre = jpr.getEntries();
    int numentries = 0;
    while ( jpre.hasMoreElements() ) {
      ++numentries;
      JposEntry je = (JposEntry) jpre.nextElement();
      String prefix = je.getLogicalName();
      System.err.println( prefix );
      Iterator all = je.getProps();
      while ( all.hasNext() ) {
        JposEntry.Prop prop = (JposEntry.Prop) all.next();
        System.err.println( prefix + ":" + prop.getName() + "=" + prop.getValueAsString() );
      }
    }
    return numentries;
  }

  /**
   * added in separate keypad facility to get system developed.
   * not integrated with conifg mechanism yet so have to stuff things in here
   * that are normally done in TerminalInfo class...
   */
  private void cashpad( EasyCursor ezc ) {
    PortProvider.Config( Main.props() );
    ezc.setString( Parameters.protocolKey, "E71" );
    cashpad = new NC50( ctrlNamer.name( "NC50" ) ); //name is used for debug messages
    cashpad.Connect( SerialConnection.makeConnection( ezc, 4800 ) );
    clerkface = nc50DisplayPad.makePad( cashpad );
    dispad = clerkface;
  }

  /**
   * @param jtl           QReceiver normally a PosTerminal instance.
   * @param equipmentlist EasyCursor properties from server configuration tales
   * @return PeripheralSet instance of this class.
   */
  public static PeripheralSet fromDescription( QReceiver jtl, EasyCursor equipmentlist ) {
    String jposroot = equipmentlist.getString( opts.root );
    JposWrapper newone = new JposWrapper( jposroot, equipmentlist );
    newone.setParent( jtl );
    newone.claimTimeout = equipmentlist.getInt( opts.claimTimeout, value.claimTimeout );
    newone.cashpad( equipmentlist );
    newone.findControls();
    return newone;
  }

  /**
   * done whenever a form definition changes, or program starts .
   * no required functionality.
   * @param form OurForm
   */
  public void cacheForm( OurForm form ) {
    formMap[form.myNumber] = "NEDIT";
  }

  /**
   * this is called when we are nicely shutting down the system.
   */
  public void detachAll() {

    try {
      if ( sigcap != null ) {
        sigcap.release();
      }
      if ( swiper != null ) {
        swiper.release();
      }
      if ( pinpad != null ) {
        pinpad.release();
      }
      if ( display != null ) {
        display.release();
      }
      if ( keypad != null ) {
        keypad.release();
      }
      if ( lpt != null ) {
        lpt.release();
      }
    }
    catch ( JposException e ) {
      trace( e );
    }
  }

  /**
   * making this class match the ROS signature hack. Need to untangle these variants.
   * @param jposish Point[]
   * @return XPoint[]
   */
  private XPoint[] pun( Point[] jposish ) {
    XPoint[] pund = new XPoint[jposish.length];
    for ( int i = pund.length; i-- > 0; ) {
      Point p = jposish[i];
      if ( p.equals( Signature.MARK ) ) {
        System.out.println( "MARK" );
      }
      pund[i] = new XPoint( p.x, p.y );
    }
    Hancock.flipY( pund ); //HACK --- complete the quadrant stuff dammit.
    return pund;
  }

  boolean signing = false;

  /**
   * endSigcap
   * @param andAcquire boolean
   */
  public void endSigcap( boolean andAcquire ) {
    if ( sigcap != null ) {
      try {
        sigcap.endCapture();
        if ( andAcquire ) {
          //byte[] rawd = sigcap.getRawData(); //for debug.  can pass this to server instead of generic format
          post( new SigData( pun( sigcap.getPointArray() ) ) );
        }
      }
      catch ( JposException e ) {
        if ( signing ) {
          trace( e );
        }
      }
      finally {
        signing = false;
      }
    }
  }

  /**
   * getClerkPad
   * @return DisplayInterface
   */
  public DisplayInterface getClerkPad() {
    if ( dispad == null ) {
      if ( haveCashpad ) {
        dispad = clerkface;
      } else {
        dispad = DisplayPad.Null();
        dispad.attachTo( lineEntry );
      }
      if ( dispad == null ) {
        dispad = DisplayPad.Null();
      }
    }
    return dispad;
  }

  /**
   * getPrinter
   * @return PrinterModel
   *         todo Implement this net.paymate.terminalClient.PeripheralSet method
   */
  public PrinterModel getPrinter() {
    return PrinterModel.Null();
  }

  /**
   * gettingSignature
   * @return boolean whether teh device is supposedly asking for a signature.
   */
  public boolean gettingSignature() {
    return signing;
  }

  /**
   * gettingSwipe
   * @return boolean
   */
  public boolean gettingSwipe() {
    return amSwiping; //swiper.getState() == JposConst.JPOS_S_BUSY;
  }

  /**
   * haveSigCap
   * @return boolean
   */
  public boolean haveSigCap() {
    if ( sigcap == null ) { // todo: add 'and haven't given up trying'
      findSigcap();
    }
    return sigcap != null;
  }

  /**
   * overlayText adds dynamic text to a stored from
   * @param overlay Legend
   */
  public void overlayText( Legend overlay ) {
    //   *                todo Implement this net.paymate.terminalClient.PeripheralSet method
  }

  /**
   * printerCuts
   * @return boolean
   */
  public boolean printerCuts() {
    try {
      return lpt != null && lpt.getCapRecPapercut();
    }
    catch ( JposException e ) {
      return false;
    }
  }

  /**
   * @param si StoreInfo
   */
  public void setStoreInfo( StoreInfo si ) {
    //used to build forms.
    // todo: have to customize forms for each store until we learn how to generate them via code.
  }

  /**
   * showIdentity  service function.
   * @param ME String
   */
  public void showIdentity( String ME ) {
    if ( display != null ) {
      try {
        display.displayTextAt( 8, 6, ME, 0 ); //todo: real display attributes
      }
      catch ( JposException e ) {
        trace( e );
      }
    }
  }

  /**
   * startPinEntry
   * @param accountNumber CardNumber
   * @param amt           RealMoney
   * @param isRefund      boolean
   */
  public void startPinEntry( CardNumber accountNumber, RealMoney amt, boolean isRefund ) {
    if ( pinpad != null ) {
      try {
        pinpad.setAccountNumber( accountNumber.Image() );
        pinpad.setAmount( amt.absValue() );
        pinpad.setPrompt( isRefund ? 12 : 14 ); //todo: get real codes here ppad_msg
        pinpad.enablePINEntry();
        pinpad.setDataEventEnabled( true );
      }
      catch ( JposException e ) {
        post( e );
      }
    }
  }

  /**
   * startSignature
   * @param currentForm OurForm
   */
  public void startSignature( OurForm currentForm ) {
    if ( sigcap != null ) {
      try {
        sigcap.clearInput();
        sigcap.beginCapture( "SIGNATURE" ); //constatn from sivault.scb via fpedemo program
        sigcap.setDataEventEnabled( true );
        signing = true;
      }
      catch ( JposException e ) {
        post( e );
      }
    } else {
      findSigcap();//try to recoennect
    }
  }

  /**
   * startSwiper
   * <p/>
   * todo Implement this net.paymate.terminalClient.PeripheralSet method
   */
  public void startSwiper() {

    if ( swiper != null ) {
      try {
        swiper.clearInput();
        //formname is set in config.xml properties.
        displayForm( "NEDIT" ); //constant tied to sivault.scb
        swiper.setDataEventEnabled( true );
        amSwiping = true;
      }
      catch ( JposException e ) {
        post( e );
      }
    } else {
      findSwiper();//try to reconnect
    }
  }

  boolean amSwiping = false;

  /**
   * stopSwiper
   * <p/>
   */
  public void stopSwiper() {
    if ( amSwiping ) {
      amSwiping = false;
      try {
        if ( swiper != null && swiper.getDataEventEnabled() ) {
          swiper.setDataEventEnabled( false );
        }
      }
      catch ( JposException e ) {
        trace( e );
        swiper = null;//stop spammimg loop
      }
    }
  }

  boolean fakeSwipes = false;

  /**
   * @param clrkItem    ClerkItem
   * @param desiredForm OurForm
   * @param formIsStale boolean
   * @param uinfo       Uinfo
   *                    todo Implement this net.paymate.terminalClient.PeripheralSet method
   * @return true if customer display takes precedence over clerk display, i.e. if form requires suppression of clerk interface item
   */
  public boolean updateInterfaces( ClerkItem clrkItem, OurForm desiredForm, boolean formIsStale, Uinfo uinfo ) {
    //here is where JPOS showed its greatest weakness. our system has a concept of two humans operating it
    //but they might have to share hardware. Here we KNOW that we have single device and we must
    //intelligently arbitrate use, not race to stake a claim...
    boolean change = formIsStale;

    if ( desiredForm.hasSignature() ) {
      if ( !gettingSignature() ) {
        stopSwiper();
        startSignature( desiredForm );
      }
      //return false; //can't do clerk interface stuff
    } else {
      if ( gettingSignature() ) {
        endSigcap( false );
      }
    }

    if ( desiredForm.isSwiper ) {
      dbg.VERBOSE( "Form accepts swipes" );
      startSwiper();
      //pick swipe text variants here
    } else {
      stopSwiper(); //which has side effect of disabling as well as discarding input
    }
    //swiping is independent of other customer input, sigcap and pinpad are not!
    //i.e. during pin acquisition we disable all other forms of input to ensure there
    // is no temporal ambiguity as to which card a pin is being entered for.
    switch ( clrkItem.Value() ) { //pick out ones which supercede forms
      case ClerkItem.SaleType:
      case ClerkItem.SalePrice:
        //post(new RealMoney(moneyfaker++));
      case ClerkItem.MerchRef:
      case ClerkItem.AVSstreet:
      case ClerkItem.AVSzip:
      case ClerkItem.SecondCopy:
      case ClerkItem.WaitApproval:
        dbg.VERBOSE( "ClerkItem takes precedence:" + clrkItem.Image() );
        return false; //caller will come back with text to display
      case ClerkItem.ClerkID:
      case ClerkItem.ClerkPasscode:

        //stuffId();
        return false;
      case ClerkItem.PaymentSelect: //swipe card
        if ( fakeSwipes ) {
          post( MSRData.fakeOne() );
        }
        return fakeSwipes;
      case ClerkItem.NeedApproval:
        return false;
      default:
        if ( change ) {
          doForm( desiredForm, uinfo );
        }
        return false;
    }
  }

  String[] formMap = new String[POSForm.Prop.numValues()];

  /**
   * doForm
   * @param desiredForm OurForm
   * @param uinfo       Uinfo
   * @return boolean
   */
  private boolean doForm( OurForm desiredForm, Uinfo uinfo ) {
    return displayForm( formMap[desiredForm.myNumber] );
  }

  /**
   * this has NOT been tested with 'strictJpos'==true
   * @param formname String
   * @return boolean whether succeeeded
   */
  public boolean displayForm( String formname ) {
    if ( formname != null ) {
      if ( strictJpos ) {
        int length = formname.length();
        int[] cmd = new int[length + 1 + 1 + 2];
        int pi = 0;
        cmd[pi++] = 'V';
        cmd[pi++] = 28; //frame separator
        cmd[pi++] = 'F';
        cmd[pi++] = 'N';

        for ( int i = 0; i < length; ) {
          cmd[pi++] = formname.charAt( i++ );
        }
        try {
          swiper.directIO( 0, cmd, null );
          return true;
        }
        catch ( JposException ex ) {
          trace( ex );
          return false;
        }
      } else { //since we have taken it upon ourselves to crank up the driver we have a reference to it and:
        Exception ex = iodriverProxy.send( "V\034FN" + formname );
        trace( ex );
        return ex == null;
      }
    } else {
      return false;
    }
  }

  /**
   * trace
   * @param ex JposException
   */
  private static void trace( JposException ex ) {
    ex.printStackTrace( System.err );
    Exception orig = ex.getOrigException();
    if ( orig != null ) {
      orig.printStackTrace();
    }
  }

  private static void trace( Exception ex ) {
    if ( ex != null ) {
      if ( ex instanceof JposException ) {
        trace( (JposException) ex );
      } else {
        ex.printStackTrace();
      }
    }
  }

}
