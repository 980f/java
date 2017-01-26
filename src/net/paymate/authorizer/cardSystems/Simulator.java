package net.paymate.authorizer.cardSystems;

import net.paymate.net.EchoServer;

/**
* Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/Simulator.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.19 $
*/


import java.io.*;
import java.net.*;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.net.*;


public class Simulator /*extends SocketLineServer*/ {
  static Tracer dbg=new Tracer(Simulator.class,ErrorLogStream.ERROR);

//  byte [] trackingTag="W0.SIGN0001".getBytes();
//  static final String signOnReply="L0. 99999999 0000MN      000000000000INVALID NUMERIC 0";

/// NOTE: Commented this out cause I don't have time to fix MAverickResponse.format() right now.

//have to override LineServer's concept of a packet. WIll have a packetServer someday...
/*
  protected void core(InputStream incoming,OutputStream replyTo) {
    dbg.Enter("core");
    try {
    VisaBuffer request=VisaBuffer.NewSender(256);
    MaverickResponse reply = new MaverickResponse();

    while(true){
      try {
        //read until we get a whole buffer
        int ch=incoming.read();
              //dbg.VERBOSE("ch:"+Formatter.ox2(ch));

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
          //we no longer do Wrapper stuff so proceed with regular message:
              if(!false){
                dbg.ERROR("request not supported:");
                for(int i=0;i<parsed.size();i++){
                  dbg.WARNING("Field["+i+"]="+parsed.itemAt(i));
                }
              }
              VisaBuffer rawReply=reply.format();
              dbg.VERBOSE("reply["+rawReply.toSpam()+"]");
              replyTo.write(rawReply.packet());
              replyTo.flush();
            } else {
              // dbg.VERBOSE("LRC so far:"+Formatter.ox2(request.showLRC()));
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
*/

//  private Simulator(int port) {
//    super();
//    bind(port,null);
//  }
//
//  public static void main(String[] args) {
//    Simulator simulator = new Simulator(Integer.parseInt(args[0]));
//  }

}
//$Id: Simulator.java,v 1.19 2003/08/21 18:24:50 andyh Exp $
