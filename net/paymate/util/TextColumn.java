/**
* Title:        TextColumn
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TextColumn.java,v 1.3 2001/03/15 02:00:01 mattm Exp $
*/
package net.paymate.util;

public class TextColumn extends TextList{//simple text one

  private static final ErrorLogStream dbg = new ErrorLogStream(TextColumn.class.getName());

  int width; // will have to be overidden as there isn't a default constructor

  // this makes something
  public TextColumn(TextList tl, int width) {
    this(width);
    if(tl != null) {
      addCentered(tl);
    }
  }

  public TextColumn(String toParse, int width) {
    this(width);
    addCentered(new TextList(toParse,width,TextList.SMARTWRAP_ON));
  }

  public TextColumn(int width){
    this();
    this.width = width;
  }

  private TextColumn(){
    super();
  }

  public void justified(String label,String value){
    add(Fstring.justified(width,label,value));
  }

  public void justified(String label,String value,char filler){
    add(Fstring.justified(width,label,value,filler));
  }

  public void centered(String label,char filler){
    add(Fstring.centered(label,width,filler));
  }

  public void add(TextList tl) {
    // no justification
    if(tl != null) {
      appendMore(tl);
    }
  }

  public void addCentered(TextList tl) {
    if(tl != null) {
      for(int i = 0; i < tl.size(); i++) {
        centered(tl.itemAt(i), ' ');
      }
    }
  }

}
//$Id: TextColumn.java,v 1.3 2001/03/15 02:00:01 mattm Exp $
