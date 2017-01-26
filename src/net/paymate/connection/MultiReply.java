package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/MultiReply.java,v $
 * Description:  for server pushes to client, appended to other replies.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import java.util.*;
import net.paymate.util.isEasy;
import net.paymate.util.EasyCursor;

//yes, just using a vector with special easyWrapping import net.paymate.util.ObjectFifo; // overkill?

public class MultiReply extends AdminReply implements isEasy {

  private Vector replies;

  public int getCount(){
    return replies!=null? replies.size():0;
  }

  public ActionReply itemAt(int i){//use should use getcount to avert npe's
    return (ActionReply) replies.elementAt(i);
  }

  public void add(ActionReply reply) {
    if(replies==null){
      replies=new Vector();
    }
    if(reply != null) {
      replies.add(reply);
    }
  }

  public MultiReply() {
    //don't create the internal vector, we may never add anything to it.
  }

  public MultiReply(int presize) {
    replies=new Vector(presize);
  }

  public void save(EasyCursor ezc){
    ezc.setVector(replies);//this works ok even though loadVector doesn't
  }

  public void load(EasyCursor ezc){
    replies=null;//in case we are reusing the object
    //we must figure out type to instantiate. Someday this could be shoved into EasyHelper.
    int i=ezc.getInt("size");
    if(i>0){
      replies=new Vector(i);
      replies.setSize(i);
      while(i-->0){
        ezc.push(Integer.toString(i));
        replies.add(i,ActionReply.fromProperties(ezc));//#preserve order
        ezc.pop();
      }
    }
  }

}
//$Id: MultiReply.java,v 1.3 2003/08/06 16:59:51 andyh Exp $

