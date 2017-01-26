package net.paymate.serial;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/serial/Key.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface Key {
  String baudRateKey    ="baud";
  String parityKey      ="parity";
  String databitsKey    ="dataBits";
  String stopbitsKey    ="stopBits";
  String flowcontrolKey ="flow";
  String bufsizeKey     ="obufsize";
  String protocolKey    ="traditional";
  String nameKey        ="portName";
  String nicenessKey    ="niceness";
}//$Id: Key.java,v 1.1 2003/01/03 23:01:57 andyh Exp $