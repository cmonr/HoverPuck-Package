#include <SI_EFM8BB1_Register_Enums.h>
#include "InitDevice.h"

#include "HoverPuck_API.h"

uint32_t msTicks = 0;
uint8_t rxData = 0;
uint8_t ctr = 0;



void hoverPuck_Init()
{
	// Peripherals already enabled

	//
	// BLE112/3
	//
	// Bring BLE module out of reset
	BLE_RST = 0;

	// TODO: Wait for 'READY\r\n'

	// TODO: Send APT\r\n

	// TODO: Blink Status LED to indicate waiting for connection
	// TODO: Wait for DATA\r\n


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

	// Turn on LEDs
	STATUS = 0;
}


// Update system at 8Hz
void hoverPuck_Update()
{
	if (!hoverPuck_lipoGood())
	{
		// Disable Bluetooth
		BLE_RST = 1;

		// Disable MicroBlowers
		hoverPuck_DisableMB0();
		hoverPuck_DisableMB1();

		// Turn off 20v Regulator
		EN_20V = 1;

		// Turn off status LED
		STATUS = 1;

		// Disable all interrupts
		//  This effectively disables the entire
		//  system until reset
		//IE_EA = 0;
	}
	else
	{
		// TODO: Parse simple UART CLI

		// Heartbeat
		STATUS = !STATUS;
	}

	//sendData(ADC0 >> 2);
	sendData(ctr++);
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

void sendData(uint8_t d)
{
	SBUF0 = d;
}

//void sendData(uint8_t data)
//{
//	SBUF0 = data;
//}


bool hoverPuck_lipoGood()
{
	// Start ADC Conversion
	ADC0CN0_ADBUSY = 1;

	// Wait for completion
	while (ADC0CN0_ADBUSY == 1)
	{
		// Chapter 7.3 in RM
		PCON0 |= PCON0_IDLE__IDLE;
		PCON0 = PCON0;
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
