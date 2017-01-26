package net.paymate.database;
/**
 * Title:         $Source: /cvs/src/net/paymate/database/QueryString.java,v $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author      Paymate.net
 * @version $Id: QueryString.java,v 1.31 2001/11/16 01:34:29 mattm Exp $
 */


import net.paymate.util.*;
import net.paymate.data.*;
import java.util.*;

public class QueryString {
  private static final ErrorLogStream dbg = new ErrorLogStream(QueryString.class.getName());

  ///////////////////////////////////////
  // Keywords:
  private static final String SELECT = " SELECT ";
  private static final String AND = " AND ";
  private static final String NOT = " NOT ";
  private static final String WHERE = " WHERE ";
  private static final String FROM = " FROM ";
  private static final String UPDATE = " UPDATE ";
  private static final String SET = " SET ";
  private static final String ORDERBY = " ORDER BY ";
  private static final String GROUPBY = " GROUP BY ";
  private static final String ASC = " ASC ";
  private static final String DESC = " DESC ";
  private static final String UNION = " UNION ";
  private static final String VALUES = " VALUES ";
  private static final String INSERTINTO = " INSERT INTO ";
  private static final String IN = " IN ";
  private static final String ON = " ON ";
  private static final String ALLFIELDS = " * ";
  private static final String SELECTALLFROM = SELECT + ALLFIELDS + FROM ;
  private static final String SINGLEQUOTES = "'";
  private static final String OPENPAREN = " (";
  private static final String CLOSEPAREN = ") ";
  private static final String OR = " OR ";
  private static final String CREATETABLE = " CREATE TABLE ";
  private static final String COMMA = " , ";
  private static final String SPACE = " ";
  private static final String EQUALS = " = ";
  private static final String GTEQ = " >= ";
  private static final String GT = " > ";
  private static final String LTEQ = " <= ";
  private static final String LT = " < ";
  private static final String ALTERTABLE = "ALTER TABLE ";
  private static final String ADDCONSTRAINT = " ADD CONSTRAINT ";
  private static final String CONSTRAINT = " CONSTRAINT ";
  private static final String PRIMARYKEY = " PRIMARY KEY ";
  private static final String EMPTY = "";
  private static final String LOCKMODEROW = "LOCK MODE ROW"; // informix
  private static final String CREATEINDEX = "CREATE INDEX ";
  private static final String IS = " IS ";
  private static final String NULL = " null ";
  private static final String ISNULL = IS + NULL; // no space since each has a space before and after


  /**
  * SQL-92 requires that all strings are inside single quotes.
  * Double quotation marks delimit special identifiers referred to in SQL-92
  * as 'delimited identifiers'.  Single quotation marks delimit character strings.
  * Within a character string, to represent a single quotation mark or
  * apostrophe, use two single quotation marks. To represent a double
  * quotation mark, use a double quotation mark
  * (which requires a backslash escape character within a Java program).
  * For example, the string
  *   "this " is a double quote"
  * is not valid.  This one is:
  *   'this " is a double quote'
  * And so is this one:
  *   "this "" is a double quote"
  * And this one:
  *   'this '' is a single quote'
  */

  protected StringBuffer guts= new StringBuffer(200);//most queries are big

  public String toString(){//works with String +
    return guts.toString();
  }

  public QueryString cat(String s){
    guts.append(s);
    return this;
  }

  public QueryString cat(char c){
    guts.append(c);
    return this;
  }

  public QueryString start(String starter){
    guts.setLength(0);//removes contents but not allocation
    guts.append(starter);
    return this;
  }


/**
 * synonym for cat
 */

  public QueryString append(String s){
    return cat(s);
  }

  public QueryString append(QueryString s){
    return cat(s.toString());
  }

  public QueryString append(int i){
    return cat(Integer.toString(i));
  }

  public QueryString where(String s){
    cat(WHERE);
    return cat(s);
  }

  public QueryString where(QueryString s){
    return where(s.toString());
  }

  public QueryString where(){
    return cat(WHERE);
  }

  public QueryString and(){
    return cat(AND);
  }

  public QueryString and(String s){
    return cat(AND).cat(s);
  }

  public QueryString and(QueryString s){
    return and(s.toString());
  }

  public QueryString or(){
    return cat(OR);
  }

