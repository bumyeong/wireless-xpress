//=========================================================
// src/InitDevice.c: generated by Hardware Configurator
//
// This file will be regenerated when saving a document.
// leave the sections inside the "$[...]" comment tags alone
// or they will be overwritten!
//=========================================================

// USER INCLUDES
#include <SI_EFM8UB1_Register_Enums.h>
#include "InitDevice.h"

// USER PROTOTYPES
// USER FUNCTIONS

// $[Library Includes]
// [Library Includes]$

//==============================================================================
// enter_DefaultMode_from_RESET
//==============================================================================
extern void enter_DefaultMode_from_RESET(void) {
	// $[Config Calls]
	// Save the SFRPAGE
	uint8_t SFRPAGE_save = SFRPAGE;
	WDT_0_enter_DefaultMode_from_RESET();
	PORTS_0_enter_DefaultMode_from_RESET();
	PORTS_1_enter_DefaultMode_from_RESET();
	PORTS_2_enter_DefaultMode_from_RESET();
	PBCFG_0_enter_DefaultMode_from_RESET();
	ADC_0_enter_DefaultMode_from_RESET();
	RSTSRC_0_enter_DefaultMode_from_RESET();
	VDDMON_0_enter_DefaultMode_from_RESET();
	CLOCK_0_enter_DefaultMode_from_RESET();
	TIMER16_2_enter_DefaultMode_from_RESET();
	TIMER16_3_enter_DefaultMode_from_RESET();
	TIMER_SETUP_0_enter_DefaultMode_from_RESET();
	SPI_0_enter_DefaultMode_from_RESET();
	UARTE_1_enter_DefaultMode_from_RESET();
	INTERRUPT_0_enter_DefaultMode_from_RESET();
	// Restore the SFRPAGE
	SFRPAGE = SFRPAGE_save;
	// [Config Calls]$

}

//================================================================================
// WDT_0_enter_DefaultMode_from_RESET
//================================================================================
extern void WDT_0_enter_DefaultMode_from_RESET(void) {
	// $[WDTCN - Watchdog Timer Control]
	SFRPAGE = 0x00;
	//Disable Watchdog with key sequence
	WDTCN = 0xDE; //First key
	WDTCN = 0xAD; //Second key
	// [WDTCN - Watchdog Timer Control]$

}

//================================================================================
// PORTS_0_enter_DefaultMode_from_RESET
//================================================================================
extern void PORTS_0_enter_DefaultMode_from_RESET(void) {
	// $[P0 - Port 0 Pin Latch]
	// [P0 - Port 0 Pin Latch]$

	// $[P0MDOUT - Port 0 Output Mode]
	/***********************************************************************
	 - P0.0 output is open-drain
	 - P0.1 output is push-pull
	 - P0.2 output is open-drain
	 - P0.3 output is open-drain
	 - P0.4 output is open-drain
	 - P0.5 output is open-drain
	 - P0.6 output is push-pull
	 - P0.7 output is open-drain
	 ***********************************************************************/
	P0MDOUT = P0MDOUT_B0__OPEN_DRAIN | P0MDOUT_B1__PUSH_PULL
			| P0MDOUT_B2__OPEN_DRAIN | P0MDOUT_B3__OPEN_DRAIN
			| P0MDOUT_B4__OPEN_DRAIN | P0MDOUT_B5__OPEN_DRAIN
			| P0MDOUT_B6__PUSH_PULL | P0MDOUT_B7__OPEN_DRAIN;
	// [P0MDOUT - Port 0 Output Mode]$

	// $[P0MDIN - Port 0 Input Mode]
	// [P0MDIN - Port 0 Input Mode]$

	// $[P0SKIP - Port 0 Skip]
	/***********************************************************************
	 - P0.0 pin is skipped by the crossbar
	 - P0.1 pin is skipped by the crossbar
	 - P0.2 pin is skipped by the crossbar
	 - P0.3 pin is skipped by the crossbar
	 - P0.4 pin is skipped by the crossbar
	 - P0.5 pin is skipped by the crossbar
	 - P0.6 pin is not skipped by the crossbar
	 - P0.7 pin is not skipped by the crossbar
	 ***********************************************************************/
	P0SKIP = P0SKIP_B0__SKIPPED | P0SKIP_B1__SKIPPED | P0SKIP_B2__SKIPPED
			| P0SKIP_B3__SKIPPED | P0SKIP_B4__SKIPPED | P0SKIP_B5__SKIPPED
			| P0SKIP_B6__NOT_SKIPPED | P0SKIP_B7__NOT_SKIPPED;
	// [P0SKIP - Port 0 Skip]$

	// $[P0MASK - Port 0 Mask]
	// [P0MASK - Port 0 Mask]$

	// $[P0MAT - Port 0 Match]
	// [P0MAT - Port 0 Match]$

}

