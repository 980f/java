/**
* Title:        OurForms
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: OurForms.java,v 1.54 2001/11/14 01:47:50 andyh Exp $
*/
package net.paymate.terminalClient;

import net.paymate.terminalClient.IviForm.* ;

import net.paymate.Main;
import net.paymate.util.*;

import java.io.*;
import java.util.Vector;

public class OurForms {
  static final Tracer dbg=new Tracer(OurForms.class.getName());
  public static EasyCursor Cfg=new EasyCursor();//avert npe's
  public static boolean checksAllowed=false;
  public static boolean debitAllowed=false;
  //////////////////////////////////////////////////////
  final static String change2check="TO USE A CHECK ";
  final static String accept=      "IF AMOUNT IS OK";
  final static String refuse=      "TO CANCEL SALE ";
  final static String candebit=    "IF DEBIT CARD";
  final static String forgotpin=   "DON'T KNOW PIN";


  //////////////////////////////////////////////////////
  // forms registry{
    static final Vector /*<OurForm>*/registry=new Vector(new POSForm().numValues());

    public static final void Register(OurForm form){
      registry.add(form); //keep a local handle
    }

    public static final OurForm Form(int i){
      return (OurForm)registry.elementAt(i);
    }

    public static final OurForm Find(int /*POSForm*/posform){
      //search list for compatible form
      for(int i=registry.size();i-->0;){
        OurForm form=Form(i);
        if(form.myNumber==posform){
          return form;
        }
      }
      return null;
    }


    public static final int FormCount(){
      return registry.size();
    }

    public static final void purge(){
      registry.clear();
    }
  //}
////////////////////////////////////////////////
  public static final boolean insertGraphic(OurForm thisform){
    dbg.Enter("insertGraphic4:"+thisform.myName);
    try {
      Cfg.push(thisform.Id().Image());
//      dbg.VERBOSE("Cfg:"+Cfg.toSpam());
      thisform.pcxResource=Cfg.getString("graphic");
      return Safe.NonTrivial(thisform.pcxResource);
    } finally {
      Cfg.pop();
      dbg.Exit();
    }
  }

  /////////////////////////////////////////////////////

  public static final OurForm NotYetImp(int posform){
    OurForm thisform= new OurForm(posform);
    thisform.BannerLine("):NOT ENABLED:(");
    thisform.addLegend("Feature:","1");
    thisform.addLegend(new POSForm(posform).Image(),"1");
    thisform.stdCancel();
    return thisform;
  }

  public static final OurForm FubarForm(){
    OurForm thisform= new OurForm(POSForm.FubarForm);
    thisform.BannerLine("IMPOSSIBLE!!");
    TextList apologia=new TextList("This should never appear where anyone can see it.\n Hopefully the clerk can still do what needs to be done.\n",OurForm.textWidth,true);
    thisform.addParagraph(apologia);
    thisform.stdCancel();
    return thisform;
  }


  public static final OurForm NotInService(){
    OurForm thisform= new OurForm(POSForm.NotInService);
    thisform.BannerLine("):NOT IN SERVICE :(");
    return thisform;
  }

  public static final OurForm couponAd(int which){
    OurForm thisform= new OurForm(which);
    return thisform;
  }

  public static final OurForm  ClerkLogin(){
    OurForm thisform= couponAd(POSForm.ClerkLogin);
    //    thisform.insertSwipe("Register is Locked");
    thisform.BannerLine("");
    return thisform;
  }

  public static final OurForm  IdleAd(){
    OurForm thisform= couponAd(POSForm.IdleAd);
    if(false&&checksAllowed){
      thisform.addButton("Info for Check", ButtonTag.DoCheck);
    }
    //due to temporary stupidities all buttons must be added directly after "couponAd"
    thisform.insertCardSwipe();
    return thisform;
  }

