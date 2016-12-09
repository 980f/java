package net.paymate.ivicm.et1K;
/**
* Title:        Callback
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Callback.java,v 1.5 2001/06/26 01:35:20 andyh Exp $
*/

//public //package specific callback
interface Callback {
  public Command Post(Command cmd);//return next command
}

/* inner class template
  class $$ implements Callback {
    public void Post(Command cmd){
       //Failure("");
    }
  }

 */
//$Id: Callback.java,v 1.5 2001/06/26 01:35:20 andyh Exp $