//================================================================================
// PORTS_1_enter_DefaultMode_from_RESET
//================================================================================
extern void PORTS_1_enter_DefaultMode_from_RESET(void) {
	// $[P1 - Port 1 Pin Latch]
	// [P1 - Port 1 Pin Latch]$

	// $[P1MDOUT - Port 1 Output Mode]
	/***********************************************************************
	 - P1.0 output is push-pull
	 - P1.1 output is open-drain
	 - P1.2 output is open-drain
	 - P1.3 output is open-drain
	 - P1.4 output is push-pull
	 - P1.5 output is open-drain
	 - P1.6 output is push-pull
	 - P1.7 output is open-drain
	 ***********************************************************************/
	P1MDOUT = P1MDOUT_B0__PUSH_PULL | P1MDOUT_B1__OPEN_DRAIN
			| P1MDOUT_B2__OPEN_DRAIN | P1MDOUT_B3__OPEN_DRAIN
			| P1MDOUT_B4__PUSH_PULL | P1MDOUT_B5__OPEN_DRAIN
			| P1MDOUT_B6__PUSH_PULL | P1MDOUT_B7__OPEN_DRAIN;
	// [P1MDOUT - Port 1 Output Mode]$

	// $[P1MDIN - Port 1 Input Mode]
	/***********************************************************************
	 - P1.0 pin is configured for digital mode
	 - P1.1 pin is configured for digital mode
	 - P1.2 pin is configured for digital mode
	 - P1.3 pin is configured for digital mode
	 - P1.4 pin is configured for digital mode
	 - P1.5 pin is configured for digital mode
	 - P1.6 pin is configured for digital mode
	 - P1.7 pin is configured for analog mode
	 ***********************************************************************/
	P1MDIN = P1MDIN_B0__DIGITAL | P1MDIN_B1__DIGITAL | P1MDIN_B2__DIGITAL
			| P1MDIN_B3__DIGITAL | P1MDIN_B4__DIGITAL | P1MDIN_B5__DIGITAL
			| P1MDIN_B6__DIGITAL | P1MDIN_B7__ANALOG;
	// [P1MDIN - Port 1 Input Mode]$

	// $[P1SKIP - Port 1 Skip]
	/***********************************************************************
	 - P1.0 pin is not skipped by the crossbar
	 - P1.1 pin is skipped by the crossbar
	 - P1.2 pin is skipped by the crossbar
	 - P1.3 pin is skipped by the crossbar
	 - P1.4 pin is skipped by the crossbar
	 - P1.5 pin is skipped by the crossbar
	 - P1.6 pin is skipped by the crossbar
	 - P1.7 pin is skipped by the crossbar
	 ***********************************************************************/
	P1SKIP = P1SKIP_B0__NOT_SKIPPED | P1SKIP_B1__SKIPPED | P1SKIP_B2__SKIPPED
			| P1SKIP_B3__SKIPPED | P1SKIP_B4__SKIPPED | P1SKIP_B5__SKIPPED
			| P1SKIP_B6__SKIPPED | P1SKIP_B7__SKIPPED;
	// [P1SKIP - Port 1 Skip]$

	// $[P1MASK - Port 1 Mask]
	// [P1MASK - Port 1 Mask]$

	// $[P1MAT - Port 1 Match]
	// [P1MAT - Port 1 Match]$

}

