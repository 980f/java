package net.paymate.ivicm.et1K;
/**
* Title:        Callback
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Callback.java,v 1.6 2005/03/02 05:23:06 andyh Exp $
*/

//public //package specific callback
interface Callback {
  public Command Post(Command cmd);//return next command
}

/* inner class template
  class $$ implements Callback {
    public void post(Command cmd){
       //Failure("");
    }
  }

 */
//$Id: Callback.java,v 1.6 2005/03/02 05:23:06 andyh Exp $
