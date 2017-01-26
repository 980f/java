package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/TerminalInfoKey.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

public interface TerminalInfoKey {
  String port="port";
  String WrapperClass="WrapperClass";
  String FormatterKey="Formatter";
  String allowSigCapKey="allowSigCap";
  String twoCopiesKey="twoCopies";
  String canStandinModifiesKey="canStandinModify";
  String ask4avs="ask4avs";
  String CLASSPROPERTY="class";//terminal's class
  String nickNameKey="nickName";//deprecated?
} //$Id: TerminalInfoKey.java,v 1.4 2004/02/24 18:31:23 andyh Exp $