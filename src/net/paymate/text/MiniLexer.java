package net.paymate.text;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/text/MiniLexer.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class MiniLexer {
  String tomatch;
  int sofar;

  public MiniLexer() {
    tomatch="";
    sofar=0;
  }

  MiniLexer nowLookFor(String token){
    tomatch=token!=null?token:"";//just exlude nulls, accept trivial
    return this;
  }

  public static MiniLexer lookerFor(String token){
    MiniLexer newone=new MiniLexer();
    return newone.nowLookFor(token);
  }

  private boolean matchcore(char c){
    if(tomatch.charAt(sofar)==c){
      ++sofar;
      return true;
    } else {
      return false;
    }
  }

  public boolean match(char c){
    if(tomatch!=null && sofar<tomatch.length()){
      if(! matchcore(c)){
        sofar=0;//start over.
        matchcore(c);//in case pattern breaker is first of token.
      }
    } else {//on overrun return FALSE, it is important to note when in sequence match occured.
      sofar=0;
    }
    return sofar==tomatch.length();
  }

  MiniLexer reset(){
    sofar=0;
    return this;
  }
//////////

  static int test1(MiniLexer lex,String stream){
    lex.reset();//for valid testing
    for(int foundat=0;foundat<stream.length();++foundat){
      if(lex.match(stream.charAt(foundat))){
         return foundat;
      }
    }
    return -1;//not found
  }

  public static void main(String[] args) {
    MiniLexer lex= MiniLexer.lookerFor("NOK");
    System.out.println("should be 11:"+ test1(lex,"ACKMD5AOKNOK"));

    lex.nowLookFor("test");
    System.out.println("should be 5:"+ test1(lex,"tetest"));
    System.out.println("should be 3:"+ test1(lex,"test"));
    System.out.println("should be 4:"+ test1(lex,"atest"));
    System.out.println("should be -1:"+ test1(lex,"tes"));
    System.out.println("should be 10:"+ test1(lex,"ttesetetest"));
  }

}
//$Id: MiniLexer.java,v 1.1 2004/02/28 01:33:21 andyh Exp $
