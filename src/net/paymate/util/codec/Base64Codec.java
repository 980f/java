/**
 * Title:        Base64Codec<p>
 * Description:  Base64Codec<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Base64Codec.java,v 1.4 2005/02/28 05:01:38 andyh Exp $
 */

// +++ rewrite this as a filterstream !!!

// if you are doing pieces of a larger block of data (file for instance),
// except for the last block you send,
// only pass in blocks (arrays) of bytes whose length is evenly divisible by 3
// and only pass in blocks of chars whose length is evenly divisible by 4

// it does work, though.

package net.paymate.util.codec;



public class Base64Codec {

  // code characters for values 0..63
  private static final char alphabet[] =
    new String("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=").toCharArray();
  private static final char XX = 255;
  // Table for decoding base64
  private static final char codes[] = {
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,62, XX,XX,XX,63,
    52,53,54,55, 56,57,58,59, 60,61,XX,XX, XX,XX,XX,XX,
    XX, 0, 1, 2,  3, 4, 5, 6,  7, 8, 9,10, 11,12,13,14,
    15,16,17,18, 19,20,21,22, 23,24,25,XX, XX,XX,XX,XX,
    XX,26,27,28, 29,30,31,32, 33,34,35,36, 37,38,39,40,
    41,42,43,44, 45,46,47,48, 49,50,51,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
  };

  public static final String toString(byte dataX3[]){
    char []ncoded=encode(dataX3);
    return new String(ncoded);
  }

  // returns an array of base64-encoded characters to represent the passed data array.
  public static final char[] encode(byte dataX3[])
  {
    int length = dataX3.length;
    int len = ((length + 2) / 3) * 4;
    char out[] = new char[len];

    // 3 bytes encode to 4 chars.  Output is always an even multiple of 4 characters.
    for (int i=0, index=0; i<length; i+=3, index+=4) {
      boolean quad = false;
      boolean trip = false;
      //assemble 24 bit integer inside a 32 bit one.
      int val = (0xFF & (int) dataX3[i]);
      val <<= 8;
      if ((i+1) < length) {
        val |= (0xFF & (int) dataX3[i+1]);
        trip = true;
      }
      val <<= 8;
      if ((i+2) < length) {
        val |= (0xFF & (int) dataX3[i+2]);
        quad = true;
      }
      //separate into base 64 digits
      out[index+3] = alphabet[(quad? (val & 0x3F): 64)];
      val >>= 6;
      out[index+2] = alphabet[(trip? (val & 0x3F): 64)];
      val >>= 6;
      out[index+1] = alphabet[val & 0x3F];
      val >>= 6;
      out[index+0] = alphabet[val & 0x3F];
    }
    return out;
  }

 /**
 *    @return an array of base64-encoded characters to represent the passed data array.
 *     3 bytes encode to 4 chars.  Output is always an even multiple of 4 characters.
 *     implemented with short data types for easy translation to microcontroller implementations.
 *     easily converted into streamable version.
 */
  public static final char[] fastEncode(byte dataX3[]){
    int length = dataX3.length;
    char out[] = new char[((length + 2) / 3) * 4]; //multiple of 3 rounded up, times 4
    byte carry;
    byte eightbits;
    int get=0;
    int put=0;
    boolean haveInput;//assignments to haveInput are for debug only
    while(haveInput=get<length) {
      eightbits= dataX3[get++];
      out[put++] = alphabet[         ((eightbits&0xFC) >>2)];//1 of 4
      carry = (byte) ((eightbits&0x03)<<4) ;//carry to 2nd output
      haveInput= get<length;
      if(haveInput){
        eightbits= dataX3[get++];
        out[put++] = alphabet[ carry | ((eightbits&0xF0)>>4) ];//2 of 4
        carry=  (byte) ((eightbits&0x0F)<<2) ; //carry to third output
        haveInput= get<length;
        if(haveInput){
          eightbits= dataX3[get++];
          out[put++] = alphabet[ carry| ((eightbits&0xC0)>>6) ];//3 of 4
          out[put++] = alphabet[eightbits&0x3F];// 4 of 4
        } else {//two bytes splits into 2nd&3rd outputs, pad 4th
          out[put++] = alphabet[carry];// 3 of 4
          out[put++] = alphabet[64];//pad 4 of 4
        }
      } else { //one byte splits into first and second, pad 3rd&4th
        out[put++] = alphabet[carry];// 2 of 4
        out[put++] = alphabet[64];//pad 3 of 4
        out[put++] = alphabet[64];//pad 4 of 4
      }
    }
    return out;
  }

