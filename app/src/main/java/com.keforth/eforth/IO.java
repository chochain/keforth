///
/// @file
/// @brief - IO module
///
package com.keforth.eforth;

import java.lang.Exception;
import java.lang.String;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Stack;
import java.util.function.Function;

///
///> console input/output
///
public class IO {
    private static final boolean DEBUG         = false;
    private static final int     CHARS_PER_ROW = 42;
    enum OP { CR, BL, EMIT, DOT, UDOT, DOTR, UDOTR }

    String        name;
    FV<Scanner>   ins = new FV<>();                         ///< input scanner stack
    Scanner       tok = null;                               ///< tokenizer (from in Scanner)
    OutputStream  out;                                      ///< Stream Output
    String        pad;                                      ///< tmp storage
//    String        dir0= null;                               ///< root directory
    StringBuffer  wd;                                       ///< working directory

    public IO(String n, InputStream i, OutputStream o) {
        name = n;                                           ///< name of the system (for mstat)
        ins.add(new Scanner(i));                            ///< stackup input streams
        out  = o;
        wd   = new StringBuffer();                          ///< working directory
    }
    ///
    ///> IO internal
    ///
    public void mstat() {
        Runtime rt   = Runtime.getRuntime();
        long    max  = rt.maxMemory() / 1024 / 1024;        ///< heap size
        long    tot  = rt.totalMemory() / 1024 / 1024;      ///< JVM allcated
        long    used = tot - rt.freeMemory() / 1024 / 1024; ///< used memory
        long    free = max - used;
        double  pct  = 100.0 * free / max;
        String  str  = new String().format(
            "\n%s, RAM %3.1f%% free (%d / %d MB)\n", name, pct, free, max);
        pstr(str);
    }
    public void rescan(String s) {                          ///< update input stream
        Scanner sc = new Scanner(s);
        ins.set(ins.size() - 1, sc);
    }
    public boolean readline() {
        String tib = null;
        tok = ins.tail().hasNextLine()                      ///< create tokenizer
            ? new Scanner(tib=ins.tail().nextLine()) : null;
        if (tib!=null && load_depth() > 0) debug(tib+"\n"); ///< echo if needed
        return tok != null;
    }
    String next_token() {                                   ///< fetch next token from in stream
        return tok.hasNext() ? tok.next() : null;
    }   
    String scan(String delim) {
        Pattern d = tok.delimiter();                        ///< keep delimiter (SPC)
        tok.useDelimiter(delim); pad = next_token();        /// * read to delimiter (into pad)
        tok.useDelimiter(d);     next_token();              /// * restore and skip off delim
        return pad==null ? null : (pad=pad.substring(1));   /// * drop first char (a SPC)
    }
    ///
    ///> IO methods
    ///
    public void pstr(String s) {
        try { out.write(s.getBytes(), 0, s.length()); }
        catch (Exception e) { err(e); }
    }
    public void pchr(int n) {
        try { out.write(n); }
        catch (Exception e) { err(e); }
    }
    public void debug(String s)  { if (DEBUG) pstr(s);  }
    public void err(Exception e) { e.printStackTrace(); }
    int    key() { return (int)next_token().charAt(0); }
    String pad() { return pad; }
    String itoa(int n, int base) { return Integer.toString(n, base); }
    void spaces(int n) {
        for (int i=0; i < Math.max(1,n); i++) pstr(" ");
    }
    void dot(OP op, int n, int r, int base) {
        switch (op) {
        case CR:   pstr("\n");                     break;
        case BL:   pchr(0x20);                     break;
        case EMIT: pchr(n);                        break;
        case DOT:  pstr(itoa(n ,base) + " ");      break;
        case UDOT: pstr(itoa(n&0x7fffffff, base)); break;
        case DOTR: {
            String s = itoa(n, base);
            spaces(r - s.length());
            pstr(s);
        } break;
        case UDOTR: {
            String s = itoa(n & 0x7fffffff, base);
            spaces( r - s.length());
            pstr(s);
        } break;
        }
    }
    void dot(OP op, int n, int r) { dot(op, n, r, 10); }
    void dot(OP op, int n)        { dot(op, n, 0, 10); }
    void cr()                     { dot(OP.CR, 0, 0, 10); }
    void bl()                     { dot(OP.BL, 0, 0, 10); }
    ///
    /// File ops
    ///
    void pwd() { pstr(wd.toString()+"/"); }
    void dir(String d) {                                     ///< display directory
        String fd  = wd.toString() + "/" + (d==null ? "" : d);
        File   dir = new File(fd);                           ///< directory on full_path
        if (!(dir.exists() && dir.isDirectory())) {
            pstr("dir " + fd + " exists?\n");
            return;
        }
        pstr("dir="+fd+"\n");
        for (File f : dir.listFiles()) {                     ///< list from 'current dir'
            pstr(f.getName()+"  ");
        }
        cr();
    }
    void cd(String d) {
        if (d==null)             wd.setLength(0);
        else if (d.equals("..")) wd.delete(wd.lastIndexOf("/"), wd.length());
        else if (!d.equals(".")) wd.append("/").append(d);
    }
    int load_depth() { return ins.size() - 1; }             /// * depth or recursive loading
    int load(InputStream st, BooleanSupplier outer) {
        Scanner tok0 = tok;                                 ///< backup tokenizer
        int i = 0;
        try (Scanner sc = new Scanner(st)) {                ///< auto-close scanner
            ins.add(sc);                                    /// * switch input stream
            while (readline()) {                            /// * load from file now
                i++;
                if (!outer.getAsBoolean()) break;           /// * pass to outer interpreter
            }
        }
        catch (Exception e) { err(e); }                     /// * just in case 
        finally {
            ins.drop();                                     /// * restore input stream
        }
        tok = tok0;
        return i;                                           /// return line loaded
    }
    int load(String fn, BooleanSupplier outer) {
         InputStream st;
        try {
            st = new FileInputStream(fn);
            return load(st, outer);
        }
        catch (IOException e) {
            err(e);
            return 0;
        }
    }
    ///
    ///> Debug ops
    ///
    void ss_dump(Stack<Integer> ss, int base) {             /// ok
        for (int n : ss) pstr(itoa(n, base)+" ");
    }
    void words(Dict dict) {
        int i=0, sz = 0; 
        for (Code w : dict) {
            pstr("  " + w.name);
            sz += w.name.length() + 2;                      /// width control
            if (sz > CHARS_PER_ROW) { cr(); sz = 0; }
        }
        cr();
    }
    void see(Code c, int base, int dp) {
        if (c==null) return;
        Consumer<String> tab = s->{
            int i = dp;
            cr();
            while (i-->0) { pstr("  "); } pstr(s);
        };
        tab.accept((dp == 0 ? ": " : "")+c.name+" ");
        c.pf.forEach(w -> see(w, base, dp+1));
        if (!c.p1.isEmpty()) {
            tab.accept("( 1-- )");  c.p1.forEach(w -> see(w, base, dp+1));
        }
        if (!c.p2.isEmpty()) {
            tab.accept("( 2-- )");  c.p2.forEach(w -> see(w, base, dp+1));
        }
        if (!c.qf.isEmpty())  {
            pstr(" \\ ="); c.qf.forEach(i -> pstr(itoa(i, base)+" "));
        }
        if (c.str != null)  pstr(" \\ =\""+c.str+"\" ");
        if (dp == 0) pstr("\n; ");
    }
    public String serialize(String fmt, Dict dict, Stack<Integer> ss) {
        Function<Character, String> t2s = (Character c) -> {
            StringBuilder n = new StringBuilder();
            n.setLength(0);  // Clear StringBuilder (equivalent to n.str(""))
            
            switch (c) {
            case 'd': n.append(ss.pop().toString());                break;
            case 'f': n.append((float)ss.pop());                    break;
            case 'x': n.append("0x").append(ss.pop().toString(16)); break;
            case 's':
                int len = ss.pop(), i_w = ss.pop();
                n.append("\"").append(dict.gets(i_w)).append("\""); break;
            case 'p':
                int p1 = ss.pop(), p2 = ss.pop();
                n.append("p ").append(p1).append(' ').append(p2);   break;
            default: n.append(c).append('?');                       break;
            }
            return n.toString();
        };
        StringBuilder pad = new StringBuilder(fmt);
        /// Process format specifiers from back to front
        /// Find % from back until not found
        for (int i = pad.lastIndexOf("%"); 
             i != -1; i = (i > 0) ? pad.lastIndexOf("%", i - 1) : -1) {
            if (i > 0 && pad.charAt(i - 1) == '%') {  /// handle %%
                pad.delete(i - 1, i);                 /// Drop one %
                i--;                                  /// Adjust index after deletion
                continue;
            }
            /// Single % followed by format character
            if (i + 1 < pad.length()) {
                String x = t2s.apply(pad.charAt(i + 1));
                pad.replace(i, i + 2, x);
            }
        }
        /// Pass to JavaScript call handler (equivalent to js_call)
        return pad.toString();
    }
}
