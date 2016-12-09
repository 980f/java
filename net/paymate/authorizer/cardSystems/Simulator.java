package net.paymate.authorizer.cardSystems;

import net.paymate.connection.EchoServer;

/**
* Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/Simulator.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.5 $
*/


import java.io.*;
import java.net.*;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.net.*;


public class Simulator extends EchoServer {
  static Tracer dbg=new Tracer(Simulator.class.getName(),ErrorLogStream.VERBOSE);

//  byte [] trackingTag="W0.SIGN0001".getBytes();
  static final String signOnReply="L0. 99999999 0000MN      000000000000INVALID NUMERIC 0";


  protected void core(InputStream incoming,OutputStream replyTo) throws IOException {
    dbg.Enter("core");
    try {
    VisaBuffer request=VisaBuffer.NewSender(256);
    VisaBuffer reply=VisaBuffer.NewReceiver(256);
    while(true){

      try {
        //read until we get a whole buffer
        int ch=incoming.read();
              //dbg.VERBOSE("ch:"+Safe.ox2(ch));

        switch(ch){
          case -1: {
            throw new IOException("peer blew me off"); //crash and burn...socket closed.
          } //break;
          case VisaBuffer.STX:{
            request.start(256);//max packet size
          } break;
          case 0:  //nul's may not be allowed...
          default:{
            request.append(ch);
            // dbg.VERBOSE("so far: "+URLEncoder.encode(new String(request.packet())));
            if(request.last() == VisaBuffer.ETX){//then we have a complete buffer
              dbg.VERBOSE("req["+request.toSpam(request.Size())+"]");
              //
              TextList parsed= request.fields();
              dbg.VERBOSE("List:"+parsed.asParagraph("/"));
              String tag=parsed.itemAt(0);
              dbg.VERBOSE("tag:"+tag+" indexof:"+tag.indexOf("W0.SIGN"));

              if(tag.indexOf("W0.SIGN")>=0){
                dbg.VERBOSE("found signon tag");
                reply.start(256);
                reply.appendFrame(tag);
//                reply.endFrame();
                dbg.VERBOSE("signon:"+ signOnReply);
                reply.appendFrame(signOnReply);
//                reply.endFrame();
                reply.endFrame();
                reply.end();
                dbg.VERBOSE("reply["+reply.toSpam(reply.Size())+"]");
                replyTo.write(reply.packet());
                replyTo.flush();
              } else {
                dbg.ERROR("not implemented");
              }
            } else {
              // dbg.VERBOSE("LRC so far:"+Safe.ox2(request.showLRC()));
            }
          }
        }
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    }
    } finally {
      dbg.Exit();
    }
  }

  public Simulator(int port) {
    super(port);
  }

  public static void main(String[] args) {
    Simulator simulator = new Simulator(Integer.parseInt(args[0]));
  }

}
//$Id: Simulator.java,v 1.5 2001/10/05 17:28:52 andyh Exp $
