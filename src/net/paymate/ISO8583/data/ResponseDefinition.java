/* $Id: ResponseDefinition.java,v 1.7 2001/10/13 11:02:27 mattm Exp $ */
package net.paymate.ISO8583.data;

/**
table entry for response decoding

*/
public class ResponseDefinition {
  String rc;
  char ActionCode;  //significance
  String verbose;   //wordy version of signficance

  ResponseDefinition(String rcode, char acode, String wordy){
    rc= rcode;
    ActionCode=acode;
    verbose=wordy;
  }

  public boolean equals(String twochars){
    return rc.equals(twochars);
  }

  public String toString(){
    return rc;
  }

  public String Action () {
    switch (ActionCode){
      default: return "Try Again";//alh addition, probaly our screw up not ntn's
      case 'A': return "Approved";
      case 'D': return "Hard declined";
      case 'L': return "Alert";
      case 'N': return "Network error";
      case 'S': return "Soft declined";
      case 'T': return "Timeout";
    }
  }

  public String actionCode() {
    return "" + ActionCode;
  }

  public String ExtendedDescription(){
    return verbose;
  }

  public String completeDescription(String divider) {
    return rc + divider + ActionCode + divider + verbose;
  }

}
//$Id: ResponseDefinition.java,v 1.7 2001/10/13 11:02:27 mattm Exp $
