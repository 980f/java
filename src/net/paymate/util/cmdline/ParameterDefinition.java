/**
 * Title:        ParameterDefinition<p>
 * Description:  Command line parameter definition<p>
 * Copyright:    2000<p>
 * Company:      paymate<p>
 * @author       PayMate.net
 * @version      $Id: ParameterDefinition.java,v 1.4 2003/07/27 05:35:27 mattm Exp $
 */
package net.paymate.util.cmdline;

import net.paymate.lang.StringX;

public class ParameterDefinition {
  public String flag;              // flag is the actual parameter flag, eg: -v.
                                   // Note that the flag String should not include a dash!
  public String name;              // the 1-word name
  public String defaultValue;      // could be null
  public String description;       // for printing out the usage
  public int    typicalValueCount; // for display purposes only

  public ParameterDefinition(String flag, String name, String defaultValue,
      String description, int typicalValueCount) {
    this.defaultValue      = defaultValue;
    this.name              = StringX.TrivialDefault(name,  String.valueOf(this));
    this.flag              = StringX.TrivialDefault(flag,  name);
    this.description       = StringX.TrivialDefault(description, name);
    this.typicalValueCount = typicalValueCount;
  }
}