  public QueryString not(){
    return cat(NOT);
  }

  public QueryString in(){
    return cat(IN);
  }

  public QueryString SetJust(String field,String value){
    return cat(SET).nvPair(field,value);
  }

  public QueryString SetJust(String field,int value){//wishing for templates....
    return cat(SET).nvPair(field,value);
  }

  public QueryString Values(String first){
    return cat(VALUES+OPENPAREN).value(first);
  }

  public QueryString Open(String s){
    return cat(OPENPAREN).cat(s);
  }

  public QueryString Close(){
    return cat(CLOSEPAREN);
  }

  public QueryString comma(String s){
    return cat(COMMA).cat(s);
  }

  public QueryString commaPair(String field, String value){
    return cat(COMMA).nvPair(field,value);
  }

  public QueryString word(String s){
    return cat(SPACE).cat(s).cat(SPACE);
  }

  public QueryString eqField(String s){
    return cat(EQUALS).cat(s).cat(SPACE);
  }

  public QueryString parenth(String s){
    return cat(SPACE).Open(EMPTY).cat(s).Close().cat(SPACE);
  }

  public QueryString parenth(QueryString s){
    return parenth(s.toString());
  }

  public QueryString value(String s){
    return cat(Quoted(s));
  }

  public QueryString value(char c){
    return cat(Quoted(c));
  }

  // +++ (next 2 functions)
  // at least for informix, you can include single quotes in a quoted string by wrapping it double quotes instead of single
  public static final String Quoted(String s){
    return SINGLEQUOTES+s+SINGLEQUOTES;
  }

  public static final String Quoted(char c){
    return SINGLEQUOTES+c+SINGLEQUOTES;
  }

  public QueryString quoted(String s){
    return cat(SPACE).value(s);
  }

  public QueryString quoted(char c){
    return cat(SPACE).value(c);
  }

  public QueryString commaQuoted(String s){
    return cat(COMMA).value(s);
  }

  public QueryString commaQuoted(char c){
    return cat(COMMA).value(c);
  }

  public QueryString eqvalue(String s){
    return cat(EQUALS) .value(s);
  }

  public QueryString eqname(String s){
    return cat(EQUALS).cat(s);
  }

  public QueryString orderbyasc(String first){
    return cat(ORDERBY).cat(first).cat(ASC);
  }

  public QueryString orderbydesc(String first){
    return cat(ORDERBY).cat(first).cat(DESC);
  }

  public QueryString groupby(String first) {
    return cat(GROUPBY).cat(first);
  }

  public QueryString nnPair(String field1, String field2){
    return word(field1).eqname(field2);
  }

  public QueryString nvPair(String field, String value){
    return word(field).eqvalue(value);
  }

  public QueryString nvPairQuoted(String field, String value){
    return word(field).eqvalue(value);
  }

  public QueryString nvPair(String field, int value){
//    return nvPair(field,);
      return word(field).cat(EQUALS).cat(Integer.toString(value));
  }

  public QueryString nGTEQv(String field, String value){
    return word(field).cat(QueryString.GTEQ).value(value);
  }

  public QueryString isNull(String field) {
    return word(field).cat(ISNULL);
  }

  public QueryString nEQemptyString(String field) {
    return nvPair(field, "");
  }

  // means is null or == ''
  public QueryString isEmpty(String field) {
    return Open("").isNull(field).or().nEQemptyString(field).Close();
  }

  /**
   * a field must be found from a field of that same name in a table
   * with filter clasuses.
   */
  public QueryString nQuery(String field,String table,QueryString clauses){
    return word(field).cat(IN+OPENPAREN+SELECT).cat(table).cat('.').cat(field).
    cat(SPACE).cat(FROM).cat(SPACE).cat(table).cat(SPACE).cat(clauses.toString()).Close();
  }

  public static final String Join(String field1, String field2){
    return SPACE+field1+EQUALS+field2+SPACE;
  }

  public QueryString join(String field1, String field2){
    return word(field1).eqField(field2);
  }

  // recode everything that uses this to use the above joins, like so:
  //join(drawer.fieldname(TERMINALID), terminal.fieldname(TERMINALID))
  public QueryString matching(String table1,String table2,String field){
    return join(table1+'.'+field, table2+'.'+field);
  }

