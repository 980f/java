package net.paymate.terminalClient;
/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/OurForms.java,v $
* Description:  manages PosForms for an entouch.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.69 $
*/


import net.paymate.terminalClient.IviForm.* ;
import net.paymate.connection.StoreConfig;
import net.paymate.lang.StringX;
import net.paymate.Main;
import net.paymate.util.*;

import java.io.*;
import java.util.Vector;

public class OurForms {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(OurForms.class);
  public static EasyCursor Cfg=new EasyCursor();//avert npe's
  public static boolean checksAllowed=false;
  public static boolean neverInit=true;

  //////////////////////////////////////////////////////
  final static String change2check="TO USE A CHECK ";
  final static String accept=      "IF AMOUNT IS OK";
  final static String refuse=      "TO CANCEL SALE ";

  final static String forgotpin=   "DON'T KNOW PIN";

  public static LocalTimeFormat ltf = LocalTimeFormat.Utc(); // SET THIS !!! +++

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
      return StringX.NonTrivial(thisform.pcxResource);
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
    TextList apologia=thisform.makeParagraph("This should never appear where anyone can see it.\n Hopefully the clerk can still do what needs to be done.");
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
//trust auto-detect    thisform.addDebit("IdleAd");
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
      thisform.addCheckInfo(ltf);
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
//    thisform.addDebit("signature screen");
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
//    thisform.addDebit("WaitPatronCredit");
    return thisform;
  }

  public static final OurForm WaitClerkCredit(){
    OurForm thisform= new OurForm(POSForm.WaitClerkCredit);
    thisform.BannerLine(OurForm.waitclerk);
    //VISA switcher will have to dynamically overwrite bannerline.
    thisform.space(1,10);
//    thisform.addDebit("KNOW PIN?");
    thisform.insertReSwipe();
    //EBT card...
    if(false&&checksAllowed){
      thisform.space(1,2);
      thisform.bigButton(change2check,ButtonTag.DoCheck);
    }
    return thisform;
  }

  /**
   * can't put up pinpad until amount has been entered.
   */
  public static final OurForm WaitClerkDebit(){
    OurForm thisform= new OurForm(POSForm.WaitClerkDebit);
    thisform.BannerLine(OurForm.waitclerk);
    thisform.space(1,10);
    thisform.addButton(forgotpin,ButtonTag.DoCredit);
    if(false&&checksAllowed){
      thisform.space(1,2);
      thisform.addButton(change2check,ButtonTag.DoCheck);
    }
    return thisform;
  }

  public static final OurForm WaitPatronDebit (){
    OurForm thisform= new OurForm(POSForm.WaitPatronDebit);
    thisform.isPinPad=true;
    thisform.isSwiper=false;//seems to be an entouch requirement
    //and the following should never be seen:
    thisform.BannerLine("SHOULD BE A PINPAD");
    thisform.addLegend("Please tell clerk: ");
    thisform.addLegend("The pin pad failed!");
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
  /**
  * @return true if forms changed by this call
  */
  public static final boolean applyOptions(StoreConfig reply){
    boolean changed=neverInit;
    if(reply!=null){
      TerminalCapabilities caps=reply.termcap;
      dbg.VERBOSE("preparing form backgrounds");
      SetConfig(null);//store config doesn't have forms def's yet.

      dbg.VERBOSE("setting options...");
      if(checksAllowed !=caps.acceptsChecks()){
        checksAllowed =caps.acceptsChecks();
        changed = true;
      }
      if(OurForm.debitAllowed != caps.doesDebit()){
        OurForm.debitAllowed  =caps.doesDebit();
        changed = true;
      }

      if(changed){
        MakeAllForms();
      }
    }
    return changed;
  }

  public static final void serviceAll(){
    for(int i=registry.size();i-->0;){
      OurForm form=Form(i);
      form.ToService();
    }
    neverInit=false;
  }


  /**
  * you had BETTER make a private copy for this guy!
  */
  private static final void SetConfig(EasyCursor cfg) {
    if(cfg==null || cfg.size()<=0){
      dbg.VERBOSE("trying to load from OurForms.properties");
      cfg=Main.Properties(OurForms.class);
      if(cfg.size()<=0){//file doesn't exist, doesn't read successfully, or is empty
        dbg.VERBOSE("trying to load from legacy location");
        cfg=Main.props();//try the appliance's config (the legacy location)
      }
    }
    Cfg=cfg.EasyExtract("Form");//we only need this branch at present
    dbg.VERBOSE(cfg.toString("forms background config little c"));
    dbg.VERBOSE(Cfg.toString("forms background config BIG C"));
  }

  public static final void MakeAllForms(){
    dbg.Enter("MakingAllForms");
    try {
      purge(); //this fixed a lot!!!
      NotInService      ();
      ClerkLogin        ();
      IdleAd            ();
      GetPayment        ();
      SwipeAgain        ();
      WaitClerkCredit   ();
      WaitPatronCredit  ();
      WaitClerkDebit    ();
      WaitPatronDebit   ();
      WaitClerkCheck    ();
      WaitPatronCheck   ();
      WaitApproval      ();
      WaitReceipt       ();
      SignCard          ();
      SeeClerk          ();
      NeedId            ();
      FubarForm         ();
      serviceAll();
    }
    finally {
      dbg.Exit();
    }
  }
/////////////////////
//tester

 private static void formspam(int posformnumber){
    OurForm f= Find(posformnumber);
//    System.err.println("Form spam:"+f.toSpam());
//    System.err.println(f.asXml(null).asParagraph());
 }

 public static final void main(String argv[]){
//    SetConfig(Main.props());
//    TerminalCapabilities caps=new TerminalCapabilities();
//    caps.debitAllowed=true;
//    applyOptions(caps,"module tester");
//    WaitClerkCredit();
//    formspam(POSForm.WaitClerkCredit);
//    formspam(POSForm.WaitPatronCredit);
//    formspam(POSForm.IdleAd);

  }

}
//$Id: OurForms.java,v 1.69 2003/07/27 05:35:16 mattm Exp $
