package net.paymate.database.sql;
import java.sql.*;
import java.io.*;
import net.paymate.database.*;
import net.paymate.util.ErrorLogStream;

/**
 *  Generates unique values for use as keys. Constructor is not public. Use
 *  DbDatabase.getSequence().
 *
 * @author     Chris
 * @created    December 13, 2001
 */

public class DbSequence {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DbSequence.class);

  String name;

  DbSequence(String name) {
    this.name = name;
  }

  public int next() throws Exception {
//      String sql = "";//props.getProperty(db.getProperty("vendor") + ".nextSequenceSql");
//      sql = net.paymate.lang.StringX.replace(sql, "${name}", name);
//      dbg.ERROR("DbSequence.next: "+ sql);
//      PreparedStatement ps = con.prepareStatement(sql);
//      ResultSet rs = ps.executeQuery();
//      if (!rs.next()) {
//        throw new Exception("Can't read " + name + " sequence");
//      }
//      int rtn = rs.getInt(1);
//      ps.close();
//      return rtn;
      return -1;
  }
}
