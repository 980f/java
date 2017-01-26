/**
* Title:        IPTerminal
* Description:  Telnet terminal server for application (really a server +++ rename)
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author PayMate.net
* @version $Id: IPTerminal.java,v 1.27 2001/11/03 00:19:55 andyh Exp $
*/

//  This is a Multi-Threaded socketed Server.
// typical port is 49852
// attaching socket must be 192.168.1.* or 127.0.0.1
// +++ eventually route all debug logs to server

package net.paymate.terminalClient;
import net.paymate.net.IPSpec;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.connection.*;
import net.paymate.awtx.*; //user interaction classes
import net.paymate.ISO8583.data.*;//saletype , transfertype and friends

import net.paymate.*;
import net.paymate.util.timer.*;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.zip.*;
/// security stuff ....
import javax.net.ssl.*;


public class IPTerminal extends Thread implements ThreadReporter,AtExit {

  static final protected ErrorLogStream dbg=new ErrorLogStream(IPTerminal.class.getName());
  ///////////////////
  // stop thread safely
  boolean safeStop=false;
  public void Stop(){
    dbg.WARNING("safeStopping");
    safeStop=true; //we will die eventually.
    // no you won't  accept() blocks.  must do this:
    try {
      if(ssl_ss!=null) {
        ssl_ss.close(); // and pray
      }
    } catch (Exception e) {
      // dont' care
    }
  }
  public void AtExit(){
    Stop();
  }
  public boolean IsDown() {
    return !isAlive();
  }

  // thread stopper
  ///////////////////

  public IPTerminal(int port, PosTerminal terminal) {
    super("IPTerminal");
    this.port = port;
    this.terminal = terminal;
    this.setDaemon(true);
    Main.OnExit(this);
  }

  int port=0;
  PosTerminal terminal=null;
  Vector sockets = new Vector();

  public String status() {
    return this.toString();
  }

  public void Inform(TextList msg) {
    for(int i=0; i < msg.size(); i++) {
      Inform(msg.itemAt(i));
    }
  }
  public void Inform(String msg) {
    for(int i=0; i < sockets.size(); i++) {
      MTSktServer socket = (MTSktServer)sockets.elementAt(i);
      socket.Inform(msg);
    }
  }

  void unloadSockets(){//could be finalize, best to just do it explicitly.
    for(int i=sockets.size(); i-->0;) {
      ((MTSktServer)sockets.elementAt(i)).Stop();
    }
  }

  ServerSocket ssl_ss        =null;

  /**
   * this run accepts socket connections and starts a terminal session.
   */
  public void run() {
    Socket ssl_s               =null;

    // start with port (whatever) and increment until you find one you can use
    while(ssl_ss == null&&!safeStop) {
      try {
        ssl_ss = new ServerSocket(port);
      } catch(Exception caught) {
        dbg.WARNING("Port " + port + " probably in use: " + caught);
        port++;
        ssl_ss = null;
      }
    }
    dbg.WARNING("SocketServer: Accepting connections on port " + port);
    while ((ssl_ss!=null)&&!safeStop) {
      try {
        ssl_s = (Socket)ssl_ss.accept();
        String remoter = ssl_s.getInetAddress().getHostAddress();
        if(canAccess(remoter)) {
          dbg.WARNING("SocketServer: Communicating on port " + port); // +++ more info
          MTSktServer socketeer = new MTSktServer(ssl_s, terminal);
          sockets.add(socketeer);
          socketeer.start();
        } else {
          dbg.WARNING("SocketServer: Not allowing connection from ip: " + remoter);
        }
      }
      catch(Exception caught) {
        dbg.Caught(caught);
      }
    }
    unloadSockets();
  }

  private static final boolean canAccess(String remoteIP) {
    // --- this may not really do much
    return remoteIP.equals("127.0.0.1") ||
    ((remoteIP.indexOf("192.168.1") == 0) && !remoteIP.equals("192.168.1.1"));
  }

  /*
  public static final void main(String args[]) {
    // +++ check for valid args
    MTSktClient client = new MTSktClient(new IPSpec(args[0]));//one that accepts "hostname:portnumber"
    client.start();
  }
  */

}

/*
class MTSktClient extends Thread implements ThreadReporter {
  static final protected ErrorLogStream dbg=new ErrorLogStream(MTSktClient.class.getName());
  IPSpec ipSpec;
  public MTSktClient(IPSpec ipSpec) {
    this.ipSpec = ipSpec;
  }
  public void run() {
    try {
      Socket socket = new Socket(ipSpec.address, ipSpec.port);
      Streamer.backgroudSwapStreams(System.in, socket.getOutputStream());
      Streamer.backgroudSwapStreams(socket.getInputStream(), System.out);
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }
  public String status() {
    return this.toString();
  }

}
*/

