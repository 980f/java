/**
* Title:        Form
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: OurForm.java,v 1.42 2001/11/14 01:47:50 andyh Exp $
*/
package net.paymate.terminalClient;

import jpos.ServiceTracker;
import jpos.ServiceObject;

import net.paymate.terminalClient.IviForm.* ;//really need to move this package!
import net.paymate.util.ErrorLogStream;
import net.paymate.util.TextList;
import net.paymate.util.Safe;

import net.paymate.util.Fstring;

import net.paymate.Main;
import net.paymate.util.OS;

import java.io.*;

import java.awt.Point;
import java.awt.Rectangle;

import net.paymate.util.ErrorLogStream;

public class OurForm extends Form {
  static final ErrorLogStream dbg=new ErrorLogStream(OurForm.class.getName());
  public boolean isSwiper     =false;
  public boolean idSwipe      =false;
  public boolean isPinPad     =false;

  public boolean showsAmount  =false;
  public boolean hasSignspot  =false;

  public boolean isStored     =false;
  public TextList altText     =null;

  public final static String CouponText="PRINT COUPON";
  public final static String DefaultLegendFont="1";
  final static String Done=" Ok ";

  final static String licenseid=   "Swipe your ID";//instructions
  public final static String waitclerk="Waiting on Cashier";

  public final static String firstSwipe="Swipe Your Card";
  public final static String reSwipe   ="Swipe Card Again";
  Field  amtSlot=null;
  Field swipeSpot=null;

  //theoretically could fetch some of these numbers from jpos service BUT
  //we need to write forms out before we start the service due to bugs in the service
  final static int textWidth=40;

  public final static int Margin=1;
  public final static int LastColumn=textWidth-Margin;
  final static int WallToWall=LastColumn-Margin;

  public final static int bigWidth=(textWidth-2*Margin)/2;

  public final static int LastRow=29;
  final static int ButtonRow=LastRow-3;

  static final Point amountLocation=  new Point( Margin,4);
  protected Point BannerLocation= new Point(Margin,1);

  protected static final String FullRule=Fstring.fill ("",WallToWall,'-');

//  public final static Rectangle AdBox=new Rectangle(Margin,3,LastColumn-Margin,22);

  static final String ManufacturedBy="Terminal by PayMate.Net\n9420 Research  Blvd.\nAustin TX 78759";

//  public void setAdName(String filename){
//    pcxFilename=filename;
//  }
//
//  public void setAdAlt(String storeInfo){
//    altText=new TextList(Safe.OnTrivial(storeInfo,ManufacturedBy),bigWidth,true);
//  }

  protected OurForm BannerLine(String toAnnounce){
    add(BannerThis(toAnnounce));
    return this;
  }

  String bannerFont="1";
  //need +++ width(font)...
  public static final String ValuePair(String name, String value){
    return Fstring.justified(bigWidth,name,value,' ');
  }

  public static final String Bannerize(String toAnnounce){
    return Fstring.centered(toAnnounce,bigWidth,' ');
  }

  public Legend BannerThis(String toAnnounce){
    return new Legend(BannerLocation,Bannerize(toAnnounce),bannerFont);
  }

  protected OurForm stdCancel(){
    String fartoff="CANCEL";
    add(new TextButton(LastColumn-fartoff.length()-Margin-1,ButtonRow,fartoff,ButtonTag.CustomerCancels));
    return this;
  }

  protected OurForm stdAmount(){
    showsAmount=true;
    amtSlot=new Field(new Legend( amountLocation, Bannerize(waitclerk),"1"));
    add(amtSlot);
    return this;
  }

  //////////////////////////////////////////////////////////////////
  /**@deprecated
  * the underlying feature itself was deprecated, not so much htis function in its own right
  */
  public OurForm switchToCard(){
    addLegend("swipe to use a card instead of a check","0");
    return this;
  }

