package net.paymate.net;

/**
 * Title:        SendMail
 * Description:  Mail send system
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: SendMail.java,v 1.78 2004/01/26 22:44:38 mattm Exp $
 * @todo: make "hourly batch" report when next email chime will occur.
 */

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*; // just to make sure it is loaded, although it isn't directly used in this class

import net.paymate.util.*;
import net.paymate.util.timer.*; // StopWatch
import java.io.*;
import java.util.*; // timezone
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;

public class SendMail extends net.paymate.util.Service implements Runnable, TimeBomb {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SendMail.class,
      ErrorLogStream.VERBOSE);

  public static final String NAME = "SendMail";

  // +++ these email settings need to come from someplace other than code +++
  public static final String MATTMELLO = "Matt Mello <matt.mello@paymate.net>";
  public static final InternetAddress JUSTMATTMELLOEMAIL = new InternetAddress();
  static {
    JUSTMATTMELLOEMAIL.setAddress(MATTMELLO);
  }
  //the long form of "messaging.sprintpcs.com" seems to always work, the short form bounces now and then.
  public static final String MATTMELLOPHONE = "5123506900@messaging.sprintpcs.com";
  public static final String ANDYHEILVEIL = "andy.heilveil@paymate.net";
  public static final String ANDYHEILVEILPHONE = "5127913529@messaging.sprintpcs.com";
  public static final String GAWDS = MATTMELLO+","+ANDYHEILVEIL;
  public static final String PHONES = MATTMELLOPHONE+","+ANDYHEILVEILPHONE;
  public static final String GAWDSANDPHONES = GAWDS+","+PHONES;
  public static final InternetAddress [ ] Gawds = Parse(GAWDS);
  public static final InternetAddress [ ] Phones = Parse(PHONES);
  public static final InternetAddress [ ] GawdsAndPhones = Parse(GAWDSANDPHONES);

  private InternetAddress [] emergencyEmail = GawdsAndPhones;

  // defaults, which can be loaded from the database
  private static final InternetAddress DEFAULTFROM = Parse("AnyServerBoot <anyserverboot@paymate.net>")[0];
  private static final String DEFAULTTOIP = "paymate.net";
  private static final String DEFAULTPOPIP = "paymate.net";
  private static final String DEFAULTUSERNAME = "anyserverboot@paymate.net";
  private static final String DEFAULTPASSWORD = "PHAULpeao";
  private static final int BATCHINTERVAL = (int) Ticks.forHours(1);
  private InternetAddress defaultFrom = DEFAULTFROM;
  private String defaultToIP = DEFAULTTOIP;
  private String defaultPopIp = DEFAULTPOPIP;
  private String defaultUsername = DEFAULTUSERNAME;
  private String defaultPassword = DEFAULTPASSWORD;
  private InternetAddress [] batchList = NULLINETADDR;

  private int batchInterval = BATCHINTERVAL;
  private TimeZone timezone = null;
  // javamail stuff ...
  private Session session = null;
  private SendMailBoxes boxes = new SendMailBoxes();
  // for web reporting
  Hashtable mailings = new Hashtable();
  private Accumulator reads = new Accumulator();
  private Accumulator writes = new Accumulator();
  private Accumulator txns = new Accumulator();
  private StopWatch sw = new StopWatch(false);
  private TextList batch = new TextList();
  private Alarmum alarmum = null;
  private Thread myThread = null;
  private boolean shoulddie = false;

  protected void loadConfigs() {
    if (configger != null) {
      String from = configger.getServiceParam(serviceName(), "defaultFrom", "");
      defaultFrom = StringX.NonTrivial(from) ? parse(from)[0] : DEFAULTFROM;
      defaultToIP = configger.getServiceParam(serviceName(), "defaultToIP",
                                              DEFAULTTOIP);
      defaultPopIp = configger.getServiceParam(serviceName(), "defaultPopIp",
                                               DEFAULTPOPIP);
      defaultUsername = configger.getServiceParam(serviceName(),
                                                  "defaultUsername",
                                                  DEFAULTUSERNAME);
      defaultPassword = configger.getServiceParam(serviceName(),
                                                  "defaultPassword",
                                                  DEFAULTPASSWORD);
      batchInterval = configger.getIntServiceParam(serviceName(),
          "batchInterval", BATCHINTERVAL);
      timezone = TimeZone.getTimeZone(configger.getServiceParam(serviceName(),
          "timezone", "America/Chicago"));
      batchList = parse(configger.getServiceParam(serviceName(), "batchList", ""));
    } else {
      dbg.ERROR("configger is null!  Using defaults.");
      defaultFrom = DEFAULTFROM;
      defaultToIP = DEFAULTTOIP;
      defaultPopIp = DEFAULTPOPIP;
      defaultUsername = DEFAULTUSERNAME;
      defaultPassword = DEFAULTPASSWORD;
      batchInterval = BATCHINTERVAL;
      timezone = TimeZone.getTimeZone("America/Chicago");
      batchList = parse(MATTMELLO);
    }
    // javamail ...
    Properties props = System.getProperties();
    props.put("mail.smtp.host", defaultToIP);
    session = Session.getInstance(props);
    session.setDebug(false);
//    onTimeout(); // do this since the batch interval may have changed
    Alarmer.Defuse(alarmum);
    Alarmer.reset(batchInterval, alarmum);
  }



  // +++ add more parameters to the constructor (are embedded for now)
  private SendMail(InternetAddress [] emergencyEmail, ServiceConfigurator cfg) {
    super(NAME, cfg);
    this.emergencyEmail = (emergencyEmail == null || (emergencyEmail.length == 0)) ?
        GawdsAndPhones : emergencyEmail;
    dbg.ERROR("emergencyEmail setto: '" + this.emergencyEmail + "'!");
    initLog();
    alarmum = Alarmer.New(batchInterval, (TimeBomb)this);
    Alarmer.Defuse(alarmum); // don't want it to go off yet
    up(); // starts it
  }

  public InternetAddress [] emergencyEmail() {
    return emergencyEmail;
  }

  private static SendMail theonlyone = null;

  public static final SendMail New(String lclEmergencyEmail,
                                   ServiceConfigurator cfg) {
    InternetAddress [] eal = Parse(lclEmergencyEmail);
    return New(eal, cfg);
  }

  public static final SendMail New(InternetAddress [] lclEmergencyEmail,
                                   ServiceConfigurator cfg) {
    // use monitor to synchronize +++
    synchronized (SendMail.class) {
      if (theonlyone == null) {
        theonlyone = new SendMail(lclEmergencyEmail, cfg);
      }
      return theonlyone;
    }
  }

  private static final TextList EMPTYTEXTLIST = new TextList();

  public final void SCREAMFORHELP(String subject) {
    send(emergencyEmail, subject, EMPTYTEXTLIST);
  }


  // legacy Service stuff ... ONLY !  Don't use this anywhere else!
  public final void send(String toWhom, String subject, TextList message) {
    send(StringX.NonTrivial(toWhom) ? parse(toWhom) : emergencyEmail, subject, message);
  }

  private Message makeMessage(String subject, TextList message) {
    Message msg = new MimeMessage(session);
    try {
      msg.setSentDate(new Date()); // now! --- requires testing
      if (subject != null) {
        msg.setSubject(subject);
      }
      msg.setFrom(defaultFrom);
      if (message != null) {
        msg.setText(message.toString());
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
    return msg;
  }

  public final boolean send(InternetAddress [] toWhom, String subject, TextList message) {
    // here add code to put the emails in a bucket
    // (iterate by toWhom[i] and create buckets,
    //  then create duplicate emails for each bucket)
    if (toWhom != null) {
      try {
        if (!shoulddie) {
          InternetAddress[] oneaddress = new InternetAddress[1];
          for(int i = toWhom.length; i-->-1;) {
            Message msg = makeMessage(subject, message);
            if(i > -1) {
              oneaddress[0] = toWhom[i];
              msg.setRecipients(Message.RecipientType.TO, oneaddress);
              SendMailBox smb = boxes.getFor(oneaddress[0]);
              smb.msgs.add(msg);
            } else { // this is the web-reporting mail bucket.  put a copy here to see later.
              msg.setRecipients(Message.RecipientType.TO, toWhom);
              String key = String.valueOf(msg.hashCode());
              mailings.put(key, msg);
            }
          }
        }
      } catch (Exception mex) {
        dbg.Caught("Exception sending email", mex);
      }
    } else {
      // +++ ???
    }
    return false;
  }

  public final Message[] list() {
    Message[] list = null;
    Enumeration ennum = mailings.elements();
    Vector lister = new Vector();
    while (ennum.hasMoreElements()) {
      try {
        String key = (String) ennum.nextElement();
        Message mailing = (Message) mailings.get(key);
        lister.add(mailing);
      } catch (Exception ex) {
        // stub
      }
    }
    list = new Message[lister.size()];
    for (int i = lister.size(); i-- > 0; ) {
      list[i] = (Message) lister.get(i);
    }
    return list;
  }

  public void run() {
    while (!shoulddie) {
      ThreadX.sleepFor(6100); //ms // +++ get from configs
      println("Starting mail run");
      synchronized(this) { // keep the sleep and while outside of here, or you will never get your down-ing thread back!
        try {
          SendMailBoxesEnumeration enumer = boxes.elements();
          while(enumer.hasMoreElements()) {
            SendMailBox mailbox = enumer.nextSMB();
            MessageList list = mailbox.msgs;
            int msgcount = list.size();
            Message msg = null;
            if(msgcount > 1) {
              TextList message = new TextList();
              for(int i = msgcount; i-- > 0; ) {
                Message innermsg = list.get();
                Address[] tos = innermsg.getAllRecipients();
                TextList tost = new TextList();
                for(int j = 0; j < tos.length; j++) {
                  Address to = tos[j];
                  tost.add(to.toString());
                }
                message.add("To: " + tost.asParagraph(","));
                message.add(innermsg.getContent().toString());
                message.add("");
                message.add("");
              } // for
              String subject = "BULK: " + String.valueOf(msgcount) +
                  " new msgs ...";
              InternetAddress[] oneaddress = new InternetAddress[1];
              oneaddress[0] = mailbox.getAddress();
              msg = makeMessage(subject, message);
              msg.setRecipients(Message.RecipientType.TO, oneaddress);
            } else { // msgcount < 2 (assume == 1)
              msg = list.get();
            }
            if(msg != null) {
              String error = null;
              String subjtoadd = null;
              try {
                subjtoadd = msg.getSubject();
                println("Sending ... [" + subjtoadd + "]");
                reads.add(1);
                sw.Start();
                boolean status = trie(msg);
                txns.add(sw.Stop());
                println("... returned after " + sw.millis() + " ms [" +
                        (status ? "SUCCESS" : "FAILURE") + "]");
                if(status) {
                  writes.add(1);
                }
              } catch(Exception e) {
                dbg.Caught(e);
                error = e.toString();
              } finally {
                if( (error != null) && (error.length() > 0)) {
                  dbg.ERROR("SendMail Error: " + error);
                }
                dbg.VERBOSE("Adding the following to the email batch: " +
                            subjtoadd);
                batch.add(subjtoadd);
              }
            }
          } // while
        } catch(Exception ex) {
          dbg.Caught(ex);
        }
      }
    }
  }

  private boolean trie(Message msg) {
    return tryonce(msg, false);
  }

  private static String SummaryItem(Message msg) {
    if(msg == null) {
      return "MESSAGE IS NULL!";
    }
    String recipients = "NO RECIPIENTS!";
    String subject = "NO SUBJECT!";
    try {
      TextList tl = new TextList();
      Address [ ] recips = msg.getAllRecipients();
      // go forward to keep them in order
      for(int i = 0; i < recips.length; i++) {
        tl.add(InternetAddress.toString(recips));
      }
      recipients = tl.asParagraph(",");
      subject = msg.getSubject();
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
    return "TO:" + recipients + " " + "RE:" + subject;
  }

  private boolean tryonce(Message msg, boolean lastTime) {
    boolean status = false;
    try {
      Transport.send(msg);
      status = true; // no exceptions, so it went
    } catch (Exception ex) { // +++ break out the different kind of exceptions
      dbg.Caught("Exception attempting to send email [" + SummaryItem(msg) + "]: ", ex);
      boolean precheck = false;
      // +++ if get an exception for need to login (whatever that is), do the pop precheck
      if(precheck) {
        String errorMess = PopClient.PopQuickCheck(defaultPopIp, defaultUsername, defaultPassword);
        if(!StringX.NonTrivial(errorMess)) {
          dbg.ERROR("Pop prechecked and trying again.");
          status = tryonce(msg, true);
          // +++ have the try/c/f in a separate function, and call it again here (but only ONE more time, or else put back in the queue) ....
        } else {
          // if the precheck failed, we don't bother to do the send.
          dbg.ERROR("`Error popchecking: " + errorMess);
        }
      }
    } finally {
      return status;
    }
  }

  private static final InternetAddress DEFAULTMAILTO = JUSTMATTMELLOEMAIL;
  public void onTimeout() {
    try {
      Alarmer.Defuse(alarmum);
      // get the list of addresses to send this to from config ...
      //since these use 2 different threads, must COPY the batch and use the copy!
      TextList batch2mail = new TextList();
      // slight mutexing problem here, but not that important right now
      batch2mail.appendMore(batch);
      if (batch != null) { // when this is called from constructor, batch might be null
        batch.clear();
      }
      send(batchList,
           hostname() + ".BatchEmailReport[" + batch2mail.size() + "]:",
           batch2mail);
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
    finally {
      Alarmer.reset(batchInterval, alarmum);
    }
  }

  public String svcTxns() {
    return "" + txns.getCount();
  }

  public String svcPend() {
    return boxes.size();
  }

  public String svcAvgTime() {
    return DateX.millisToSecsPlus(txns.getAverage());
  }

  public String svcWrites() {
    return printByteStats(writes);
  }

  public String svcReads() {
    return printByteStats(reads);
  }

  public boolean isUp() {
    return (myThread != null) && myThread.isAlive();
  }

  public synchronized void down() {
    shoulddie = true;
    Alarmer.Defuse(alarmum);
    Thread mt = myThread;
    if(mt != null) {
      mt.interrupt();
    }
    markStateChange();
  }

  public synchronized void up() {
    loadConfigs();
    shoulddie = false;
    if((myThread == null) || (!myThread.isAlive())) {
      myThread = new Thread(this, NAME);
      myThread.setDaemon(true);
      myThread.start();
    }
    Alarmer.reset(batchInterval, alarmum); // setup an alarmer to kill me if I don't come back within TIMEOUT seconds!
    markStateChange();
  }

  // +++ create a web interface to this !!!
  public synchronized void empty() {
    for(Enumeration ennum = mailings.keys(); ennum.hasMoreElements();) {
      mailings.remove(ennum.nextElement());
    }
  }

  private static final InternetAddress [ ] NULLINETADDR = new InternetAddress [0] ;
  public final InternetAddress [ ] parse(String addr) {
    InternetAddress [ ] ret = NULLINETADDR;
    try {
      ret = InternetAddress.parse(addr, false);
    } catch (Exception ex) {
      dbg.Caught("Exception parsing internet address: " + addr, ex);
    } finally {
      return ret;
    }
  }
  /*package*/ static final InternetAddress [ ] Parse(String addr) {
    InternetAddress [ ] ret = NULLINETADDR;
    try {
      ret = InternetAddress.parse(addr, false);
    } catch (Exception ex) {
      dbg.Caught("Exception parsing internet address: " + addr, ex);
    } finally {
      return ret;
    }
  }
}

class MessageList {
  ObjectFifo messages = new ObjectFifo();
  public void add(Message m) {
    messages.put(m);
  }
  public Message get() {
    return (Message)messages.next();
  }
  public int size() {
    return messages.Size();
  }
}

class SendMailBox {
  public SendMailBox(InternetAddress ia) {
    this.ia = (InternetAddress)ia.clone();
  }
  private InternetAddress ia = null;
  public InternetAddress getAddress() {
    return ia;
  }
  public MessageList msgs = new MessageList();
  public UTC emptied = null;
  public boolean isTime(long intervalMs) {
    return false; // +++ calculate
  }
  public int size() {
    return msgs.size();
  }
}

class SendMailBoxes {
  private Hashtable boxes = new Hashtable();
  public SendMailBox getFor(InternetAddress ia) {
    SendMailBox smb = (SendMailBox)boxes.get(ia);
    if(smb == null) {
      smb = new SendMailBox(ia);
      put(ia, smb);
    }
    return smb;
  }
  public void put(InternetAddress ia, SendMailBox smb) {
    boxes.put(ia, smb);
  }
  public SendMailBoxesEnumeration elements() {
    return new SendMailBoxesEnumeration(boxes.elements());
  }
  public String size() {
    int boxcount = 0;
    int mailcount = 0;
    for(SendMailBoxesEnumeration ennum = elements(); ennum.hasMoreElements();) {
      SendMailBox smb = ennum.nextSMB();
      boxcount++;
      mailcount += smb.size();
    }
    return ""+mailcount+" in " +boxcount;
  }
}
class SendMailBoxesEnumeration implements java.util.Enumeration {
  public SendMailBoxesEnumeration(Enumeration objects) {
    ennum = objects;
  }
  Enumeration ennum = null;
  public boolean hasMoreElements() {
    return ennum.hasMoreElements();
  }
  public Object nextElement() {
    return ennum.nextElement();
  }
  public SendMailBox nextSMB() {
    return (SendMailBox)nextElement();
  }
}
