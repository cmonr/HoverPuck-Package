#include <SI_EFM8BB1_Register_Enums.h>
#include "InitDevice.h"

#include "HoverPuck_API.h"

uint32_t msTicks = 0;
uint8_t rxData = 0;
uint8_t ledCtr = 0;
uint8_t ctr = 0;

uint8_t state = DISCONNECTED;

void hoverPuck_Init()
{
	// Disconnect BC UART pins
	BC_EN = 0;
	rxLED = 1;

	// Peripherals already enabled


	//
	// BLE112/3
	//
	// Bring BLE module out of reset
	BLE_RST = 0;

	// Wait for BLE Module to send READY
	readStr("READY");

	// Set broadcasting
	writeStr("ATP\n");

	// Update LED Status
	state = DISCONNECTED;

	// Wait for BLE Module to send DATA
	readStr("DATA");


	writeStr("Hi");

	readStr("abcde");
	state = CONNECTED;



	//writeStr("HELLO!");


	//
	// Microblowers
	//
	// Turn on 20v Regulator
	EN_20V = 0;

	// Give regulator time to ramp
	//delayMS(100);

	// Enable MicroBlowers
	hoverPuck_EnableMB0();
	hoverPuck_EnableMB1();
}


// Update system at 8Hz
void hoverPuck_Update()
{
	// Check Lipo Status
	switch (state)
	{
		case DISCONNECTED:
			break;
		case CONNECTED:
			// TODO: Parse simple UART CLI
			// TODO: Drive MicroBlowers

			// Send battery status
			/*writeChar('0');
			writeChar('x');
			writeChar(((ADC0 >> 2) & 0xF0) >> 4 + '0');
			writeChar(((ADC0 >> 2) & 0x0F) + '0');
			writeChar('\n');*/
			//writeChar(ctr++);

			// Check Lipo status
			/*if (!hoverPuck_lipoGood())
			{
				//
				// Conserve as much power as possible
				//

				// Disable Bluetooth
				BLE_RST = 1;

				// Disable MicroBlowers
				hoverPuck_DisableMB0();
				hoverPuck_DisableMB1();

				// Turn off 20v Regulator
				EN_20V = 1;

				// TODO: Disable all interrupts except Timer2

				state = LIPO_LOW;
			}*/
			break;
		case LIPO_LOW:
			break;
	}

	// Generate Blink pattern from state
	ledCtr = ++ledCtr & 0x07;
	LED = !((state >> ledCtr) & 0x01);

	// Update other LED based on what was received
	rxLED = !(rxData >> 7);
}



//
// Helper Functions
//
void hoverPuck_EnableMB0()
{
	PCA0CPM0 |= PCA0CPM0_TOG__ENABLED;
}

void hoverPuck_EnableMB1()
{
	PCA0CPM1 |= PCA0CPM1_TOG__ENABLED;
}

void hoverPuck_DisableMB0()
{
	PCA0CPM0 &= ~PCA0CPM0_TOG__BMASK;
}

void hoverPuck_DisableMB1()
{
	PCA0CPM1 &= ~PCA0CPM1_TOG__BMASK;
}

//
// Simple String Helper functions
//
int8_t readChar()
{
	rxData = 0;
	while(rxData == 0)
	{
		// Chapter 7.3 in RM
		//PCON0 |= PCON0_IDLE__IDLE;
		//PCON0 = PCON0;
	}

	return rxData;
}

void readStr(int8_t* str)
{
	int8_t buff[16];
	int8_t buffIndex = 0;

	while(1)
	{
		// Add data to buffer
		buff[buffIndex++] = readChar();

		// Check strings
		if (rxData == '\r' || rxData == '\n')
		{
			int8_t i=0;
			int8_t* strPtr = str;
			for(; i<buffIndex; i++)
			{
				if ((*strPtr) != buff[i])
				{
					break;
				}
				strPtr++;
			}

			// If at end of index, string was matched
			//  Subtract 1 due to \r
			//  Make sure single character isn't being compared against
			if (i == buffIndex-1 && buffIndex != 1)
				return;

			// Otherwise, reset everything
			buffIndex = 0;
		}
	}
}

void writeChar(int8_t c)
{
	SBUF0 = c;
	// Wait for TI interrupt to fire
	while(SCON0_TI == 0)
	{
		// Chapter 7.3 in RM
		//PCON0 |= PCON0_IDLE__IDLE;
		//PCON0 = PCON0;
	}

	// Transmit success interrupt woke us up
	SCON0_TI = 0;
}

void writeStr(int8_t* str)
{
	int8_t c;
	while((c = *(str++)) != 0)
	{
		writeChar(c);
	}
}


//
// Misc Helper functions
//
bool hoverPuck_lipoGood()
{
	// Start ADC Conversion
	ADC0CN0_ADBUSY = 1;

	// Wait for completion
	while (ADC0CN0_ADBUSY == 1)
	{
		// Chapter 7.3 in RM
		//PCON0 |= PCON0_IDLE__IDLE;
		//PCON0 = PCON0;
	}

	// Return comparison
	return ADC0 > LIPO_GOOD_THRESH;
}


/*
void delayMS(uint32_t delay)
{
	uint32_t startTicks = msTicks;
	while (msTicks < delay)
	{
		// Chapter 7.3 in RM
		PCON0 |= PCON0_IDLE__IDLE;
		PCON0 = PCON0;
	}
}
*/
