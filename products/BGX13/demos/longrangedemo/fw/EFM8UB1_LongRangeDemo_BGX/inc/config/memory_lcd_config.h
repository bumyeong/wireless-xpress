#ifndef MEMORY_LCD_CONFIG_H_
#define MEMORY_LCD_CONFIG_H_

// Build Options
#define RENDER_STR_LINE_BUILD                   1
#define RENDER_LARGE_STR_LINE_BUILD             0
#define RENDER_NUMERIC_BUILD                    0
#define RENDER_VERTICAL_STR_LINE_BUILD          0

// Memory spaces for function parameters
#define RENDER_LINE_SEG                         SI_SEG_IDATA
#define RENDER_STR_SEG                          SI_SEG_GENERIC
#define RENDER_SPRITE_SEG                       const SI_SEG_CODE

#endif /* MEMORY_LCD_CONFIG_H_ */
