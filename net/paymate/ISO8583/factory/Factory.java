package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/Factory.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface Factory {
  public Create Creator(); //
  public Extract Extractor();//
}
//$Id: Factory.java,v 1.1 2001/11/14 13:53:45 andyh Exp $