/* $Id: CardSwiped.java,v 1.5 2000/06/04 20:37:34 alien Exp $ */
package net.paymate.jpos.Terminal;

import net.paymate.jpos.data.*;

public class CardSwiped implements Event{

  public EventType Type(){
    return new EventType(EventType.CardAcquired);
  }

  MSRData theSwipe;
  public MSRData Value(){
    return new MSRData(theSwipe);
  }

  CardSwiped(MSRData aswipe){
    theSwipe= aswipe;
  }

}
//$Id: CardSwiped.java,v 1.5 2000/06/04 20:37:34 alien Exp $
