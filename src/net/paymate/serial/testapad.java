package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/testapad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.awtx.*;
import net.paymate.lang.ContentType;
import net.paymate.ivicm.SerialDevice;

public class testapad implements AnswerListener {
  DisplayPad uut;

  public boolean onReply(Question beingAsked, int opcode){
    System.out.print("got "+opcode+" on ");
    System.out.println(beingAsked.toSpam());
    Question q;
    int nextq=beingAsked.inandout.asInt()%10;
    System.out.println("nextq:"+nextq);

    switch (nextq) {
      case ContentType.money: q= Question.Ask(1,"enter money",new MoneyValue()); break;
      case ContentType.hex: q= Question.Ask(2,"pick item",new EnumValue(new ContentType())); break;
      case ContentType.alphanum: q= Question.Ask(3,"text input",new TextValue()); break;
      default: q= Question.Ask(0,"enter a number",new DecimalValue(54321)); break;
    }
    uut.ask(q);
    return true;
  }
/*
  public final static int CANCELLED=-1; //escaped or such
  public final static int HELPME   = 0; //tool tip desired
  public final static int SUBMITTED= 1; //enter w/ change
  public final static int ACCEPTED = 2; //enter on default
*/


  public testapad (DisplayPad uut){
    this.uut=uut;
    uut.attachTo(this);
    uut.ask(Question.Ask(0,"pick menu item",new EnumValue(new ContentType())));
  }

}
//$Id: testapad.java,v 1.5 2003/07/27 05:35:14 mattm Exp $