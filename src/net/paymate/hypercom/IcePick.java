package net.paymate.hypercom;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/hypercom/IcePick.java,v $</p>
 * <p>Description: describe a set of selections for a customer to pick from</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import java.util.*;
import net.paymate.lang.ObjectX;

class pickItem {
  public String prompt;
  public int retval;//expected to be a terminalClient.ButtonTag ...
  public pickItem(String prompt, int buttonValue){
    this.prompt=prompt;
    this.retval=buttonValue;
  }
}

public class IcePick {
  private Vector /*pairs*/ list;

  final static int justInfo=ObjectX.INVALIDINDEX;
  private IcePick(int guesshowmany) {
    list=new Vector(guesshowmany);
  }

  public static IcePick New(int numItems){
    return new IcePick(numItems);
  }

  public IcePick addItem(String prompt, int buttonValue){
    list.addElement(new pickItem(prompt, buttonValue));
    return this;
  }

  public IcePick addText(String prompt){
    list.addElement(new pickItem(prompt, justInfo));
    return this;
  }

  public IceCommand command(){
    IceCommand cmd=IceCommand.Create(1000);
    cmd.append(IceCommand.FormInput);

    for (Iterator i = list.iterator(); i.hasNext(); ) {
      pickItem item= (pickItem) i.next();
      cmd.append(item.retval +'A');
      cmd.appendFrame(item.prompt);
    }
    return cmd;
  }

}
//$Id: IcePick.java,v 1.4 2003/07/27 05:35:02 mattm Exp $