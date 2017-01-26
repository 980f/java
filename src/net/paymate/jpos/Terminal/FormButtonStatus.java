package net.paymate.jpos.Terminal;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/FormButtonStatus.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class FormButtonStatus {
  public byte ID;
  public boolean wasPressed;

  public FormButtonStatus(byte even, byte odd){
    ID=even;
    wasPressed= odd!=0;
  }

}
//$Id: FormButtonStatus.java,v 1.1 2001/12/04 06:52:53 andyh Exp $