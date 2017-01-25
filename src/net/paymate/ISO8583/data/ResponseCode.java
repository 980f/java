/**
* Title:        ResponseCode.java
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ResponseCode.java,v 1.18 2001/10/15 22:39:44 andyh Exp $
*/
package net.paymate.ISO8583.data;
/**
upon receipt of the two character response code we look up its
significance
*/

public class ResponseCode {

  protected ResponseDefinition rd;

  public boolean isApproved(){
    return rd.ActionCode=='A';//@MS@ misses the wierd ones like "10"
  }

  public boolean equals(String twochars){
    return rd.equals(twochars);
  }

  public String toString(){
    return rd.toString();
  }

  public String actionCode() {
    return rd.actionCode();
  }

  public String Action () {
    return rd.Action();
  }

  public String ExtendedDescription(){
    return rd.ExtendedDescription();
  }

  public String completeDescription(String divider){
    return rd.completeDescription(divider);
  }

  public ResponseCode(String twochars){
    for(int i=0;i<legalOnes.length;i++){//must forward iterate
      rd=legalOnes[i];
      if(rd.equals(twochars)){
        return;
      }
    }
    rd=legalOnes[0];//!!! someone added this line to give OK when bad input is given to the constructor!!!
    //rd==last entry in the table which is rigged to never match
    // and to indicate that it is bogus.
  }

  //lazy so we just make the string not a code.
  public final static String AuthorizerDown="91";

