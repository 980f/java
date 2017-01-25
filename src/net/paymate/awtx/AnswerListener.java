/**
* Title:        AnswerListener
* Description:  $Source: /cvs/src/net/paymate/awtx/AnswerListener.java,v $
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: AnswerListener.java,v 1.3 2001/10/10 22:47:53 andyh Exp $
*/
package net.paymate.awtx;
//import  net.paymate. ;
public interface AnswerListener {
  public void onReply(Question beingAsked, int opcode);
  public final static int CANCELLED=-1; //escaped or such
  public final static int HELPME   = 0; //tool tip desired
  public final static int SUBMITTED= 1; //enter w/ change
  public final static int ACCEPTED = 2; //enter on default
  }
//$Id: AnswerListener.java,v 1.3 2001/10/10 22:47:53 andyh Exp $
