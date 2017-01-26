package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/OldStatusTranslator.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import net.paymate.*;
import java.net.*;         // DatagramPacket
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

// this class is only needed until the next server release after ptgwsi20!

public class OldStatusTranslator extends UDPGateway {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(OldStatusTranslator.class);

  public OldStatusTranslator(String name, boolean isDaemon, IPSpec source, IPSpec destination) {
    super(name, isDaemon, source, destination);
  }

  private static final int pt1 = 3;
  private static final int pt2 = 16;
  private static final int pt3 = 32;
  private static final int pt4 = 43;

  protected DatagramPacket convert(DatagramPacket oldp) {
    byte [] oldbytes = oldp.getData();
// note that _ is a null (\0)
//    Convert messages like this:
//      0430060EF117A22_192.168.1.104_1_1040151767_
//      0430060EF21855F_66.134.116.2191_1040151709_
  // eg LLLMMMMMMMMMMMMMIIIIIIIIIIIIII??TTTTTTTTTTT
    // O = start,end
    // L = 0, 3
    // M = 3, 3+13=16
    // I = 16, 16+14=30
    // ? = who cares
    // T = 32, 32+11=43
//    into this :
//      043HELIOSSERVER_192.168.1.50   _1039787936_
//      043HERMESSERVER_127.0.0.1      _1040151689_
//      043VESTASERVER _127.0.0.1      _1040151690_
    sp.clear();
    sp.ethernetAddress = new String(ByteArray.subString(oldbytes,pt1,pt2)); // macid, 13
    // since there is no way to get the wanip to them otherwise, put it in the lanip
    sp.ipAddress       = StringX.TrivialDefault(oldp.getAddress().getHostAddress(), "");//new String(StringX.subString(oldbytes,pt2,pt3)); // ipaddr, 14 + 2 for a bug
    byte [] tmp        = ByteArray.subString(oldbytes,pt3,pt4);  // time, 11
    sp.time            = StringX.parseLong(new String(tmp).trim()) * 1000;
    // fix the ipAddress [assume there are 3 dots]
    // find the last one
    int dot = sp.ipAddress.lastIndexOf(".");
    if(dot > ObjectX.INVALIDINDEX) {
      // find 3 digits past the last one.
      int end = dot + 3; // end = start + length
      if(sp.ipAddress.length() > end) {
        // chop off anything beyond that
        sp.ipAddress = StringX.subString(sp.ipAddress, 0, end);
      }
    }
    byte [] withoutnulls = ByteArray.replaceNulWithSpace(sp.toBytes()); // just to be sure
    oldp.setData(withoutnulls);
    return oldp;
  }
  // above func() only called by a single thread, so can reuse objects on the object, and not local
  private StatusPacket sp = new StatusPacket();


  private static Main app;
  private static OldStatusTranslator udpg;
  public static void main(String [] args) {
    if(args.length < 2) {
      System.out.println("Usage: OldStatusTranslator listenip:listenport destinationip:port");
      return;
    }
    app=new Main(OldStatusTranslator.class);
    //in case we don't have a logcontrol file:
    LogSwitch.SetAll(LogSwitch.WARNING);
    PrintFork.SetAll(LogSwitch.WARNING);
    //now get overrides from file:
    app.stdStart(args); //starts logging etc. merges argv with system.properties and thisclass.properties
    IPSpec destination = IPSpec.New(args[1]);
    IPSpec source = IPSpec.New(args[0]);
    boolean isDaemon = false;
    dbg.ERROR("Starting OldStatusTranslator " + " from " + source + " to " + destination + " as " + (isDaemon ? "daemon" : "nondaemon") );
    udpg = new OldStatusTranslator("OldStatusTranslator", isDaemon, source, destination);
    // you don't get here until the program exits!
  }
}