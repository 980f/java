package net.paymate.terminalClient;
/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/OurForm.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: OurForm.java,v 1.59 2003/12/08 22:45:42 mattm Exp $
*/

import net.paymate.terminalClient.IviForm.* ;//really need to move this package!
import net.paymate.util.ErrorLogStream;
import net.paymate.util.TextList;
import net.paymate.io.IOX;
import net.paymate.lang.Fstring;
import net.paymate.lang.StringX;
import net.paymate.Main;
import net.paymate.util.OS;

import java.io.*;

import net.paymate.awtx.*;
import net.paymate.util.*;

public class OurForm extends Form {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(OurForm.class);
  public static boolean debitAllowed=false;
  public boolean isSwiper     =false;
  public boolean idSwipe      =false;
  public boolean isPinPad     =false;

  public boolean showsAmount  =false;

  public boolean isStored     =false;
  public boolean hasDebitButton=false;
  public TextList altText     =null;

  public final static String CouponText="PRINT COUPON";
  public final static String DefaultLegendFont="1";
  final static String Done=" Ok ";

  final static String licenseid=   "Swipe your ID";//instructions

  final static String waitclerk ="Waiting on Cashier";
  final static String firstSwipe="Swipe Your Card";
  final static String reSwipe   ="Swipe Card Again";
  final static String candebit  ="DEBIT?";

  Field  amtSlot=null;
  Field swipeSpot=null;

  //theoretically could fetch some of these numbers from jpos service BUT
  //we need to write forms out before we start the service due to bugs in the service
  private int textWidth=40;

  public TextList makeParagraph(String msg){
    return new TextList(msg,textWidth,true);
  }
  public int Margin=1;
  public int LastColumn=textWidth-Margin;
  int WallToWall=LastColumn-Margin;

  public int bigWidth=(textWidth-2*Margin)/2;

  public int LastRow=29;
  int ButtonRow=LastRow-3;

  XPoint amountLocation= new XPoint( Margin,4);
  XPoint BannerLocation= new XPoint(Margin,1);

  String FullRule=Fstring.fill ("",WallToWall,'-');

  //  public final static Rectangle AdBox=new Rectangle(Margin,3,LastColumn-Margin,22);

  static final String ManufacturedBy="Terminal by PayMate.Net\n9420 Research  Blvd.\nAustin TX 78759";

  //  public void setAdName(String filename){
    //    pcxFilename=filename;
  //  }
  //
  //  public void setAdAlt(String storeInfo){
    //    altText=new TextList(StringX.OnTrivial(storeInfo,ManufacturedBy),bigWidth,true);
  //  }

  OurForm BannerLine(String toAnnounce){
    add(BannerThis(toAnnounce));
    return this;
  }

  String bannerFont="1";
  //need +++ width(font)...
  public String ValuePair(String name, String value){
    return Fstring.justified(bigWidth,name,value,' ');
  }

  public String Bannerize(String toAnnounce){
    return Fstring.centered(toAnnounce,bigWidth,' ');
  }

  public Legend BannerThis(String toAnnounce){
    return new Legend(BannerLocation,Bannerize(toAnnounce),bannerFont);
  }

  OurForm stdCancel(){
    String fartoff="CANCEL";
    add(new TextButton(LastColumn-fartoff.length()-Margin-1,ButtonRow,fartoff,ButtonTag.CustomerCancels));
    return this;
  }

  OurForm stdAmount(){
    showsAmount=true;
    amtSlot=new Field(new Legend( amountLocation, Bannerize(waitclerk),"1"));
    add(amtSlot);
    return this;
  }

  //////////////////////////////////////////////////////////////////
  /**@deprecated
  * the underlying feature itself was deprecated, not so much htis function in its own right
  */
  OurForm switchToCard(){
    addLegend("swipe to use a card instead of a check","0");
    return this;
  }

  public OurForm showalt(boolean allbig){
    if(TextList.NonTrivial(altText)){
//      for(int i=0;i<altText.size();i++){
//        addLegend(altText.itemAt(i),((!allbig &&i>0)?"0":"1"));
//        //til enTouch is fixed:
//        //if(!allbig)
//        break; //entouch can't handle as much text as we would like
//      }
// until the above is fixed, (won't compile on gcj), do this:
      addLegend(altText.itemAt(0),((!allbig &&0>0)?"0":"1"));
    }
    return this;
  }

