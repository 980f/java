package net.paymate.database.sql;
import java.util.*;
import java.sql.*;
import net.paymate.database.*;
import net.paymate.util.ErrorLogStream;

/**
 *  A class used to delete records from SQL tables. The constructor is not
 *  public. To obtain a DbDeleter call DbTable.deleter(); Example: To delete all
 *  the people who are (younger than 18 or older than 80) and whose name is
 *  "Fred"... <PRE>
 * DbDatabase db = ...;
 * DbTable people = db.getTable("PEOPLE");
 * DbDeleter deleter = people.deleter();
 * deleter.setWhere(people.getColumn("AGE").lessThan(new Integer(18)).or(
 *    people.getColumn("AGE").greaterThan(new Integer(80))).and(
 *    people.getColumn("NAME").equal("FRED"));
 * int numberOfPeopleDeleted = deleter.execute();
 * </PRE> This is equivilent to... <PRE>
 * DELETE FROM PEOPLE WHERE (AGE < 18 OR AGE > 80 ) AND NAME='Fred'
 * </PRE> Note the use of equal(), NOT equals().
 *
 * @author     Chris Bitmead
 * @created    December 13, 2001
 */

public class DbDeleter {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DbDeleter.class);

  TableProfile table;
  DbExpr where;

  DbDeleter(TableProfile table) {
    this.table = table;
  }

  /**
   *  Set the where condition for this delete operation.
   *
   * @param  where  The new where value
   */
  public void setWhere(DbExpr where) {
    this.where = where;
  }

  /**
   *  Execute this delete command on a specific connection.
   *
   * @param  dbcon            Description of Parameter
   * @return                  The number of record affected.
   * @exception  Exception  Description of Exception
   */
  public int execute() {
//      PreparedStatement stmt = dbcon.con.prepareStatement(getQueryString());
//      if (where != null) {
//        int i = where.setSqlValues(stmt, 1);
//      }
//      return stmt.executeUpdate();
    return -1;//
  }

  String getQueryString() throws Exception {
    String rtn = "DELETE FROM " + table.fullname();
    if (where != null) {
      rtn += " WHERE ";
      rtn += where.getQueryString();
    }
    dbg.WARNING("getQueryString(): "+ rtn);
    return rtn;
  }

}
