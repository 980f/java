package net.paymate.terminalClient.PosSocket;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/ReceiptAggregator.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */
import net.paymate.util.*;
import net.paymate.util.codec.Base64Codec;
import net.paymate.data.*;
import net.paymate.lang.ObjectX;
import net.paymate.awtx.Quadrant;
import net.paymate.awtx.XPoint;
import net.paymate.jpos.awt.*;
import net.paymate.jpos.data.*;


import net.paymate.ivicm.et1K.ncraSignature;
import net.paymate.ivicm.et1K.SignatureType;
import net.paymate.terminalClient.Receipt;

public class ReceiptAggregator {
//holds state for accumulating a receipt
  private TxnReference txnref;
  Receipt rcp;

  String printerModel;
  boolean isPreformatted;
  TextList preformatted;
  FormattedLines body;
/////////////
  int signatureLine;
//  XDimension sigbox;
  Hancock hancock;
  int strokesRemaining;

  ////////////////
  private void recycle(){
    rcp=new Receipt();//avert NPE
    setNoSignature();
    printerModel="";
    isPreformatted=false;
    preformatted=null;
    body=null;
  }

  public ReceiptAggregator setReference(TxnReference txnref){
    if(TxnReference.NonTrivial(this.txnref) && ! this.txnref.equals(txnref)){
      //then we are reusing this rag.
      this.recycle();
    }
    this.txnref= txnref;
    return this;
  }
  public TxnReference TxnReference(){
    return txnref!=null?txnref:TxnReference.New();
  }
  ////////////////
  //
//  public void setSigbox(int width,int height){
//    sigbox= new XDimension(width,height);
//  }

  public void startHancock(int strokes){  //paymate verbose format
    hancock=  new Hancock() ;
    hancock.setQuadrant(Quadrant.First());
    strokesRemaining= strokes;
  }

  public void moreSignature(TextListIterator fields){
    if(strokesRemaining-->0){
      int strokelen=fields.nextInt();
      Stroke stroke= new Stroke();
      while(strokelen-->0){
        XPoint vertex= new XPoint();
        vertex.setLocation(fields.nextInt(),fields.nextInt());
        stroke.addVertex(vertex);
      }
      Hancock().addStroke(stroke);
    }
  }

  public Hancock Hancock(){
    return hancock;
  }

/////////////
  int linesAcquired;
  public boolean expectingSig(){
    return signatureLine!= ObjectX.INVALIDINDEX;
  }

  public boolean sigComplete(){
    return Hancock.NonTrivial(hancock) && strokesRemaining==0;
  }

  public ReceiptAggregator setNoSignature(){
    signatureLine=ObjectX.INVALIDINDEX;
    strokesRemaining=0;
//    sigbox=null;
    hancock=null;
    return this;
  }

  public ReceiptAggregator() {
    recycle();
  }

  public ReceiptAggregator simpleSignature(SignatureType sigType,String base64data){
    hancock=Hancock.Create(SigData.CreateFrom(Base64Codec.fromString(base64data),sigType));//4th quad
    strokesRemaining=0;
    return this;
  }
}
//$Id: ReceiptAggregator.java,v 1.8 2003/12/08 22:45:43 mattm Exp $