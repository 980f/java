package net.paymate.terminalClient;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: mattm $
* @version $Id: ReceiptFormat.java,v 1.13 2003/07/27 05:35:16 mattm Exp $
*/
import net.paymate.util.*;
import net.paymate.Main;
import net.paymate.lang.StringX;

public class ReceiptFormat implements isEasy {
public final static String DefaultTimeFormat="MM/dd/yy hh:mma";

  public String Header;
  public String Tagline;
  public String TimeFormat;
  public boolean showSignature;
  public String abide;

  final static String HeaderKey     ="Header";
  final static String TaglineKey    ="Tagline";
  final static String TimeFormatKey ="TimeFormat";
  final static String showSignatureKey= "showSignature";
  final static String abideKey      ="abide";


  static ReceiptFormat Default;
  ////////////////////////////////////////////
  public void save(EasyCursor ezp){
    ezp.setString(HeaderKey    ,Header  );
    ezp.setString(TaglineKey    ,Tagline );
    ezp.setString(TimeFormatKey,TimeFormat);
    ezp.setBoolean(showSignatureKey,showSignature);
    ezp.setString(abideKey,abide);
  }

  private static boolean inited = false;
  private Monitor receiptFormatlassMonitor = new Monitor("ReceiptFormat.class");
  public void load(EasyCursor ezp){
    // this synchronized version seems to work much better for some reason.
    try {
      receiptFormatlassMonitor.getMonitor();
      if(!inited){
        inited = true;
        Default=new ReceiptFormat();
        Default.load(Main.props("Receipt"));
      }
    } finally {
      receiptFormatlassMonitor.freeMonitor();
      Header  =   StringX.unescapeAll(ezp.getString(HeaderKey,Default.Header));
      Tagline =   StringX.unescapeAll(ezp.getString(TaglineKey,Default.Tagline));
      TimeFormat= StringX.unescapeAll(ezp.getString(TimeFormatKey,Default.TimeFormat));
      showSignature= ezp.getBoolean(showSignatureKey,Default.showSignature);
      abide=      ezp.getString(abideKey,Default.abide);
    }
  }

  public ReceiptFormat() {//internal defaults
    Header="Receipt Header\nMultiple lines allowed";
    Tagline="Receipt Tagline\nMultiple lines allowed";
    TimeFormat=DefaultTimeFormat;
    showSignature=false;
    abide="I agree to pay per my cardholder agreement";
  }

  public boolean equals(ReceiptFormat newone){
    return
    Header.equals(newone.Header) &&
    Tagline.equals(newone.Tagline)&&
    TimeFormat.equals(newone.TimeFormat)&&
    showSignature==showSignature&&
    abide.equals(newone.abide)
    ;
  }

}
//$Id: ReceiptFormat.java,v 1.13 2003/07/27 05:35:16 mattm Exp $
