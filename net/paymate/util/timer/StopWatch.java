/**
 * Title:        $Id: StopWatch.java,v 1.10 2001/11/14 01:47:58 andyh Exp $<p>
 * Description:  null<p>
 * Copyright:    null<p>
 * Company:      PayMate.net<p>
 * @author PayMate.net
 * @version $Version$
 * Synch's were removed as being gratuitous protection against ridiculous activity.
 * If a particular instance is CONTROLLED by different threads then behavior is
 * already impossible to make any good sense out of. The only remaining reason to
 * Synch is to ensure that "long" is atomic. That can be done by multiple reads at
 * far less expense than locks
 * Synch'ing is only needed for non-atomic data that might get written while it is being read.
 * @todo: add registry so that we can do the same adjustments done by Alarmer.
 */

package net.paymate.util.timer;
import  net.paymate.util.*;

public class StopWatch {
  long started;
  long stopped;
  boolean running;

  public boolean isRunning(){
    return running; //is atomic so no synch needed.
  }

  public long startedAt(){
    return started;
  }

  public double seconds(){ //can be read while running
    return ((double)millis())/Ticks.perSecond;
  }

  public long millis(){ //can be read while running
    return (running ? System.currentTimeMillis() : stopped) -started;
  }

  public void Start(){
    stopped=started=System.currentTimeMillis();
    running=true;
  }

  public long Stop(){
    if(running){
      stopped=System.currentTimeMillis();
      running=false;
    }
    return millis();
  }

  public void Reset(){
    running=false;
    stopped=started=0;
  }

  public StopWatch(boolean hitTheFloorRunning) {
    Reset();
    if (hitTheFloorRunning) {
      Start();
    }
  }

  public StopWatch() {
    this(true);
  }
}
//$Id: StopWatch.java,v 1.10 2001/11/14 01:47:58 andyh Exp $
