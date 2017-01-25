/**
* Title:        Question
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Question.java,v 1.5 2001/11/03 02:53:35 andyh Exp $
*/
package net.paymate.awtx;

import net.paymate.data.Value;


import net.paymate.data.ContentType ;//definitely in the wrong package!

public class Question {
  public String prompt;
  public Value inandout;
  public int guid;

  public Question(int guid,String prompt,Value inandout) {
    this.guid     =guid;
    this.prompt   =prompt;
    this.inandout =inandout;
  }

  /* for legibility elsewhere republish parts of Value class: */
  public ContentType charType(){
    return inandout.charType();
  }

  public void Clear(){
    inandout.Clear();
  }

  public String toSpam(){
    return "["+prompt+"("+guid+")="+inandout.Image()+"]";
  }

}
//$Id: Question.java,v 1.5 2001/11/03 02:53:35 andyh Exp $