  public OurForm addCheckInfo(LocalTimeFormat ltf){
    hrule();

    if(TextList.NonTrivial(altText)){
      hrule();
      addLegend("Or Write Check to:","1");
      showalt(true);
    }
    //+_+ get time format from Receipt class.
    addLegend(Fstring.justified(bigWidth,"Date:", ltf.format(UTC.Now())),"1");
    hrule();
    stdCancel();
    return this;
  }

  public Legend AmountLegend(String image){
    return (amtSlot!=null)? amtSlot.display(image): new Legend(0,0,"");
  }

  OurForm insertSwipe(String prompt){
    isSwiper  =true;
    if(StringX.NonTrivial(prompt)){
      if(prompt.length()<=15){//+++ compute
        prompt="->"+prompt+"<-";
      }
      return BannerLine(prompt);
    } else {
      return this;
    }
  }


  OurForm insertCardSwipe(String prompt){
    idSwipe=false;
    return insertSwipe(prompt);
  }

  OurForm insertCardSwipe(){
    return insertCardSwipe(firstSwipe);
  }

  OurForm insertIdSwipe(){
    idSwipe=true;
    insertSwipe(licenseid);
    space(1,3);
    addLegend("Driver's License");
    addLegend("or State ID card");
    return this;
  }

  OurForm insertHideSwipe(){
    idSwipe=true;
    return insertSwipe("");
  }

  OurForm insertReSwipe(){
    idSwipe=false;
    return insertSwipe("");
  }

  OurForm addRawButton(int tagger){
    XPoint p=new XPoint(Margin,nextY());
    ButtonTag button=new ButtonTag(tagger);
    TextButton wtf;
    add(wtf=new TextButton(p,button.Image()+","+button.Value(),button.Value()));
    dbg.VERBOSE("button:"+wtf.xml());
    return this;
  }

  /**
   * @return true if string will fit inside the biggest button
   * presumes big font!
   */
  static final boolean willFit(String prompt) {
    return prompt.length()<=6;
  }

  OurForm bigButton(String text, int tagger){//one per line...
    boolean inside=willFit(text);
    //added a second +1 below to space things better on screen.
    XPoint p=new XPoint(inside?Margin+1:Margin,nextY()+1+1);//room for button margin, else they overlap
    Legend lege=new Legend(p,text,"1");
    if(inside){
      add(new TextButton(lege,tagger));
    } else {
      add(new BigButton(lege,tagger,true));
    }
    return this;
  }

  OurForm bigButtonNext(String text, int tagger){//one per line...
    boolean inside=willFit(text);
    XPoint p=new XPoint(inside?nextX()+1:nextX(),inside?Y()+1:Y());//room for button margin, else they overlap
    Legend lege=new Legend(p,text,"1");
    if(inside){
      add(new TextButton(lege,tagger));
    } else {
      add(new BigButton(lege,tagger,true));
    }
    return this;
  }


  OurForm bigButtonCenter(String text, int tagger){//one per line...
    boolean inside=willFit(text);

    int x=(inside?Margin+1:Margin)+ ( (WallToWall- text.length() ) /2);
    XPoint p=new XPoint(x,nextY()+1+1);//room for button margin, else they overlap
    Legend lege=new Legend(p,text,"1");
    if(inside){
      add(new TextButton(lege,tagger));
    } else {
      add(new BigButton(lege,tagger,true));
    }
    return this;
  }


  OurForm bigButtonRight(String text, int tagger){//one per line...
    boolean inside=willFit(text);
    int xwidth= text.length()+(inside? 2 : BigButton.buttwidth);
    int why= (xwidth+nextX()>=LastColumn)?(nextY()+1):Y();

    XPoint p=new XPoint(LastColumn-1,why+1);//room for button margin, else
    Legend lege=new Legend(p,text,"1");
    if(inside){
      add(new TextButton(lege,tagger,false));
    } else {
      add(new BigButton(lege,tagger,false));
    }
    return this;
  }

  //    OurForm insertAd(boolean withCoupon){
    //    //  add(new Button(AdBox,ButtonTag.NullButton));
    //    if(StringX.NonTrivial(pcxFilename) ){
      //      pcxResource= pcxFilename;
    //    } else {
      //      space(AdBox.x+1,AdBox.y+1);
      //      showalt(true);
    //    }
    //    if(withCoupon){
      //      addButton(CouponText,ButtonTag.CouponDesired);
    //    }
    //    return this;
  //  }

  OurForm addSigBox(/* receipt.sigBox and aspectRatio..*/){
    int vertical=24/8; //derive the 24 from data hinted at in comment above
    add(new SigBox(new XRectangle(Margin,nextY(),WallToWall,nextY()+vertical)));

    addLegend("Use Pen to press");
    addLegend(Done+" when done--v");
    bigButton("ERASE",ButtonTag.ClearForm);
//too late, will be authroizing as credit before this form shows.    addDebit("KNOW PIN?");
    bigButtonRight(Done,ButtonTag.Signed);

    return this;
  }

