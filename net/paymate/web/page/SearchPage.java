/**
 * Title:        SearchPage
 * Description:  Used to find transactions based on rough estimates of data
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: SearchPage.java,v 1.8 2001/07/19 01:06:56 mattm Exp $
 */

package net.paymate.web.page;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.util.*;
import  net.paymate.database.*;//for search range types

public class SearchPage extends PayMatePage {

  public SearchPage() {
    super(name(), null);
  }

  public static final String name() {
    return "Search";
  }
  public static final String key() {
    return PayMatePage.key(SearchPage.class);
  }

  public static final String card1tag = "card1";
  public static final String card2tag = "card2";
//  public static final String date1tag = "date1";
//  public static final String date2tag = "date2";
// +++ implement these, see TranjourFilter classes
//  public static final String storeTag = "store";
//  public static final String terminalTag = "terminal";
  public static final String amount1tag = "amount1";
  public static final String amount2tag = "amount2";
  public static final String stan1tag = "stan1";
  public static final String stan2tag = "stan2";
  public static final String appr1tag = "appr1";
  public static final String appr2tag = "appr2";
  public static final String date1month = "d1mon";
  public static final String date1day = "d1day";
  public static final String date1year = "d1yr";
  public static final String date1hour = "d1hr";
  public static final String date1minute = "d1min";
  public static final String date2month = "d2mon";
  public static final String date2day = "d2day";
  public static final String date2year = "d2yr";
  public static final String date2hour = "d2hr";
  public static final String date2minute = "d2min";

  private static final String optional ="-";// "to (optional)";

  public static final Element defaultPage(String comment /* like instructions */, String url) {
    Form form = new Form();
    form.setAction(url).setName("searchform").setMethod(Form.POST);

    Input searchbutton = new Input(Input.SUBMIT, SUBMITBUTTON, "Search");
    TD td4 = new TD(searchbutton);
    td4.setAlign(AlignType.CENTER/*RIGHT*/).setColSpan(4);
    TR tr4 = new TR(td4);


    Table t = new Table();
    Element header=searchHeader();

    t.addElement(header);
    int SIZEANDLENGTH=2;
    ElementContainer d1 = new ElementContainer();
    d1.addElement(input(Input.TEXT, date1month, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(input(Input.TEXT, date1day, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / 20 ")
      .addElement(input(Input.TEXT, date1year, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" ")
      .addElement(input(Input.TEXT, date1hour, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" : ")
      .addElement(input(Input.TEXT, date1minute, "", SIZEANDLENGTH, SIZEANDLENGTH));
    ElementContainer d2 = new ElementContainer();
    d2.addElement(input(Input.TEXT, date2month, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(input(Input.TEXT, date2day, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / 20 ")
      .addElement(input(Input.TEXT, date2year, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" ")
      .addElement(input(Input.TEXT, date2hour, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" : ")
      .addElement(input(Input.TEXT, date2minute, "", SIZEANDLENGTH, SIZEANDLENGTH));

    t.addElement(rowPrompt("Card Number ", Input.TEXT, card1tag, "", optional, Input.TEXT, card2tag, ""))
     .addElement(rowPrompt("Date ", d1, optional, d2))
     // +++ make a link on the Date prompt to show a table that shows how to convert to 24-hour time from AM/PM
     // +++ insert a tiny "current month" calendar, that stuffs the date field when days on it are clicked, with left and right arrows for skipping between months.
     .addElement(rowPrompt("Amount ", Input.TEXT, amount1tag, "", optional, Input.TEXT, amount2tag, ""))
     .addElement(rowPrompt("Txn # ", Input.TEXT, stan1tag, "", optional, Input.TEXT, stan2tag, ""))
     .addElement(rowPrompt("Approval ", Input.TEXT, appr1tag, "", optional, Input.TEXT, appr2tag, ""));
     // +++ add selectors for store and terminal
    t.addElement(header);

    t.addElement(tr4);

    form.addElement(new Center(t));

    if(comment !=null) {
      form.addElement(BRLF)
          .addElement(comment)
          .addElement(BRLF);
    }

    return form;
  }

  private static final TR searchHeader() {
    TR tr  = new TR();
    tr.addElement(new TH("(exact if ending left blank)"));
    tr.addElement(new TH("Start"));
    tr.addElement(new TH(""));
    tr.addElement(new TH("End"));
    return tr;
  }

  // +++ put this stuff in a baser class and use elsewhere ... maybe
  private static final Element input(String inputType, String fieldName, String defaultValue, int maxlength, int size) {
    defaultValue = Safe.TrivialDefault(defaultValue, "");
    fieldName = Safe.TrivialDefault(fieldName, "");
    inputType = Safe.TrivialDefault(inputType, "");
    Input i = new Input(inputType, fieldName, defaultValue);
    if(maxlength > 0) {
      i.setMaxlength(maxlength);
    }
    if(size > 0) {
      i.setSize(size);
    }
    return i;
  }
  private static final Element input(String inputType, String fieldName, String defaultValue) {
    defaultValue = Safe.TrivialDefault(defaultValue, "");
    fieldName = Safe.TrivialDefault(fieldName, "");
    inputType = Safe.TrivialDefault(inputType, "");
    return input(inputType, fieldName, defaultValue, -1, -1);
  }
  private static final TR rowPrompt(String prompt1, String inputType, String fieldName, String defaultValue, String prompt2, String inputType2, String fieldName2, String defaultValue2) {
    // deal with nulls
    Element field1 = input(inputType, fieldName, defaultValue);
    Element field2 = input(inputType2, fieldName2, defaultValue2);
    return rowPrompt(prompt1, field1, prompt2, field2);
  }
  private static final TR rowPrompt(String prompt1, Element field1, String prompt2, Element field2) {
    // deal with nulls
    prompt1 = Safe.TrivialDefault(prompt1, "");
    prompt2 = Safe.TrivialDefault(prompt2, " ");
    // build the row
    TD td1 = new TD(prompt1);
    TD td2 = new TD(field1);
    TD td3 = new TD(prompt2);
    TD td4 = new TD(field2);
    TR tr  = new TR(td1);
    tr.addElement(td2);
    tr.addElement(td3);
    tr.addElement(td4);
    return tr;
  }

}
