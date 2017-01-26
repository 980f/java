package net.paymate.terminalClient.IviForm;
/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/IviForm/Form.java,v $
* Description:
* Copyright:    2000-2003 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.19 $
*/

import net.paymate.util.*;
import java.util.*;
import java.io.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;

public class Form {
  public String myName;
  public String pcxResource=null;
  public File pcxFile; //will get generated from pcxResource
  public int myNumber;

  protected Vector content=new Vector();
  private int sigindex=-1; //for not present

  public int buttonCount=0;

  public Form add(FormItem thing){
    content.addElement(thing);
    if(thing instanceof SigBox){
      //if sigindex!=-1 we have an error +_+, only one sigbox allowed per form.
      sigindex=content.size()-1;
    }
    if(thing instanceof Button){
      ++buttonCount;
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
    return StringX.NonTrivial(pcxResource);//but file could be missing
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

  public String toString(){
    return myName+"["+myNumber+"]";
  }
  public String toSpam(){
    StringBuffer spam=new StringBuffer(80);
    spam.append(ReflectX.shortClassName(this,myName));

    if(this.hasSignature()){
      spam.append(" gets signature");
    }
    if(hasGraphic()){
      spam.append(" bgnd:");
      spam.append(this.pcxResource);
    }
    return String.valueOf(spam);
  }

  public TextList asXml(TextList addto){
    if(addto==null){
      addto=new TextList();
    }
    EasyCursor attribs=new EasyCursor();
    attribs.setBoolean("hasGraphic",hasGraphic());
    attribs.setBoolean("hasSignature",hasSignature());
    attribs.setString("background",this.pcxResource);
    attribs.setInt("POSForm",this.myNumber);

    addto.add(Xml.attributed("form",attribs));
    for(int i=0;i<content.size();i++){//in construction order
      FormItem fi= (FormItem) content.elementAt(i);
      addto.add(fi.xml());
    }
    addto.add( Xml.end("form"));
    return addto;
  }

}
//$Id: Form.java,v 1.19 2003/07/27 05:35:17 mattm Exp $
