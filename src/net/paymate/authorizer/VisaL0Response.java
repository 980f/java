package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/VisaL0Response.java,v $
 * Description:  holds data received from an authorizer
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;

public class VisaL0Response {

  public String respcode="  ";
  public String authcode="      ";
  public String authmsg="AUTH INCOMPLETE!"; //explanation of respcode!
  public String authRefNumber="";
//everything else is unused by us and not implemented by this class.

  public String toString() {
    return "respcode="+respcode+", authcode="+authcode+", authmsg="+authmsg+", authRefNumber="+authRefNumber+".";
  }

  public static VisaL0Response newFrom(VisaBuffer unwrapped) {
    VisaL0Response newone= new VisaL0Response();
    newone.parse(unwrapped);
    return newone;
  }

  private VisaL0Response(){
  //see initializers.
  }


 /**
 * @param vb must already be parsed up to and including the "L0."
 */

  void parse(VisaBuffer vb){
    String CPSBS=vb.getFixed(1);       //'E' CPS bullshit is entirely not defined.
    String authstoreid=vb.getFixed(8); // first 8 of authtermid, which we know.
    String authsource= vb.getFixed(1); //'5' we don't care...
    int seqnum= Safe.parseInt(vb.getFixed(4)); //short sequence number
    /*String*/ respcode= vb.getFixed(2); //The response code!
    /*String*/ authcode= vb.getFixed(6); //The authorization blurb
    String yymmddhhmmss=vb.getFixed(12);  //date in unknown timezone
    authmsg=  vb.getFixed(16); //conveniently the size of our clerk display
    String avrcode = vb.getFixed(1);
    authRefNumber=  vb.getFixed(12);
    String msdataid= vb.getFixed(1);
    //
    String moreCPSbs= vb.getROF();
  }

}
/*
Response Format (Visa "L")
## FieldName               DataFormat DataLength Section
 1 Response Format                  A 1          5.1
 2 Application Type                 A 1          5.2
 3 Message Delimiter                A 1          5.3
 4 Returned ACI                     A 1          5.4
 5 Terminal Number                  N 8          5.5
 6 Auth. Source Code                A 1          5.6
 7 Tran. Sequence Number            N 4          5.7
 8 Response Code                    A 2          5.8
 9 Approval Code                    A 6          5.9
10 Local Transaction Date           N 6          5.10
11 Local Transaction Time           N 6          5.11
12 Auth Response Message            A 16         5.12
13 Address Verification Result Code A 1          5.13
14 Retrieval Reference Number       N 12         5.14
15 Market Specific Data ID          A 1          5.15
16 Transaction Identifier           A 0 or 15    5.16
17 Field Separator                  A 1          5.17
18 Validation Code                  X 0 or 4     5.18
19 Field Separator                  A 1          5.17
20 Free-Form Message Data           A 0-120      5.19
*/
//$Id: VisaL0Response.java,v 1.5 2001/10/13 11:02:28 mattm Exp $