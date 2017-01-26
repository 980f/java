package net.paymate.web.page.accounting;


/**
 * Title:        $Source: /cvs/src/net/paymate/web/page/accounting/ServicePage.java,v $
 * Description:  Page displaying the details of a single Service.java
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.17 $
 */

import net.paymate.web.page.*;
import net.paymate.util.*; // service, errorlogstream
import org.apache.ecs.*;
import org.apache.ecs.html.*;
import net.paymate.util.*;
import net.paymate.database.*; //for search range types
import net.paymate.lang.*;
import net.paymate.web.*;
import net.paymate.web.color.*;
import net.paymate.web.table.*;
import net.paymate.web.table.query.*;
import javax.servlet.http.*; // HttpSessionContext
import net.paymate.servlet.*; // SessionedServletService
import java.util.Vector;
import net.paymate.net.*; // mail stuff
import net.paymate.authorizer.Authorizer;
import net.paymate.authorizer.AuthTermAgentList;
import javax.mail.Message;
import net.paymate.util.timer.Alarmer;
import net.paymate.data.sinet.LogEntityHome;

public class ServicePage extends Acct {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ServicePage.class);

  public ServicePage(LoginInfo linfo, AdminOpCode opcodeused,
                     HttpSessionContext context, EasyProperties ezp) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(service(ezp, linfo, opcodeused, context));
  }

  public static final String baseURL(String url, String servicename) {
    return url+"&s="+servicename;
  }

  private static final String TOGGLEPW = "tpw";

  public final Element defaultPage(String comment, String url, Service service) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ColorScheme colors = linfo.colors();
    String baseURL = baseURL(url, service.serviceName());
    ElementContainer ec = new ElementContainer();

    // comment
    if(comment !=null) {
      ec.addElement(BRLF)
        .addElement(comment);
    }

    // refresh link
    ec.addElement(flyoverLink(baseURL, "Click here to refresh the service's page ...", "Refresh"));

    // line
    ec.addElement(new HR())
      .addElement(BRLF);

    // up/down form
    boolean isup = service.isUp();
    Form form = NewPostForm(baseURL+"&"+(isup ? "dn":"up")+"=1");
    String updowntxt = Service.upText(!isup);
    TD td1 = new TD().addElement("Enter Password to " + updowntxt + " service: ");
    TD td2 = new TD().addElement(new Input(Input.PASSWORD, TOGGLEPW, ""));
    TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue(updowntxt));
    TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
    Table t = new Table().addElement(tr1);
    form.addElement(t);
    ec.addElement(form);

    // service detail
    Service [] services = new Service[1];
    services[0] = service;
    ec.addElement(new ServicesFormat(services, colors, service.serviceName() + " Status", null));
    if(!service.selfConfiguring()) {
      // param change form
      ec.addElement(new HR());
      TR tr2 = new TR();
      Form f = NewPostForm(baseURL);
      f.addElement(new Table().setCellSpacing(4).setCellPadding(0).setBorder(0).addElement(tr2));
      TextList paramnames = db.getServiceParamsNames(service.serviceName());
      Option [] params = new Option[paramnames.size()];
      for(int i = 0; i < paramnames.size(); i++) {
        String toset = paramnames.itemAt(i);
        params[i] = new Option(toset).addElement(toset);
      }
      tr2.addElement(new TD().addElement("Parameter: ").addElement(new Select("set", params)));
      tr2.addElement(new TD().addElement("Value: ").addElement(new Input(Input.TEXT, "to", "")));
      tr2.addElement(new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Change")));
      ec.addElement(f);
      // now the parameters
      EasyProperties ezc = db.getAllServiceParams(service.serviceName());
      ec.addElement(EasyCursorTableGen.output(service.serviceName()+" Configuration Parameters",
                                              colors, ezc))
        .addElement(new HR());
    } else {
      // do nothing since the service is  self-configuring
    }

    // more service detail
    return ec;
  }

  public final Element service(EasyProperties ezp, LoginInfo linfo,
                               AdminOpCode opcodeused,
                               HttpSessionContext context) {
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    String serviceName = ezp.getString("s");
    Service service = Service.getServiceByName(serviceName);
    ColorScheme colors = linfo.colors();
    String url = baseURL(serviceUrl(), service.serviceName());
    // first, check to see if we are sending it up or down
    boolean up   = !service.isUp();
    // opcode can only be up, down, or service, so deal with it
    boolean toggled = false;
    String togglepw = ezp.getString(TOGGLEPW);
    if(StringX.NonTrivial(togglepw)) {
      if(linfo.assoc.passes(togglepw)) {
        String ret = changeServiceState(service, up); // up takes priority
        if(StringX.NonTrivial(ret)) {
          ec.addElement(new StringElement(ret));
        } else {
          ec.addElement("Error " + Service.upText(!up) + "-ing service " +
                        service + "!").addElement(PayMatePage.BRLF);
        }
      } else {
        ec.addElement("Can't toggle service; password error.").addElement(PayMatePage.BRLF);
      }
    } else {
      // they don't want to toggle it
    }
    // then, check to see if we are setting a parameter
    String paramname = StringX.TrivialDefault(ezp.getString("set"), "");
    String paramvalue  = StringX.TrivialDefault(ezp.getString("to"), "");
    if(StringX.NonTrivial(paramname) && StringX.NonTrivial(paramvalue)) {
      // which one, and to what level?
      String oldvalue = db.getServiceParam(service.serviceName(), paramname, paramvalue);
      db.setServiceParam(service.serviceName(), paramname, paramvalue);
      String newvalue = db.getServiceParam(service.serviceName(), paramname, paramvalue);
      ec.addElement("Parameter " + paramname + " set to " + newvalue).addElement(PayMatePage.BRLF);
    }
    // then DO service-specific stuff
    if(service.is(LogControlService.NAME)) {
      // debug logs & settings
      // add the ability to change them !!!  only database administrators have this pOwEr
      // setto
      // first, check to see if we are supposed to change a loglevel
      String set = ezp.getString("set");
      String to  = ezp.getString("to");
      if(StringX.NonTrivial(set) && StringX.NonTrivial(to)) {
        String message = "";
        // which one, and to what level?
        if(StringX.equalStrings(set, "ALL")) {
          LogControlService.THE().setAll(to);
          message = "ALL LogSwitches set to " + to;
        } else {
          // maybe put more of this chunk into the LogControlService class?
          // if exists +++
          LogSwitch ls = LogSwitch.getFor(set);
          if(ls == null) {
            message = "Log switch not found: " + set;
          } else {
            LogControlService.THE().set(ls, to);
            message = set + " set to " + to;
          }
        }
        ec.addElement(new Center().addElement(new H3(message))).addElement(BRLF);
      }
    }
    ec.addElement(defaultPage("", serviceUrl(), service));  // then display the bulk of the page

    // then display service-specific stuff
    // +++ need a switch here and an enumeration of services
    if(service.is(DBMacrosService.NAME)) {
      ec.addElement(PayMatePage.BRLF)
//          .addElement(new StatementsFormat(colors, "Open Queries"))
//          .addElement(new HR())
          .addElement(EasyCursorTableGen.output("Database Connection Pools", colors, db.getPoolProfile()))
          .addElement(PayMatePage.BRLF);
      ec.addElement(new HR())
          .addElement(DBPage.generatePage(ezp, url, linfo.colors(), null/*forDbPage*/));
    } else if(service.is(LogControlService.NAME)) {
      ec.addElement(new HR());
      TR tr2 = new TR();
      Form f = NewPostForm(url);
      f.addElement(new Table().setCellSpacing(4).setCellPadding(0).setBorder(0).addElement(tr2));
      Vector debuggers = LogSwitch.Sorted();
      Option [] logswitches = new Option[debuggers.size()+1];
      logswitches[0] = new Option("ALL").addElement("ALL");
      for(int i = 0; i < debuggers.size(); i++) {
        LogSwitch ls = (LogSwitch)debuggers.elementAt(i);
        String toset = (ls != null) ? ls.Name() : "[not found]";
        logswitches[i+1] = new Option(toset).addElement(toset);
      }
      LogLevelEnum llenum = new LogLevelEnum();
      Option [] loglevels = new Option[llenum.numValues()];
      for(int i = 0; i < llenum.numValues(); i++) {
        llenum.setto(i);
        loglevels[i] = new Option(llenum.Image()).addElement(llenum.Image());
      }
      tr2.addElement(new TD().addElement("Debug or Fork: ").addElement(new Select("set", logswitches)));
      tr2.addElement(new TD().addElement("Log Level: ").addElement(new Select("to", loglevels)));
      tr2.addElement(new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Change")));
      String title = "";//service.hostname() + " Logs";
      LogSwitchTableGen.output(title, linfo.colors());  // +++ so that it makes checkboxes and inserts code for it.
      ec.addElement(new Center(new H2(title)))
          .addElement(BRLF)
          .addElement(BRLF)
          .addElement(f)
          .addElement(BRLF)
          .addElement(LogSwitchTableGen.output("Log Levels", linfo.colors()))  // again to make it all work (--- I forgot why, but I think it has to do with the Log stuff)
          .addElement(new HR());
    } else if(service.is(LogFileService.NAME)) {
      ec.addElement(PayMatePage.BRLF)
          .addElement(new LogFileFormat(colors, "Log Files"))
          .addElement(PayMatePage.BRLF);
    } else if(service.is(SendMail.NAME)) {
      SendMail sender = (SendMail)service;
      Message [ ] list = sender.list();
      ec.addElement(SendMailFormatter.output("", linfo.colors(), list, linfo.ltf()));
    } else if(service.is(SessionCleaner.NAME)) {
      ec.addElement(PayMatePage.BRLF)
          .addElement(HttpSessionTableGen.output("HttpSessions", colors, context))
          .addElement(PayMatePage.BRLF);
    } else if(service.is(SinetServer.NAME)) {
      ec.addElement(PayMatePage.BRLF)
          .addElement(new HR())
          .addElement(EasyCursorTableGen.output("Loaded Servlet Parameters", colors, SinetServer.THE().grabbedParams))
          .addElement(PayMatePage.BRLF);
      EasyProperties logentities = LogEntityHome.status();
      ec.addElement(new HR())
          .addElement(EasyCursorTableGen.output("Log Entities", colors, logentities))
          .addElement(PayMatePage.BRLF);
    } else if(service.is(SystemService.NAME)) {
      EasyProperties alarms = Alarmer.EzpDump();
      ec.addElement(PayMatePage.BRLF)
          .addElement(new TimesFormat(colors, "Times", linfo.ltf().getZone()))
          .addElement(new HR())
          .addElement(new ThreadFormat(colors, "Threads"))
          .addElement(new HR())
          .addElement(new RunTimeFormat(colors, "Disk"))
          .addElement(new HR())
          .addElement(MonitorTableGen.output("Monitors", colors, Monitor.dumpall()))
          .addElement(new HR())
          .addElement(EasyCursorTableGen.output("System Properties", colors, new EasyCursor(System.getProperties()), 80))
          .addElement(new HR())
          .addElement(PackageArrayTableGen.output("Packages", colors, Package.getPackages()))
          .addElement(new HR())
          .addElement(EasyCursorTableGen.output("Alarms", colors, new EasyCursor(alarms)))
          .addElement(new HR())
          .addElement(PayMatePage.BRLF);
    } else if(service.isAuthService()) {
      Authorizer auth = (Authorizer)service;
      AuthorizerTerminalAgentsFormat formatter = new AuthorizerTerminalAgentsFormat(auth, linfo.colors());
      ec.addElement(PayMatePage.BRLF)
          .addElement(new HR())
          .addElement(formatter)
          .addElement(PayMatePage.BRLF);
    }
    return ec;
  }

  private static final String changeServiceState(Service service, boolean up) {
    // first, find the service
    // +++ use an object registry !!!
    String ret = "ERROR changing service state";
    String serviceName = service.serviceName();
    try {
      if(up) {  // try to get the service to restart
        service.up(); // up's shouldn't do anything if the service is already up!
      } else {
        service.down();
      }
      ret = "Service " + serviceName + " " + service.upText() + " (desired " + Service.upText(up) + ")!";
    } catch (Exception e) {
      dbg.Caught(e);
      ret = "Exception attempting to " + Service.upText(up) + " service " + serviceName + "!";
    }
    return ret;
  }

/*
  // +++ put this stuff in a baser class and use elsewhere ... maybe
  private static final Element input(String inputType, String fieldName, String defaultValue, int maxlength, int size) {
    defaultValue = StringX.TrivialDefault(defaultValue, "");
    fieldName = StringX.TrivialDefault(fieldName, "");
    inputType = StringX.TrivialDefault(inputType, "");
    Input i = new Input(inputType, fieldName, defaultValue);
    if(maxlength > 0) {
      i.setMaxlength(maxlength);
    }
    if(size > 0) {
      i.setSize(size);
    }
    return i;
  }
  private static final Element input(String inputType, String fieldName, String defaultValue) {
    defaultValue = StringX.TrivialDefault(defaultValue, "");
    fieldName = StringX.TrivialDefault(fieldName, "");
    inputType = StringX.TrivialDefault(inputType, "");
    return input(inputType, fieldName, defaultValue, -1, -1);
  }
  private static final TR rowPrompt(String prompt1, String inputType, String fieldName, String defaultValue, String prompt2, String inputType2, String fieldName2, String defaultValue2) {
    // deal with nulls
    Element field1 = input(inputType, fieldName, defaultValue);
    Element field2 = input(inputType2, fieldName2, defaultValue2);
    return rowPrompt(prompt1, field1, prompt2, field2);
  }
  private static final TR rowPrompt(String prompt1, Element field1, String prompt2, Element field2) {
    // deal with nulls
    prompt1 = StringX.TrivialDefault(prompt1, "");
    prompt2 = StringX.TrivialDefault(prompt2, " ");
    // build the row
    TD td1 = new TD(prompt1);
    TD td2 = new TD(field1);
    TD td3 = new TD(prompt2);
    TD td4 = new TD(field2);
    TR tr  = new TR(td1);
    tr.addElement(td2);
    tr.addElement(td3);
    tr.addElement(td4);
    return tr;
  }
*/
}
