///
/// @file
/// @brief - Renderer Interface
///
/// * Abstract interface for different rendering backends
/// * Decouples drawing logic from specific graphics APIs
/// * Makes it easy to support different backends (Canvas, OpenGL, SVG, etc.)
/// * Commands are simple and focused on drawing primitives
package com.demo.logo;

public interface Renderer {
    void clear();
    void setColor(int color);
    void setWidth(int width);
    
    void moveTo(float x, float y, boolean penDown);
    void label(String txt, float x, float y, float angle);
    void drawTurtle(float x, float y, float angle, int color, boolean show);
    
    void render();                                 /// Finish drawing and display
}