  public static final OurForm NeedId(){
    OurForm thisform= new OurForm(POSForm.NeedID);
    thisform.insertIdSwipe();
    return thisform;
  }

  public static final OurForm SwipeAgain(){
    OurForm thisform= new OurForm(POSForm.SwipeAgain );
    thisform.insertCardSwipe(OurForm.reSwipe);
    thisform.stdAmount();
    return thisform;
  }

  public static final OurForm GetPayment (){
    OurForm thisform= new OurForm(POSForm.GetPayment );
    thisform.insertCardSwipe();
    thisform.stdAmount();
    thisform.space(1,1);
    if(checksAllowed){
      thisform.addCheckInfo();
    }
    return thisform;
  }

  public static final OurForm  SignCard(){
    OurForm thisform= new OurForm(POSForm.SignCard);

    thisform.BannerLine("Sign, Touch "+OurForm.Done);
    thisform.stdAmount();
    //reserve some space for better instructions...
    thisform.addLegend("");

    thisform.addSigBox();
    //thisform.stdCancel();
    return thisform;
  }

  public static final OurForm WaitPatronCheck(){
    if(true){
      OurForm thisform= new OurForm(POSForm.WaitPatronCheck);
      return thisform.insertIdSwipe();
    } else {
      OurForm thisform= new OurForm(POSForm.WaitPatronCheck);
      thisform.insertIdSwipe();
      thisform.stdAmount();
      thisform.askYesno("Amount Ok?");
      return thisform;
    }
  }

  public static final OurForm WaitClerkCheck(){
    OurForm thisform= new OurForm(POSForm.WaitClerkCheck);
    thisform.insertIdSwipe();
    return thisform;
  }

  public static final OurForm WaitPatronCredit(){
    OurForm thisform= new OurForm(POSForm.WaitPatronCredit);
    thisform.insertReSwipe();
    thisform.stdAmount();
    thisform.space(1,2);

    thisform.addParagraph("\"I agree to the above amount per my cardholder agreement\"","1");

    thisform.askOkWrong();
    if(debitAllowed){
      thisform.bigButton(candebit,ButtonTag.DoDebit);
    }
    return thisform;
  }

  public static final OurForm oldWaitPatronCredit(){
    OurForm thisform= new OurForm(POSForm.WaitPatronCredit);
    thisform.insertReSwipe();
    thisform.stdAmount();
    thisform.space(1,2);
    thisform.askYesno("AMOUNT OK?");
    if(debitAllowed){
      thisform.bigButton(candebit,ButtonTag.DoDebit);
    }
    return thisform;
  }


  public static final OurForm WaitClerkCredit(){
    OurForm thisform= new OurForm(POSForm.WaitClerkCredit);
    thisform.BannerLine(OurForm.waitclerk);
    //VISA switcher will have to dynamically overwrite bannerline.
    if(debitAllowed){
      thisform.space(1,5);
      thisform.bigButton(candebit,ButtonTag.DoDebit);
    }
    //EBT card...
    if(false&&checksAllowed){
      thisform.space(1,2);
      thisform.bigButton(change2check,ButtonTag.DoCheck);
    }
    //    thisform.bigButtonRight("is DEBIT CARD",ButtonTag.DoDebit);
    return thisform;
  }

  public static final OurForm WaitClerkDebit(){
    OurForm thisform= new OurForm(POSForm.WaitClerkDebit);
    thisform.BannerLine(OurForm.waitclerk);
    thisform.space(1,5);
    thisform.bigButton(forgotpin,ButtonTag.DoCredit);
    if(false&&checksAllowed){
      thisform.space(1,2);
      thisform.bigButton(change2check,ButtonTag.DoCheck);
    }
    //    thisform.bigButtonRight("is DEBIT CARD",ButtonTag.DoDebit);
    return thisform;
  }

