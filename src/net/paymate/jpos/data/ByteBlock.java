/**
* Title:        $Source: /cvs/src/net/paymate/jpos/data/ByteBlock.java,v $
* Description:  two dimensional array of bytes.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ByteBlock.java,v 1.4 2004/01/22 18:09:39 andyh Exp $
 * @todo: relocate to util package.
*/
package net.paymate.jpos.data;

public class ByteBlock {
  byte [][] content;

  public ByteBlock(int numLines,int lineLength){
    try {
      content= new byte[numLines][lineLength];
    } catch(ArrayIndexOutOfBoundsException ignored){
      content= new byte[0][0];
    }
  }

  public int length(){//to match Vector
    return content.length;
  }

  public byte [][] raw(){//ease transition from direct use.
    return content;
  }

  public byte [] line(int index){
    try {
      return content[index];
    } catch (ArrayIndexOutOfBoundsException ignored){
      return  new byte[0];
    }
  }

  public byte cell(int index,int offset){
    try {
      return content[index][offset];
    } catch (ArrayIndexOutOfBoundsException ignored){
      return 0;
    }
  }

  public ByteBlock(ByteBlock rhs){//copy contents
    content=new byte[rhs.length()][];
    for(int i=length();i-->0;){
      content[i]=(byte []) rhs.content[i].clone();
    }
  }

  public static final ByteBlock EmptyBlock(){
    return new ByteBlock(0,0);
  }

}
//$Id: ByteBlock.java,v 1.4 2004/01/22 18:09:39 andyh Exp $