  final static String pushpen="Please Use the Pen";

  OurForm askOkWrong(){
    space(0,1);
    addLegend(pushpen);
    bigButton("OK",ButtonTag.CustomerAmountOk);

    addDebit("KNOW PIN?");
    bigButtonRight("WRONG",ButtonTag.CustomerCancels);
    space(0,1);
    addLegend(pushpen);
    return this;
  }

  OurForm askYesno(String question){
    addLegend(question);
    space(0,1);
    addLegend(pushpen);
    bigButton("YES",ButtonTag.CustomerAmountOk);
    bigButtonRight("NO",ButtonTag.CustomerCancels);
    space(0,1);
    addLegend(pushpen);
    return this;
  }

  OurForm hrule(){
    //kill hrules until enTouch can handle more text per packet set.
    //    add(new Legend(Margin,nextY(),FullRule,"0"));
    return this;
  }

  OurForm space(int width,int height){
    add(new Spacer(new XRectangle(nextX(),nextY(),width,height)));
    return this;
  }

  boolean isFirst=true;

  OurForm addButton(String legend, int buttontag){
    int xloc=isFirst?Margin:(nextX()+1);
    add(new TextButton(xloc,Y(),legend, buttontag));
    isFirst=false;
    return this;
  }

  /**
   * debit legend must always be adjusted to fit.
   */
  void addDebit(String tracer){
    if(debitAllowed){
      dbg.VERBOSE("debit button: "+tracer);
      space(2,0);
      bigButtonNext(tracer,ButtonTag.DoDebit);
//      addButton(tracer,ButtonTag.DoDebit);
      hasDebitButton=true;
    }
    else {
      dbg.VERBOSE("NO debit on "+tracer);
    }
  }

  OurForm addLegend(String legend,Font f){
    add(new Legend(Margin,nextY(),legend,f));
    return this;
  }

  OurForm addLegend(String legend,String font){
    return addLegend(legend,Font.Create(font));
  }

  OurForm addLegend(String legend){
    return addLegend(legend,DefaultLegendFont);
  }

  OurForm addParagraph(TextList para,Font f){
    //textColumn is too primitive, FormattedLines would make us create a printer...
    int numlines=para.size();
    for(int i=0;i<numlines;i++){
      addLegend(para.itemAt(i),f);//small font
    }
    return this;
  }

  OurForm addParagraph(TextList para){
    return addParagraph(para,Font.Create("0"));
  }

  OurForm addParagraph(String toWrap,String fontname){
    Font f=Font.Create(fontname);
    TextList wrapped=new TextList(toWrap, (f.Width()>1)? bigWidth:textWidth,true);
    return addParagraph(wrapped,f);
  }

  /////////////////////////////////////////////////////////
  final static String PCX= ".pcx";

  OurForm ToService()
//  throws java.io.IOException
  {
//    ServiceTracker.storeService(new ServiceObject(myName,this));//??? use super?
    if(StringX.NonTrivial(pcxResource)){//normalize file naming.
      pcxFile =Main.LocalFile(pcxResource,"pcx");//same place as testpos.properties
      if(!IOX.FileExists(pcxFile)){
        dbg.ERROR("PCX definition: file:"+pcxFile.getAbsolutePath()+" does not exist on path:"+System.getProperty("user.dir"));
      }
    }
    return this;
  }

  public OurForm(POSForm pfnum) {
    super(pfnum.Image(),pfnum.Value()) ;
    dbg.VERBOSE("Making Form "+pfnum.Image());
    /*bgndDefined=*/OurForms.insertGraphic(this);
    OurForms.Register(this);
  }

  public OurForm(int pfnum) {
    this(new POSForm(pfnum));
  }

  public POSForm Id(){
    return new POSForm(myNumber);
  }

  public String toSpam(){
    StringBuffer spam=new StringBuffer(50);
    if(this.hasDebitButton){
      spam.append(" hasDebitButton");
    }

    if(this.idSwipe){
      spam.append(" gets id card");
    }

    if(this.isPinPad){
      spam.append(" is PinPad");
    }

    if(this.showsAmount){
      spam.append(" shows amount");
    }

    return super.toSpam()+String.valueOf(spam);
  }

}
//$Id: OurForm.java,v 1.59 2003/12/08 22:45:42 mattm Exp $
