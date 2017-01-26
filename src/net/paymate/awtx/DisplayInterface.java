package net.paymate.awtx;

/**
 * Title:        $Source: /cvs/src/net/paymate/awtx/DisplayInterface.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface DisplayInterface {

/**
 * normal start of user interface session
 */
  public void ask(Question toAsk);
  /**
   * initiate a question/answer session with human
   * don't know why this is public...legacy of DisplayPad class???
   */
  public void getString(Question q);
/**
 * (re) start last question
 * don't know why this is public...legacy of DisplayPad class???
 */
  public void StartQuestion();

  /**
   * interrupt the question being asked for a special announcement
   * at next keystroke we refresh with local data.
   * @todo add timer to reset without keystroke, after some unspecified delay.
   */
  public void flash(String blink);

/**
 * diagnostic on this class.
 * return what should be in user's view
 */
  public String WhatsUp();


//  public void KeyStroked(int keystroke);

  /**
  * complete user entry IF
  * @param ofInterest is the id of the question being asked, if invalid then returns false
  * @return true if an enter was generated
  * @todo DOESN"T WORK WITH CLERKPAD! can't force it to complete input!
  */
  public boolean autoEnterIf(int quid);
  /**
   * @return this
   */
  public DisplayInterface attachTo(AnswerListener replyTo);

}
//$Id: DisplayInterface.java,v 1.2 2003/01/25 01:38:00 andyh Exp $