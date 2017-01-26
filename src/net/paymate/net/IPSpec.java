package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/IPSpec.java,v $
 * Description:  provides for separate but associated ip and port values,
 *               like java.net.InetSocketAddress, but doesn't do nasty library loads.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.21 $
 */

import net.paymate.util.*;
import java.net.InetAddress; //what we are wrapping here
import net.paymate.lang.StringX;

public class IPSpec {
  public String address= "localhost";
  public int port=0;

  /**
   * @return whether this is an address that has a chance of being real
   */
  public boolean isTrivial(){
    return port==0  || address.equalsIgnoreCase("dummy");
  }

  /**
   * @return whether this is a local service accessed via broadcast
   */
  public boolean isService(){
    return port!=0 && !StringX.NonTrivial(address) ;
  }

/**
 * @return whether @param octet is typical localhost value of 127.0.01
 */
  public static boolean isLocalHost(byte [] octet){
    return octet[0]==127 && octet[1]==0 && octet[2]== 0 && octet[3]==1;
  }

  /**
   * @return a new ipspec made from @param hostname or dotted decimal and @param aport
   */
  public static final IPSpec New(String ipname,int aport){
    return new IPSpec().set(ipname, aport);
  }

  /**
   * @return dotted decimal representation of host address
   */
  public static final String dottedDecimal(byte [] inetadr){
    StringBuffer sb=new StringBuffer(4*3-1);
    for(int i=0;i<4;i++){
      if(i!=0){
        sb.append('.');
      }
      sb.append(String.valueOf(inetadr[i]));
    }
    return sb.toString();
  }

  /**
   * @construct from @param ipname as "hostname:portnumber"
   */
  public static IPSpec New(String ipname){
    IPSpec newone=new IPSpec();
    return newone.fromString(ipname);
  }

  /**
   * @construct empty one for reflective construction
   */
  public IPSpec() {
    //
  }

  /**
   * @construct from separate @param ipname hostname  and @param aport port values
   */
  public IPSpec set(String ipname,int aport) {
    address= ipname;
    port=aport;
    return this;
  }

  /**
   * if no ':port' then current port is preserved
   */
  public IPSpec fromString(String ipname) {
    if(StringX.NonTrivial(ipname)){
      int divider = ipname.lastIndexOf(":");
      if(divider >=0) {
        String justip = ipname.substring(0, divider);
        int aport = Integer.parseInt(ipname.substring(divider+1));
        return set(justip, aport);
      } else {
        address=ipname;
      }
    }
    return this;
  }

  public String toString(){
    return address+":"+port;
  }

  // the following function generates an int, as if you:
  // 1) Convert each decimal between the dots into hex, eg:
  //    64.92.151.4 = 40.5C.97.04
  // 2) Put the numbers together as though they were one big number:
  //    405C9704
  // 3) Convert it decimal -> what you see when you print the returned int:
  //    1079809796
  // that int fits in a regular 4-byte int (eg: database)
  public static int ipToInt(String address) {
    int ret = 0;
    try {
      if (StringX.NonTrivial(address)) {
//      ret = InetAddress.getByName(address).hashCode();
        // consists of 4 integers, dotted, that need to be combined into one for storage purposes
        int [ ] ints = new int[4];
        ThreeCharDecimal tcd = new ThreeCharDecimal();
        int whichInt = 0;
        for (int i = 0; i < address.length(); i++) {
          char chr = address.charAt(i);
          if(chr == '.') {
            ints[whichInt] = tcd.asInt();
            tcd.clear();
            whichInt++;
          } else {
            if(chr == ' ') {
              break; // there are not supposed to be any spaces in these numbers!
            } else {
              if(Character.isDigit(chr)) {
                tcd.append(chr);
              } else {
                // +++ dbg
              }
            }
          }
        }
        // do the last one
        ints[whichInt] = tcd.asInt();
        ret = from4ints(ints);
      } else {
        // +++ dbg
      }
    } catch (Exception ex) {
        // +++ dbg
    } finally {
      return ret;
    }
  }

  public int ipToInt() {
    return ipToInt(address);
  }

  private static final int from4ints(int [ ] ints) {
    // now that we have them divided up, put them back together
    int finalint = 0;
    if(ints.length == 4) {
      for (int i = 0; i < 4; i++) {
        finalint <<= 8;
        finalint += ints[i];
      }
    }
    return finalint;
  }

  private static final int MASK = 0xFF;
  private static final int [ ] to4ints(int complete) {
    int [ ] ints = new int[4];
    // since we know there are only 4, no need to loop and multiply, really.  waste of cpu ...
    for(int i = ints.length; i-->0;) {
      ints[i] = complete & MASK;
      complete >>= 8;
    }
    return ints;
  }

  private static final String dot = ".";
  private static final String toDottedInts(int [ ] ints) {
    return ""+ints[0]+dot+ints[1]+dot+ints[2]+dot+ints[3];
  }

  public static String ipFromInt(int ip) {
    return toDottedInts(to4ints(ip));
  }

  // the ip parameter is an int like 12345678, not an ip like 64.92.151.4
  public static String ipFromInt(String ip) {
    return toDottedInts(to4ints(StringX.parseInt(ip)));
  }
}

class ThreeCharDecimal {
  char [ ] chars = new char[3];
  int index = -1;
  public ThreeCharDecimal() {
    clear();
  }
  public void clear() {
    chars[0] = chars[1] = chars[2] = ' ';
    index = -1;
  }
  public void append(char chr) {
    if(++index < chars.length) {
      chars[index]=chr;
    } else {
      // +++ dbg
    }
  }
  public String toString() {
    return String.valueOf(chars);
  }
  public int asInt() {
    return StringX.parseInt(toString());
  }
}

//$Id: IPSpec.java,v 1.21 2004/01/09 11:46:06 mattm Exp $