class MTSktServer extends Thread implements ThreadReporter,AtExit {

  static final protected ErrorLogStream dbg=new ErrorLogStream(MTSktServer.class.getName());
  Socket ssl_s;
  PosTerminal terminal=null;
  PrintStream out = null;
  InputStream in = null;
  StringBuffer buffer = new StringBuffer();

  ///////////////////
  // stop thread safely
  boolean safeStop=false;
  public void Stop(){
    safeStop=true; //we will die eventually.
  }
  public void AtExit(){
    Stop();
  }
  public boolean IsDown() {
    return !isAlive();
  }

  // thread stopper
  ///////////////////

  static final String miniprompt = "$ ";
  static final String CRLF = "\n\r";
  static final String prompt = CRLF + miniprompt;
  static final int promptSize = miniprompt.length();
  static final String theBye = "bye";
  static final String theQuit = "quit";
  static final String theExit = "exit";
  static final String theHistory = "<ESC>";

  // boolean maxSocketsExceeded = false;
  boolean loggedin = false;


  MTSktServer (Socket s, PosTerminal terminal) {
    super("MTSktServer: "+s);
    ssl_s = s;
    this.terminal = terminal;
    this.setDaemon(true);
    Main.OnExit(this);
  }

  public String status() {
    return this.toString();
  }

  TextList info = new TextList();

  String [] zeta = {
    "eatdeceasedbeef",
    "101bytethebitbucket010",
  };

  public void Inform(String msg) {
    info.add(msg);
  }

  public void run() {
    try {
      out = new PrintStream(ssl_s.getOutputStream());
      in  = ssl_s.getInputStream();
    } catch (IOException ioe) {
      dbg.Caught(ioe);
    }
    int chr = 0;
    out.print("POSIPComm " + terminal.rev + CRLF);
    out.print("Type '" + theBye + "' to quit the telnet session but leave the program running."+CRLF);
    out.print("Type '" + theHistory + "' to get a list of the commands used thus far."+CRLF);
    out.print(prompt);
    out.flush();
    TextList commands = new TextList();
    int cmdIndex = -1;
    boolean lastWasEnter = false;
    while(!safeStop) {
      try {
        switch(chr) {
          case 27: { /* ESC: show command history */
            info.add("Command History:");
            info.appendMore(commands);
          } break;
          case 21: { /* CTRL-U : previous command */
            if(cmdIndex == 0) {
              // beep (can't go any higher)
              out.print((char)7);
            } else {
              cmdIndex--;
              backspace(buffer.length(), true, false);
              buffer.append(commands.itemAt(cmdIndex));
              if(buffer.length() > 0) {
                out.print(buffer.toString());
              }
              out.flush();
            }
          } break;
          case 4: { /* CTRL-D : next command */
            if(cmdIndex+1 == commands.size()) {
              // beep (can't go any lower)
              out.print((char)7);
            } else {
              cmdIndex++;
              backspace(buffer.length(), true, false);
              buffer.append(commands.itemAt(cmdIndex));
              if(buffer.length() > 0) {
                out.print(buffer.toString());
              }
              out.flush();
            }
          } break;
          case 10:
          case 13: { // also enter
            String buff = buffer.toString();
            dbg.VERBOSE("IPTerminal Executing: '" + buff + "'.");
            //buff = Safe.TrivialDefault(buff, (!lastWasEnter ? "?" :""));
            if(Safe.NonTrivial(buff)) {
              commands.add(buff);
              cmdIndex = commands.size();
              if(buff.equalsIgnoreCase(theBye) || buff.equalsIgnoreCase(theQuit) || buff.equalsIgnoreCase(theExit) ) {
                safeStop=true;//closeAll();
                break;
                //return;
              }
              out.print(CRLF);
              out.flush();
              // here is where we implement password-based security (fixed login for now)!
              // how about "101bytethebitbucket010"?
              TextList tl = null;
              if(!loggedin) {
                commands.setSize(0);  // clear the command history so the password can't be seen
                for(int i = zeta.length; i-->0;) {
                  if(buff.equalsIgnoreCase(zeta[i])) {
                    loggedin = true;
                    buff = "";
                    out.print(terminal.id());
                  }
                }
                if(!loggedin) { // get one chance !!!
                  safeStop = true;//closeAll();
                  break;
                  //return;
                }
              }
              if(loggedin) {
                tl = handleTerminalCommand(buff);
                dbg.VERBOSE("Done executing: '" + buff + "'.");
                if(tl != null) {
                  if(!lastWasEnter) {
                    out.print(CRLF);
                  }
                  for(int i=0; i< tl.size(); i++) {
                    String msg = tl.itemAt(i);
                    dbg.VERBOSE("out: '" + msg + "'");
                    out.print(msg+CRLF);
                  }
                }
              }
            }
            if(!lastWasEnter) {
              out.print(miniprompt);
              out.flush();
              // drop out
              buffer.setLength(0);
              dbg.VERBOSE("Starting fresh.");
            }
            lastWasEnter = true;
          } break;
          //          case 27: // escape
          case 8:  // backspace
          case 127: { // delete (which we will use to mean backspace)
            backspace(1, true, false);
            lastWasEnter = false;
          } break;
          default: {
            if((chr > 31) && (chr < 127)) {
              buffer.append((char)chr);
              // don't allow password to be visible
              if(loggedin) {
                out.print((char)chr);
              }
            } else {
              if(dbg.myLevel() == ErrorLogStream.VERBOSE) {
                info.add("Invalid character: " + chr);
              }
            }
            lastWasEnter = false;
            //            out.print((char)chr);
          } break;
        }
        dbg.VERBOSE("'" + buffer.toString() + "'[" + (char)chr + "/" + chr + "]");
        // +++ do a better way (use the incoming thread to deal with it)
        while((in.available() == 0) && !safeStop) {
          if(loggedin) {
            if(info.size() > 0) {
              // backspace
              backspace(buffer.length()+promptSize, false, true);
              while(info.size()>0) {
                out.print(info.itemAt(0)+CRLF);
                info.remove(0);
              }
              out.print(miniprompt);
              if(buffer.length() > 0) {
                out.print(buffer.toString());
              }
              out.flush();
            }
          }
          ThreadX.sleepFor(13/*113*//*47*/);
        }
        chr = in.read();
      } catch (Exception e) {
        dbg.Caught(e);
      }
    }
    closeAll();
  }

