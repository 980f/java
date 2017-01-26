package net.paymate.awtx;
/**
* Title:        $Source: /cvs/src/net/paymate/awtx/Question.java,v $
* Description:  @see AnswerListener
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.10 $
*/


import net.paymate.data.*;
import net.paymate.util.*;//do we need to relocate TrueEnum??
import net.paymate.lang.Value;
import net.paymate.lang.ContentType;
import net.paymate.lang.TrueEnum;

public class Question {

/**
 * what to ask the human
 */
  public String prompt;
  /**
   * the potential answer to this question.
   * we don't extend value as we don't want to have to code a matching set
   * of classes for a question of each Value type.
   */
  public Value inandout;
/**
 * probably holds someone else's enumeration for this question.
 * It is not this class's job to use this, just carry it around so that the prompt
 * can vary but the question still be identified.
 */
  public int guid;

  /**
   * get value, presuming an enumeration, and CLEAR content.
   */
  public TrueEnum pullEnum(){
    try {
      return ((EnumValue)inandout).Value();
    } finally {
      Clear();
    }
  }

 public boolean isMenu(){
   return inandout!=null && inandout instanceof EnumValue;
 }

  public Question(int guid,String prompt,Value inandout) {
    this.guid     =guid;
    this.prompt   =prompt;
    this.inandout =inandout;
  }

  //will internationalize at this point.
  public static Question Ask(int guid,String prompt,Value inandout) {
    return new Question(guid,prompt,inandout);
  }


  /** @return type of input allowed */
  public ContentType charType(){
    return inandout.charType();
  }
/** zero the internal value of the answer*/
  public void Clear(){
    inandout.Clear();
  }

  public Question setAnswer(Object various){
    inandout.setto(String.valueOf(various));
    return this;
  }

  /**
   * @return image of proposed value for enumerated variant of content
   */
  public String EnumImage(int val){
    return inandout instanceof  EnumValue ?  ((EnumValue)inandout).ImageFor(val) : String.valueOf(val);
  }

  public String toSpam(){
    return "["+prompt+"("+guid+")="+inandout.Image()+"]";
  }

}
//$Id: Question.java,v 1.10 2003/07/27 05:34:52 mattm Exp $
