package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SocketModemPool.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.*; // Service

public class SocketModemPool extends Service {

  public SocketModemPool(String name, ServiceConfigurator cfg) {
    super(name, cfg);
  }

  public void up() {
    // +++ do the work here !!!
  }

  public void down() {
    // +++ do some work here !!!
  }

  public boolean isUp() {
    // +++ do some work here !!!
    return false;
  }

//  public static final void main(String [] args) {
    // +++
    // takes a config file that has one or more SocketModems listed
    // for each one listed, creates a SocketModem

    // +++ future ...
    // implements a "control layer" that provides a servlet on a socket, more or less (+++ needs to be integrated into a servlet system on Monster).
    //  this control layer allows us to kill sockets, hangup modems, stop the software, etc.

    // until above is implemented ...
    // creates a PIDLOCK file in the OS
    // and puts its MAIN thread in a loop, sleeping for one second, then checking for that PIDFILE.
    // If the pidfile is gone, closes the program [exits the loop after notifying all SocketModems and joining their threads].
//  }
}