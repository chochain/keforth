///
/// @file
/// @brief - Blip Interface, the View renderer
///
/// * Abstract interface for different rendering backends
/// * Decouples drawing logic from specific graphics APIs
/// * Makes it easy to support different backends (Canvas, OpenGL, SVG, etc.)
/// * Commands are simple and focused on drawing primitives
///
package com.keforth.logo;

public interface Blip {
    void init(int w, int h, int fg, int pw, int ts);       ///< initialize attributes
    void setColor(int color);                              ///< set pen color
    void setWidth(int pw);                                 ///< set pen stroke width
    void setTextSize(int ts);                              ///< set text font size
    
    void moveTo(float x, float y, boolean penDown);        ///< move turtle to x,y
    void label(String txt, float x, float y, float angle); ///< place label at x,y
    
    void draw(float x, float y, float angle, int color, boolean show); ///< place turtle at x,y
    void clear();                                          ///< clean draw path
}
