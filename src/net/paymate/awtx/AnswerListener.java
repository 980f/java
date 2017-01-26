/**
* Title:        $Source: /cvs/src/net/paymate/awtx/AnswerListener.java,v $
* Description:  question and answer interface, response part thereof
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: AnswerListener.java,v 1.5 2002/05/30 02:18:25 andyh Exp $
*/
package net.paymate.awtx;
//import  net.paymate. ;
public interface AnswerListener {
  public boolean onReply(Question beingAsked, int opcode);
  public final static int FUNCTIONED=-2;//reset everything!
  public final static int CANCELLED=-1; //escaped or such
  public final static int HELPME   = 0; //tool tip desired
  public final static int SUBMITTED= 1; //enter w/ change
  public final static int ACCEPTED = 2; //enter on default
}
//$Id: AnswerListener.java,v 1.5 2002/05/30 02:18:25 andyh Exp $
