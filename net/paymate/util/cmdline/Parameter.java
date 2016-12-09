/**
 * Title:        Parameter<p>
 * Description:  Command line parameter definition with parsed info<p>
 * Copyright:    2000<p>
 * Company:      paymate<p>
 * @author       PayMate.net
 * @version      $Id: Parameter.java,v 1.2 2000/07/09 22:50:44 mattm Exp $
 */

package net.paymate.util.cmdline;

import net.paymate.util.TextList;

public class Parameter extends ParameterDefinition {
  // parse results:
  public boolean present = false;        // whether it was in the commandline when it was parsed
  public TextList value = new TextList(10);     // any extensions that were found for the parameter; must be extractable!
  public boolean required = false;

  public Parameter(String theflag, String thename, String defaultValue, String descript, int typicalValueCount) {
    super(theflag, thename, defaultValue, descript, typicalValueCount);
    required = (defaultValue == null);
  }
  public Parameter(ParameterDefinition def) {
    this(def.flag, def.name, def.defaultValue, def.description, def.typicalValueCount);
  }

  public String firstValue() {
    return (value.size()>0) ? value.itemAt(0) : null;
  }
}

