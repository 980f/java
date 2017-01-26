/* $Id: Person.java,v 1.25 2003/08/04 22:23:49 andyh Exp $ */
package net.paymate.jpos.data;

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class Person implements isEasy { //five parts to a person's name...
  private String Surname       ;

  public void setSurname(String name){
    Surname=name;
  }

  public boolean isReasonable(){//valid is too strong a name
    return StringX.NonTrivial(CompleteName());
  }

  public void Clear(){
    Surname       ="";
  }

  public Person(){
    Clear();
  }

  public String CompleteName(){
    return Surname;
  }

  public Person(Person old){
    Surname       =old.Surname       ;
  }


  /////////////////
  // transport

  public void save(EasyCursor ezp){
    ezp.setString("Surname",       Surname      );
  }

  public void load(EasyCursor ezp){
    Surname       =ezp.getString("Surname");
  }

/**
 * @param track1 just the part between the carets please!
 */
  public void Parse(String track1){
      Surname=track1.trim();
  }

}
//$Id: Person.java,v 1.25 2003/08/04 22:23:49 andyh Exp $
