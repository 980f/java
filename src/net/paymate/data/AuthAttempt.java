package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/AuthAttempt.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.EasyUrlString;

public class AuthAttempt {

  public AuthAttempt() {
  }

  public AuthattemptId id = new AuthattemptId();
  public Txnid txnid = new Txnid(); // this may or may not be right;  check your code! (awaiting fix during SS2]
  public Authid authid = new Authid(); // this may or may not be right;  check your code! (awaiting fix during SS2]
  public Terminalid terminalid = new Terminalid(); // this may or may not be right;  check your code! (awaiting fix during SS2]
  // final means no more constant checking for nulls ...
  public final EasyUrlString authrequest=new EasyUrlString();
  public final EasyUrlString authresponse = new EasyUrlString();

  public boolean hasAuthRequest() {
    return EasyUrlString.NonTrivial(authrequest);
  }

  public byte [ ] getAuthRequest() {
    return authrequest.rawValue().getBytes();
  }

  public String getEncodedAuthRequest() {
    return authrequest.encodedValue();
  }

  public void setAuthRequest(byte []raw) {
    authrequest.setrawto(raw);
  }


  public boolean hasAuthResponse() {
    return EasyUrlString.NonTrivial(authresponse);
  }

  public void setAuthResponse(byte []raw) {
    authresponse.setrawto(raw);
  }

  public void setEncodedAuthResponse(String resp) {
    authresponse.setencodedto(resp);
  }

  public byte [ ] getAuthResponse() {
    return authresponse.rawValue().getBytes();
  }

  public String getEncodedAuthResponse() {
    return authresponse.encodedValue();
  }

}