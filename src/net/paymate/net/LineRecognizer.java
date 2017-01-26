package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/LineRecognizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface LineRecognizer {
  /* @return whether @param context is a line.
    @param incoming is the last char stuffed into the context, for your convenience.
    you can modify context if you wish...generally should leave that up to the LineServerUser.
    */
  public boolean endOfLineDetected(StringBuffer context,int incoming);
}