package net.paymate.hypercom;
/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IceSwipe.java,v $
 * Description:  Ice terminal cardswipe control
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */
import net.paymate.util.*;
import net.paymate.peripheral.*;
import net.paymate.jpos.data.MSRData;
import net.paymate.data.*;

public class IceSwipe extends CardSwipe {
  IceTerminal parent;

  public PosPeripheral setEnable(boolean beon){
    parent.sendEnableCommand(IceCommand.Swiper,beon);
    return this;
  }

  public MSRData swipeFrom(AsciiBufferParser bp){
    MSRData card=new MSRData();
    while(bp.remaining()>0){
      char trackindicator=bp.getChar();
      switch (trackindicator){
        case 'A':{
          card.setTrack(card.T1,bp.getROF());
          card.ParseTrack1();
        } break;
        case 'B':{
          card.setTrack(card.T2,bp.getROF());
          card.ParseTrack2();
        } break;
        case 'a':{
          card.addError("T[1].error:"+bp.getROF());
        } break;
        case 'b':{
          card.addError("T[2].error:"+bp.getROF());
        } break;
      }
    }
    return card;
  }

  public IceSwipe(IceTerminal parent) {
    super(parent);//IceTerminal wraps the posterminal to ease order of construction
    this.parent=parent;
  }
}
//$Id: IceSwipe.java,v 1.5 2003/01/22 18:48:44 andyh Exp $