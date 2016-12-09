/**
 * Title:        SessionTermParams<p>
 * Description:  HttpSession Termination Parameters (when to kill it) <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SessionTermParams.java,v 1.2 2001/07/19 01:06:52 mattm Exp $
 */

package net.paymate.servlet;

// until a better method comes along <sigh>

// "<1" for time means don't do it at all
// so, "don't do it at all" takes precedent over doit
// and a shorter time takes precedent over a longer time

public class SessionTermParams {

  public static final String sessionTermParamsKey = SessionTermParams.class.getName();

  public long maxAgeMillis = -1;
  public long maxUnaccessedMillis = -1;

  public SessionTermParams(SessionTermParams copy) {
    this(copy.maxAgeMillis, copy.maxUnaccessedMillis);
  }

  public SessionTermParams(long maxAgeMillis, long maxUnaccessedMillis) {
    this.maxAgeMillis = maxAgeMillis;
    this.maxUnaccessedMillis = maxUnaccessedMillis;
  }

  public SessionTermParams() {
  }

  public SessionTermParams mergeNew(SessionTermParams other) {
    SessionTermParams baby = new SessionTermParams();
    merge(this, other, baby);
    return baby;
  }

  public SessionTermParams mergeInto(SessionTermParams other) {
    merge(this, other, other);
    return other;
  }

  public SessionTermParams mergeFrom(SessionTermParams other) {
    merge(this, other, this);
    return this;
  }

  private static final void merge(SessionTermParams mommy, SessionTermParams daddy, SessionTermParams baby) {
    baby.maxAgeMillis        = Math.min(mommy.maxAgeMillis,        daddy.maxAgeMillis);
    baby.maxUnaccessedMillis = Math.min(mommy.maxUnaccessedMillis, daddy.maxUnaccessedMillis);
  }
}

