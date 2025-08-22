///
/// @file 
/// @brief - Code class - the core component of eForth
///
package eforth

import java.util.*

typealias Unnest = ArithmeticException

class Immd : Code {
    constructor(n: String, f: Xt) : super(n, true, f) {}  ///< immediate words
}
open class Code {
    companion object {
        @JvmStatic
        var fence = 0                      ///< token index
    }
    var name: String
    var immd: Boolean = false
    var token: Int = 0
    var stage: Int = 0
    
    var xt: Xt? = null                     ///< execution token
    var pf = FV<Code>()                    ///< if..pf..
    var p1 = FV<Code>()                    ///< else..p1..then
    var p2 = FV<Code>()                    ///< aft..next
    var qf = FV<DU>()                      ///< variable storage
    var str: String? = null                ///< string storage
    ///
    ///> constructors
    ///
    constructor(n: String, im: Boolean=false, f: Xt) {  ///< built-in words
        name = n; xt = f; immd = im; token = fence++
    }
    constructor(n: String) {                      ///< colon words
        name = n; token = fence++
    }
    constructor(f: Xt, n: String) {               ///< branching nodes
        name = n; xt = f
    }
    constructor(f: Xt, n: String, d: DU) {        ///< int literal
        name = n; xt = f; qf.add(d)
    }
    constructor(f: Xt, n: String, s: String) {    ///< string literal
        name = n; xt = f; str = s
    }
    ///
    ///> attribute setting
    ///
    fun immediate(): Code { immd = true; return this }
    ///
    ///> variable storage management methods
    ///
    fun comma(v: DU)          = pf.head().qf.add(v)
    fun setVar(i: Int, v: DU) = pf.head().qf.set(i, v)
    fun getVar(i: Int): DU    = pf.head().qf.get(i)
    ///
    ///> inner interpreter
    ///
    fun nest() {
        xt?.let { it(this); return }
        for (w in pf) {
            try { w.nest() }
            catch (e: Unnest) { break; }           /// * capture UNNEST
        }
    }
    fun nest(pf: FV<Code>) { for (w in pf) { w.nest() } }
    fun unnest() { throw Unnest() }
    ///
    ///> branching, looping methods
    ///
    fun branch(ss: Stack<DU>) {
        for (w in if (ss.pop() != 0) pf else p1) {
            w.nest()
        }
    }
    fun begin(ss: Stack<DU>) {
        val b = stage
        while (true) {
            nest(pf)                              /// * begin..
            if (b == 0 && ss.pop() != 0) break    /// * ..until
            if (b == 1) continue                  /// * ..again
            if (b == 2 && ss.pop() == 0) break    /// * ..while..repeat
            nest(p1)
        }
    }
    fun dofor(rs: Stack<DU>) {
        try {
            var i: DU
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
        } catch (e: Unnest) {                     /// * handle EXIT
            /* leave */
        } finally { rs.pop() }                    ///> pop off index
    }
    fun loop(rs: Stack<DU>) {                     ///> do..loop
        try {
            var i: DU
            val m = rs.pop()
            while (true) {
                nest(pf)
                i = rs.pop()
                rs.push(++i)
                if (i >= m) break
            }
        } catch (e: Unnest) {                      /// * handle LEAVE
            /* leave */
        } finally { rs.pop() }
    }
}

