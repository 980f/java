/**
* Title:        Form
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Form.java,v 1.11 2001/11/14 01:47:58 andyh Exp $
*/
package net.paymate.terminalClient.IviForm;

import net.paymate.util.Safe;
import  java.util.Vector;
import java.io.PrintStream;

public class Form {
  public String myName;
  public String pcxResource=null;
  public int myNumber;

  protected Vector content=new Vector();
  private int sigindex=-1; //for not present

  public Form add(FormItem thing){
    content.addElement(thing);
    if(thing instanceof SigBox){
      //if sigindex!=-1 we have an error +_+, only one sigbox allowed per form.
      sigindex=content.size()-1;
    }
    return this;
  }

  public boolean hasSignature(){
    return sigindex>=0;
  }

  public SigBox signature(){
    return hasSignature()? (SigBox) item(sigindex): null;
  }

  public FormItem tail(){
    return (FormItem) (content.isEmpty()?new FormItem():content.lastElement());
  }

  public int Y(){
    return tail().y();
  }

  public int nextY(){//this only makes sense when do add's in order
    return tail().nextY();
  }

  public int nextX(){//this only makes sense when do add's in order
    return tail().nextX();
  }

  public boolean hasGraphic(){
    return Safe.NonTrivial(pcxResource);//but file could be missing
  }
  //+++ need replace and remove by type, for example:

  public boolean replaceButton(Button scab){
    for(int i=size();i-->0;){
      FormItem fue=(FormItem)content.elementAt(i);
      if(fue instanceof Button){
        if(((Button)fue).guid==scab.guid){
          content.set(i,scab);
          return true;
        }
      }
    }
    add(scab);
    return false;
  }

  public int size(){
    return content.size();
  }

  public FormItem item(int i){
    //check index! return null
    return (FormItem) (content.elementAt(i));
  }

  public Form setBackground(String bgname){
    pcxResource=bgname;
    return this;
  }

  public Form(String name,int number) {
    myNumber=number;
    myName=name;
  }

  public Form(String name) {
    //form #1 is picked whenever a form number is incorrect
    this(name,1);
  }

  public String toSpam(){
    return this.getClass().getName()+"."+myName+" ["+hasGraphic()+myNumber+"]";
  }

}
//$Id: Form.java,v 1.11 2001/11/14 01:47:58 andyh Exp $
