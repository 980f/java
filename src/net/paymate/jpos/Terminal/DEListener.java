/* $Id: DEListener.java,v 1.4 2000/05/26 01:44:11 alien Exp $ */
/*
a trivial little class to take care of typing the pair
of items below repeatedly.

*/

package net.paymate.jpos.Terminal;
import  net.paymate.util.ErrorLogStream;

import jpos.events.*;

public interface DEListener extends DataListener, ErrorListener {
 //a simple "Or"ing of pre-existing interfaces
}
//$Id: DEListener.java,v 1.4 2000/05/26 01:44:11 alien Exp $
