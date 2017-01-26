package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/EasyHelper.java,v $
 * Description:  load a string representable object that doesn't implement class isEasy from an easyCursor
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface EasyHelper {
  /** * save object to cursor, might be null in which case save default values */
  public void helpsave(EasyCursor ezc,Object uneasy);
  /** * load guaranteed existing null constructed object from cursor */
  public Object helpload(EasyCursor ezc,Object uneasy);

}
//$Id: EasyHelper.java,v 1.2 2003/04/22 01:50:31 andyh Exp $