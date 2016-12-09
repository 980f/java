/**
 * Title:        ThreadReporter
 * Description:  Interface that allows reporting on thread statuses (app-specific details)
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ThreadReporter.java,v 1.2 2001/04/06 01:20:06 mattm Exp $
 */

package net.paymate.util;

public interface ThreadReporter {

  public String status();

}