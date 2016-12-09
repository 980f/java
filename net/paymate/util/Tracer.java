package net.paymate.util;

/**
 * Title:        Tracer
 * Description:  Extension of ErrorLogStream that allows the developer to mark code within a function or try/catch.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: Tracer.java,v 1.7 2001/06/29 22:29:00 andyh Exp $
 */

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
    if(Safe.NonTrivial(location)) {
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

  public Tracer(String tag) {
    super(tag);
    location="start";
  }

  public Tracer(String cname,int spam){
    super(cname, spam);
    location="start";
  }

}
//$Id: Tracer.java,v 1.7 2001/06/29 22:29:00 andyh Exp $
