package net.paymate.jpos.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/FormProblem.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.terminalClient.IviForm.Form; //+_+ will have a base class someday.
public class FormProblem extends Problem { //maybe this is in wrong package...
  Form POSform;
  public FormProblem(String description,Form POSform) {
    super(description);
    this.POSform=POSform;
  }

  public String toString(){
    return super.toString()+" on form:"+POSform.toSpam();
  }

}
//$Id: FormProblem.java,v 1.2 2001/12/20 01:25:45 andyh Exp $