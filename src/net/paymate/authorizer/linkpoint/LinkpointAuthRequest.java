package net.paymate.authorizer.linkpoint;
//no license for clearcommerce, module gutted.

import net.paymate.authorizer.*; // AuthRequest
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.*; // ErrorLogStream
import net.paymate.net.*; // IPSpec
import net.paymate.data.*; // MerchantInfo
//import clearcommerce.ssl.*;
import net.paymate.lang.StringX;
import net.paymate.jpos.data.MSRData;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LinkpointAuthRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @@author PayMate.net
 * @@version $Revision: 1.24 $
 */

public class LinkpointAuthRequest extends AuthRequest implements LPConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LinkpointAuthRequest.class, ErrorLogStream.VERBOSE);

  private final VisaBuffer makeRequest() {
    return null;
  }

//  private LinkpointAuthorizer lpa = null;
  private boolean live = false;
  private String path2files = "";
  private String keyfilebase = "";
  private String certfilebase = "";

  public LinkpointAuthRequest(/*LinkpointAuthorizer lpa, */boolean live, String path2files, String keyfilebase, String certfilebase) {
//    this.lpa = lpa;
    this.live = live;
    this.path2files = path2files;
    this.keyfilebase = keyfilebase;
    this.certfilebase = certfilebase;
  }

  protected LinkpointAuthRequest(LinkpointAuthorizer lpa) {
    // legacy until we rid ourselves of the XML version or vice-versa
  }

  public byte [] toBytes() {
    return null;
  }

  protected int maxRequestSize() {
    return 0;
  }

  public int compareTo(Object o) {
    // nothing special to compare to. let the super handle it
    return 0;
  }

//  private static final int ordertype(TransferType tt) {
//    int toadd = JRequest.DECLINE;
//    switch(tt.Value()) {
//      case TransferType.Sale: {
//        toadd = JRequest.SALE;
//      } break;
//      case TransferType.Return: {
//        toadd = JRequest.CREDIT;
//      } break;
//      case TransferType.Reversal: {
//        toadd = JRequest.VOIDSALE;
//      } break;
//    }
//    return toadd;
//  }

//  public JRequest req   = new JRequest();
//  public JOrder   order = new JOrder();
  private String keyfilepath = "";
  private String certfilepath = "";

  public AuthRequest fromRequest(TxnRow tjr, TxnRow original, MerchantInfo merch) {
    TransferType tt = tjr.transfertype();
    boolean isReversal = tt.is(TransferType.Reversal);
    dbg.VERBOSE("path2files="+path2files+", keyfilepath="+keyfilepath+", certfilepath="+certfilepath);
    keyfilepath = path2files+merch.authmerchid+keyfilebase;
    certfilepath = path2files+merch.authmerchid+certfilebase;
//    // this is where the thing is generated, for the most part
//    req.setResult(live ? JRequest.AUTH : JRequest.GOOD);
//    // track data overrides card number/exp if the motoflag is RETAIL_TRANSACTION
//    req.setChargeType(ordertype(tt));
//    if(!isReversal) {
//      MSRData card = tjr.card();
//      req.setCardNumber(card.accountNumber.Image());
//      String exp = card.expirationDate.YYmm();
//      req.setExpMonth(StringX.subString(exp, 2, 4));
//      req.setExpYear (StringX.subString(exp, 0, 2));
//      order.setTrack(StringX.TrivialDefault(card.track(MSRData.T2).Data()));
//      double value = tjr.rawAuthAmount().Value() / 100.00;
//      req.setChargeTotal(value);
//      order.setSubtotal(value);
//      order.setMotoFlag(isReversal ? JOrder.RETAIL_TRANSACTION : JOrder.RETAIL_TRANSACTION);
//    }
//    order.setOid(isReversal ? original.txnid().toString() : tjr.txnid().toString());
//    // set these later in the module that does the writing?
//    req.setConfigFile(merch.authmerchid); // eg: 800001
//    req.setKeyfile(keyfilepath);
//    req.setCertfile(certfilepath);
    // these are set in the LPAuthSocketAgent before sending
    //    req.setHost(host);
    //    req.setPort(port);
    dbg.VERBOSE("Request="+toSpam());
    return this;
  }

  public String toString() {
    return toSpam();
  }

  private static final void addLine(boolean request, String name, String value, StringBuffer buffer) {
    buffer.append("\n"+(request?"req":"order")+"."+name+"=["+value+"]");
  }