  public OurForm showalt(boolean allbig){
    if(TextList.NonTrivial(altText)){
      for(int i=0;i<altText.size();i++){
        addLegend(altText.itemAt(i),((!allbig &&i>0)?"0":"1"));
//til enTouch is fixed:
        //if(!allbig)
        break; //entouch can't handle as much text as we would like
      }
    }
    return this;
  }

  public OurForm addCheckInfo(){
    hrule();

    if(TextList.NonTrivial(altText)){
      hrule();
      addLegend("Or Write Check to:","1");
      showalt(true);
    }
    //+_+ get time format from Receipt class.
    addLegend(Fstring.justified(bigWidth,"Date:", Receipt.LocalTime(Safe.Now())),"1");
    hrule();
    stdCancel();
    return this;
  }

  public Legend AmountLegend(String image){
    return (amtSlot!=null)? amtSlot.display(image): new Legend(0,0,"");
  }

  protected OurForm insertSwipe(String prompt){
    isSwiper  =true;
    if(Safe.NonTrivial(prompt)){
      if(prompt.length()<=15){//+++ compute
        prompt="->"+prompt+"<-";
      }
      return BannerLine(prompt);
    } else {
      return this;
    }
  }


  protected OurForm insertCardSwipe(String prompt){
    idSwipe=false;
    return insertSwipe(prompt);
  }

  protected OurForm insertCardSwipe(){
    return insertCardSwipe(firstSwipe);
  }

  protected OurForm insertIdSwipe(){
    idSwipe=true;
    insertSwipe(licenseid);
    space(1,3);
    addLegend("Driver's License");
    addLegend("or State ID card");
    return this;
  }

  protected OurForm insertHideSwipe(){
    idSwipe=true;
    return insertSwipe("");
  }

  protected OurForm insertReSwipe(){
    idSwipe=false;
    return insertSwipe("");
  }

  protected OurForm addRawButton(int tagger){
    Point p=new Point(Margin,nextY());
    ButtonTag button=new ButtonTag(tagger);
    TextButton wtf;
    add(wtf=new TextButton(p,button.Image()+","+button.Value(),button.Value()));
    dbg.VERBOSE("button:"+wtf.xml());
    return this;
  }

  protected static final boolean willFit(String prompt) {
    return prompt.length()<=6;;
  }

  protected OurForm bigButton(String text, int tagger){//one per line...
    boolean inside=willFit(text);
    //added a second +1 below to space things better on screen.
    Point p=new Point(inside?Margin+1:Margin,nextY()+1+1);//room for button margin, else they overlap
    Legend lege=new Legend(p,text,"1");
    if(inside){
      add(new TextButton(lege,tagger));
    } else {
      add(new BigButton(lege,tagger,true));
    }
    return this;
  }

  protected OurForm bigButtonRight(String text, int tagger){//one per line...
    boolean inside=willFit(text);
    int xwidth= text.length()+(inside? 2 : BigButton.buttwidth);
    int why= (xwidth+nextX()>=LastColumn)?(nextY()+1):this.Y();

    Point p=new Point(LastColumn-1,why+1);//room for button margin, else
    Legend lege=new Legend(p,text,"1");
    if(inside){
      add(new TextButton(lege,tagger,false));
    } else {
      add(new BigButton(lege,tagger,false));
    }
    return this;
  }

//  protected OurForm insertAd(boolean withCoupon){
//    //  add(new Button(AdBox,ButtonTag.NullButton));
//    if(Safe.NonTrivial(pcxFilename) ){
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

  public OurForm addSigBox(/* receipt.sigBox and aspectRatio..*/){
    int vertical=24/8; //derive the 24 from data hinted at in comment above
    add(new SigBox(new Rectangle(Margin,nextY(),WallToWall,nextY()+vertical)));
    hasSignspot=true;
    addLegend("Use Pen to press");
    addLegend(Done+" when done--v");
    bigButton("ERASE",ButtonTag.ClearForm);
    bigButtonRight(Done,ButtonTag.Signed);

    return this;
  }

