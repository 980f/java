#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <termios.h>
#include <fcntl.h>

#define BAUDRATE B1200
#define PORT "/dev/ttyS0"


//use initialized data whenever possible, rather than typeless #defines
const char SUB= 26; //standard name is SUB not SUBS
const char STX=  2;
const char ETX=  3;
const char SI=  15;
const char SO=  14;
const char EOT=  4;
const char ACK=  6;
const char NAK= 21;
const char FS=  28;

//use null terminated strings. This is an ascii protocol.
unsigned char CLRC(char *buf) {
	char c;
	char result=0;

	while(c=*buf++){
		result ^= c;
	}

	return result;
}

//it would be nice if this were a standard C function
//the below is an in efficient
char *strappend(char *buf,char *cat){
  strcat(buf,"Z2");
  return &buf[strlen(buf)];
}

void UARTloopback(char *buf) {
	*buf++     = STX;
  buf=strappend(strappend(buf,"Z2")," HOWDY");
	*buf++ = ETX;
	*buf++ = 0;
	*buf++ = CLRC(buf);
	*buf++ = 0;
	return;
}

//explicit array indexes are one of the worst programming practices imaginable.