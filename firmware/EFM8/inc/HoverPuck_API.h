/*
 * HoverPuck_API.h
 *
 *  Created on: Mar 21, 2015
 *      Author: mrcruz
 */

#ifndef HOVERPUCK_API_H_
#define HOVERPUCK_API_H_

#include <SI_EFM8BB1_Register_Enums.h>

SI_SBIT(EN_20V,  SFR_P0, 0);
//SI_SBIT(STATUS,  SFR_P0, 7);
SI_SBIT(STATUS,  SFR_P1, 5);
SI_SBIT(BLE_RST, SFR_P1, 0);


void hoverPuck_Init( void );
void hoverPuck_Update( void );

void hoverPuck_EnableMB0( void );
void hoverPuck_EnableMB1( void );
void hoverPuck_DisableMB0( void );
void hoverPuck_DisableMB1( void );


// (3v/2)/3.3v * (2^10 - 1) = 465
#define LIPO_GOOD_THRESH	465

bool hoverPuck_lipoGood();


/*
 * Utilities
*/

//extern uint32_t msTicks;
//void delayMS( uint32_t );



#endif /* HOVERPUCK_API_H_ */