  private void closeAll() {
    out.print(CRLF);
    out.print("disconnecting ..."+CRLF);
    //    out.flush();
    Safe.Close(out);
    Safe.Close(in);
  }

  // +++ borken!
  private void backspace(int howMany, boolean fromBuffer, boolean withPrompt) {
    howMany = Math.min(howMany, buffer.length()+(withPrompt ? promptSize : 0));
    for(int i = howMany; i-->0;) {
      if(fromBuffer) {
        buffer.deleteCharAt(i);
      }
      if(loggedin) { // not outputting if not logged in
        out.print((char)8);
        out.print(' ');
        out.print((char)8);
      }
    }
  }

  synchronized String commandQuoted(String cmd){
    return "Command '" + cmd + "' ";
  }

  synchronized String tcNeeds(String cmd,String needsthis){
    return commandQuoted(cmd)+ "requires "+needsthis;
  }

  synchronized String tcNeeds(String cmd,TrueEnum needsthis){
    return commandQuoted(cmd)+"requires one of "+needsthis.toSpam();
  }

  static final boolean validEnumOption(TrueEnum ennum, String parameter, String cmd, TextList responses) {
    boolean stat = ennum.isLegal();
    if(!stat) {
      // check to see if it could be numeric instead
      int pint = Safe.parseInt(parameter);
      pint = (parameter.equals("0") ? 0 : (pint == 0 ? -1 : pint));
      ennum.setto(pint);
      stat = ennum.isLegal();
      if(!stat) {
        if(!parameter.equals("?") && !parameter.equals("")) {
          responses.add("Option '" + parameter + "' not valid for command '" + cmd + "'.");
        }
        dumpEnumOptions(ennum, cmd, responses);
      }
    }
    return stat;
  }


  static final void dumpEnumOptions(TrueEnum ennum, String title, TextList tl) {
    tl.add(title + " options:");
    tl.appendMore(ennum.dump("  "));
  }

  void Handle(int qi,String value){
    terminal.Post(new ItemEntry(qi,value));
  }

