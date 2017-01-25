/**
 * Title:        $Source: /cvs/src/net/paymate/util/FormattedLineItem.java,v $
 * Description:
 * Copyright:    Paymate.net 2000
 * Company:      PayMate.net
 * @author      PayMate.net
 * @version     $Revision: 1.13 $
 */
package net.paymate.util;

public class FormattedLineItem implements isEasy {//carries info, doesn't actually DO any formatting
  // data
  public String              name          = "";
  public String              value         = "";
  public char                filler        = ' ';
  public ColumnJustification justification = new ColumnJustification(ColumnJustification.PLAIN);
  // keys
  public String nameKey          = "name";
  public String valueKey         = "value";
  public String fillerKey        = "fill";
  public String justificationKey = "just";
  // quickies
  public static final ColumnJustification justified  = new ColumnJustification(ColumnJustification.JUSTIFIED);
  public static final ColumnJustification centered   = new ColumnJustification(ColumnJustification.CENTERED);
  public static final ColumnJustification plain      = new ColumnJustification(ColumnJustification.PLAIN);
  public static final ColumnJustification winged     = new ColumnJustification(ColumnJustification.WINGED);
  // storage functions
  public EasyCursor asProperties() {
    EasyCursor ezp = new EasyCursor();
    save(ezp);
    return ezp;
  }

  public void save(EasyCursor ezp){
    ezp.setString(nameKey         , name);
    ezp.setString(valueKey        , value);
    ezp.setChar  (fillerKey       , filler);
    ezp.saveEnum (justificationKey, justification);
  }

  public void load(EasyCursor ezp) {
    name   = ezp.getString(nameKey);
    value  = ezp.getString(valueKey);
    filler = ezp.getChar(fillerKey);
    ezp.loadEnum(justificationKey, justification);
  }

  // constructors
  private FormattedLineItem() {
  //see initializers
  }

  public FormattedLineItem(EasyCursor ezp) {
    this();
    load(ezp);
  }

  public FormattedLineItem(String name, String value, char filler, ColumnJustification just) {
    this();
    this.name           = name;
    this.value          = value;
    this.filler         = filler;
    this.justification  = just;
  }

  public FormattedLineItem(String name, char filler) {
    this(name,"",filler,centered);
  }

  public FormattedLineItem(String name, String value, char filler) {
    this(name, value, filler, justified);
  }

  // default filler = '.', default just = justified
  public FormattedLineItem(String name, String value) {
    this(name, value, '.', justified);
  }

  public FormattedLineItem(String name) {
    this(name, "", '.', plain);
  }

  public static final FormattedLineItem winger(String name) {
    return new FormattedLineItem(name, "", ' ', winged);
  }

  public String toSpam(){//4debug
    return this.justification.Image()+":"+this.name+", *["+this.filler+"],"+this.value;
  }
}
//$Id: FormattedLineItem.java,v 1.13 2001/07/19 01:06:54 mattm Exp $
