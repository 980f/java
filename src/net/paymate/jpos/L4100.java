package net.paymate.jpos;

import com.hypercom.commservice.CommException;
import com.hypercom.commservice.IODriver;
import com.hypercom.commservice.tcpip.TCPIPOptions;
import com.hypercom.fpe.jpos.ClaimManager;
import com.hypercom.util.Logger;
import net.paymate.util.EasyCursor;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * <p>Title: $Source:
 * /home/andyh/localcvs/pmnet/cvs/src/builders/serversinet.jpx,v $</p>
 * <p>Description:
 * start the iodriver that listens for L4100 terminals.
 * at presetn it only tolerates listening to one terminal.
 * once hypercom tells us how to run multi-terminals we will
 * do whatever is needed in this class.
 * </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */
public class L4100 {
  private String ioDriverUrl() {
    return "//localhost:" + registryPort + "/iodriver";
  }

  private int registryPort;// = 4209;
  //for demo can only deal with one due to intolerable quantity of code in xml registry
  public ControlNamer ctrlNamer;
  public TCPIPOptions ioDriverOptions = new TCPIPOptions(); //ecr host and port.
  public IODriver ioDriver; // = new IODriver();

  public static interface key {
    String terminalHost = "host";
    String terminalPort = "port";
    String serverPort = "rmiport";
  }

  public static interface value {
    String terminalHost = "localhost";
    int terminalPort = 5110;

  }


  /**
   * this guy will create a Jpos Registry in memory, no need for any files.
   * @return JposEntryRegistry
   * @deprecated dependent work not completed.
   */
//  public static JposEntryRegistry getJposRegistry() {
  //   if ( theRegistry == null ) {
//      System.setProperty( jpos.util.JposProperties.JPOS_SERVICE_MANAGER_CLASS_PROP_NAME, jpos.loader.simple.SimpleServiceManager.class.getName() );
  //     System.setProperty( jpos.util.JposProperties.JPOS_REG_POPULATOR_CLASS_PROP_NAME,net.paymate.jpos.L4100.class.getName());
  //    theRegistry = JposWrapper.jposRegistry();
  //   }
//    return theRegistry;
//  }

  /**
   * paymate convention: rmi port is one less than terminal port.
   * will have to write jposregistry.xml with values derived from our own storage in order to keep names synchronized:
   * terminal has name and port number
   * JposEntry gets terminalname.device for its logical and the iodriver.host and iodriver.port from here.
   * @param ezc EasyCursor
   */
  public L4100( ControlNamer ctrlNamer, EasyCursor ezc ) {
    ezc.push( "L4100" );
    try {
      this.ctrlNamer = ctrlNamer;
      System.setProperty( jpos.util.JposProperties.JPOS_SERVICE_MANAGER_CLASS_PROP_NAME, jpos.loader.simple.SimpleServiceManager.class.getName() );
      System.setProperty( jpos.util.JposProperties.JPOS_REG_POPULATOR_CLASS_PROP_NAME, net.paymate.jpos.L4100RegPopulator.class.getName() );

      ioDriverOptions.setTCPIPhost( ezc.getString( key.terminalHost, value.terminalHost ) );
      ioDriverOptions.setTCPIPPort( ezc.getInt( key.terminalPort, value.terminalPort ) );
      registryPort = ezc.getInt( key.serverPort, 4209 );//magic value from hell

      L4100RegPopulator.addTerminal( this);
    }
    finally {
      ezc.pop();
    }
  }


//  too many protected fields to tunnel into with out real source code
  // SignatureCaptureService getSignatureCapture(){
//    SignatureCaptureService sigcap= new com.hypercom.fpe.jpos.sigcap.SignatureCaptureService();
//    sigcap.
//    return sigcap;
//  }

  public void startLogging() {
    System.setProperty( "com.hypercom.util.LogLevel", "3" );
    Logger.removeAllLogWriters();
    Logger.addLogWriter( new com.hypercom.util.PrintStreamLogWriter( System.err ) );
    Logger.addLogWriter( new com.hypercom.util.PrintStreamLogWriter( "/tmp/jpos.hypercom.log" ) );
    Logger.setLogLevel( Logger.LOG_DEBUG );
  }

  /**
   * RMI registry preparation
   */
  public void prepareRegistry() {
    System.setProperty( "java.rmi.dgc.leaseValue", "10000" ); //this presumes that RMI stuff is running in same JVM
    try {
      LocateRegistry.createRegistry( registryPort );
    }
    catch ( RemoteException ex ) {
      try {
        LocateRegistry.getRegistry( registryPort );
      }
      catch ( RemoteException ex1 ) {
        Logger.logError( "Could not prepare registry at port " + registryPort, ex1 );
      }
    }
    Logger.logInfo( "RMI registry ready" );
  }

  /**
   * create the  iodriver.
   */
  public void prepareIodriver() {
    try {
      ioDriver = new IODriver();
      ioDriver.setOptions( ioDriverOptions );
      Naming.bind( ioDriverUrl(), ioDriver );
      Logger.logInfo( "IODriver bound" );
    }
    catch ( AlreadyBoundException ex ) {
      Logger.logInfo( "IODriver already bound at " + registryPort );
    }
    catch ( RemoteException ex ) {
      /** @todo Handle this exception */
    }
    catch ( CommException ex ) {
      /** @todo Handle this exception */
    }
    catch ( MalformedURLException ex ) {
      /** @todo Handle this exception */
    }
  }

  public void prepareClaimManager() {
    try {
      Naming.bind( "//localhost:" + registryPort + "/claim_manager", new ClaimManager() );
      Logger.logInfo( "ClaimManager bound" );
    }
    catch ( AlreadyBoundException ex ) {
      Logger.logInfo( "ClaimManager already bound at " + registryPort );
    }
    catch ( RemoteException ex ) {
      /** @todo Handle this exception */
    }
    catch ( MalformedURLException ex ) {
      /** @todo Handle this exception */
    }
  }

  public void run() {
    startLogging();
    prepareRegistry();
    prepareIodriver();
    prepareClaimManager();
    //and objects in the above stay alive apparently due to registry bindings.
  }

  public Exception send( byte[] cmd ) {
    if ( ioDriver != null ) {
      try {
        ioDriver.send( cmd );
        return null;
      }
      catch ( CommException ex ) {
        return ex;
      }
      catch ( RemoteException ex ) {
        return ex;
      }
    } else {
      return null; //or maybe NPE with locaiton information.
    }
  }

  public Exception send( String cmd ) {
    return send( cmd.getBytes() );
  }

}
