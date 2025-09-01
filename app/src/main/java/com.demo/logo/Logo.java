///
/// @file
/// @brief - Logo on Android SurfaceView
///
package com.demo.logo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.util.AttributeSet;

public class Logo extends View {
    private Paint paint;

    public Logo(Context context) {
        super(context);
        init();
    }
    public Logo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw a red line
        canvas.drawLine(50, 50, 200, 200, paint);
    }
}
/*
const RGB = (v)=>`rgb(${(v>>16)&0xff} ${(v>>8)&0xff} ${v&0xff})`
const HSV = (h)=>{                    // 0 < h < 100
    let s = 1.0, v = 1.0
    let i = Math.floor(h * 0.06)
    let f = h * 0.06 - i
    let p = v * (1.0 - s)
    let q = v * (1.0 - f * s)
    let t = v * (1.0 - (1.0 - f) * s)
    var r, g, b
    switch (i % 6) {
    case 0: r = v, g = t, b = p; break
    case 1: r = q, g = v, b = p; break
    case 2: r = p, g = v, b = t; break
    case 3: r = p, g = q, b = v; break
    case 4: r = t, g = p, b = v; break
    case 5: r = v, g = p, b = q; break
    }
    return `rgb(${r*255|0} ${g*255|0} ${b*255|0})`
}
const RAD = Math.PI / 180.0
class Logo {
    static new_cv(n, w, z) {
        return ""+
            "<canvas class='sfc'"+
            `id="${n}" width="${w}" height="${w}" style="z-index:${z}" `+
            "oncontextmenu='event.preventDefault()'></canvas>"
    }
    constructor(div) {
        let e = document.getElementById(div)
        let w = e.offsetWidth, h = e.offsetHeight
        e.innerHTML += '<pre id="hint">To load Turtle Graphics, type \'s" forth/logo.fs" included\'</pre>'
        e.innerHTML += Logo.new_cv('eve', w, 0)
        e.innerHTML += Logo.new_cv('sfc', w, 1)
        this.pre = document.getElementById('hint')
        this.eve = document.getElementById('eve').getContext('2d')
        this.sfc = document.getElementById('sfc').getContext('2d')
        this.st  = {
            w: w, h: h, dir: 0, pw: 3, pen: 1, show: 0,
            fg: '#000', bg: '#FFF'
        }
//        console.log('logo='+JSON.stringify(this))
    }
    /// LOGO implementation
    clear_eve() {
        let e = this.eve
        e.beginPath()                 // erase Eve
        e.fillStyle = this.st.bg
        e.arc(16,0,24,0,360.0*RAD)    // circle to erase
        e.fill()
        this.pre.style.display = 'none'
    }
    draw_eve(c) {
        const W = Math.PI/15, X = Math.PI/6
        let e = this.eve
        e.beginPath()
        e.moveTo(0, 0)
        e.arc(0, 0, 20, -W, -X, true) // left shoulder
        e.moveTo(0, 0)
        e.arc(0, 0, 20, W, X)         // right shoulder
        e.moveTo(24, 0)
        e.arc(24, 0, 4, 0, Math.PI*2) // head
        e.strokeStyle = c
        e.stroke()
    }
    xform(x, y, d, rst=false) {
        let s = this.sfc, e = this.eve
        if (rst) {
            s.resetTransform()
            e.resetTransform()
            this.st.dir = 0
        }
        this.st.dir -= d
        s.translate(x, y); s.rotate(d)
        e.translate(x, y); e.rotate(d)
    }
    reset() {
        let t = this.st
        this.xform(t.w/2, t.h/2, -90.0*RAD, true)
        this.sfc.lineWidth   = t.pw
        this.eve.lineWidth   = 3
        this.eve.strokeStyle = '#F00'
        if (t.show) this.draw_eve(t.fg)
    }
    update(av) {
        let op = av[1],   v = av[2]
        let t  = this.st, s = this.sfc
        this.clear_eve()
        s.beginPath()
        s.moveTo(0, 0)
        switch (op) {
        case 'cs':
            s.save()
            s.resetTransform()
            s.clearRect(0,0,t.w,t.h)
            s.restore()
            break
        case 'st': t.show = 1;               break
        case 'ht': t.show = 0;               break
        case 'ct': this.reset();             break
        case 'pd': t.pen  = 1;               break
        case 'pu': t.pen  = 0;               break
        case 'hd': this.xform(0, 0, t.dir - v*RAD); break
        case 'fd': this.xform(v, 0, 0);      break
        case 'bk': this.xform(-v, 0, 0);     break  // ( d -- )
        case 'rt': this.xform(0, 0, v*RAD);  break  // ( a -- )
        case 'lt': this.xform(0, 0, -v*RAD); break  // ( a -- )
        case 'pc': t.fg = HSV(v);            break  // ( hue -- )
        case 'fg': t.fg = RGB(v);            break  // ( r g b -- )
        case 'bg': t.bg = RGB(v);            break  // ( r g b -- )
        case 'pw': s.lineWidth = t.pw = v;   break  // ( w -- )
        case 'xy':                                  // ( x y -- )
            let x = (v|0)&0xffff, y = (v|0)>>16
            x |= (x & 0x8000) ? 0xffff0000 : 0      // x negative
            y |= (y & 0x8000) ? 0xffff0000 : 0      // y negative
            this.xform(t.w/2+x, t.h/2-y,
                       -t.dir, true);        break
        default: return 0
        }
        if (t.pen) s.lineTo(0, 0)
        else       s.moveTo(0, 0)
        s.strokeStyle = t.fg
        s.stroke()
        if (t.show) this.draw_eve(t.fg)
        
        console.log(JSON.stringify(t))   // CC: tracing
        return 1
    }
}
*/
