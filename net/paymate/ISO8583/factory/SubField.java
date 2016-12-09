/* $Id: SubField.java,v 1.5 2001/10/15 22:39:45 andyh Exp $ */
package net.paymate.ISO8583.factory;

public abstract class SubField {//commonality is place and length

  protected int startsat;
  protected int length;

  public int Length(){//expected use is to help presize owning field
    return length;
  }

  public SubField(int s, int l){
    startsat=s;
    length=l;
  }

  /** returns the value of the subfield  */
  public abstract String insertInto(StringBuffer field);

  protected String inserter(StringBuffer field, String image, boolean prefix, char fillChar){

    int sourceLength=image.length();
    int deficit=length-sourceLength;

    if(deficit<0){//essence is too long!
      //+_+ log error
      if(prefix){   //clip leading
        image=image.substring(-deficit);
      } else {      //clip trailing
        image=image.substring(0,length);
      }
      deficit=0;
      sourceLength=length;
    }

    int cursor;

    if(prefix){
      cursor=startsat;
      while(deficit-->0){
        field.setCharAt(cursor++,fillChar);//prefix
      }
      while(sourceLength-->0){
        //recycling 'deficit' to be pointer into image
        field.setCharAt(cursor++,image.charAt(++deficit));//copy image
      }
    }
    else {//reverse copy for speed
      cursor=startsat+length;
      while(deficit-->0){
        field.setCharAt(--cursor,fillChar);//postfix
      }
      while(sourceLength-->0){
        field.setCharAt(--cursor,image.charAt(sourceLength));//copy image
      }
    }
    return image;
  }

  public String parse(String clump){
    if(clump!=null){
      int sourcelength=clump.length();
      int endchar=startsat+length; //theoretical end
      if(startsat<sourcelength){
        if(endchar>sourcelength){
          endchar=sourcelength;
        }
        if(endchar>startsat){
          return clump.substring(startsat,endchar);
        }
      }
    }
    return "";
  }

}
//$Id: SubField.java,v 1.5 2001/10/15 22:39:45 andyh Exp $
