/**
 * Title:        ThreadReporter
 * Description:  Interface that allows reporting on thread statuses (app-specific details)
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ThreadReporter.java,v 1.1 2003/07/27 05:35:10 mattm Exp $
 */

package net.paymate.lang;

public interface ThreadReporter {

  public String status();

}