//================================================================================
// PORTS_2_enter_DefaultMode_from_RESET
//================================================================================
extern void PORTS_2_enter_DefaultMode_from_RESET(void) {
	// $[P2 - Port 2 Pin Latch]
	// [P2 - Port 2 Pin Latch]$

	// $[P2MDOUT - Port 2 Output Mode]
	/***********************************************************************
	 - P2.0 output is push-pull
	 - P2.1 output is push-pull
	 - P2.2 output is open-drain
	 - P2.3 output is push-pull
	 ***********************************************************************/
	P2MDOUT = P2MDOUT_B0__PUSH_PULL | P2MDOUT_B1__PUSH_PULL
			| P2MDOUT_B2__OPEN_DRAIN | P2MDOUT_B3__PUSH_PULL;
	// [P2MDOUT - Port 2 Output Mode]$

	// $[P2MDIN - Port 2 Input Mode]
	// [P2MDIN - Port 2 Input Mode]$

	// $[P2SKIP - Port 2 Skip]
	/***********************************************************************
	 - P2.0 pin is skipped by the crossbar
	 - P2.1 pin is not skipped by the crossbar
	 - P2.2 pin is not skipped by the crossbar
	 - P2.3 pin is skipped by the crossbar
	 ***********************************************************************/
	SFRPAGE = 0x20;
	P2SKIP = P2SKIP_B0__SKIPPED | P2SKIP_B1__NOT_SKIPPED
			| P2SKIP_B2__NOT_SKIPPED | P2SKIP_B3__SKIPPED;
	// [P2SKIP - Port 2 Skip]$

	// $[P2MASK - Port 2 Mask]
	// [P2MASK - Port 2 Mask]$

	// $[P2MAT - Port 2 Match]
	// [P2MAT - Port 2 Match]$

}

//================================================================================
// PBCFG_0_enter_DefaultMode_from_RESET
//================================================================================
extern void PBCFG_0_enter_DefaultMode_from_RESET(void) {
	// $[XBR2 - Port I/O Crossbar 2]
	/***********************************************************************
	 - Weak Pullups enabled 
	 - Crossbar enabled
	 - UART1 TX1 RX1 routed to Port pins
	 - UART1 RTS1 unavailable at Port pin
	 - UART1 CTS1 unavailable at Port pin
	 ***********************************************************************/
	SFRPAGE = 0x00;
	XBR2 = XBR2_WEAKPUD__PULL_UPS_ENABLED | XBR2_XBARE__ENABLED
			| XBR2_URT1E__ENABLED | XBR2_URT1RTSE__DISABLED
			| XBR2_URT1CTSE__DISABLED;
	// [XBR2 - Port I/O Crossbar 2]$

	// $[PRTDRV - Port Drive Strength]
	// [PRTDRV - Port Drive Strength]$

	// $[XBR0 - Port I/O Crossbar 0]
	/***********************************************************************
	 - UART0 I/O unavailable at Port pin
	 - SPI I/O routed to Port pins
	 - SMBus 0 I/O unavailable at Port pins
	 - CP0 unavailable at Port pin
	 - Asynchronous CP0 unavailable at Port pin
	 - CP1 unavailable at Port pin
	 - Asynchronous CP1 unavailable at Port pin
	 - SYSCLK unavailable at Port pin
	 ***********************************************************************/
	XBR0 = XBR0_URT0E__DISABLED | XBR0_SPI0E__ENABLED | XBR0_SMB0E__DISABLED
			| XBR0_CP0E__DISABLED | XBR0_CP0AE__DISABLED | XBR0_CP1E__DISABLED
			| XBR0_CP1AE__DISABLED | XBR0_SYSCKE__DISABLED;
	// [XBR0 - Port I/O Crossbar 0]$

	// $[XBR1 - Port I/O Crossbar 1]
	// [XBR1 - Port I/O Crossbar 1]$

}

