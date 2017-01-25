/* $Id: StringStack.java,v 1.4 2001/09/01 09:33:39 mattm Exp $
 A string stack that does not throw empty stack exceptions

 */

package net.paymate.util;

import java.util.Stack;
import java.util.EmptyStackException;

public class StringStack {
  protected Stack myStack;
  public final static String stringForEmpty="StackEmpty";

  public String push(String item){
    myStack.push(item);
    return item; //???no spec on this!
  }

  public String pop() {
    try {
      return (String) myStack.pop();
    } catch (EmptyStackException ignored){
      return stringForEmpty;
    }
  }

  public String peek() {
    try {
      return (String) myStack.peek();
    } catch (EmptyStackException ignored){
      return stringForEmpty;
    }
  }

  public boolean isEmpty(){
    return myStack.empty();
  }

  public void Clear(){
    myStack.clear();
  }

  public int search(String o){
    return myStack.search(o);
  }

  public StringStack(){
    myStack= new Stack();
  }

  public String toString() {
    return toString("/");
  }

  // --- not threadsafe
  public String toString(String separator) {
    StringBuffer sb = new StringBuffer(100);
    for(int i = myStack.size(); i-->0;) {
      sb.insert(0, myStack.elementAt(i));
      sb.insert(0, separator);
    }
    return sb.toString();
  }

}
//$Id: StringStack.java,v 1.4 2001/09/01 09:33:39 mattm Exp $
