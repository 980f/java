/**
 * Title:        ColorSet<p>
 * Description:  Set of colors (background and foreground)<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ColorSet.java,v 1.1 2000/07/25 17:23:31 mattm Exp $
 */

package net.paymate.web.color;

// +_+ just store as strings for now; later may go to/from other formats
public class ColorSet {
  public String FG;
  public String BG;

  public ColorSet(String fg, String bg) {
    this.FG = fg;
    this.BG = bg;
  }
}
