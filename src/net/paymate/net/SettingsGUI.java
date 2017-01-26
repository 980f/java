package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/SettingsGUI.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class SettingsGUI {

  public SettingsGUI() {
  }
}

/*
#include <FL/Fl.H>
#include <FL/Fl_Window.H>
#include <FL/Fl_Button.H>
#include <FL/Fl_Light_Button.H>
#include <FL/Fl_Box.H>
#include <FL/Fl_Output.H>
#include <FL/Fl_Input.H>
#include <FL/fl_ask.H>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>

void save_button_callback(Fl_Widget *W, void *data);
void quit_button_callback (Fl_Widget *W, void *data);
void DHCP_button_callback (Fl_Widget *W, void *data);
void STATIC_button_callback (Fl_Widget *W, void *data);
void HELP_button_callback (Fl_Widget *W, void *data);
void dhcp_callback_kill(Fl_Widget *W, void *data);
void help_callback_kill(Fl_Widget *W, void *data);
void IPaddr_change_callback(Fl_Widget *W, void *data);
void netmask_change_callback(Fl_Widget *W, void *data);
void gateway_change_callback(Fl_Widget *W, void *data);
void gateway_change_callback(Fl_Widget *W, void *data);
void broadcast_change_callback(Fl_Widget *W, void *data);
void network_change_callback(Fl_Widget *W, void *data);
void dns1_change_callback(Fl_Widget *W, void *data);
void dns2_change_callback(Fl_Widget *W, void *data);
void ftps1_change_callback(Fl_Widget *W, void *data);
void ftps2_change_callback(Fl_Widget *W, void *data);
void dlg_cancel_callback(Fl_Widget *W, void *data);
void dlg_ok_callback(Fl_Widget *W, void *data);



Fl_Window *HELP_window;

Fl_Window *DLG_window;
Fl_Output *DLG_output1;
Fl_Output *DLG_output2;
Fl_Output *DLG_output3;
Fl_Input  *DLG_input;
Fl_Button *DLG_OK;
Fl_Button *DLG_CANCEL;

Fl_Output *IPaddr2;
Fl_Output *netmask2;
Fl_Output *gateway2;
Fl_Output *broadcast2;
Fl_Output *network2;
Fl_Output *dns12;
Fl_Output *dns22;
Fl_Output *ftps12;
Fl_Output *ftps22;
Fl_Output *clock2;

char IPADDR[16];
char NETMASK[16];
char GATEWAY[16];
char BROADCAST[16];
char NETWORK[16];
char DNS1[16];
char DNS2[16];
char FTPS1[16];
char FTPS2[16];
char MACID[18];
char DATE[30];
int selection = 0;

int mode = 0;  //let '0' be DHCP, a '1' will represent Static assignment

Fl_Light_Button *DHCP_button;
Fl_Light_Button *STATIC_button;
Fl_Button *HELP_button;


int main(int argc, char **argv) {
     time_t timeval;

     strcpy(IPADDR, "192.168.100.101");
     strcpy(NETMASK, "255.255.255.248");
     strcpy(GATEWAY, "192.168.100.254");
     strcpy(BROADCAST, "192.168.100.255");
     strcpy(NETWORK, "192.168.100.0");
     strcpy(DNS1, "123.255.127.100");
     strcpy(DNS2, "123.255.127.101");
     strcpy(FTPS1, "208.58.21.60");
     strcpy(FTPS2, "64.92.151.4");



     time(&timeval);
     strcpy(DATE, ctime(&timeval));
     DATE[strlen(DATE) - 1] = 0;


     Fl_Window *window = new Fl_Window(0,-60,800,600);
     window->label("PayMate.net Network Utility");

     DHCP_button = new Fl_Light_Button(1,20, 266, 80, "DHCP");
     DHCP_button->type(FL_RADIO_BUTTON);
     DHCP_button->callback(DHCP_button_callback, NULL);

     STATIC_button = new Fl_Light_Button(267, 20, 266, 80, "STATIC IP");
     STATIC_button->type(FL_RADIO_BUTTON);
     STATIC_button->callback(STATIC_button_callback, NULL);

     HELP_button = new Fl_Button(533,20, 266, 80, "HELP!");
     HELP_button->callback(HELP_button_callback, NULL);


     Fl_Button *save_button = new Fl_Button(1,540, 200, 60, "Save/Activate");
     save_button->callback(save_button_callback, NULL);

     Fl_Button *quit_button = new Fl_Button(599,540, 200, 60, "Quit");
     quit_button->callback(quit_button_callback, NULL);


     Fl_Box *stat_box = new Fl_Box(0,100,800,440, "");
     stat_box->box(FL_ENGRAVED_BOX);


     //---Done.------//
     Fl_Output *IPaddr = new Fl_Output(20, 120, 550, 20, "");
     IPaddr->box(FL_FLAT_BOX);
     IPaddr->value("IP Address:");
     IPaddr2 = new Fl_Output(570, 120, 120, 20, "");
     IPaddr2->box(FL_FLAT_BOX);
     IPaddr2->value(IPADDR);
     Fl_Button *IPaddr_change = new Fl_Button(690, 120, 90, 20, "Change...");
     IPaddr_change->callback(IPaddr_change_callback, NULL);



     //----Done.-----//
     Fl_Output *netmask = new Fl_Output(20, 160, 550, 20, "");
     netmask->box(FL_FLAT_BOX);
     netmask->value("NetMask:");
     netmask2 = new Fl_Output(570, 160, 120, 20, "");
     netmask2->box(FL_FLAT_BOX);
     netmask2->value(NETMASK);
     Fl_Button *netmask_change = new Fl_Button(690, 160, 90, 20, "Change...");
     netmask_change->callback(netmask_change_callback, NULL);


     //----Done.-----//
     Fl_Output *gateway = new Fl_Output(20, 200, 550, 20, "");
     gateway->box(FL_FLAT_BOX);
     gateway->value("Gateway Address:");
     gateway2 = new Fl_Output(570, 200, 120, 20, "");
     gateway2->box(FL_FLAT_BOX);
     gateway2->value(GATEWAY);
     Fl_Button *gateway_change = new Fl_Button(690, 200, 90, 20, "Change...");
     gateway_change->callback(gateway_change_callback, NULL);


     //----Done.-----//
     Fl_Output *broadcast = new Fl_Output(20, 240, 550, 20, "");
     broadcast->box(FL_FLAT_BOX);
     broadcast->value("Broadcast Address:");
     broadcast2 = new Fl_Output(570, 240, 120, 20, "");
     broadcast2->box(FL_FLAT_BOX);
     broadcast2->value(BROADCAST);
     Fl_Button *broadcast_change = new Fl_Button(690, 240, 90, 20, "Change...");
     broadcast_change->callback(broadcast_change_callback, NULL);


     //----Done.-----//
     Fl_Output *network = new Fl_Output(20, 280, 550, 20, "");
     network->box(FL_FLAT_BOX);
     network->value("Network Address:");
     network2 = new Fl_Output(570, 280, 120, 20, "");
     network2->box(FL_FLAT_BOX);
     network2->value(NETWORK);
     Fl_Button *network_change = new Fl_Button(690, 280, 90, 20, "Change...");
     network_change->callback(network_change_callback, NULL);


     //----Done.-----//
     Fl_Output *dns1 = new Fl_Output(20, 320, 550, 20, "");
     dns1->box(FL_FLAT_BOX);
     dns1->value("DNS1 Address:");
     dns12 = new Fl_Output(570, 320, 120, 20, "");
     dns12->box(FL_FLAT_BOX);
     dns12->value(DNS1);
     Fl_Button *dns1_change = new Fl_Button(690, 320, 90, 20, "Change...");
     dns1_change->callback(dns1_change_callback, NULL);


     //----Done.-----//
     Fl_Output *dns2 = new Fl_Output(20, 360, 550, 20, "");
     dns2->box(FL_FLAT_BOX);
     dns2->value("DNS2 Address:");
     dns22 = new Fl_Output(570, 360, 120, 20, "");
     dns22->box(FL_FLAT_BOX);
     dns22->value(DNS2);
     Fl_Button *dns2_change = new Fl_Button(690, 360, 90, 20, "Change...");
     dns2_change->callback(dns2_change_callback, NULL);


     //----Done.-----//
     Fl_Output *ftps1 = new Fl_Output(20, 400, 550, 20, "");
     ftps1->box(FL_FLAT_BOX);
     ftps1->value("Transaction Processing Server IP #1:");
     ftps12 = new Fl_Output(570, 400, 120, 20, "");
     ftps12->box(FL_FLAT_BOX);
     ftps12->value(FTPS1);
     Fl_Button *ftps1_change = new Fl_Button(690, 400, 90, 20, "Change...");
     ftps1_change->callback(ftps1_change_callback, NULL);

     //----Done.-----//
     Fl_Output *ftps2 = new Fl_Output(20, 440, 550, 20, "");
     ftps2->box(FL_FLAT_BOX);
     ftps2->value("Transaction Processing Server IP #2:");
     ftps22 = new Fl_Output(570, 440, 120, 20, "");
     ftps22->box(FL_FLAT_BOX);
     ftps22->value(FTPS2);
     Fl_Button *ftps2_change = new Fl_Button(690, 440, 90, 20, "Change...");
     ftps2_change->callback(ftps2_change_callback, NULL);


     //---------//
     Fl_Output *clock = new Fl_Output(20, 480, 550, 20, "");
     clock->box(FL_FLAT_BOX);
     clock->value("System Time and Date:");
     clock2 = new Fl_Output(570, 480, 210, 20);
     clock2->box(FL_FLAT_BOX);
     clock2->value(DATE);



     window->end();
     window->show(argc, argv);
     return Fl::run();
}


void save_button_callback(Fl_Widget *W, void *data) {
	int retval;
	char cmd[256];
	FILE* fd;


	printf("\nNetwork Startup.\n");
	retval = system("ifconfig eth0 down\n");
	if (retval == 0) {
		printf("ifconfig \"eth0\"  down executed OK.\n");
	} else {
		printf("ifconfig \"eth0\" down had problems starting.\n");
	}

	retval = system("ifconfig lo down\n");
	if (retval == 0) {
		printf("ifconfig \"lo\"  down executed OK.\n");
	} else {
		printf("ifconfig \"lo\" down had problems starting.\n");
	}


	strcpy(cmd, "ifconfig lo 127.0.0.1 \n");
	retval = system(cmd);
	if (retval == 0) {
		printf("ifconfig \"lo\" executed OK.\n");
	} else {
		printf("ifconfig \"lo\" had problems starting.\n");
	}

	strcpy(cmd, "route add -net 127.0.0.0 netmask 255.0.0.0 lo \n");
	if (retval == 0) {
		printf("route \"lo\" executed OK.\n");
	} else {
		printf("route \"lo\" had problems starting.\n");
	}


	if (mode == 0) {
		fl_message("You must select either the DHCP or Static button.");
		return;
	}
	if (mode == 1) {
		retval = system("dhcpcd\n");
		if (retval >= 0) {
			printf("DHCP started OK.\n");
			fl_message("DHCP Client started OK!");
		} else {
			printf("DHCP did NOT start.\n");
			fl_message("DHCP Server NOT found. Please check Cabling, Hub-LEDs, etc.");
		}

	}
	if (mode == 2) {        //STATIC IP configuration.
		strcpy(cmd, "ifconfig eth0 ");
		strcat(cmd, IPADDR);
		strcat(cmd, " netmask ");
		strcat(cmd, NETMASK);
		strcat(cmd, " broadcast ");
		strcat(cmd, BROADCAST);
		strcat(cmd, "\n");
		retval = system(cmd);
		if (retval == 0) {
			printf("ifconfig \"eth0\" executed OK.\n");
		} else {
			printf("ifconfig \"eth0\" had problems starting.\n");
		}

		strcpy(cmd, "route add default gw ");
		strcat(cmd, GATEWAY);
		strcat(cmd, " netmask 0.0.0.0");
		retval = system(cmd);
		if (retval == 0) {
			printf("route \"eth0\" executed OK.\n");
		} else {
			printf("route \"eth0\" had problems starting.\n");
		}

		fd = fopen("/tmp/resolv.conf", "w");
		if (fd == NULL) {
			perror("/tmp/resolv.conf");
			exit(-1);
		}
		strcpy(cmd, "nameserver ");
		strcat(cmd, DNS1);
		strcat(cmd, "\n");
		fputs(cmd, fd);
		strcpy(cmd, "nameserver ");
		strcat(cmd, DNS2);
		strcat(cmd, "\n");
		fputs(cmd, fd);
		fclose(fd);
		fl_message("Static IP Assignment Complete.");
	}

	//start the clock-server....
	strcpy(cmd, "/sbin/clock-client ");
	strcat(cmd, FTPS1);
	strcat(cmd, "\n");
	printf(cmd);
	retval = system(cmd);
	if (retval < 0) {
		fl_alert("There was a problem setting the system clock.");
	}

	time_t timeval;
	time(&timeval);
        strcpy(DATE, ctime(&timeval));
        DATE[strlen(DATE) - 1] = 0;
        clock2->value(DATE);

	return;

}

void quit_button_callback (Fl_Widget *W, void *data) {
	printf("\nGoodbye!\n");
	exit(0);
}

void DHCP_button_callback(Fl_Widget *W, void *data) {
	printf("\nUser selected DHCP.\n");
	mode = 1;
	return;
}

void STATIC_button_callback(Fl_Widget *W, void *data) {
	printf("\nUser selected STATIC IP assignment.\n");
	mode = 2;
	return;

}

void HELP_button_callback(Fl_Widget *W, void *data) {
	printf("\nUser selected Help!.\n");
	HELP_window = new Fl_Window(400,400,200,120);
	Fl_Box *box = new Fl_Box(1,1,199,80, "No help available.\n");
	printf("Compiled: " __DATE__ " at " __TIME__ "\n");

        Fl_Button *button = new Fl_Button(1,81, 199,40, "OK");
        button->callback(help_callback_kill, NULL);

        HELP_window->add(button);
        HELP_window->show();
        HELP_window->end();

	return;
}


void help_callback_kill(Fl_Widget *W, void *data) {
	HELP_window->hide();
	return;
}

void IPaddr_change_callback(Fl_Widget *W, void *data) {
        selection = 1;

	DLG_window = new Fl_Window(400,400,300,200, "IP Address");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current IP Address:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(IPADDR);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New IP Address:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(IPADDR);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}


void netmask_change_callback(Fl_Widget *W, void *data) {
        selection = 2;

	DLG_window = new Fl_Window(400,400,300,200, "NetMASK");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current NetMask:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(NETMASK);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New NetMASK:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(NETMASK);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

void gateway_change_callback(Fl_Widget *W, void *data) {
        selection = 3;

	DLG_window = new Fl_Window(400,400,300,200, "Gateway IP");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current Gateway IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(GATEWAY);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New Gateway IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(GATEWAY);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

void broadcast_change_callback(Fl_Widget *W, void *data) {
        selection = 4;

	DLG_window = new Fl_Window(400,400,300,200, "Broadcast IP");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current Broadcast IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(BROADCAST);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New Broadcast IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(BROADCAST);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

void network_change_callback(Fl_Widget *W, void *data) {
        selection = 5;

	DLG_window = new Fl_Window(400,400,300,200, "Network IP");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current Network IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(NETWORK);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New Network IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(NETWORK);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

 void dns1_change_callback(Fl_Widget *W, void *data) {
        selection = 6;

	DLG_window = new Fl_Window(400,400,300,200, "DNS1 IP");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current DNS1 IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(DNS1);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New DNS1 IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(DNS1);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

void dns2_change_callback(Fl_Widget *W, void *data) {
        selection = 7;

	DLG_window = new Fl_Window(400,400,300,200, "DNS2 IP");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current DNS2 IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(DNS2);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New DNS2 IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(DNS2);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}


void ftps1_change_callback(Fl_Widget *W, void *data) {
        selection = 8;

	DLG_window = new Fl_Window(400,400,300,200, "FTPS IP #1:");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current FTPS1 IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(FTPS1);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New FTPS1 IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(FTPS1);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

void ftps2_change_callback(Fl_Widget *W, void *data) {
        selection = 9;

	DLG_window = new Fl_Window(400,400,300,200, "FTPS IP #2:");

	DLG_output1 = new Fl_Output(5,20, 170, 20);
	DLG_output1->value("Current FTPS2 IP:");
	DLG_output1->box(FL_FLAT_BOX);

	DLG_output2 = new Fl_Output(175,20, 120,20);
	DLG_output2->value(FTPS2);
	DLG_output2->box(FL_FLAT_BOX);

	DLG_output3 = new Fl_Output(5,60, 170,20);
	DLG_output3->value("New FTPS2 IP:");
	DLG_output3->box(FL_FLAT_BOX);

	DLG_input = new Fl_Input(175, 60, 120,20);
	DLG_input->value(FTPS2);

	DLG_OK = new Fl_Button(30, 120, 120, 60, "OK");
	DLG_OK->callback(dlg_ok_callback, DLG_input->value());

	DLG_CANCEL = new Fl_Button(150, 120, 120, 60, "CANCEL");
	DLG_CANCEL->callback(dlg_cancel_callback, NULL);

	DLG_window->add(DLG_OK);
	DLG_window->add(DLG_CANCEL);
	DLG_window->add(DLG_output1);
	DLG_window->add(DLG_output2);
	DLG_window->add(DLG_output3);

	DLG_window->add(DLG_input);
	DLG_window->show();

	return;
}

void dlg_cancel_callback(Fl_Widget *W, void *data) {
	DLG_window->hide();
	return;
}

void dlg_ok_callback(Fl_Widget *W, void* data) {

	switch (selection) {
		case 1 : printf("User changed IP address.\n");
		         strcpy(IPADDR, (char *)data);
		         IPaddr2->value(IPADDR);
		         break;
		case 2 : printf("User changed netmask.\n");
		         strcpy(NETMASK, (char *)data);
		         netmask2->value(NETMASK);
		         break;
		case 3 : printf("User changed Gateway IP.\n");
			 strcpy(GATEWAY, (char *)data);
			 gateway2->value(GATEWAY);
			 break;
		case 4 : printf("User changed Broadcast IP.\n");
			 strcpy(BROADCAST, (char *)data);
			 broadcast2->value(BROADCAST);
			 break;
		case 5 : printf("User changed Network IP.\n");
			 strcpy(NETWORK, (char *)data);
			 network2->value(NETWORK);
			 break;
		case 6 : printf("User changed DNS1 IP.\n");
			 strcpy(DNS1, (char *)data);
			 dns12->value(DNS1);
			 break;
		case 7 : printf("User changed DNS2 IP.\n");
			 strcpy(DNS2, (char *)data);
			 dns22->value(DNS2);
			 break;
		case 8 : printf("User changed FTPS1 IP.\n");
			 strcpy(FTPS1, (char *)data);
			 ftps12->value(FTPS1);
			 break;
		case 9 : printf("User changed FTPS2 IP.\n");
			 strcpy(FTPS2, (char *)data);
			 ftps22->value(FTPS2);
			 break;

		default: perror("select."); exit (-1);
	}

	DLG_window->hide();
	return;
}

*/
