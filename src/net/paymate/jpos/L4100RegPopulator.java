package net.paymate.jpos;

import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.config.simple.SimpleEntry;

import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by: andyh
 * Date: Mar 27, 2005   9:25:11 PM
 * (C) 2005 hal42
 */
public class L4100RegPopulator extends jpos.config.simple.AbstractRegPopulator {

  static Vector /*<L4100>*/ terminals = new Vector();

  public static void addTerminal( L4100 terminal ) {
    terminals.add( terminal );
  }

  public String getClassName() {
    return this.getClass().getName();
  }

  public void save( Enumeration entries ) throws Exception {

  }

  public void save( Enumeration entries, String fileName ) throws Exception {

  }

  public L4100RegPopulator( String id ) {
    super( id );
  }

  /**
   * required constructor for SimpleServiceManager
   */
  public L4100RegPopulator() {
    super( "L4100-demo" );
    System.err.println( "Using paymate registry populator for L4100" );
  }

  public void load() {
    //for each terninal
    for ( int i = 0; i < terminals.size(); i++ ) {
      L4100 terminal = (L4100) terminals.elementAt( i );

      JposEntry msrentry = new SimpleEntry( this );
      setTypeProperties( msrentry, terminal.ctrlNamer.msrName(), com.hypercom.fpe.jpos.msr.MSRService.class.getName(), "MSR", "1.7" );
      setCommonProperties( msrentry, terminal );
      //set default prompt?
      getJposEntries().put( msrentry.getLogicalName(), msrentry );
//
      JposEntry sigentry = new SimpleEntry( this );
      setTypeProperties( sigentry, terminal.ctrlNamer.sigName(), com.hypercom.fpe.jpos.sigcap.SignatureCaptureService.class.getName(), "SignatureCapture", "1.7" );
      setCommonProperties( sigentry, terminal );
      //set idle form?
      getJposEntries().put( sigentry.getLogicalName(), sigentry );
//      JposEntry pinentry;
//

    }

  }

  public static void coerce( JposEntryRegistry jpr ) {
    L4100RegPopulator crammer = new L4100RegPopulator();
    crammer.load();
    Enumeration cp = crammer.getEntries();
    while ( cp.hasMoreElements() ) {
      JposEntry jpe = (JposEntry) cp.nextElement();
      jpr.addJposEntry( jpe );
    }
  }

  private void setTypeProperties( JposEntry entry, String logicalName, String fpeclass, String jposClass, String jposLevel ) throws IllegalArgumentException {
    entry.addProperty( JposEntry.LOGICAL_NAME_PROP_NAME, logicalName );
    entry.addProperty( JposEntry.SERVICE_CLASS_PROP_NAME, fpeclass );
    entry.addProperty( JposEntry.DEVICE_CATEGORY_PROP_NAME, jposClass );
    entry.addProperty( JposEntry.JPOS_VERSION_PROP_NAME, jposLevel ); //can name for highest level we care about, even if higher is in existence
  }

  private void setCommonProperties( JposEntry entry, L4100 terminal ) throws IllegalArgumentException {
    entry.addProperty( JposEntry.SI_FACTORY_CLASS_PROP_NAME, "com.hypercom.fpe.jpos.ServiceInstanceFactory" );
    entry.addProperty( JposEntry.VENDOR_NAME_PROP_NAME, "Hypercom Corporation" );
    entry.addProperty( JposEntry.VENDOR_URL_PROP_NAME, "http://www.hypercom.com" );
    entry.addProperty( JposEntry.PRODUCT_DESCRIPTION_PROP_NAME, "Hypercom JavaPOS for FPE terminal" );
    entry.addProperty( JposEntry.PRODUCT_NAME_PROP_NAME, "Hypercom JavaPOS for FPE terminal" );
    entry.addProperty( JposEntry.PRODUCT_URL_PROP_NAME, "http://www.hypercom.com" );
    //these are need for multiple terminal systems but fail now because 'localhost' is not accepted
    // entry.addProperty( "iodriver.host", terminal.ioDriverOptions.getTCPIPhost() );
    // entry.addProperty( "iodriver.port", new Integer( terminal.ioDriverOptions.getTCPIPPort() ) );
  }

  public void load( String fileName ) {
    load();
  }

  public URL getEntriesURL() {
    return null;
  }

  public String getName() {
    return getClassName();
  }

}