//================================================================================
// ADC_0_enter_DefaultMode_from_RESET
//================================================================================
extern void ADC_0_enter_DefaultMode_from_RESET(void) {
	// $[ADC0CN1 - ADC0 Control 1]
	// [ADC0CN1 - ADC0 Control 1]$

	// $[ADC0MX - ADC0 Multiplexer Selection]
	/***********************************************************************
	 - Select ADC0.15
	 ***********************************************************************/
	ADC0MX = ADC0MX_ADC0MX__ADC0P15;
	// [ADC0MX - ADC0 Multiplexer Selection]$

	// $[ADC0CF - ADC0 Configuration]
	// [ADC0CF - ADC0 Configuration]$

	// $[ADC0AC - ADC0 Accumulator Configuration]
	// [ADC0AC - ADC0 Accumulator Configuration]$

	// $[ADC0TK - ADC0 Burst Mode Track Time]
	// [ADC0TK - ADC0 Burst Mode Track Time]$

	// $[ADC0PWR - ADC0 Power Control]
	// [ADC0PWR - ADC0 Power Control]$

	// $[ADC0GTH - ADC0 Greater-Than High Byte]
	// [ADC0GTH - ADC0 Greater-Than High Byte]$

	// $[ADC0GTL - ADC0 Greater-Than Low Byte]
	// [ADC0GTL - ADC0 Greater-Than Low Byte]$

	// $[ADC0LTH - ADC0 Less-Than High Byte]
	// [ADC0LTH - ADC0 Less-Than High Byte]$

	// $[ADC0LTL - ADC0 Less-Than Low Byte]
	// [ADC0LTL - ADC0 Less-Than Low Byte]$

	// $[ADC0CN0 - ADC0 Control 0]
	/***********************************************************************
	 - Enable ADC0 
	 ***********************************************************************/
	ADC0CN0 |= ADC0CN0_ADEN__ENABLED;
	// [ADC0CN0 - ADC0 Control 0]$

}

//================================================================================
// HFOSC_0_enter_DefaultMode_from_RESET
//================================================================================
extern void HFOSC_0_enter_DefaultMode_from_RESET(void) {
	// $[HFOCN - High Frequency Oscillator Control]
	/***********************************************************************
	 - Force High Frequency Oscillator 0 to run
	 - Force High Frequency Oscillator 1 to run
	 ***********************************************************************/
	SFRPAGE = 0x10;
	HFOCN = HFOCN_HFO0EN__ENABLED | HFOCN_HFO1EN__ENABLED;
	// [HFOCN - High Frequency Oscillator Control]$

}

//================================================================================
// RSTSRC_0_enter_DefaultMode_from_RESET
//================================================================================
extern void RSTSRC_0_enter_DefaultMode_from_RESET(void) {
	// $[RSTSRC - Reset Source]
	/***********************************************************************
	 - A power-on or supply monitor reset did not occur
	 - A missing clock detector reset did not occur
	 - A Comparator 0 reset did not occur
	 - A USB0 reset did not occur
	 ***********************************************************************/
	RSTSRC = RSTSRC_PORSF__NOT_SET | RSTSRC_MCDRSF__NOT_SET
			| RSTSRC_C0RSEF__NOT_SET | RSTSRC_USBRSF__NOT_SET;
	// [RSTSRC - Reset Source]$

}

//================================================================================
// VDDMON_0_enter_DefaultMode_from_RESET
//================================================================================
extern void VDDMON_0_enter_DefaultMode_from_RESET(void) {
	// $[VDM0CN - Supply Monitor Control]
	/***********************************************************************
	 - Supply Monitor Disabled
	 ***********************************************************************/
	VDM0CN &= ~VDM0CN_VDMEN__BMASK;
	// [VDM0CN - Supply Monitor Control]$

}

//================================================================================
// CLOCK_0_enter_DefaultMode_from_RESET
//================================================================================
extern void CLOCK_0_enter_DefaultMode_from_RESET(void) {
	// $[HFOSC1 Setup]
	// [HFOSC1 Setup]$

	// $[CLKSEL - Clock Select]
	/***********************************************************************
	 - Clock derived from the Internal High Frequency Oscillator 0
	 - SYSCLK is equal to selected clock source divided by 4
	 ***********************************************************************/
	CLKSEL = CLKSEL_CLKSL__HFOSC0 | CLKSEL_CLKDIV__SYSCLK_DIV_4;
	while ((CLKSEL & CLKSEL_DIVRDY__BMASK) == CLKSEL_DIVRDY__NOT_READY)
		;
	// [CLKSEL - Clock Select]$

}

