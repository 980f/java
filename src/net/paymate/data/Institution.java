package net.paymate.data;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: andyh $
* @version $Id: Institution.java,v 1.1 2001/02/07 06:10:32 andyh Exp $
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

  /**
  * someone else figures out this code belongs with this class,
  * this function is called to make an exact object
  * for instance "card issuer" is discoverd in one place, then the class that
  * represents CardIssuers uses the number to make MC,VISA,AMEX and such objects.
  */

  public Institution setFromIIN(int sixdigitcode);
  //and reflection will be used to create a newInstance and then call setFromIIN...

}
//$Id: Institution.java,v 1.1 2001/02/07 06:10:32 andyh Exp $
