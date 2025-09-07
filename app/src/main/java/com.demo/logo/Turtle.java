///
/// @file
/// @brief - Renderer Interface
///
/// * Abstract interface for different rendering backends
/// * Decouples drawing logic from specific graphics APIs
/// * Makes it easy to support different backends (Canvas, OpenGL, SVG, etc.)
/// * Commands are simple and focused on drawing primitives
package com.demo.logo;

public interface Turtle {
    void clear();
    void moveTo(float x, float y, boolean penDown);
    void setColor(int color);
    void setWidth(int width);
    void drawText(String text, float x, float y, float angle);
    void drawTurtle(float x, float y, float angle, int color, boolean visible);
    
    void show();                                 /// Finish drawing and display
}