  protected static final ResponseDefinition legalOnes[]={
  //00 must stay as the first entry.
    new  ResponseDefinition("00", 'A', "Approved"), //or completed successfully
    new  ResponseDefinition("01", 'D', "Refer to card issuer"),
    new  ResponseDefinition("02", 'L', "Card issuer's special condition"),
    new  ResponseDefinition("03", 'S', "Invalid merchant"),
    new  ResponseDefinition("04", 'D', "Pick up card"),
    new  ResponseDefinition("05", 'D', "Do not honor"),
    new  ResponseDefinition("06", 'D', "Error"),
    new  ResponseDefinition("08", 'S', "Honor with identification"),
    new  ResponseDefinition("10", 'A', "Approved for partial amount"),
    new  ResponseDefinition("12", 'D', "Invalid transaction"),
    new  ResponseDefinition("13", 'S', "Invalid amount"),
    new  ResponseDefinition("14", 'D', "Invalid card number"),
    new  ResponseDefinition("15", 'S', "No such issuer"),
    new  ResponseDefinition("18", 'D', "Resubmit As Debit"),
    new  ResponseDefinition("19", 'S', "Re-enter transaction"),
    new  ResponseDefinition("20", 'S', "Invalid response"),
    new  ResponseDefinition("21", 'D', "Invalid capture flag"),
    new  ResponseDefinition("22", 'D', "Key sync error"),
    new  ResponseDefinition("23", 'D', "Terminal rec error"),
    new  ResponseDefinition("25", 'S', "Unable to locate record"),
    new  ResponseDefinition("30", 'D', "Format error"),
    new  ResponseDefinition("32", 'D', "Batch release error"),
    new  ResponseDefinition("35", 'S', "Contact acquirer"),
    new  ResponseDefinition("36", 'D', "Authorizer error"),
    new  ResponseDefinition("38", 'D', "Allowable PIN retries exceeded"),
    new  ResponseDefinition("41", 'D', "Lost card"),
    new  ResponseDefinition("43", 'D', "Stolen card"),
    new  ResponseDefinition("44", 'D', "DL in Customer ID file"),
    new  ResponseDefinition("45", 'D', "SS in Customer ID file"),
    new  ResponseDefinition("46", 'D', "DL in Negative ID file"),
    new  ResponseDefinition("47", 'D', "SS in Negative ID file"),
    new  ResponseDefinition("51", 'S', "Not sufficient funds"),
    new  ResponseDefinition("52", 'D', "No checking account"),
    new  ResponseDefinition("53", 'D', "No savings account"),
    new  ResponseDefinition("54", 'S', "Expired card"),
    new  ResponseDefinition("55", 'S', "Retry - invalid PIN"),
    new  ResponseDefinition("56", 'S', "No card record"),
    new  ResponseDefinition("57", 'S', "Txn not permitted to card holder"),
    new  ResponseDefinition("58", 'S', "Txn not permitted to terminal"),
    new  ResponseDefinition("59", 'D', "Suspect fraud"),
    new  ResponseDefinition("61", 'D', "Exceeds store or paytype amount limit"),
    new  ResponseDefinition("62", 'D', "Restricted card"),
    new  ResponseDefinition("65", 'S', "Exceeds withdrawal frequency limit"),
    new  ResponseDefinition("68", 'D', "Response received too late"),
    new  ResponseDefinition("80", 'D', "Format error"),
    new  ResponseDefinition("81", 'D', "Expired card"),
    new  ResponseDefinition("82", 'D', "Invalid STAN"),
    new  ResponseDefinition("83", 'A', "Transaction has been voided"),
    new  ResponseDefinition("90", 'D', "Cutoff in progress"),
    new  ResponseDefinition("91", 'N', "Issuer/switch inoperative"),
    new  ResponseDefinition("92", 'S', "Bank not supported"),
    new  ResponseDefinition("93", 'D', "Transaction cannot be completed"),
    new  ResponseDefinition("94", 'D', "Duplicate transmission"),
    new  ResponseDefinition("95", 'D', "Reconcile error"),
    new  ResponseDefinition("96", 'D', "System malfunction"),
    new  ResponseDefinition("D1", 'A', "Incorrect Phone Number"),
    new  ResponseDefinition("D2", 'A', "Moved Out of Area"),
    new  ResponseDefinition("D3", 'A', "Wrong Address"),
    new  ResponseDefinition("D4", 'A', "First Time Bad Check"),
    new  ResponseDefinition("D5", 'A', "Social Security Number Wrong"),
    new  ResponseDefinition("D6", 'A', "Drivers License Number Wrong"),
    new  ResponseDefinition("D7", 'A', "Needs Application"),
    new  ResponseDefinition("D8", 'D', "Deceased"),
    new  ResponseDefinition("DS", 'D', "Invalid Card"),
    new  ResponseDefinition("DT", 'D', "Application Rejected"),
    new  ResponseDefinition("DU", 'D', "Reissue - Invalid Address"),
    new  ResponseDefinition("ED", 'D', "Permanent Hold Return Checks"),
    new  ResponseDefinition("EE", 'D', "Hold Card - Call Manager"),
    new  ResponseDefinition("EF", 'D', "Lost/Stolen"),
    new  ResponseDefinition("M0", 'S', "Original transaction not found"),
    new  ResponseDefinition("M1", 'D', "Invalid check type for member"),
    new  ResponseDefinition("M2", 'D', "Invalid original transaction"),
    new  ResponseDefinition("M3", 'D', "Required ID not presented"),
    new  ResponseDefinition("M4", 'D', "ID not already on file"),
    new  ResponseDefinition("MC", 'D', "Multiple customers found"),
    new  ResponseDefinition("MD", 'D', "Multiple declines in transaction"),
    new  ResponseDefinition("N0", 'T', "Host timeout response"),
    new  ResponseDefinition("N1", 'S', "Transaction not reversible"),
    new  ResponseDefinition("N2", 'D', "Obtain voice authorization"),
    new  ResponseDefinition("SU", 'D', "Switch unavailable"),
    new  ResponseDefinition("U0", 'L', "One-time check amount exceeded (alert)"),
    new  ResponseDefinition("U1", 'D', "One-time check amount exceeded (decline)"),
    new  ResponseDefinition("U2", 'L', "One-time cashback exceeded (alert)"),
    new  ResponseDefinition("U3", 'D', "One-time cashback exceeded (decline)"),
    new  ResponseDefinition("U4", 'L', "Number of checks per day exceeded (alert)"),
    new  ResponseDefinition("U5", 'D', "Number of checks per day exceeded (decline)"),
    new  ResponseDefinition("U6", 'L', "Total check amount per day exceeded (alert)"),
    new  ResponseDefinition("U7", 'D', "Total check amount per day exceeded (decline)"),
    new  ResponseDefinition("U8", 'L', "Total cashback per day exceeded (alert)"),
    new  ResponseDefinition("U9", 'D', "Total cashback per day exceeded (decline)"),
    new  ResponseDefinition("UA", 'L', "Number of stores per day exceeded (alert)"),
    new  ResponseDefinition("UB", 'D', "Number of stores per day exceeded (decline)"),
    new  ResponseDefinition("UC", 'L', "Number of checks during period exceeded (alert)"),
    new  ResponseDefinition("UD", 'D', "Number of checks during period exceeded (decline)"),
    new  ResponseDefinition("UE", 'L', "Total check amount during period exceeded (alert)"),
    new  ResponseDefinition("UF", 'D', "Total check amount during period exceeded (decline)"),
    new  ResponseDefinition("UG", 'L', "Total cashback during period exceeded (alert)"),
    new  ResponseDefinition("UH", 'D', "Total cashback during period exceeded (decline)"),
    new  ResponseDefinition("UI", 'L', "Number of stores during period exceeded (alert)"),
    new  ResponseDefinition("UJ", 'D', "Number of stores during period exceeded (decline)"),
    new  ResponseDefinition("UK", 'L', "Number of bad checks exceeded (alert)"),
    new  ResponseDefinition("UL", 'D', "Number of bad checks exceeded (decline)"),
    new  ResponseDefinition("UM", 'D', "No cashback with bad checks"),
    new  ResponseDefinition("Y1", 'D', "GC account already exists"),
    new  ResponseDefinition("Y2", 'D', "GC account does not exist"),
    new  ResponseDefinition("Y3", 'D', "GC account is not active"),
    new  ResponseDefinition("Y4", 'D', "GC account has expired date"),
    new  ResponseDefinition("Y5", 'D', "Invalid vendor code"),
    new  ResponseDefinition("Y6", 'D', "GC transaction amount exceeded"),
    new  ResponseDefinition("Y7", 'D', "Single-use-only certificate"),
    new  ResponseDefinition("Z0", 'D', "Problem with ACH member's account"),
    new  ResponseDefinition("Z1", 'D', "Customer's ACH account has been deactivated"),
    new  ResponseDefinition("Z2", 'D', "Customer does not have an ACH member PIN"),
    new  ResponseDefinition("NOT",'X', "Totally Bogus Response")

  };//end responseDefinition table
  //
}//$Id: ResponseCode.java,v 1.18 2001/10/15 22:39:44 andyh Exp $