//================================================================================
// TIMER16_3_enter_DefaultMode_from_RESET
//================================================================================
extern void TIMER16_3_enter_DefaultMode_from_RESET(void) {
	// $[Timer Initialization]
	// Save Timer Configuration
	uint8_t TMR3CN0_TR3_save;
	TMR3CN0_TR3_save = TMR3CN0 & TMR3CN0_TR3__BMASK;
	// Stop Timer
	TMR3CN0 &= ~(TMR3CN0_TR3__BMASK);
	// [Timer Initialization]$

	// $[TMR3CN1 - Timer 3 Control 1]
	// [TMR3CN1 - Timer 3 Control 1]$

	// $[TMR3CN0 - Timer 3 Control]
	// [TMR3CN0 - Timer 3 Control]$

	// $[TMR3H - Timer 3 High Byte]
	/***********************************************************************
	 - Timer 3 High Byte = 0xF8
	 ***********************************************************************/
	TMR3H = (0xF8 << TMR3H_TMR3H__SHIFT);
	// [TMR3H - Timer 3 High Byte]$

	// $[TMR3L - Timer 3 Low Byte]
	/***********************************************************************
	 - Timer 3 Low Byte = 0x06
	 ***********************************************************************/
	TMR3L = (0x06 << TMR3L_TMR3L__SHIFT);
	// [TMR3L - Timer 3 Low Byte]$

	// $[TMR3RLH - Timer 3 Reload High Byte]
	/***********************************************************************
	 - Timer 3 Reload High Byte = 0xE8
	 ***********************************************************************/
	TMR3RLH = (0xE8 << TMR3RLH_TMR3RLH__SHIFT);
	// [TMR3RLH - Timer 3 Reload High Byte]$

	// $[TMR3RLL - Timer 3 Reload Low Byte]
	/***********************************************************************
	 - Timer 3 Reload Low Byte = 0x13
	 ***********************************************************************/
	TMR3RLL = (0x13 << TMR3RLL_TMR3RLL__SHIFT);
	// [TMR3RLL - Timer 3 Reload Low Byte]$

	// $[TMR3CN0]
	/***********************************************************************
	 - Start Timer 3 running
	 ***********************************************************************/
	TMR3CN0 |= TMR3CN0_TR3__RUN;
	// [TMR3CN0]$

	// $[Timer Restoration]
	// Restore Timer Configuration
	TMR3CN0 |= TMR3CN0_TR3_save;
	// [Timer Restoration]$

}

//================================================================================
// TIMER_SETUP_0_enter_DefaultMode_from_RESET
//================================================================================
extern void TIMER_SETUP_0_enter_DefaultMode_from_RESET(void) {
	// $[CKCON0 - Clock Control 0]
	/***********************************************************************
	 - System clock divided by 4
	 - Counter/Timer 0 uses the system clock
	 - Timer 2 high byte uses the clock defined by T2XCLK in TMR2CN0
	 - Timer 2 low byte uses the system clock
	 - Timer 3 high byte uses the clock defined by T3XCLK in TMR3CN0
	 - Timer 3 low byte uses the system clock
	 - Timer 1 uses the system clock
	 ***********************************************************************/
	CKCON0 = CKCON0_SCA__SYSCLK_DIV_4 | CKCON0_T0M__SYSCLK
			| CKCON0_T2MH__EXTERNAL_CLOCK | CKCON0_T2ML__SYSCLK
			| CKCON0_T3MH__EXTERNAL_CLOCK | CKCON0_T3ML__SYSCLK
			| CKCON0_T1M__SYSCLK;
	// [CKCON0 - Clock Control 0]$

	// $[CKCON1 - Clock Control 1]
	// [CKCON1 - Clock Control 1]$

	// $[TMOD - Timer 0/1 Mode]
	/***********************************************************************
	 - Mode 2, 8-bit Counter/Timer with Auto-Reload
	 - Mode 2, 8-bit Counter/Timer with Auto-Reload
	 - Timer Mode
	 - Timer 0 enabled when TR0 = 1 irrespective of INT0 logic level
	 - Timer Mode
	 - Timer 1 enabled when TR1 = 1 irrespective of INT1 logic level
	 ***********************************************************************/
	TMOD = TMOD_T0M__MODE2 | TMOD_T1M__MODE2 | TMOD_CT0__TIMER
			| TMOD_GATE0__DISABLED | TMOD_CT1__TIMER | TMOD_GATE1__DISABLED;
	// [TMOD - Timer 0/1 Mode]$

	// $[TCON - Timer 0/1 Control]
	// [TCON - Timer 0/1 Control]$

}

