package net.paymate.database.sql;
import java.sql.*;
import java.util.*;

/**
 * An SQL expression of the form FUNCNAME(parameter....).
 *
 * @author Chris Bitmead
 */
public class DbMiscExpr extends DbExpr {
  String func;
  Object args[];

  public DbMiscExpr(String func) {
    super();
    this.func = func;
  }

  public DbMiscExpr(String func, Object arg1) {
      super();
    this.func = func;
    args = new Object[1];
    args[0] = arg1;
  }

  public DbMiscExpr(String func, Object arg1, Object arg2) {
      super();
    this.func = func;
    args = new Object[2];
    args[0] = arg1;
    args[1] = arg2;
  }

  public String getQueryString() throws Exception {
    StringTokenizer st = new StringTokenizer(func, "?");
    String rtn = "";
    int c = 0;
    while (st.hasMoreTokens()) {
      rtn += st.nextToken();
      if (c < args.length) {
        rtn += getString(args[c++]);
      }
    }
    return rtn;
  }

  public int setSqlValues(PreparedStatement ps, int i) throws Exception, SQLException {
    for (int c = 0; c < args.length; c++) {
      i = setSqlValue(ps, i, args[c], null);
    }
    return i;
  }

  public void usesTables(Set coll) {
    for (int c = 0; c < args.length; c++) {
      usesTables(coll, args[c]);
    }
  }
}