  public static final byte[] fromString(String s){
    char [] caster = new char[s.length()];
    for(int i=caster.length;i-->0;){
      caster[i]= s.charAt(i);
    }
    return decode(caster);
  }


  // Returns an array of bytes which were encoded in the passed character array.
  public static final byte[] decode(char dataX4[])
  {
    int length = dataX4.length;
    int len = ((length + 3) / 4) * 3;
    if (length>0 && dataX4[length-1] == '=') --len;
    if (length>1 && dataX4[length-2] == '=') --len;
    byte out[] = new byte[len];

    int shift = 0;   // # of excess bits stored in accum
    int accum = 0;   // excess bits
    int index = 0;

    for (int ix=0; ix<length; ix++) {
      int value = codes[ dataX4[ix] & 0xFF ];   // ignore high byte of char
      if ( value >= 0 ) {                     // skip over non-code
        accum <<= 6;            // bits shift up by 6 each time thru
        shift += 6;             // loop, with new bits being put in
        accum |= value;         // at the bottom.
        if ( shift >= 8 ) {     // whenever there are 8 or more shifted in,
          shift -= 8;         // write them out (from the top, leaving any
          byte c = (byte) ((accum >> shift) & 0xff);
          if(index < len) {
            out[index++] = c;     // excess at the bottom for next iteration.
          }
        }
      }
    }
    return out;
  }

  // testing stuff:
  public static final String uncoded(char []coded){
    int length=coded.length;
    StringBuffer unc=new StringBuffer(length);
    unc.setLength(length);
    while(length-->0){
      unc.setCharAt(length, codes[ coded[length]]);
    }
    return unc.toString();
  }

