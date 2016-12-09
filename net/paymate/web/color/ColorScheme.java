/**
 * Title:        ColorScheme<p>
 * Description:  Sets [dark, medium, light] of colors in a color scheme<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ColorScheme.java,v 1.4 2001/07/19 01:06:56 mattm Exp $
 */

package net.paymate.web.color;

public class ColorScheme {
  public ColorSet DARK;
  public ColorSet MEDIUM;
  public ColorSet LIGHT;

  public ColorScheme(ColorSet dark, ColorSet medium, ColorSet light) {
    this.DARK = dark;
    this.MEDIUM = medium;
    this.LIGHT = light;
  }

// NOTE; The typical way to do this is to set the DARK for the table header, making the font bold
// then, alternate light then medium for table rows
// (for the more black and white schemes, light and medium don't mean so much)

// these *could* eventually be put into an actual TrueEnum:
// all of these color schemes could easily go into style sheets
/*
Color Schemes
                   Dark (header)        Medium (alternating color1) Light (alternating color2 and header font)
Heat (Red)         128,0,0     - 800000 192,128,128 - C08080        255,192,192 - FFC0C0
Money (Green)      0,128,0     - 008000 128,192,128 - 80C080        192,255,192 - C0FFC0
Tranquility (Blue) 0,0,128     - 000080 128,128,192 - 8080C0        192,192,255 - C0C0FF
Midnight (Grey)    0,0,0       - 000000 128,128,128 - 808080        192,192,192 - C0C0C0
Bronze             128,128,0   - 808000 192,192,128 - C0C080        255,255,192 - FFFFC0
Plum               128,0,128   - 800080 192,128,192 - C080C0        255,192,255 - FFC0FF
Teal               0,128,128   - 008080 128,192,192 - 80C0C0        192,255,255 - C0FFFF
Monochrome (b&w)   0,0,0       - 000000 255,255,255 - FFFFFF        0,0,0       - 000000 (font must be white)
Simple (grey&w)    255,255,255 - FFFFFF 192,192,192 - C0C0C0        255,255,255 - FFFFFF
Plain (no colors)  255,255,255 - FFFFFF 255,255,255 - FFFFFF        255,255,255 - FFFFFF

*/


  // B&W
  public static final String WHITE  = "#FFFFFF";
  public static final String BLACK  = "#000000";
  public static final String LTGRAY = "#C0C0C0";
  public static final String DKGRAY = "#808080";
  // reds
  public static final String LTRED = "#FFC0C0";
  public static final String MDRED = "#C08080";
  public static final String DKRED = "#800000";
  // greens
  public static final String LTGREEN = "#C0FFC0";
  public static final String MDGREEN = "#80C080";
  public static final String DKGREEN = "#008000";
  // blues
  public static final String LTBLUE = "#C0C0FF";
  public static final String MDBLUE = "#8080C0";
  public static final String DKBLUE = "#000080";
  // yellows
  public static final String LTBRONZE = "#FFFFC0";
  public static final String MDBRONZE = "#C0C080";
  public static final String DKBRONZE = "#808000";
  // purples
  public static final String LTPURPLE = "#FFC0FF";
  public static final String MDPURPLE = "#C080C0";
  public static final String DKPURPLE = "#800080";
  // teals
  public static final String LTTEAL = "#C0FFFF";
  public static final String MDTEAL = "#80C0C0";
  public static final String DKTEAL = "#008080";

  // actual construction of the color schemes (maybe overkill with so many classes)
  public static final ColorScheme PLAIN = new ColorScheme(
    new ColorSet(BLACK, WHITE), /* dark */
    new ColorSet(BLACK, WHITE), /* medium */
    new ColorSet(BLACK, WHITE)  /* light */
  );
  public static final ColorScheme SIMPLE = new ColorScheme(
    new ColorSet(BLACK, WHITE), /* dark */
    new ColorSet(BLACK, WHITE), /* medium */
    new ColorSet(BLACK, LTGRAY)  /* light */
  );
  public static final ColorScheme MONOCHROME = new ColorScheme(
    new ColorSet(WHITE, BLACK), /* dark */
    new ColorSet(WHITE, BLACK), /* medium */
    new ColorSet(BLACK, WHITE)  /* light */
  );
  public static final ColorScheme TEAL = new ColorScheme(
    new ColorSet(LTTEAL, DKTEAL), /* dark */
    new ColorSet(BLACK,  MDTEAL), /* medium */
    new ColorSet(BLACK,  LTTEAL)  /* light */
  );
  public static final ColorScheme PLUM = new ColorScheme(
    new ColorSet(LTPURPLE, DKPURPLE), /* dark */
    new ColorSet(BLACK,    MDPURPLE), /* medium */
    new ColorSet(BLACK,    LTPURPLE)  /* light */
  );
  public static final ColorScheme BRONZE = new ColorScheme(
    new ColorSet(LTBRONZE, DKBRONZE), /* dark */
    new ColorSet(BLACK,    MDBRONZE), /* medium */
    new ColorSet(BLACK,    LTBRONZE)  /* light */
  );
  public static final ColorScheme MIDNIGHT = new ColorScheme(
    new ColorSet(LTGRAY, BLACK), /* dark */
    new ColorSet(BLACK,  DKGRAY), /* medium */
    new ColorSet(BLACK,  LTGRAY)  /* light */
  );
  public static final ColorScheme TRANQUILITY = new ColorScheme(
    new ColorSet(LTBLUE, DKBLUE), /* dark */
    new ColorSet(BLACK,  MDBLUE), /* medium */
    new ColorSet(BLACK,  LTBLUE)  /* light */
  );
  public static final ColorScheme MONEY = new ColorScheme(
    new ColorSet(LTGREEN, DKGREEN), /* dark */
    new ColorSet(BLACK,   MDGREEN), /* medium */
    new ColorSet(BLACK,   LTGREEN)  /* light */
  );
  public static final ColorScheme HEAT = new ColorScheme(
    new ColorSet(LTRED, DKRED), /* dark */
    new ColorSet(BLACK, MDRED), /* medium */
    new ColorSet(BLACK, LTRED)  /* light */
  );

  public static final ColorScheme schemeForName(String name) {
    ColorSchema cs = new ColorSchema(name);
    switch(cs.Value()) {
      case ColorSchema.PLAIN:       return ColorScheme.PLAIN;
      case ColorSchema.SIMPLE:      return ColorScheme.SIMPLE;
      case ColorSchema.MONOCHROME:  return ColorScheme.MONOCHROME;
      case ColorSchema.TEAL:        return ColorScheme.TEAL;
      case ColorSchema.PLUM:        return ColorScheme.PLUM;
      case ColorSchema.BRONZE:      return ColorScheme.BRONZE;
      case ColorSchema.MIDNIGHT:    return ColorScheme.MIDNIGHT;
      case ColorSchema.TRANQUILITY: return ColorScheme.TRANQUILITY;
      default:
      case ColorSchema.MONEY:       return ColorScheme.MONEY;
      case ColorSchema.HEAT:        return ColorScheme.HEAT;
    }
  }

}