  public static final OurForm WaitPatronDebit (){
    OurForm thisform= new OurForm(POSForm.WaitPatronDebit);
    thisform.isPinPad=true;
    thisform.isSwiper=false;//seems to be an entouch requirement
    //and the following should never be seen:
    thisform.BannerLine("THIS SHOULD BE A PINPAD");
    thisform.addLegend("Please tell clerk: ");
    thisform.addLegend("\" The pin pad failed\"");
    return thisform;
  }

  public static final OurForm WaitApproval(){
    OurForm thisform= couponAd(POSForm.WaitApproval);
    //    OurForm thisform= new OurForm(POSForm.WaitApproval);
    thisform.BannerLine("Approving ...");
    //  thisform.stdCancel();//too late to reliably cancel.
    return thisform;
  }

  public static final OurForm WaitReceipt(){
    //    OurForm thisform= new OurForm(POSForm.WaitReceipt);
    OurForm thisform= new OurForm(POSForm.WaitCompletion);
    thisform.BannerLine("Thank you,");
    thisform.addLegend("Please come again");
    return thisform;
  }

  public static final OurForm SeeClerk(){
    OurForm thisform= new OurForm(POSForm.SeeClerk);
    thisform.BannerLine("There is a problem");
    thisform.addLegend("        ");
    thisform.addLegend("Please ask Cashier");
    thisform.addLegend("    for Assistance");
    return thisform;
  }

  /////////////////////////////////////////////////////////
  public static final void applyOptions(TerminalCapabilities caps){
    if(caps!=null){
      dbg.VERBOSE("setting options...");
      checksAllowed =caps.acceptsChecks();
      debitAllowed  =caps.doesDebit();
      MakeAllForms();
    }
  }

  public static final void applyStoreInfo(String realHeader){
    dbg.VERBOSE("Setting storeInfo:"+realHeader);
    //+_+ this used to actually do something useful...
    MakeAllForms();
  }

  public static final void serviceAll(){
    for(int i=registry.size();i-->0;){
      OurForm form=Form(i);
      try {
        form.ToService();
      } catch (java.io.IOException up){
        dbg.ERROR("Form#"+form.myNumber+" Caught "+up);
      }
    }
  }
/**
 * you had BETTER make a private copy for this guy!
 */
  public static final void SetConfig(EasyCursor cfg) {
    if(cfg!=null){
      dbg.VERBOSE("incoming:"+cfg.toSpam());
      Cfg=cfg.EasyExtract("Form");
      dbg.VERBOSE("my copy:"+Cfg.toSpam());
    }
  }

  public static final void MakeAllForms(){
    dbg.Enter("MakingAllForms");
    try {
dbg.mark("purge first");
    purge(); //this fixed a lot!!!
dbg.mark("NIS");
    NotInService      ();
dbg.mark("clogin");
    ClerkLogin        ();
dbg.mark("idle");
    IdleAd            ();
dbg.mark("getpayment");
    GetPayment        ();
dbg.mark("swipeagain");
    SwipeAgain        ();
dbg.mark("wclerkcredit");
    WaitClerkCredit   ();
dbg.mark("wpcredit");
    WaitPatronCredit  ();
dbg.mark("wcldebit");
    WaitClerkDebit    ();
dbg.mark("wpdebit");
    WaitPatronDebit   ();
dbg.mark("wclerkcheck");
    WaitClerkCheck    ();
dbg.mark("wpaycheck");
    WaitPatronCheck   ();
dbg.mark("waitapproval");
    WaitApproval      ();
dbg.mark("waitreceipt");
    WaitReceipt       ();
dbg.mark("signcard");
    SignCard          ();
dbg.mark("seeclerk");
    SeeClerk          ();
dbg.mark("needif");
    NeedId            ();
dbg.mark("fubar");
    FubarForm         ();
dbg.mark("serviceall");
    serviceAll();

    }
    finally {
      dbg.Exit();
    }

  }

}
//$Id: OurForms.java,v 1.54 2001/11/14 01:47:50 andyh Exp $
