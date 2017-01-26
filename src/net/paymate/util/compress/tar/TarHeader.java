/*
 ** Authored by Timothy Gerard Endres
 ** <mailto:time@gjt.org>  <http://www.trustice.com>
 **
 ** This work has been placed into the public domain.
 ** You may use this work in any way and for any purpose you wish.
 **
 ** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
 ** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
 ** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
 ** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 ** REDISTRIBUTION OF THIS SOFTWARE.
 **
 */

package net.paymate.util.compress.tar;

/**
 * This class encapsulates the Tar Entry Header used in Tar Archives.
 * The class also holds a number of tar constants, used mostly in headers.
 */

import net.paymate.util.Ascii;

public class TarHeader implements TarHeaderConstants {

  public StringBuffer name; // The entry's name.
  public int mode; // The entry's permission mode.
  public int userId; // The entry's user id.
  public int groupId; // The entry's group id.
  public long size; // The entry's size.
  public long modTime; // The entry's modification time.
  public int checkSum; // The entry's checksum.
  public byte linkFlag; // The entry's link flag.
  public StringBuffer linkName; // The entry's link name.
  public StringBuffer magic; // The entry's magic tag.
  public StringBuffer userName; // The entry's user name.
  public StringBuffer groupName; // The entry's group name.
  public int devMajor; // The entry's major device number.
  public int devMinor; // The entry's minor device number.

  public TarHeader() {
    this.magic = new StringBuffer(TarHeader.TMAGIC);

    this.name = new StringBuffer();
    this.linkName = new StringBuffer();

    String user =
        System.getProperty("user.name", "");

    if(user.length() > 31) {
      user = user.substring(0, 31);
    }
    this.userId = 0;
    this.groupId = 0;
    this.userName = new StringBuffer(user);
    this.groupName = new StringBuffer("");
  }

  private static StringBuffer cloneName(StringBuffer name){
    return name == null ? null : new StringBuffer(name.toString());
  }

  /**
   * TarHeaders can be cloned.
   */
  public Object clone() {
    TarHeader hdr = null;

    try {
      hdr = (TarHeader)super.clone();

      hdr.name = cloneName(this.name);
      hdr.mode = this.mode;
      hdr.userId = this.userId;
      hdr.groupId = this.groupId;
      hdr.size = this.size;
      hdr.modTime = this.modTime;
      hdr.checkSum = this.checkSum;
      hdr.linkFlag = this.linkFlag;
      hdr.linkName = cloneName(this.linkName);
      hdr.magic = cloneName(this.magic);
      hdr.userName = cloneName(this.userName );
      hdr.groupName = cloneName(this.groupName);
      hdr.devMajor = this.devMajor;
      hdr.devMinor = this.devMinor;
    } catch(CloneNotSupportedException ex) {
      ex.printStackTrace();
    }

    return hdr;
  }

  /**
   * Get the name of this entry.
   *
   * @return Teh entry's name.
   */
  public String getName() {
    return this.name.toString();
  }

  /**
   * Parse an octal string from a header buffer. This is used for the
   * file permission mode value.
   *
   * @param header The header buffer from which to parse.
   * @param offset The offset into the buffer from which to parse.
   * @param length The number of header bytes to parse.
   * @return The long value of the octal string.
   */
  public static long parseOctal(byte[] header, int offset, int length) throws InvalidHeaderException {
    long result = 0;
    boolean stillPadding = true;

    int end = offset + length;
    for(int i = offset; i < end; ++i) {
      if(header[i] == 0) {
        break;
      }

      if(header[i] == Ascii.SP /*(byte) ' '*/ ||
         header[i] == Ascii.ZERO /*'0'*/) {
        if(stillPadding) {
          continue;
        }

        if(header[i] == Ascii.SP /*(byte) ' '*/) {
          break;
        }
      }

      stillPadding = false;

      result =
          (result << 3)
          + (header[i] - '0');
    }

    return result;
  }

  /**
   * Parse an entry name from a header buffer.
   *
   * @param header The header buffer from which to parse.
   * @param offset The offset into the buffer from which to parse.
   * @param length The number of header bytes to parse.
   * @return The header's entry name.
   */
  public static StringBuffer parseName(byte[] header, int offset, int length) throws InvalidHeaderException {
    StringBuffer result = new StringBuffer(length);

    int end = offset + length;
    for(int i = offset; i < end; ++i) {
      if(header[i] == 0) {
        break;
      }
      result.append( (char) header[i]);
    }

    return result;
  }

  /**
   * Determine the number of bytes in an entry name.
   *
   * @param header The header buffer from which to parse.
   * @param offset The offset into the buffer from which to parse.
   * @param length The number of header bytes to parse.
   * @return The number of bytes in a header's entry name.
   */
  public static int getNameBytes(StringBuffer name, byte[] buf, int offset, int length) {
    int i;

    for(i = 0; i < length && i < name.length(); ++i) {
      buf[offset + i] = (byte) name.charAt(i);
    }

    for(; i < length; ++i) {
      buf[offset + i] = 0;
    }

    return offset + length;
  }

  /**
   * Parse an octal integer from a header buffer.
   *
   * @param header The header buffer from which to parse.
   * @param offset The offset into the buffer from which to parse.
   * @param length The number of header bytes to parse.
   * @return The integer value of the octal bytes.
   */
  public static int getOctalBytes(long value, byte[] buf, int offset, int length) {
    byte[] result = new byte[length];

    int idx = length - 1;

    buf[offset + idx] = 0;
    --idx;
    buf[offset + idx] = Ascii.SP /*(byte) ' '*/;
    --idx;

    if(value == 0) {
      buf[offset + idx] = Ascii.ZERO /*(byte) '0'*/;
      --idx;
    } else {
      for(long val = value; idx >= 0 && val > 0; --idx) {
        buf[offset + idx] = (byte)(Ascii.ZERO /*(byte) '0'*/ + (byte) (val & 7));
        val = val >> 3;
      }
    }

    for(; idx >= 0; --idx) {
      buf[offset + idx] = Ascii.SP /*(byte) ' '*/;
    }

    return offset + length;
  }

  /**
   * Parse an octal long integer from a header buffer.
   *
   * @param header The header buffer from which to parse.
   * @param offset The offset into the buffer from which to parse.
   * @param length The number of header bytes to parse.
   * @return The long value of the octal bytes.
   */
  public static int getLongOctalBytes(long value, byte[] buf, int offset, int length) {
    byte[] temp = new byte[length + 1];
    TarHeader.getOctalBytes(value, temp, 0, length + 1);
    System.arraycopy(temp, 0, buf, offset, length);
    return offset + length;
  }

  /**
   * Parse the checksum octal integer from a header buffer.
   *
   * @param header The header buffer from which to parse.
   * @param offset The offset into the buffer from which to parse.
   * @param length The number of header bytes to parse.
   * @return The integer value of the entry's checksum.
   */
  public static int getCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
    TarHeader.getOctalBytes(value, buf, offset, length);
    buf[offset + length - 1] = Ascii.SP /*(byte) ' '*/;
    buf[offset + length - 2] = 0;
    return offset + length;
  }

}