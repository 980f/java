package net.paymate.util;

/**
 * Title:        Tracer
 * Description:  Extension of ErrorLogStream that allows the developer to mark code within a function or try/catch.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: Tracer.java,v 1.9 2003/07/27 05:35:25 mattm Exp $
 */

import net.paymate.lang.StringX;

public class Tracer extends ErrorLogStream {
  public String location;

  public String prefix(){
    return location !=null? "At "+location+":":"";
  }

  public String functionName(){
    return ActiveMethod;
  }

  public void mark(String location){
    this.location=location;
    if(StringX.NonTrivial(location)) {
      VERBOSE("mark");
    }
  }

  public void Caught(Throwable caught){
    super.Caught(prefix(),caught);
  }

  public void ERROR(String msg){
    super.ERROR(prefix()+msg);
  }

  public void WARNING(String msg){
    super.WARNING(prefix()+msg);
  }

  public void VERBOSE(String msg){
    super.VERBOSE(prefix()+msg);
  }

  protected Tracer(LogSwitch ls){
    super(ls);
  }

  public Tracer(Class claz,String suffix) {
    super(LogSwitch.getFor(claz,suffix));
    location="start";
  }

  public Tracer(Class claz) {
    this(claz,null);
  }

  public Tracer(Class claz,int spam){
    this(claz,null,spam);
  }

  public Tracer(Class claz,String suffix,int spam){
    this(claz,suffix);
    this.setLevel(spam);
  }

}
//$Id: Tracer.java,v 1.9 2003/07/27 05:35:25 mattm Exp $