  public synchronized TextList handleTerminalCommand(String fullCmdFromIp) {
    TextList responses = new TextList();
    // break the command out into words
    TextList words = new TextList();
    words.wordsOfSentence(fullCmdFromIp);
    dbg.VERBOSE("IPTerminalCommand: '" + fullCmdFromIp + "'");
    if(words.size() < 1) {
      words.add("?");
    }
    // --- NOT!  make the first word (command) lower case
    String cmd = words.itemAt(0);
    TerminalCommand tc = new TerminalCommand(cmd);
    if(!tc.isLegal()){
      tc.setto(Safe.parseInt(cmd));
    }
    String param0 = words.itemAt(1);
    String param1 = words.itemAt(2);

    switch(tc.Value()) {
      default: {
        if(!cmd.equals("?") && !cmd.equals("")) {
          responses.add("Command '" + cmd + "' not understood.");
        }
        dumpEnumOptions(new TerminalCommand(), "Command", responses);
      } break;

      //Singel terminal commands
      //      case TerminalCommand.coupon:
      //      case TerminalCommand.print:
      case TerminalCommand.Reconnect:
      case TerminalCommand.sendSignature:
      case TerminalCommand.Pond:
      case TerminalCommand.Poff:
      case TerminalCommand.Clear:{
        terminal.Post(tc);
      } break;

      //all terminals commands.
      case TerminalCommand.Quiet:
      case TerminalCommand.Shout:
      case TerminalCommand.Shutdown:
      case TerminalCommand.Identify: {
        Appliance.BroadCast(tc);
      } break;

      case TerminalCommand.Alarms:{
        dbg.ERROR("alarms dump, then check",Alarmer.dump().toArray());
        Alarmer.Check();
      } break;

      case TerminalCommand.debug: {
        DebugOp op = new DebugOp(param0);
        if(validEnumOption(op, param0, cmd, responses)) {
          terminal.Post(new DebugCommand(op.Value()));
        }
      } break;
      case TerminalCommand.form: {//+_+ redo with POSForm enum. that's what it is for.
        int formNum = Safe.parseInt(Safe.TrivialDefault(param0, "-1"));
        OurForm form = OurForms.Find(formNum);
        if(form == null) {
          responses.add("Option '" + param0 + "' not valid for command '" + cmd + "'.");
          responses.add("form options [must use the NUMBER!]:");
          for(int i = 0; i < OurForms.FormCount(); i++) {//+_+ iterate over POSFOrm
            try {
              responses.add("  " + i + ": " + OurForms.Find(i).myName);
            }
            catch (Exception ex) {
              dbg.ERROR(ex.getMessage());
              dbg.ERROR(""+i+"th out of "+OurForms.FormCount());
            }
          }
        } else {
          terminal.Post(new OurForm(formNum));//###
          responses.add("Form '" + param0 + "' show attempted.");
        }
      } break;
      case TerminalCommand.function: {
        Functions op = new Functions(param0);
        if(validEnumOption(op, param0, cmd, responses)) {
          terminal.Post(new Functions(op.Value()));
        }
      } break;
      case TerminalCommand.login: {
        logLevels(responses);  // gets the password you just typed in to scroll off the screen
        if(words.size() < 3) {
          responses.add(commandQuoted(cmd)+"usage: " + cmd + " name password");
        } else {
          terminal.Post(new ClerkLoginCommand(new ClerkIdInfo(param0, param1)));
        }
      } break;

      case TerminalCommand.logLevel: {
        // displays (dumps) or sets the log levels (maybe do this with a special loglevel: set it to verbose, then it dumps all of the log levels and sets itself back to OFF)
        // all is a special case
        boolean all = param0.equalsIgnoreCase("ALL") || param0.equals("*") || param0.equals("") ;
        boolean justsave= param0.equalsIgnoreCase("SAVE");
        if(justsave){
          Main.saveLogging(param1);
          break;
        }
        Vector debuggers = LogSwitch.Sorted();
        if(Safe.NonTrivial(param1)) { // we are setting
          LogLevelEnum level = new LogLevelEnum(param1);
          if(validEnumOption(level, param1, cmd, responses)) {
            if(all) {
              LogSwitch.SetAll(level);
            } else {
              LogSwitch.setOne(param0, level);
            }
          } else {
            responses.add("Not a valid option: " + cmd);
          }
        }
        if(all) {
          logLevels(responses);
        } else {
          LogSwitch ls = LogSwitch.find(param0);
          if(ls == null) {
            responses.add("Log switch not found: " + param0);
          } else {
            responses.add(ls.Name() + ": " + ls.Level());
          }
        }
      } break;
      case TerminalCommand.setAsk: {
        ClerkItem op = new ClerkItem(param0);
        if(validEnumOption(op, param0, cmd, responses)) {
          if(Safe.NonTrivial(param1)){
            Question toanswer=terminal.clerkui.QuestionFor(op.Value());
            toanswer.inandout.setto(param1);
            dbg.VERBOSE("answering "+toanswer.prompt+" with "+toanswer.inandout.Image());
            terminal.clerkui.onReply (toanswer, AnswerListener.SUBMITTED);
          } else {
            dbg.VERBOSE("showing prompt");
            terminal.clerkui.ask(op.Value());
          }
        }
      } break;

      case TerminalCommand.history: {
        // +++ optimize this function, and add a line between header and body
        // displays the last N transactions, as saved in the ActionList, to this (and to the log?)
        //move most of it into posterminal
        terminal.ActionHistoryReport(responses);
      } /*break;*/
      case TerminalCommand.stats: {
        terminal.ActionStatsReport(responses);
      } break;
      case TerminalCommand.sendTxn: {
        terminal.postClerkEvent(ClerkEvent.Send);
        // does a "dump" when it is done.
        terminal.dump(responses);
      } break;
      case TerminalCommand.set:{
        //        tsc(param0,param1);
        dbg.VERBOSE("set:");
        // for setting data members of the PosTerminal
        if(Safe.NonTrivial(param0)) {
          param0 = Safe.replace(param0, ".", "");
          // +++ code more sets =>use ClerkItems and ClerkUI questions...
          TerminalSetCommand tsc = new TerminalSetCommand(param0);
          boolean valid = (Safe.NonTrivial(param1) && tsc.isLegal());
          switch(tsc.Value()) {
            default : {
              responses.add(tcNeeds(cmd,tsc));
            } break;

            case TerminalSetCommand.cardaccountNumber: {
              if(!valid) {
                responses.add(tcNeeds(cmd,"a card number parameter (no spaces)."));
              } else {
                terminal.Post(new PaySelect(PaySelect.ManualCard));
                Handle( ClerkItem.CreditNumber,param1);
                responses.add("");
              }
            } break;
            case TerminalSetCommand.cardexpirationDate: {
              if(!valid) {
                responses.add(tcNeeds(cmd,"a 4-digit (mmYY) expiration date."));
              } else {
                Handle(ClerkItem.CreditExpiration,param1);
              }
            } break;
            case TerminalSetCommand.saletypepayby: {
              if(!valid) {
                responses.add(tcNeeds(cmd,new PaySelect()));
              } else {
                responses.add(genSuccessStr(cmd,terminal.Post(new PaySelect(param1)) ));
              }
            } break;
            case TerminalSetCommand.saletypeop: {
              if(!valid) {
                responses.add(tcNeeds(cmd,new TransferType()));//+_+ current value not available
              } else {
                responses.add(genSuccessStr(cmd,terminal.Post(new TransferType(param1)) ));
              }
            } break;
            case TerminalSetCommand.saletypesource: {
              if(!valid) {
                responses.add(tcNeeds(cmd,new EntrySource()));
              } else {
                responses.add(genSuccessStr(cmd,terminal.Post(new EntrySource(param1))));
              }
            } break;
            case TerminalSetCommand.salemoneyamount: {
              if(!valid) {
                responses.add(tcNeeds(cmd,"the number of cents in the amount (not dollars)."));
              } else {
                Handle(ClerkItem.SalePrice,param1);
              }
            } break;
            case TerminalSetCommand.salepreapproval: {
              if(!valid) {
                responses.add(tcNeeds(cmd,"the preapproval code."));
              } else {
                responses.add(tcNeeds(cmd,"an implementation!"));
              }
            } break;
            case TerminalSetCommand.checkIdlicense: {
              if(!valid) {
                responses.add(tcNeeds(cmd,"a driver's license."));
              } else {
                Handle(ClerkItem.License,param1);
              }
            } break;
            case TerminalSetCommand.voidstan: {
              if(!valid) {
                responses.add(tcNeeds(cmd,"the stan of the transaction to be voided."));
              } else {
                Handle(ClerkItem.RefNumber,param1);
              }
            } break;
          }
        }
        terminal.dump(responses);
      } break;
    }
    return responses;
  }

  synchronized void logLevels(TextList responses) {
    responses.appendMore(LogSwitch.listLevels());
  }

  private synchronized String genSuccessStr(String cmd, boolean set) {
    return "Command '" + cmd + "' " + (set ? "" : "NOT ") +"successful";
  }

}
//$Id: IPTerminal.java,v 1.27 2001/11/03 00:19:55 andyh Exp $
