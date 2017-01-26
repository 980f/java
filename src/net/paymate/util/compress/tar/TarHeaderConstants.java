package net.paymate.util.compress.tar;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/compress/tar/TarHeaderConstants.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.Ascii;

public interface TarHeaderConstants {
  /**
   * The length of the name field in a header buffer.
   */
  public static final int		NAMELEN = 100;
  /**
   * The length of the mode field in a header buffer.
   */
  public static final int		MODELEN = 8;
  /**
   * The length of the user id field in a header buffer.
   */
  public static final int		UIDLEN = 8;
  /**
   * The length of the group id field in a header buffer.
   */
  public static final int		GIDLEN = 8;
  /**
   * The length of the checksum field in a header buffer.
   */
  public static final int		CHKSUMLEN = 8;
  /**
   * The length of the size field in a header buffer.
   */
  public static final int		SIZELEN = 12;
  /**
   * The length of the magic field in a header buffer.
   */
  public static final int		MAGICLEN = 8;
  /**
   * The length of the modification time field in a header buffer.
   */
  public static final int		MODTIMELEN = 12;
  /**
   * The length of the user name field in a header buffer.
   */
  public static final int		UNAMELEN = 32;
  /**
   * The length of the group name field in a header buffer.
   */
  public static final int		GNAMELEN = 32;
  /**
   * The length of the devices field in a header buffer.
   */
  public static final int		DEVLEN = 8;

  /**
   * LF_ constants represent the "link flag" of an entry, or more commonly,
   * the "entry type". This is the "old way" of indicating a normal file.
   */
  public static final byte	LF_OLDNORM	= 0;
  /**
   * Normal file type.
   */
  public static final byte	LF_NORMAL	= Ascii.ZERO;//(byte) '0';
  /**
   * Link file type.
   */
  public static final byte	LF_LINK		= Ascii.ONE;//(byte) '1';
  /**
   * Symbolic link file type.
   */
  public static final byte	LF_SYMLINK	= Ascii.TWO;//(byte) '2';
  /**
   * Character device file type.
   */
  public static final byte	LF_CHR		= Ascii.THREE;//(byte) '3';
  /**
   * Block device file type.
   */
  public static final byte	LF_BLK		= Ascii.FOUR;//(byte) '4';
  /**
   * Directory file type.
   */
  public static final byte	LF_DIR		= Ascii.FIVE;//(byte) '5';
  /**
   * FIFO (pipe) file type.
   */
  public static final byte	LF_FIFO		= Ascii.SIX;//(byte) '6';
  /**
   * Contiguous file type.
   */
  public static final byte	LF_CONTIG	= Ascii.SEVEN;//(byte) '7';

  /**
   * The magic tag representing a POSIX tar archive.
   */
  public static final String	TMAGIC		= "ustar";

  /**
   * The magic tag representing a GNU tar archive.
   */
  public static final String	GNU_TMAGIC	= "ustar  ";

}