//================================================================================
// PCA_0_enter_DefaultMode_from_RESET
//================================================================================
extern void PCA_0_enter_DefaultMode_from_RESET(void) {
	// $[PCA Off]
	PCA0CN0_CR = PCA0CN0_CR__STOP;
	// [PCA Off]$

	// $[PCA0MD - PCA Mode]
	// [PCA0MD - PCA Mode]$

	// $[PCA0CENT - PCA Center Alignment Enable]
	// [PCA0CENT - PCA Center Alignment Enable]$

	// $[PCA0CLR - PCA Comparator Clear Control]
	// [PCA0CLR - PCA Comparator Clear Control]$

	// $[PCA0L - PCA Counter/Timer Low Byte]
	// [PCA0L - PCA Counter/Timer Low Byte]$

	// $[PCA0H - PCA Counter/Timer High Byte]
	// [PCA0H - PCA Counter/Timer High Byte]$

	// $[PCA0POL - PCA Output Polarity]
	// [PCA0POL - PCA Output Polarity]$

	// $[PCA0PWM - PCA PWM Configuration]
	// [PCA0PWM - PCA PWM Configuration]$

	// $[PCA On]
	// [PCA On]$

}

//================================================================================
// SPI_0_enter_DefaultMode_from_RESET
//================================================================================
extern void SPI_0_enter_DefaultMode_from_RESET(void) {
	// $[SPI0CKR - SPI0 Clock Rate]
	/***********************************************************************
	 - SPI0 Clock Rate = 0x05
	 ***********************************************************************/
	SPI0CKR = (0x05 << SPI0CKR_SPI0CKR__SHIFT);
	// [SPI0CKR - SPI0 Clock Rate]$

	// $[SPI0FCN0 - SPI0 FIFO Control 0]
	// [SPI0FCN0 - SPI0 FIFO Control 0]$

	// $[SPI0FCN1 - SPI0 FIFO Control 1]
	// [SPI0FCN1 - SPI0 FIFO Control 1]$

	// $[SPI0CFG - SPI0 Configuration]
	/***********************************************************************
	 - Enable master mode. Operate as a master
	 ***********************************************************************/
	SPI0CFG |= SPI0CFG_MSTEN__MASTER_ENABLED;
	// [SPI0CFG - SPI0 Configuration]$

	// $[SPI0CN0 - SPI0 Control]
	/***********************************************************************
	 - Enable the SPI module
	 - 3-Wire Slave or 3-Wire Master Mode
	 ***********************************************************************/
	SPI0CN0 &= ~SPI0CN0_NSSMD__FMASK;
	SPI0CN0 |= SPI0CN0_SPIEN__ENABLED;
	// [SPI0CN0 - SPI0 Control]$

}