  public static final void main(String[] argv) {
    try {
      int argc=argv.length;
      boolean deen = true;
      boolean fromfile = false;
      String str  = "!_!";
      switch (argc){
        case 2: str  = argv[--argc];
        case 1: deen = !argv[--argc].startsWith("-");
          String filetest = argv[argc];
          fromfile = (filetest.length() > 1) &&
              ((filetest.charAt(1) == 'f') || (filetest.charAt(1) == 'F'));
          break;
        default: System.out.println("Excess command line args\n");
        case 0:  System.out.println("parameters: {+|-}[f] string");
          return;
      }
      if(fromfile) {
        String file = str;
        str = net.paymate.io.IOX.FileToString(file);
        System.out.println("loading string from file " + file + " ...");
      }
      System.out.println( (deen ? "En" : "De") + "coding '" +
                          (deen ? net.paymate.util.Ascii.image(str.getBytes()).toString() : str) +
                          "' \nresulted in '" +
                          (deen ? new String(encode(str.getBytes())) :
                           net.paymate.util.Ascii.image(decode(str.toCharArray())).toString()) + "'.");
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}

/*
import java.io.*;

public class BASE64Encoder extends CharacterEncoder
{
    private static final char[] pem_array
  = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    protected int bytesPerAtom() {
  return 3;
    }

    protected int bytesPerLine() {
  return 57;
    }

    protected void encodeAtom(OutputStream outputstream, byte[] is, int i,
            int i_0_)
  throws IOException {
  if (i_0_ == 1) {
      int i_1_ = is[i];
      int i_2_ = 0;
      boolean bool = false;
      outputstream.write(pem_array[i_1_ >>> 2 & 0x3f]);
      outputstream.write(pem_array[(i_1_ << 4 & 0x30) + (i_2_ >>> 4
                     & 0xf)]);
      outputstream.write(61);
      outputstream.write(61);
  } else if (i_0_ == 2) {
      int i_3_ = is[i];
      int i_4_ = is[i + 1];
      int i_5_ = 0;
      outputstream.write(pem_array[i_3_ >>> 2 & 0x3f]);
      outputstream.write(pem_array[(i_3_ << 4 & 0x30) + (i_4_ >>> 4
                     & 0xf)]);
      outputstream.write(pem_array[(i_4_ << 2 & 0x3c) + (i_5_ >>> 6
                     & 0x3)]);
      outputstream.write(61);
  } else {
      int i_6_ = is[i];
      int i_7_ = is[i + 1];
      int i_8_ = is[i + 2];
      outputstream.write(pem_array[i_6_ >>> 2 & 0x3f]);
      outputstream.write(pem_array[(i_6_ << 4 & 0x30) + (i_7_ >>> 4
                     & 0xf)]);
      outputstream.write(pem_array[(i_7_ << 2 & 0x3c) + (i_8_ >>> 6
                     & 0x3)]);
      outputstream.write(pem_array[i_8_ & 0x3f]);
  }
    }
}


public class BASE64Decoder extends CharacterDecoder
{
    private static final char[] pem_array
  = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
    private static final byte[] pem_convert_array = new byte[256];
    byte[] decode_buffer = new byte[4];

    protected int bytesPerAtom() {
  return 4;
    }

    protected int bytesPerLine() {
  return 72;
    }

    protected void decodeAtom(InputStream inputstream,
            OutputStream outputstream, int i)
  throws IOException {
  int i_0_ = -1;
  int i_1_ = -1;
  int i_2_ = -1;
  int i_3_ = -1;
  if (i < 2)
      throw new IOException
          ("BASE64Decoder: Not enough bytes for an atom.");
  int i_4_;
  do {
      i_4_ = inputstream.read();
      if (i_4_ == -1)
    throw new CEStreamExhausted();
  } while (i_4_ == 10 || i_4_ == 13);
  decode_buffer[0] = (byte) i_4_;
  i_4_ = this.readFully(inputstream, decode_buffer, 1, i - 1);
  if (i_4_ == -1)
      throw new CEStreamExhausted();
  if (i > 3 && decode_buffer[3] == 61)
      i = 3;
  if (i > 2 && decode_buffer[2] == 61)
      i = 2;
  switch (i) {
  case 4:
      i_3_ = pem_convert_array[decode_buffer[3] & 0xff];
      // fall through
  case 3:
      i_2_ = pem_convert_array[decode_buffer[2] & 0xff];
      // fall through
  case 2:
      i_1_ = pem_convert_array[decode_buffer[1] & 0xff];
      i_0_ = pem_convert_array[decode_buffer[0] & 0xff];
      // fall through
  default:
      switch (i) {
      case 2:
    outputstream.write((byte) (i_0_ << 2 & 0xfc
             | i_1_ >>> 4 & 0x3));
    break;
      case 3:
    outputstream.write((byte) (i_0_ << 2 & 0xfc
             | i_1_ >>> 4 & 0x3));
    outputstream.write((byte) (i_1_ << 4 & 0xf0
             | i_2_ >>> 2 & 0xf));
    break;
      case 4:
    outputstream.write((byte) (i_0_ << 2 & 0xfc
             | i_1_ >>> 4 & 0x3));
    outputstream.write((byte) (i_1_ << 4 & 0xf0
             | i_2_ >>> 2 & 0xf));
    outputstream.write((byte) (i_2_ << 6 & 0xc0 | i_3_ & 0x3f));
    break;
      }
  }
    }

    static {
  for (int i = 0; i < 255; i++)
      pem_convert_array[i] = (byte) -1;
  for (int i = 0; i < pem_array.length; i++)
      pem_convert_array[pem_array[i]] = (byte) i;
    }
}
*/


/*
// for raw unsigned char data, since nstring does not like anything that doesn't end in a '\0' (and resizes with that in mind)
class uCharBuffer : public dVector<unsigned char>
{
public:
  void fromString(const char *tmp)
  {
    if(!tmp) {
      resize(0);
      return;
    }
    resize(safeStrLen(tmp));
    int i;
    forVectorElements((*this), i) {
      (*this)[i] = tmp[i];
    }
  }

  nString AsnString()
  {
    nString tmp(size() + 1);
    int i;
    forVectorElements((*this), i) {
      tmp[i] = (*this)[i];
    }
    return tmp;
  }
};

*/
// NOTE!!!!!  '\0' are not appended to strings,
//            as the buffers could contain ANYTHING, not just printable characters
//            therefore, you should make your buffer one char longer than needed and
//            place a '\0' in the character position at usedLength.

/*
base64 encoding, in short

QP (base64-encoded) contains uppercase, lowercase, numbers, '+', '/' and '='.

Take the encoded stuff in groups of 4 characters and turn each character into a code 0 to 63 thus:

    A means of labelling the content of mail messages.
    A-Z map to 0 to 25
    a-z map to 26 to 51
    0-9 map to 52 to 61
    + maps to 62
    / maps to 63

Express the four numbers thus found (all 0 to 63) in binary:

00aaaaaa 00bbbbbb 00cccccc 00dddddd

This then maps to _three_ real bytes formed thus:

aaaaaabb bbbbcccc ccdddddd

Equals signs (one or two) are used at the end of the encoded block to indicate that the text was not an integer multiple of three bytes long.

postmaster@epfl.ch
See:  http://www.cis.ohio-state.edu/htbin/rfc/rfc2045.html
      http://info.internet.isi.edu/in-notes/rfc/files/rfc1421.txt
*/

