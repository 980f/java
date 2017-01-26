package net.paymate.net;

/**
 * <p>Title: $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/net/backgroundSocketOpener.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class backgroundSocketOpener implements QActor {
  private QAgent socketCreationAgent;
  private boolean stopped;

  ErrorLogStream dbg;

  public backgroundSocketOpener(ErrorLogStream dbg, String morename) {
    this.dbg = dbg;
    socketCreationAgent = QAgent.New("bgSocketOpener" +
                                     (StringX.NonTrivial(morename) ? morename : ""),
                                     this);
    socketCreationAgent.config(dbg);//squelches 'about to runone' spew.
    socketCreationAgent.Start();
  }

  private synchronized String sizeMessage(String prefix){
    return prefix +" another SocketRequest with " + socketCreationAgent.Size() + " already in the queue.";
  }

  public void Post(SocketRequest arf) {
    dbg.WARNING(sizeMessage("post() adding"));
    socketCreationAgent.Post(arf);
  }

  public void runone(Object fromq) {
    dbg.WARNING(sizeMessage("runone() processing"));
    if(!stopped) {
      SocketRequest request=(SocketRequest)fromq;
      request.go();
      //expedite purging of abandoned's
//      socketCreationAgent.removeAny(request);// @see equals function for this class
      //previous failure to use a SocketRequest above left all the expired ones in the queue.
      //which shouldn't have taken much time to deal with.
    }
  }

  public void Stop() {
    stopped = true;
  }

  public void stopAgent() {
    socketCreationAgent.Stop();
  }

}

//$Id: backgroundSocketOpener.java,v 1.6 2005/03/02 05:23:07 andyh Exp $