//================================================================================
// UARTE_1_enter_DefaultMode_from_RESET
//================================================================================
extern void UARTE_1_enter_DefaultMode_from_RESET(void) {
	// $[SBCON1 - UART1 Baud Rate Generator Control]
	/***********************************************************************
	 - Enable the baud rate generator
	 - Prescaler = 1
	 ***********************************************************************/
	SFRPAGE = 0x20;
	SBCON1 = SBCON1_BREN__ENABLED | SBCON1_BPS__DIV_BY_1;
	// [SBCON1 - UART1 Baud Rate Generator Control]$

	// $[SMOD1 - UART1 Mode]
	// [SMOD1 - UART1 Mode]$

	// $[UART1FCN0 - UART1 FIFO Control 0]
	/***********************************************************************
	 - RFRQ will be set anytime new data arrives in the RX FIFO 
	 - TFRQ will be set when the TX FIFO is empty
	 - UART1 interrupts will be generated if RFRQ is set
	 - UART1 interrupts will not be generated when TFRQ is set
	 ***********************************************************************/
	UART1FCN0 = UART1FCN0_RXTH__ZERO | UART1FCN0_TXTH__ZERO
			| UART1FCN0_RFRQE__ENABLED | UART1FCN0_TFRQE__DISABLED;
	// [UART1FCN0 - UART1 FIFO Control 0]$

	// $[SBRLH1 - UART1 Baud Rate Generator High Byte]
	/***********************************************************************
	 - UART1 Baud Rate Reload High = 0xFF
	 ***********************************************************************/
	SBRLH1 = (0xFF << SBRLH1_BRH__SHIFT);
	// [SBRLH1 - UART1 Baud Rate Generator High Byte]$

	// $[SBRLL1 - UART1 Baud Rate Generator Low Byte]
	/***********************************************************************
	 - UART1 Baud Rate Reload Low = 0xE6
	 ***********************************************************************/
	SBRLL1 = (0xE6 << SBRLL1_BRL__SHIFT);
	// [SBRLL1 - UART1 Baud Rate Generator Low Byte]$

	// $[UART1LIN - UART1 LIN Configuration]
	// [UART1LIN - UART1 LIN Configuration]$

	// $[SCON1 - UART1 Serial Port Control]
	/***********************************************************************
	 - UART1 reception enabled
	 ***********************************************************************/
	SCON1 |= SCON1_REN__RECEIVE_ENABLED;
	// [SCON1 - UART1 Serial Port Control]$

	// $[UART1FCN1 - UART1 FIFO Control 1]
	/***********************************************************************
	 - A receive timeout will occur after 16 idle periods on the UART RX line
	 - The RI flag will not generate UART1 interrupts
	 - The TI flag will not generate UART1 interrupts
	 ***********************************************************************/
	UART1FCN1 &= ~(UART1FCN1_RIE__BMASK | UART1FCN1_TIE__BMASK);
	UART1FCN1 |= UART1FCN1_RXTO__TIMEOUT_16;
	// [UART1FCN1 - UART1 FIFO Control 1]$

}

