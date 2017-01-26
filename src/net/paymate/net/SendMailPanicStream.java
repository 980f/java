package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SendMailPanicStream.java,v $</p>
 * <p>Description: panic emailer</p>
 * <p>Copyright: Copyright (c) 2003, extracted from 'Service'</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.net.SendMail; // SendMail
import net.paymate.util.PanicStream;
import net.paymate.util.TextList;
import net.paymate.util.DateX;
import net.paymate.lang.StringX;
import net.paymate.util.PrintFork;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.TextListIterator;


public class SendMailPanicStream implements PanicStream {
  private ErrorLogStream dbg=ErrorLogStream.getForClass(SendMailPanicStream.class);

  private SendMail mailer;
  private String mailList;
  private PrintFork problemLog;
  private String which;

  public void setMailList(String to) {
    mailList = to;
  }

  public void sendTo(SendMail mailer) {
    this.mailer = mailer;
  }

  public void logTo(PrintFork pf){
    problemLog=pf;
  }

  public void PANIC(String re) {
    PANIC(re, "");
  }
  /**
   * send panic message to stored mailing list
   * @param re
   * @param panicky
   */
  public void PANIC(String re, Object panicky) {
    PANIC(mailList, re, panicky);
  }
  /**
   * don't know why this is public ...
   * @param re subject of email
   * @return time and host stamped subject line.
   */
  public String preface(String re){
    return which + "/" + DateX.timeStampNowYearless() + ":" + re;
  }
  /**
   * make message out of a:
   * @param re  subject
   * @param panicky something related to the subject to convert into text.
   * @return
   */
  public TextList makeMessage(String re, Object panicky) {
    TextList tl = new TextList(2);
    tl.add(preface(re));
    if  (panicky instanceof TextList) {
      tl.add((TextList) panicky);//have to copy as list might be used by its creator for some other purpose
    } else {
      tl.add(String.valueOf(panicky));
    }
    return tl;
  }
  /**
   * log message
   * @param tl the multiple lines of a message
   * @todo: restore logging this as one long line (tl.asParagraph())
   */
  public void logText(TextList tl){
    if (problemLog != null) {
      TextListIterator text=TextListIterator.New(tl);
      while(text.hasMoreElements()){
        problemLog.println(text.next());
      }
    }
  }

  public void logObject(String re, Object panicky) {
    logText(makeMessage(re,panicky));
  }

  public void logLine(String toPrint) {
    if (problemLog != null) {
      problemLog.println(toPrint);
    }
  }

  /**
   * send panicmessage to passsed in mailing list
   * @param toWhom  mailing list
   * @param re      topic/subject line
   * @param panicky object containing details.
   */

  public void PANIC(String toWhom, String re, Object panicky) {
    TextList tl = makeMessage(re,panicky);
    logText(tl);
    if (mailer != null) {
      if (StringX.NonTrivial(toWhom)) {
        dbg.VERBOSE("PANIC: message=[" + tl + "]");
        mailer.send(toWhom, tl.itemAt(0)+tl.itemAt(1), tl);
      }
      else {
        dbg.WARNING("toWhom is NULL; not mailing; logged only.");
      }
    }
    else {
      dbg.WARNING("EEK! mailer is NULL!");//warning not error since some instances may be used for the nice log message formatting.
    }
  }

  private SendMailPanicStream() {
  //
  }

  public void setName(String whom){
    which=whom;
  }

  public static SendMailPanicStream Create(String whom,ErrorLogStream dbg) {
    SendMailPanicStream newone=new SendMailPanicStream ();
    newone.which=whom;
    newone.dbg=dbg;
    return newone;
  }
}
//$Id: SendMailPanicStream.java,v 1.2 2003/11/25 17:50:40 andyh Exp $