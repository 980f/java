/* $Id: Person.java,v 1.20 2001/07/30 20:51:49 andyh Exp $ */
package net.paymate.jpos.data;

import net.paymate.util.*;

public class Person implements isEasy { //five parts to a person's name...
  static final ErrorLogStream dbg=new ErrorLogStream(Person.class.getName());
  public String Title         ;
  public String FirstName     ;
  public String MiddleInitial ;
  public String Surname       ;
  public String Suffix        ;
/**
 * defeats all parsing when true
 */

  private static final boolean hopeless=true;
  //parssing tokens:
  protected final static int CARET=94;
  protected final static int EQUALSIGN=61;
  protected final static int SLASH=47;
  protected final static int SPACE=32;


  public boolean isReasonable(){//valid is too strong a name
    return Safe.NonTrivial(CompleteName());
  }

  public void Clear(){
    Title         ="";
    FirstName     ="";
    MiddleInitial ="";
    Surname       ="";
    Suffix        ="";
  }

  public Person(){
    Clear();
  }

  public String CompleteName(){
    StringBuffer complete=new StringBuffer(40);
    if(Safe.NonTrivial(Title)){
      complete.append(Title).append(' ');
    }
    if(Safe.NonTrivial(FirstName)){
      complete.append(FirstName).append(' ');
    }
    if(Safe.NonTrivial(MiddleInitial)){
      complete.append(MiddleInitial);//sometimes the middle initial is a full middle name!
      if(MiddleInitial.length()==1){
        complete.append("."); //only a few chinese folk will dislike this
      }
      complete.append(" ");
    }
    if(Safe.NonTrivial(Surname)){
      complete.append(Surname);
    }
    if(Safe.NonTrivial(Suffix)){
      complete.append(", ").append(Suffix);
    }
    return complete.toString();
  }

  public Person(Person old){
    Title         =old.Title         ;
    FirstName     =old.FirstName     ;
    MiddleInitial =old.MiddleInitial ;
    Surname       =old.Surname       ;
    Suffix        =old.Suffix        ;
  }


  public static final Person jposParsed(String t,String f,String m,String l,String s){
    Person newone=new Person();
    newone.Title         =t;
    newone.FirstName     =f;
    newone.MiddleInitial =m;
    newone.Surname       =l;
    newone.Suffix        =s;
    return newone;
  }
  /////////////////
  // transport

  public void save(EasyCursor ezp){
    ezp.setString("Title",         Title        );
    ezp.setString("FirstName",     FirstName    );
    ezp.setString("MiddleInitial", MiddleInitial);
    ezp.setString("Surname",       Surname      );
    ezp.setString("Suffix",        Suffix       );
  }

  public void load(EasyCursor ezp){
    Title         =ezp.getString("Title");
    FirstName     =ezp.getString("FirstName");
    MiddleInitial =ezp.getString("MiddleInitial");
    Surname       =ezp.getString("Surname");
    Suffix        =ezp.getString("Suffix");
  }

/**
 * @param track1 just the part between the carets please!
 */
  public void Parse(String track1){
    if(hopeless){
      Surname=track1.trim();
      return;
    }
    dbg.Enter("Parse");
    dbg.VERBOSE("parsing:"+track1);
    int slash = track1.indexOf(SLASH);
    dbg.VERBOSE("Slashed at:"+slash);
    StringBuffer slasher;
    StringBuffer trasher;
    if(slash>=0){
      slasher= new StringBuffer(track1.substring(slash+1));
      trasher= new StringBuffer(track1.substring(0,slash));//disregard what follows slash
    } else {
      slasher= new StringBuffer();
      trasher= new StringBuffer(track1);//no first names
    }
    dbg.VERBOSE("Prenom:"+slasher);
    dbg.VERBOSE("Surnom:"+trasher);

    //the order of the following three is ESSENTIAL, cutWord modifies its argument
    Surname = Safe.cutWord(trasher);
    dbg.VERBOSE("Surnom1:"+trasher);
    Suffix =  Safe.cutWord(trasher);
    dbg.VERBOSE("Surnom2:"+trasher);
    Title =   Safe.cutWord(trasher);
    dbg.VERBOSE("Surnom3:"+trasher);
//and ditto on order here:
    FirstName =     Safe.cutWord(slasher);
    dbg.VERBOSE("Prenom1:"+slasher);
    MiddleInitial = Safe.cutWord(slasher);
    dbg.VERBOSE("Prenom2:"+slasher);
    dbg.VERBOSE("Resulting in:"+this.CompleteName());
    dbg.Exit();
  }

}
//$Id: Person.java,v 1.20 2001/07/30 20:51:49 andyh Exp $
