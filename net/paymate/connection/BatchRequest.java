/**
* Title:        BatchRequest
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: BatchRequest.java,v 1.6 2001/07/06 18:56:36 andyh Exp $
*/
package net.paymate.connection;
import net.paymate.util.*;
public class BatchRequest extends AdminRequest implements isEasy  {
  public ActionType Type(){
    return new ActionType(ActionType.batch);
  }

  public boolean isClosing=false;
  static final String isClosingKey="isClosing";

  public BatchRequest(boolean isClosing){
    this.isClosing=isClosing;
  }

  // default required for server-side instantiation
  public BatchRequest(){
    // stub
  }

  public void save(EasyCursor ezp){
    ezp.setBoolean(isClosingKey,isClosing);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    isClosing=ezp.getBoolean(isClosingKey);
    super.load(ezp);
  }

}
//$Id: BatchRequest.java,v 1.6 2001/07/06 18:56:36 andyh Exp $