  protected final static String pushpen="Please Use the Pen";

  public OurForm askOkWrong(){
    space(0,1);
    addLegend(pushpen);
    bigButton("OK",ButtonTag.CustomerAmountOk);
    bigButtonRight("WRONG",ButtonTag.CustomerCancels);
    space(0,1);
    addLegend(pushpen);
    return this;
  }

  public OurForm askYesno(String question){
    addLegend(question);
    space(0,1);
    addLegend(pushpen);
    bigButton("YES",ButtonTag.CustomerAmountOk);
    bigButtonRight("NO",ButtonTag.CustomerCancels);
    space(0,1);
    addLegend(pushpen);
    return this;
  }

  public OurForm hrule(){
  //kill hrules until enTouch can handle more text per packet set.
//    add(new Legend(Margin,nextY(),FullRule,"0"));
    return this;
  }

  public OurForm space(int width,int height){
    add(new Spacer(new Rectangle(nextX(),nextY(),width,height)));
    return this;
  }

  boolean isFirst=true;

  public OurForm addButton(String legend, int buttontag){
    int xloc=isFirst?Margin:(nextX()+1);
    add(new TextButton(xloc,ButtonRow,legend, buttontag));
    isFirst=false;
    return this;
  }

  public OurForm addLegend(String legend,Font f){
    add(new Legend(OurForm.Margin,nextY(),legend,f));
    return this;
  }

  public OurForm addLegend(String legend,String font){
    return addLegend(legend,Font.Create(font));
  }

  public OurForm addLegend(String legend){
    return addLegend(legend,DefaultLegendFont);
  }


  public OurForm addParagraph(TextList para,Font f){
    //textColumn is too primitive, FormattedLines would make us create a printer...
    int numlines=para.size();
    for(int i=0;i<numlines;i++){
      addLegend(para.itemAt(i),f);//small font
    }
    return this;
  }
  public OurForm addParagraph(TextList para){
    return addParagraph(para,Font.Create("0"));
  }

  public OurForm addParagraph(String toWrap,String fontname){
    Font f=Font.Create(fontname);
    TextList wrapped=new TextList(toWrap, (f.Width()>1)? bigWidth:textWidth,true);
    return addParagraph(wrapped,f);
  }

  /////////////////////////////////////////////////////////
  protected final static String PCX= ".pcx";

  public OurForm ToService() throws java.io.IOException {
    ServiceTracker.storeService(new ServiceObject(myName,this));//??? use super?
    if(Safe.NonTrivial(pcxResource)){//normalize file naming.
      if(!pcxResource.endsWith(PCX)){//must have this extension, legacy
        pcxResource+=PCX;
      }
      //+_+ try using Main.localFile();
      File pcxFile =new File(System.getProperty("user.dir")+ File.separatorChar+pcxResource);
      //had to be that explicit coz of bugs in JAVA! it couldn't find the freaking file (C: problem)
      if(Safe.FileExists(pcxFile)){
        ServiceTracker.storeService(new ServiceObject(pcxResource,pcxFile));
      } else {
        dbg.ERROR("PCX definition: file:"+pcxFile.getAbsolutePath()+" does not exist on path:"+System.getProperty("user.dir"));
      }
    }
    return this;
  }

  public OurForm(POSForm pfnum) {
    super(pfnum.Image(),pfnum.Value()) ;
    dbg.VERBOSE("Making Form "+pfnum.Image());
    //load graphic
    if(OurForms.insertGraphic(this)){
      dbg.VERBOSE(pfnum.Image()+"graphic:"+pcxResource);
    } else {
      dbg.VERBOSE("no graphic for "+pfnum.Image());
    }
    OurForms.Register(this);
  }

  public OurForm(int pfnum) {
    this(new POSForm(pfnum));
  }

  public POSForm Id(){
    return new POSForm(myNumber);
  }

}
//$Id: OurForm.java,v 1.42 2001/11/14 01:47:50 andyh Exp $
