package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/PopClient.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

// +++ needs more work only if it is ever going to ACTUALLY check pop email.

import java.io.*;
import java.net.*;
import net.paymate.util.*;

public class PopClient {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PopClient.class, ErrorLogStream.VERBOSE);

  public PopClient() {
  }

  public static final String PopQuickCheck(String toIP, String username, String password) {
    PopClient pc = new PopClient();
    return pc.popQuickCheck(toIP, username, password);
  }

  private OutputStream writeSocket = null;

  private static final byte [] CRLFbytes = {13, 10};
  private static final String CRLF = new String(CRLFbytes);

  /**
   * Allows you to use POP to precheck the mailbox for systems that require that for general internet SMTP access.
   *
   * Returns null if no errors occurred.
   *
   */
  private String popQuickCheck(String toIP, String username, String password) {
    String errorMessage = null;
    Socket socket = null;
    BufferedReader readSocket = null;
    try {
      dbg.Enter("popQuickCheck");
      socket = new Socket(toIP, 110);
      readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writeSocket = /*new PrintWriter(*/socket.getOutputStream()/*, true)*/;
      if((readSocket == null) || (writeSocket == null)) {
        dbg.ERROR("Could not open socket correctly: readSocket="+readSocket+", writeSocket="+writeSocket);
      } else {
        for(int i = 0; i < 5; i++) {
          String cmd = "";
          switch(i) {
            case 0: {
              cmd = null; // means not to write; this reads for that first +OK, saying who the server is
            } break;
            case 1: {
              cmd = "USER "+username+CRLF;
            } break;
            case 2: {
              cmd = "PASS "+password+CRLF;
            } break;
            case 3: {
              cmd = "STAT"+CRLF;
            } break;
            case 4: {
              cmd = "QUIT"+CRLF;
            } break;
          }
          if(cmd != null) {
            byte [] toWrite = cmd.getBytes();
            writeSocket.write(toWrite);
            writeSocket.flush();
            dbg.VERBOSE("Sent "+Ascii.bracket(toWrite)+".");
          }
          String rets = readSocket.readLine(); //read response
          dbg.VERBOSE("Received "+Ascii.bracket(rets)+".");
          if(!rets.startsWith("+OK")) {
            errorMessage = "POP Error: " + rets;
            throw new Exception(errorMessage);
          }
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      try {
        if(readSocket != null) {
          readSocket.close();
        }
        if(writeSocket != null) {
          writeSocket.close();
        }
        if(socket != null) {
          socket.close();
        }
      } catch (Exception e) {
        dbg.Caught(e);
      }
      return errorMessage;
    }
  }

}