//================================================================================
// INTERRUPT_0_enter_DefaultMode_from_RESET
//================================================================================
extern void INTERRUPT_0_enter_DefaultMode_from_RESET(void) {
	// $[EIE1 - Extended Interrupt Enable 1]
	/***********************************************************************
	 - Disable ADC0 Conversion Complete interrupt
	 - Disable ADC0 Window Comparison interrupt
	 - Disable CP0 interrupts
	 - Disable CP1 interrupts
	 - Disable all Port Match interrupts
	 - Disable all PCA0 interrupts
	 - Disable all SMB0 interrupts
	 - Enable interrupt requests generated by the TF3L or TF3H flags
	 ***********************************************************************/
	SFRPAGE = 0x00;
	EIE1 = EIE1_EADC0__DISABLED | EIE1_EWADC0__DISABLED | EIE1_ECP0__DISABLED
			| EIE1_ECP1__DISABLED | EIE1_EMAT__DISABLED | EIE1_EPCA0__DISABLED
			| EIE1_ESMB0__DISABLED | EIE1_ET3__ENABLED;
	// [EIE1 - Extended Interrupt Enable 1]$

	// $[EIE2 - Extended Interrupt Enable 2]
	/***********************************************************************
	 - Disable all I2C0 slave interrupts
	 - Disable Timer 4 interrupts
	 - Enable UART1 interrupts
	 - Disable all USB0 interrupts
	 - Disable all VBUS and VBUS and USB Charger Detect interrupts
	 ***********************************************************************/
	SFRPAGE = 0x10;
	EIE2 = EIE2_EI2C0__DISABLED | EIE2_ET4__DISABLED | EIE2_ES1__ENABLED
			| EIE2_EUSB0__DISABLED | EIE2_EVBUS__DISABLED;
	// [EIE2 - Extended Interrupt Enable 2]$

	// $[EIP1H - Extended Interrupt Priority 1 High]
	// [EIP1H - Extended Interrupt Priority 1 High]$

	// $[EIP1 - Extended Interrupt Priority 1 Low]
	// [EIP1 - Extended Interrupt Priority 1 Low]$

	// $[EIP2 - Extended Interrupt Priority 2]
	// [EIP2 - Extended Interrupt Priority 2]$

	// $[EIP2H - Extended Interrupt Priority 2 High]
	// [EIP2H - Extended Interrupt Priority 2 High]$

	// $[IE - Interrupt Enable]
	/***********************************************************************
	 - Enable each interrupt according to its individual mask setting
	 - Disable external interrupt 0
	 - Disable external interrupt 1
	 - Enable interrupt requests generated by SPI0
	 - Disable all Timer 0 interrupt
	 - Disable all Timer 1 interrupt
	 - Enable interrupt requests generated by the TF2L or TF2H flags
	 - Disable UART0 interrupt
	 ***********************************************************************/
	SFRPAGE = 0x00;
	IE = IE_EA__ENABLED | IE_EX0__DISABLED | IE_EX1__DISABLED
			| IE_ESPI0__ENABLED | IE_ET0__DISABLED | IE_ET1__DISABLED
			| IE_ET2__ENABLED | IE_ES0__DISABLED;
	// [IE - Interrupt Enable]$

	// $[IP - Interrupt Priority]
	/***********************************************************************
	 - External Interrupt 0 priority LSB set to low
	 - External Interrupt 1 priority LSB set to low
	 - SPI0 interrupt priority LSB set to high
	 - Timer 0 interrupt priority LSB set to low
	 - Timer 1 interrupt priority LSB set to low
	 - Timer 2 interrupt priority LSB set to low
	 - UART0 interrupt priority LSB set to low
	 ***********************************************************************/
	IP = IP_PX0__LOW | IP_PX1__LOW | IP_PSPI0__HIGH | IP_PT0__LOW | IP_PT1__LOW
			| IP_PT2__LOW | IP_PS0__LOW;
	// [IP - Interrupt Priority]$

	// $[IPH - Interrupt Priority High]
	/***********************************************************************
	 - External Interrupt 0 priority MSB set to low
	 - External Interrupt 1 priority MSB set to low
	 - SPI0 interrupt priority MSB set to high
	 - Timer 0 interrupt priority MSB set to low
	 - Timer 1 interrupt priority MSB set to low
	 - Timer 2 interrupt priority MSB set to low
	 - UART0 interrupt priority MSB set to low
	 ***********************************************************************/
	SFRPAGE = 0x10;
	IPH = IPH_PHX0__LOW | IPH_PHX1__LOW | IPH_PHSPI0__HIGH | IPH_PHT0__LOW
			| IPH_PHT1__LOW | IPH_PHT2__LOW | IPH_PHS0__LOW;
	// [IPH - Interrupt Priority High]$

}

extern void TIMER16_2_enter_DefaultMode_from_RESET(void) {
	// $[Timer Initialization]
	// Save Timer Configuration
	uint8_t TMR2CN0_TR2_save;
	TMR2CN0_TR2_save = TMR2CN0 & TMR2CN0_TR2__BMASK;
	// Stop Timer
	TMR2CN0 &= ~(TMR2CN0_TR2__BMASK);
	// [Timer Initialization]$

	// $[TMR2CN1 - Timer 2 Control 1]
	// [TMR2CN1 - Timer 2 Control 1]$

	// $[TMR2CN0 - Timer 2 Control]
	// [TMR2CN0 - Timer 2 Control]$

	// $[TMR2H - Timer 2 High Byte]
	// [TMR2H - Timer 2 High Byte]$

	// $[TMR2L - Timer 2 Low Byte]
	// [TMR2L - Timer 2 Low Byte]$

	// $[TMR2RLH - Timer 2 Reload High Byte]
	// [TMR2RLH - Timer 2 Reload High Byte]$

	// $[TMR2RLL - Timer 2 Reload Low Byte]
	// [TMR2RLL - Timer 2 Reload Low Byte]$

	// $[TMR2CN0]
	/***********************************************************************
	 - Start Timer 2 running
	 ***********************************************************************/
	TMR2CN0 |= TMR2CN0_TR2__RUN;
	// [TMR2CN0]$

	// $[Timer Restoration]
	// Restore Timer Configuration
	TMR2CN0 |= TMR2CN0_TR2_save;
	// [Timer Restoration]$

}

