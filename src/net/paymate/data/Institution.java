package net.paymate.data;

/**
* Title:        $Source: /cvs/src/net/paymate/data/Institution.java,v $
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author       Paymate.net
* @version $Id: Institution.java,v 1.2 2001/11/25 16:34:50 andyh Exp $
*/

public interface Institution {
  /**
  * @return text suitable for a cramped display or GUI pulldown box.
  */

  public String Abbreviation();

  /**
  * @return text suitable for a printed report
  */

  public String FullName();

  public InstitutionClass Class();

  /**
  * someone else figures out this code belongs with this class,
  * this function is called to make an exact object
  * for instance "card issuer" is discoverd in one place, then the class that
  * represents CardIssuers uses the number to make MC,VISA,AMEX and such objects.
  */

  public Institution setFromIIN(int sixdigitcode);
  //and reflection will be used to create a newInstance and then call setFromIIN...

}
//$Id: Institution.java,v 1.2 2001/11/25 16:34:50 andyh Exp $