  /**
   * if the range wraps we must parenthesize and OR the terms
   * parens don't hurt if we aren't wrapping.
   * this presumes that the data being search is already within the modular range.
   */
  private QueryString rangy(boolean wrapper,String field, String start, String end,boolean closeEnd){
    return Open(field).cat(GTEQ).value(start) .cat(wrapper?OR:AND)
    .word(field).cat(closeEnd?LTEQ:LT).value(end).Close();
  }

  /**
   * this variant presumes you are sloppy with the end points and want a
   * 'normal' interior range.
   * @deprecated
   */
  public QueryString inRange(String field, String start, String end, boolean closeEnd){
    int twb=start.compareTo(end);
    return (twb<0)? rangy(false,field, start, end,closeEnd): //normal
           (twb>0)? rangy(false,field, end, start,closeEnd): //swap ends
           nvPair(field,start); //ends are the same. just do equals
  }

/**
 * @todo remove the forced AND
 */
  public QueryString timeInRange(TimeRange tr){
    return andRange(tr.fieldName(),tr);
  }

  public QueryString andRange(String rangeName,ObjectRange strange){
    if(ObjectRange.NonTrivial(strange)){
      dbg.VERBOSE("andRange:"+strange.toString());
      if(strange.singular()){
        and().nvPair(rangeName,strange.one()); //ends are the same. just do an equals
      } else {
        and().rangy(false,rangeName, strange.one(),strange.two(),true);
      }
    }
    return this;
  }

  public QueryString andModularRange(String rangeName,ObjectRange strange){
    if(StringRange.NonTrivial(strange)){
      if(strange.singular()){
        and().nvPair(rangeName,strange.one()); //ends are the same. just do equals
      } else {
        and().rangy(true,rangeName, strange.one(),strange.two(),true);
      }
    }
    return this;
  }



  public static final QueryString rangeQuery(String field, String start, String end, boolean closeEnd) {
    return QueryString.Clause("").inRange(field, start, end, closeEnd);
  }

/**
 * before adding the 'closeEnd' parameter the end was always closed.
 */
  public QueryString inRange(String field, String start, String end){
    return inRange(field, start, end,true);
  }

/**
 * deals with a modular range, where the end might be less than the start.
 * this depends upon valid data and that end and start are already modularized
 * IF the values are equal it is presumed they want that one alone.
 */
  public QueryString inModularRange(String field, String start, String end,boolean closeEnd){
    return rangy( (start.compareTo(end)>0) ,field,start,end,closeEnd);
  }
/**
 * before adding the 'closeEnd' parameter the end was always closed.
 */

  public QueryString inModularRange(String field, String start, String end){
    return inModularRange(field, start, end, true);
  }
  ///////////////////////////////
  public QueryString(String starter) {
    start(starter);
  }

  public static final QueryString Select(String first){
    QueryString qs=new QueryString(SELECT);
    return qs.cat(first);
  }

  public QueryString from(String first){
    return cat(FROM).cat(first);
  }

  public QueryString from(TableProfile first){
    return cat(FROM).cat(first.name());
  }

  public QueryString union(QueryString two){
    return cat(UNION).cat(two.toString());
  }

  public static final QueryString SelectAllFrom(String first){
    QueryString qs=new QueryString(SELECTALLFROM);
    return qs.cat(first);
  }

  public static final QueryString Insert(String tablename){
    QueryString qs=new QueryString(INSERTINTO);
    return qs.cat(tablename);
  }
  public static final QueryString Insert(String tablename, TextList fields, TextList values) {
      return QueryString.Insert(tablename, fields, values, null);
  }
  // this next function is needed by the porter:
  public static final QueryString Insert(String tablename, TextList fields, TextList values, String whereClause) {
    // +++ test for field count matching value count!  what to do it they don't?
    QueryString query = QueryString.Insert(tablename);
    if((fields != null) && (values != null)) {
      if(fields.size() != values.size()) {
        dbg.ERROR("Insert(): fields and values lists are not the same length !!!");
      }
      int howMany = Math.min(fields.size(), values.size());
      for(int i = 0; i < howMany; i++) {
        String field = fields.itemAt(i);
        if(i == 0) {
          query.Open(field);
        } else {
          query.comma(field);
        }
      }
      query.Close();
      for(int i = 0; i < howMany; i++) {
        String value = values.itemAt(i);
        if(i == 0) {
          query.Values(value);
        } else {
          query.commaQuoted(value);
        }
      }
      query.Close();
      if(Safe.NonTrivial(whereClause)) {
        query.cat(whereClause);
      }
    }
    return query;
  }
  // least possible coding: utilize above function (for now; eventually get rid of above function)
  public static final QueryString Insert(String tablename, Properties toInsert) {
    TextList fields = new TextList(70);
    TextList values = new TextList(70);
    if(toInsert != null) {
      for(Enumeration ennum = toInsert.propertyNames(); ennum.hasMoreElements();) {
        String name = (String)ennum.nextElement();
        fields.add(name);
        values.add(toInsert.getProperty(name));
      }
    }
    return Insert(tablename, fields, values);
  }

