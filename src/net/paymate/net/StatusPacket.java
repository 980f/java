package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/StatusPacket.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.util.*; // Fstring, ErrorLogStream
import java.net.*; // InetAddress, DatagramPacket
import net.paymate.util.DateX;
import net.paymate.lang.StringX;
import net.paymate.lang.Fstring;

// +++ extend the 'Packet' class?

public class StatusPacket {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StatusPacket.class);

  public String ethernetAddress;
  public String ipAddress;
  public String wanIpAddress; // for RECEIVING only!
  public long time; // currentTimeMillis [divided and multiplied by 1000 for transport, below, but raw millis here]

  public StatusPacket() {
    clear();
  }

  public StatusPacket(String ethernetAddress, String ipAddress) {
    super();
    this.ethernetAddress = ethernetAddress;
    this.ipAddress = ipAddress;
  }

  private static final String ENDSTRING = " "; // was a null, but now a space!
  private static final int LENLEN = 3;
  private static final int ETHADDRESSLEN = 12; // + '\0'
  private static final int IPADDRESSLEN = 15; // + '\0'
  private static final int DATELEN = 10; // + '\0'
  private static final int ENDSTRINGLEN = ENDSTRING.length();
  public static final int TOTALLEN = LENLEN + ENDSTRINGLEN + ETHADDRESSLEN + ENDSTRINGLEN + IPADDRESSLEN + ENDSTRINGLEN + DATELEN;

  private Fstring ethaddress = new Fstring(ETHADDRESSLEN);
  private Fstring IPaddress = new Fstring(IPADDRESSLEN);
  private Fstring date  = new Fstring(DATELEN, '0');
  private Fstring len = new Fstring(LENLEN, '0');

  public String toChars() {
    ethaddress.setto(ethernetAddress);
    IPaddress.setto(ipAddress);
    if(time == 0L) {
      time = DateX.utcNow(); // is it okay to set it here?  Setting it in the constructor caused reused packets to never get their time updated!
    }
    date.righted(String.valueOf(time/1000));
    String toSend = ethaddress + ENDSTRING + IPaddress + ENDSTRING + date + ENDSTRING;
    len.righted(String.valueOf(LENLEN + toSend.length()));
    return String.valueOf(len) + toSend;
  }

  public void clear() {
    ethernetAddress = "";
    ipAddress = "";
    wanIpAddress = ""; // for RECEIVING only!
    time = 0L; // currentTimeMillis [divided and multiplied by 1000 for transport, below, but raw millis here]
  }

  // allowing the \0 characters to print causes my text editor to go into hex mode!
  public String toString() {
    return StringX.replace(toChars(),"\0","^");
  }

  public byte [] toBytes() {
    return toChars().getBytes();
  }

  public DatagramPacket toDatagramPacket(InetAddress iaddr, int port) {
    byte [] bytes = toBytes();
    return new DatagramPacket(bytes, bytes.length, iaddr, port);
  }

  public String Spam() {
    return "ethernetAddress = " + ethernetAddress + ", " +
           "ipAddress = " + ipAddress + ", " +
           "wanIpAddress = " + wanIpAddress + ", " +
           "time = " + UTC.New(time);
  }

  /**
   * parse
   */
  public static StatusPacket fromDatagramPacket(DatagramPacket from) {
    StatusPacket ret = new StatusPacket();
    try {
      byte [] data = ByteArray.replaceNulWithSpace(from.getData());
      // +++ recode better when we have time
      byte [] tmp = ByteArray.subString(data, LENLEN, LENLEN + ETHADDRESSLEN);
      ret.ethernetAddress = new String(tmp).trim();
      tmp = ByteArray.subString(data, LENLEN + ETHADDRESSLEN+1, LENLEN+ ETHADDRESSLEN+1 + IPADDRESSLEN);
      ret.ipAddress = new String(tmp).trim();
      tmp = ByteArray.subString(data, LENLEN + ETHADDRESSLEN+1 + IPADDRESSLEN+1, LENLEN + ETHADDRESSLEN+1 + IPADDRESSLEN+1 + DATELEN);
      ret.time = StringX.parseLong(new String(tmp).trim()) * 1000;
      ret.wanIpAddress = StringX.TrivialDefault(from.getAddress().getHostAddress(), "");
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return ret;
    }
  }
}
