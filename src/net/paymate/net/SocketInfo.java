package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/SocketInfo.java,v $
 * Description:  socket debug utilites.
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.*;
import net.paymate.text.Formatter;
//+_+ prune list
import java.io.*;
import java.net.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;

public class SocketInfo {
////////////////////////////////////////////////////////////

  public static final IPSpec hostAddress(Socket sock){
    return IPSpec.New(sock.getInetAddress().getHostAddress(),sock.getPort());
  }

  public static final IPSpec localAddress(Socket sock){
    return IPSpec.New(sock.getLocalAddress().getHostAddress(),sock.getLocalPort());
  }

 /**
  * @return whether current socket is to a somewhat local host using paymate's conventions
  * @param socket being investigated
  * @param ifLan true if LAN is to be considered local, else only localhost is "local"
  * @specification: return whether connection is local enough to be slack on security.
  */
  public static boolean isLocal(Socket socket, boolean ifLan) {
    byte [] octet= socket.getInetAddress().getAddress() ;
    return IPSpec.isLocalHost(octet) || (ifLan &&
    octet[0]==192&& octet[1]==168 && octet[2]<=1 && //typical class C
    (octet[3]>1&&octet[3]<254));//disallow gateways:
  }

  private static final void showSession(SSLSocket sslsock,EasyCursor ezc){
    if(sslsock!=null){
      SSLSession session = sslsock.getSession();
      ezc.push("session");
      try {
        if(session!=null){
          ezc.setString("ciphersuite",session.getCipherSuite());
          ezc.setLong("created",session.getCreationTime());//string this with an LTF
          ezc.setString("id",Formatter.hexImage(session.getId()).toString());
          String []vnames=session.getValueNames();
          ezc.push("values");
          try {
            for(int i=vnames.length;i-->0;){
              String name=vnames[i];
              Object value=session.getValue(name);
              ezc.setString(name+".class",value.getClass().getName());
              ezc.setString(name,String.valueOf(value));
            }
          }
          finally {
            ezc.pop();
          }
          SSLSessionContext context= session.getSessionContext();
          if(context!=null){
            Enumeration ofgodknowswhat = context.getIds();
          }
        }
      }
      finally {
        ezc.pop();
      }
    }
  }


  public static final EasyCursor socketProperties(Socket sock){
    EasyCursor ezc=new EasyCursor();
    if(sock!=null){
      try {
        ezc.push("address");
        try {
          ezc.setObject("host",hostAddress(sock));
          ezc.setObject("local",localAddress(sock));
        } finally {
          ezc.pop();
        }

        ezc.push("bufferSize");
        try {
          ezc.setInt("send",sock.getSendBufferSize());
          ezc.setInt("receive",sock.getReceiveBufferSize());
        } finally {
          ezc.pop();
        }

        ezc.push("option");
        try {
//          ezc.setBoolean("keepalive",sock.getKeepAlive()); // not compatible with gcj, so leave out for now!
          ezc.setBoolean("TcpNoDelay",sock.getTcpNoDelay());
          ezc.setInt("SoLinger",sock.getSoLinger());
          ezc.setInt("SoTimeout",sock.getSoTimeout());
        } finally {
          ezc.pop();
        }

        if(sock instanceof SSLSocket) {
          SSLSocket sslsock = (SSLSocket)sock;
          ezc.push("SSL");
          try {
            ezc.setBoolean ("EnableSessionCreation",sslsock.getEnableSessionCreation());
            ezc.setBoolean ("NeedClientAuth"       ,sslsock.getNeedClientAuth());
            ezc.setBoolean ("UseClientMode"        ,sslsock.getUseClientMode());
            ezc.setTextList("EnabledCipherSuites"  ,new TextList(sslsock.getEnabledCipherSuites()));
            ezc.setTextList("SupportedCipherSuites",new TextList(sslsock.getSupportedCipherSuites()));
            showSession(sslsock,ezc);
          } finally {
            ezc.pop();
          }
        }
      }
      catch (Exception ex) {
        //ignore 'em.
      }
    }
    return ezc;
  }

  public static final String socketSpam(Socket sock){
    return "Socket ["+socketProperties(sock).asParagraph()+"]";
  }

}
//$Id: SocketInfo.java,v 1.4 2003/08/11 14:22:13 andyh Exp $