  public static final QueryString Update(String tablename){
    QueryString qs=new QueryString(UPDATE);
    return qs.cat(tablename);
  }

  public static final QueryString createTable(String tablename) {
    QueryString qs=new QueryString(CREATETABLE);
    return qs.cat(tablename);
  }

  public static final QueryString Clause(String predicate){
    return new QueryString(predicate+SPACE);
  }

  public boolean equals(Object o){
    if(o instanceof StringBuffer){
      return ((StringBuffer)o).equals(guts);
    }
    if(o instanceof String){
      return ((String)o).equals(guts);
    }
    return false;
  }

  // testing these new things ...
  public static final QueryString generateTableCreate(TableProfile tp) {
    QueryString qs = QueryString.createTable(tp.name());
    if(tp.numColumns() > 1) {
      qs.Open(EMPTY);
    }
    for(int i = 0; i < tp.numColumns(); i++) {
      qs.createFieldClause(tp.column(i), false /* not an existing table */);
      // on all but the last one, add a comma
      if(!(i == (tp.numColumns()-1))) {
        qs.comma(EMPTY);
      }
    }
    if(tp.numColumns() > 1) {
      qs.Close();
    }
    qs.word(LOCKMODEROW);
    return qs;
  }

  public QueryString createFieldClause(ColumnProfile cp, boolean existingTable) {
    QueryString qs = this;
    qs.word(cp.name());
    DBTypesFiltered dbt = new DBTypesFiltered(cp.type().toUpperCase());
    String addMore = "";
    if(cp.autoIncrement()) {
      if(dbt.is(dbt.SERIAL)) {
        // this is fine!
      } else {
        if(dbt.is(dbt.INT) ||
           dbt.is(dbt.INTEGER) ||
           dbt.is(dbt.SMALLINT) ||
           dbt.is(dbt.TINYINT)) {
          dbt = new DBTypesFiltered(DBTypesFiltered.SERIAL);
        } else {
          // +++ bitch !!!
        }
      }
    }
    switch(dbt.Value()) {
      case DBTypesFiltered.CHAR: {
        qs.append(dbt.Image()).Open(EMPTY + cp.size()).Close();
      } break;
      // +++ DO MORE OF THESE!!!
      case DBTypesFiltered.DATETIME: {
        qs.append(dbt.Image());
      }
      default: {
        qs.append(dbt.Image());
        dbg.ERROR("Data type not considered in createFieldClause(): " + dbt.Image() + "!");
      }
    }
    qs.word(addMore);
    qs.word(cp.nullImage());
    return qs;
  }

  private QueryString primaryKey(String fieldExpression) {
    return append(PRIMARYKEY).parenth(fieldExpression);
  }

  public static final QueryString PrimaryKeyConstraint(String constraintName, String tableName, String fieldExpression) {
    QueryString qs=new QueryString(ALTERTABLE).append(tableName).append(ADDCONSTRAINT).primaryKey(fieldExpression).append(CONSTRAINT).append(constraintName);
    return qs;
  }

  public static final QueryString CreateIndex(String indexName, String tableName, String fieldExpression) {
    QueryString qs=new QueryString(CREATEINDEX).append(indexName).append(ON).append(tableName).parenth(fieldExpression);
    return qs;
  }

}
//$Id: QueryString.java,v 1.31 2001/11/16 01:34:29 mattm Exp $
