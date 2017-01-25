package net.paymate;

/**
 * Title:        $Source: /cvs/src/net/paymate/WebServer.java,v $
 * Description:  WebServer
 * --- We don't have to figure out the configuration files. We can write a java app
 * that instantiates a server and calls functions on it to set options.
 * With source code and its javadoc I think that would be faster than figuring
 * out the xml configuration stuff.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import  net.paymate.util.*;

public class WebServer {

  private static final ErrorLogStream dbg=new ErrorLogStream(WebServer.class.getName());
  public Main app;
  public String hostname = "uninitialized";

  public WebServer(String argv[]) {
    try {
      dbg.Enter("WebServer");
      app=new Main(WebServer.class);
      // load config
      app.stdStart(argv);
// start the webserver here, pass the config in

    } catch (Throwable caught) {
      dbg.Caught(caught);
      //if embedded give a special exit code to restart the app.
      //will need different codes for different exceptions.
      Main.stdExit(3);
    } finally {
      dbg.Exit();
    }
    // do we add an internal retry???
    Main.stdExit(2);
  }

  public static final void main(String argv[]){
    new WebServer(argv); // maybe keep this for use later?
  }

  private boolean goingdown = false;
  public void shutdown() {
    goingdown = true;
// somehow, shutdown the webserver here
  }
  public boolean isGoingdown() {
    return goingdown;
  }

}
