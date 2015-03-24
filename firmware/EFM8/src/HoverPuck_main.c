//-----------------------------------------------------------------------------
// HoverPuck
//-----------------------------------------------------------------------------
/*
Peripherals:
  SYSCLK - 24.5 MHz / 16
  TIMER1 - UART Baud Rate
  TIMER2 - System Update Rate (8Hz)
  TIMER3 - 1ms System Tick
  ADC0   - Low Battery Detection
  UART0  - BLE Serial Comm
  PCA0 CEX0 - 25kHz Square Wave
  PCA0 CEX1 - 25kHz Square Wave

Pinouts:
  P0.0   - GPIO: 20V_EN (Active High)
  P0.1   - ADC: Battery Low Warning
  P0.2
  P0.3   - PCA CEX0: Microblower 0
  P0.4   - UART: TX (EFM8 -> BLE112)
  P0.5   - UART: RX (BLE112 -> EFM8)
  P0.6   - PCA CEX1: Microblower 1
  P0.7   - GPIO: Status LED (Active High)
  P1.0   - GPIO: BLE_RST (Active Low)
*/


//-----------------------------------------------------------------------------
// Includes
//-----------------------------------------------------------------------------
#include <SI_EFM8BB1_Register_Enums.h>                      // SFR declarations
#include "InitDevice.h"

#include "HoverPuck_API.h"


//-----------------------------------------------------------------------------
// main() Routine
// ----------------------------------------------------------------------------
int main (void)
{
	// Configure EFM8BB1 from Reset
	enter_DefaultMode_from_RESET();

	// Initialize HoverPuck Systems
	hoverPuck_Init();

	// Enable interrupts
	IE_EA = 1;

	// Enter Idle Mode
	//  Everything is interrupt driven
	//  The fun stuff happens in hoverPuck_Update
	while(1)
	{
		// Chapter 7.3 in RM
		PCON0 |= PCON0_IDLE__IDLE;
		PCON0 = PCON0;
	}
}
