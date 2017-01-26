package net.paymate.hwApplet;

// $Id: MACaccess1.java,v 1.4 2003/05/26 17:13:50 mattm Exp $

/*
This java + dll kit successfully acquires the MAC address,
when this program is run on WIN-98 systems ONLY!

In this directory are the Windows DLL file and Java class file,
for acquiring the MAC address.
Copy the files to a common directory, and type "java MACaccess1".
You should get your ethernet MAC address printed out to the console.

Also, find the source code, both the C code and the JAVA code.
Type the following, as an example, to use the JNI application:

javac MACaccess1.java
javah -jni MACaccess1
#<roll yer own DLL, using the "bar.cpp" file: I use borland's command-line compiler>
java MACaccess1
#<you should have your MAC address printed here!>

*/

class MACaccess1 {
	public native void foo();

	public static final void main(String args[]) {
    	try {
    		System.loadLibrary("bar");
        } catch (Error e) {
        	System.out.println("Oooops.");
        }
    	System.out.println("Beginning....");
        new MACaccess1().foo();
        System.out.println("finished.");
    }
}


// $Id: MACaccess1.java,v 1.4 2003/05/26 17:13:50 mattm Exp $

///////////////////////////////////////////////////////////////////////
// MACAddress.h
///////////////////////////////////////////////////////////////////////
/*
// DO NOT EDIT THIS FILE - it is machine generated
#include "jni.h"
// Header for class MACaccess1

#ifndef _Included_MACaccess1
#define _Included_MACaccess1
#ifdef __cplusplus
extern "C" {
#endif

// Class:     MACaccess1
// Method:    foo
/ Signature: ()V

JNIEXPORT void JNICALL Java_MACaccess1_foo
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
*/

///////////////////////////////////////////////////////////////////////
// MACAddress.c
///////////////////////////////////////////////////////////////////////

/*  MACAddress.c -- use this with no args, and it returns
 *                  the MAC (Media Access Control) addresses
 *                  of all NICs inside your windoze PC.
 *                  If it doesn't work, it's because you don't
 *                  have NETBIOS bindings to your NIC.
 *
 *   May 16, 2000   -- Initial release. Good luck using it!
 *
 * $Id: MACaccess1.java,v 1.4 2003/05/26 17:13:50 mattm Exp $
 */
/*
#include <windows.h>
#include <wincon.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include "MACaccess1.h"

typedef struct _ASTAT_ {
   	ADAPTER_STATUS adapt;
	NAME_BUFFER    NameBuff [30];
}ASTAT;

   ASTAT Adapter;

JNIEXPORT void JNICALL
Java_MACaccess1_foo(JNIEnv *env, jobject thisObj) {
   	NCB Ncb;
	UCHAR uRetCode;
	char NetName[50];
	LANA_ENUM   lenum;
	int      i;

	memset( &Ncb, 0, sizeof(Ncb) );
	Ncb.ncb_command = NCBENUM;
	Ncb.ncb_buffer = (UCHAR *)&lenum;
	Ncb.ncb_length = sizeof(lenum);
	uRetCode = Netbios( &Ncb );

	for(i=0; i < lenum.length ;i++) {
		memset( &Ncb, 0, sizeof (Ncb) );
		Ncb.ncb_command = NCBASTAT;
		Ncb.ncb_lana_num = lenum.lana[i];

		strcpy( Ncb.ncb_callname,  "*               " );
		Ncb.ncb_buffer = (char *) &Adapter;
		Ncb.ncb_length = sizeof(Adapter);

		uRetCode = Netbios( &Ncb );
		if ( uRetCode == 0 ) {
			printf( "The Ethernet Number for this PC is: %02x:%02x:%02x:%02x:%02x:%02x.\n",
				Adapter.adapt.adapter_address[0],
				Adapter.adapt.adapter_address[1],
				Adapter.adapt.adapter_address[2],
				Adapter.adapt.adapter_address[3],
				Adapter.adapt.adapter_address[4],
				Adapter.adapt.adapter_address[5] );
		}
	}

}
*/