package net.paymate.net;

/**
 * Title:        SendMail
 * Description:  Mail send system (may not work)
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: SendMail.java,v 1.20 2001/11/17 20:06:37 mattm Exp $
 */

import net.paymate.util.*;
import java.io.*;

public class SendMail implements QActor {

  private static final ErrorLogStream dbg = new ErrorLogStream(SendMail.class.getName());

  private QAgent agent = null;

  // +++ add more parameters to the constructor (are embedded in SmtpMail & this class for now)
  public SendMail() {
    agent = QAgent.New("SendMail"+Thread.currentThread().getName(), this);
    agent.config(120000); // ms between forced wakeups
    agent.config(dbg);
    agent.Clear(); // starts it
  }

  //the long form of "messaging.sprintpcs.com" seems to always work, the short form bounces now and then.
  public final void send(String toWhom, String subject, String message) {
    send("smtp.paymate.net", toWhom, "PayMate.net <info@paymate.net>", subject, message, "mail.paymate.net", "info", "pm1234");
  }

  public final boolean send(String toIP, String toWhom, String from, String subject, String message, String popIP, String username, String password) {
    boolean ret = false;
    SmtpMail mailer = new SmtpMail("smtp.paymate.net", true, popIP, username, password);
    mailer.setHeloHost("DUDE");
    mailer.setMailFrom(from);
    mailer.setMailTo(toWhom);
    mailer.setMailSubject(subject);
    mailer.setMailMessage(message);
    return agent.Post(mailer);
  }

  public void runone(Object torun) {
    SmtpMail mailer = null;
    try {
      mailer = (SmtpMail)torun;
      dbg.WARNING("Send() returned " + mailer.send());
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      if(mailer != null) {
        String error = mailer.getErrorMessage();
        if((error != null) && (error.length() > 0)) {
          dbg.ERROR("SendMail Error: " + error);
        }
      }
    }
  }

  public void Stop(){
    // stub.  Used by QAgent!  Don't put anything in here, and DEFINITELY don't call shutdown() from here, or you will get into an infinite loop!
  }

}

//$Id: SendMail.java,v 1.20 2001/11/17 20:06:37 mattm Exp $
