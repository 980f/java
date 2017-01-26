package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SocketModemConfig.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.*; // EasyCursor, isEasy
import net.paymate.serial.*; // Parameters
import java.util.Vector; //for init strings

/*
EG:
#Sample SocketModemConfig:
modemParams.baud=9600
listenPort=24135
localTimeout=3000
modemParams.dataBits=8
modemParams.niceness=10
modemParams.obufsize=8192
modemParams.portName=COM7
modemParams.flow=hard
modemParams.parity=N
phonenumber=3278598
connectTimeout=30000
modemParams.stopBits=1
*/

public class SocketModemConfig implements isEasy {

  private SocketModemConfig() {
  }

  public SocketModemConfig(String name) {
    socketModemName = name;
  }

  public SocketModemConfig(String name, EasyCursor ezc) {
    socketModemName = name;
    load(ezc);
  }

  String socketModemName = "SocketModemConfig.notnamed";

  int listenPort         = -1;    // socket port configuration
  int localTimeout       = -1;    // how long to wait for the modem to respond to local commands, in millis
  int connectTimeout     = -1;    // how long to wait for the dial response, in millis
  String phonenumber     = "notinited"; // --- this is the modem init string
  Parameters portParams = new Parameters("");  // serial port configuration
//  boolean waitForENQ  = true; //most hosts send ENQ when they answer the modem.
  double sleeptimeSeconds= 3; // the default
  boolean EOLusesLF      = true; // the default
  Vector init=new Vector();
  boolean justOnce       = true;

  /* things we need in this class:
      list of init commands for the modem to do when a socket tries to connect to you
      each command consists of:
        1. what to send
        2. what you will get back, and whether that is good or not
        3. how long to wait for it
  */

  private static final String listenPortKey       = "listenPort";
  private static final String localTimeoutKey     = "localTimeout";
  private static final String connectTimeoutKey   = "connectTimeout";
  private static final String phonenumberKey      = "phonenumber";
  private static final String portParamsKey       = "port";
  private static final String sleeptimeSecondsKey = "sleeptimeSeconds";
  private static final String EOLusesLFkey        = "EOLusesLF";
  private static final String initKey             = "init";
  private static final String justOnceKey         = "justOnce";

  public void save(EasyCursor ezc){
    ezc.setInt    (listenPortKey      , listenPort);
    ezc.setInt    (localTimeoutKey    , localTimeout);
    ezc.setInt    (connectTimeoutKey  , connectTimeout);
    ezc.setString (phonenumberKey     , phonenumber);
    ezc.setBlock  (portParams         , portParamsKey);
    ezc.setNumber (sleeptimeSecondsKey, sleeptimeSeconds);
    ezc.setBoolean(EOLusesLFkey       , EOLusesLF);
    ezc.setBoolean(justOnceKey        , justOnce);
  }

  public void load(EasyCursor ezc){
    if(ezc != null) {
      listenPort       = ezc.getInt(listenPortKey, listenPort);
      localTimeout     = ezc.getInt(localTimeoutKey, localTimeout);
      connectTimeout   = ezc.getInt(connectTimeoutKey, connectTimeout);
      phonenumber      = ezc.getString(phonenumberKey, phonenumber);
      ezc.getBlock(portParams, portParamsKey);
      sleeptimeSeconds = ezc.getNumber(sleeptimeSecondsKey, sleeptimeSeconds);
      EOLusesLF        = ezc.getBoolean(EOLusesLFkey, EOLusesLF);
      init             = ezc.loadVector(initKey,ModemInitPair.class);
      justOnce         = ezc.getBoolean(justOnceKey, justOnce);
    }
  }

  public String toSpam() {
    EasyCursor ezc = new EasyCursor();
    save(ezc);
    return ezc.toString();
  }

  public String toString() {
    return toSpam();
  }

  public static void main(String args[]) {
    // currently, this just outputs a sample ...
    SocketModemConfig smc = new SocketModemConfig("SampleSocketModem", new EasyCursor());
    System.out.println("#Sample SocketModemConfig:\n"+smc);
  }

}