//  public static final String TOSPAM(JRequest req, JOrder order) {
//    StringBuffer buffer = new StringBuffer();
//    boolean request = true;
//    boolean isorder = !request;
//    buffer.append("\nREQUEST:");
//    if(req != null) {
//      addLine(request, "getAddr", req.getAddr(), buffer);
//      addLine(request, "getCardNumber", req.getCardNumber(), buffer);
//      addLine(request, "getCertfile", req.getCertfile(), buffer);
//      addLine(request, "getChargeTotal", String.valueOf(req.getChargeTotal()), buffer);
//      addLine(request, "getChargeType", String.valueOf(req.getChargeType()), buffer);
//      addLine(request, "getConfigFile", req.getConfigFile(), buffer);
//      addLine(request, "getEmail", req.getEmail(), buffer);
//      addLine(request, "getExpMonth", req.getExpMonth(), buffer);
//      addLine(request, "getExpYear", req.getExpYear(), buffer);
//      addLine(request, "getHost", req.getHost(), buffer);
//      addLine(request, "getIp", req.getIp(), buffer);
//      addLine(request, "getKeyfile", req.getKeyfile(), buffer);
//      addLine(request, "getPONumber", req.getPONumber(), buffer);
//      addLine(request, "getPort", String.valueOf(req.getPort()), buffer);
//      addLine(request, "getReferenceNumber", req.getReferenceNumber(), buffer);
//      addLine(request, "getResult", String.valueOf(req.getResult()), buffer);
//      addLine(request, "getSwitchIssueNumber", req.getSwitchIssueNumber(), buffer);
//      addLine(request, "getSwitchStartDate", req.getSwitchStartDate(), buffer);
//      addLine(request, "getTaxExmpt", String.valueOf(req.getTaxExmpt()), buffer);
//      addLine(request, "getZip", req.getZip(), buffer);
//    } else {
//      buffer.append("NULL");
//    }
//    buffer.append("\nORDER:");
//    if(order != null) {
//      addLine(isorder, "getBaddr1", order.getBaddr1(), buffer);
//      addLine(isorder, "getBaddr2", order.getBaddr2(), buffer);
//      addLine(isorder, "getBcity", order.getBcity(), buffer);
//      addLine(isorder, "getBcompany", order.getBcompany(), buffer);
//      addLine(isorder, "getBcountry", order.getBcountry(), buffer);
//      addLine(isorder, "getBname", order.getBname(), buffer);
//      addLine(isorder, "getBstate", order.getBstate(), buffer);
//      addLine(isorder, "getBzip", order.getBzip(), buffer);
//      addLine(isorder, "getCVMIndicator", order.getCVMIndicator(), buffer);
//      addLine(isorder, "getCVMValue", order.getCVMValue(), buffer);
//      addLine(isorder, "getComments", order.getComments(), buffer);
//      addLine(isorder, "getFax", order.getFax(), buffer);
//      addLine(isorder, "getItemcount", String.valueOf(order.getItemcount()), buffer);
//      addLine(isorder, "getMotoFlag", order.getMotoFlag(), buffer);
//      addLine(isorder, "getOid", order.getOid(), buffer);
//      addLine(isorder, "getPhone", order.getPhone(), buffer);
//      addLine(isorder, "getRecurringFlag", order.getRecurringFlag(), buffer);
//      addLine(isorder, "getRefer", order.getRefer(), buffer);
//      addLine(isorder, "getSaddr1", order.getSaddr1(), buffer);
//      addLine(isorder, "getSaddr2", order.getSaddr2(), buffer);
//      addLine(isorder, "getScity", order.getScity(), buffer);
//      addLine(isorder, "getScountry", order.getScountry(), buffer);
//      addLine(isorder, "getShipping", String.valueOf(order.getShipping()), buffer);
//      addLine(isorder, "getShiptype", order.getShiptype(), buffer);
//      addLine(isorder, "getSname", order.getSname(), buffer);
//      addLine(isorder, "getSstate", order.getSstate(), buffer);
//      addLine(isorder, "getSubtotal", String.valueOf(order.getSubtotal()), buffer);
//      addLine(isorder, "getSzip", order.getSzip(), buffer);
//      addLine(isorder, "getTDate", String.valueOf(order.getTDate()), buffer);
//      addLine(isorder, "getTax", String.valueOf(order.getTax()), buffer);
//      addLine(isorder, "getTerminalType", order.getTerminalType(), buffer);
//      addLine(isorder, "getTrack", order.getTrack(), buffer);
//      addLine(isorder, "getUserid", order.getUserid(), buffer);
//      addLine(isorder, "getVattax", String.valueOf(order.getVattax()), buffer);
//    } else {
//      buffer.append("NULL");
//    }
//    return buffer.toString();
//  }
//
//  public String toSpam(){
//    return TOSPAM(req, order);
//  }
}
