#include <SI_EFM8BB1_Register_Enums.h>
#include "InitDevice.h"

#include "HoverPuck_API.h"

uint32_t msTicks;


void hoverPuck_Init()
{
	// Peripherals already enabled

	// Enable Bluetooth
	BLE_RST = 1;

	// Turn on 20v Regulator
	EN_20V = 1;

	// Give regulator time to ramp
	//delayMS(100);

	// Enable MicroBlowers
	hoverPuck_EnableMB0();
	hoverPuck_EnableMB1();

	// Turn on LEDs
	STATUS = 1;
}


// Update system at 8Hz
void hoverPuck_Update()
{
	if (!hoverPuck_lipoGood())
	{
		// Disable Bluetooth
		BLE_RST = 0;

		// Disable MicroBlowers
		hoverPuck_DisableMB0();
		hoverPuck_DisableMB1();

		// Turn off 20v Regulator
		EN_20V = 0;

		// Turn off status LED
		STATUS = 1;

		// Disable all interrupts
		//  This effectively disables the entire
		//  system until reset
		IE_EA = 0;
	}
	else
	{
		// TODO: Parse simple UART CLI

		// Heartbeat
		STATUS = !STATUS;
	}
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
