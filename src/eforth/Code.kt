///
/// @file 
/// @brief - Code class - the core component of eForth
///
package eforth

import java.util.*

class Code {
    companion object {
        @JvmStatic
        var fence = 0                      ///< token index
    }

    var name: String? = null
    var immd: Boolean = false
    var token: Int = 0
    var stage: Int = 0
    
    var xt: ((Code) -> Unit)? = null       ///< execution token
    var pf = FV<Code>()                    ///< if..pf..
    var p1 = FV<Code>()                    ///< else..p1..then
    var p2 = FV<Code>()                    ///< aft..next
    var qf = FV<Int>()                     ///< variable storage
    var str: String? = null                ///< string storage
    
    ///
    ///> constructors
    ///
    constructor(n: String, f: (Code) -> Unit, im: Boolean) {                 ///< built-in words
        name = n
        xt = f
        immd = im
        token = fence++
    }
    
    constructor(n: String) {                                                  ///< colon words
        name = n
        token = fence++
    }
    
    constructor(f: (Code) -> Unit, n: String) {                             ///< branching nodes
        name = n
        xt = f
    }
    
    constructor(f: (Code) -> Unit, n: String, d: Int) {                     ///< int literal
        name = n
        xt = f
        qf.add(d)
    }
    
    constructor(f: (Code) -> Unit, n: String, s: String) {                  ///< string literal
        name = n
        xt = f
        str = s
    }
    
    ///
    ///> attribute setting
    ///
    fun immediate(): Code {
        immd = true
        return this
    }
    
    ///
    ///> variable storage management methods
    ///
    fun comma(v: Int) {
        pf.head().qf.add(v)
    }
    
    fun setVar(i: Int, v: Int) {
        pf.head().qf.set(i, v)
    }
    
    fun getVar(i: Int): Int {
        return pf.head().qf.get(i)
    }
    
    ///
    ///> inner interpreter
    ///
    fun nest() {
        xt?.let { 
            it(this)
            return 
        }
        for (w in pf) {
            try {
                w.nest()
            } catch (e: ArithmeticException) {
                break   ///* capture UNNEST
            }
        }
    }
    
    fun nest(pf: FV<Code>) {
        for (w in pf) {
            w.nest()
        }
    }
    
    fun unnest() {
        throw ArithmeticException()
    }
    
    ///
    ///> branching, looping methods
    ///
    fun branch(ss: Stack<Int>) {
        for (w in if (ss.pop() != 0) pf else p1) {
            w.nest()
        }
    }
    
    fun begin(ss: Stack<Int>) {
        val b = stage
        while (true) {
            nest(pf)                              /// * begin..
            if (b == 0 && ss.pop() != 0) break   /// * ..until
            if (b == 1) continue                  /// * ..again
            if (b == 2 && ss.pop() == 0) break   /// * ..while..repeat
            nest(p1)
        }
    }
    
    fun dofor(rs: Stack<Int>) {
        try {
            var i: Int
            val b = stage
            do {
                nest(pf)                          ///> for..
                if (b > 0) break                  ///> ..aft..
                i = rs.pop()                      ///> decrement i (expensive)
                rs.push(--i)
            } while (i >= 0)
            
            while (b > 0) {
                nest(p2)
                i = rs.pop()
                rs.push(--i)
                if (i < 0) break
                nest(p1)
            }
        } catch (e: Exception) {
            // leave
        } finally {
            rs.pop()                              ///> pop off index
        }
    }
    
    fun loop(rs: Stack<Int>) {                    ///> do..loop
        try {
            var i: Int
            val m = rs.pop()
            while (true) {
                nest(pf)
                i = rs.pop()
                rs.push(++i)
                if (i >= m) break
            }
        } catch (e: Exception) {
            // leave - handle LEAVE
        } finally {
            rs.pop()
        }
    }
}
