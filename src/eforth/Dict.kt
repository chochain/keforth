///
/// @file 
/// @brief - generic List class - extended ArrayList
///
package eforth

import java.util.*
import java.io.*
import java.time.*

class Dict : FV<Code>() {
    companion object {
        @JvmStatic
        private val dict = Dict()
        
        @JvmStatic
        fun getInstance(): Dict = dict                   ///< singleton
    }
    ///
    /// dictionary indexing i_w = |   pf index   |  word index  |
    /// and proxies               |<-- 16-bit -->|<-- 16-bit -->|
    ///
    fun idx(): Int {
        return ((dict.last().pf.size - 1) shl 16) or dict.last().token
    }
    fun getv(iw: DU): DU?   = dict[iw and 0x7fff]?.getVar(iw shr 16)
    fun setv(iw: DU, n: DU) = dict[iw and 0x7fff]?.setVar(iw shr 16, n)
    fun str(iw: DU): String = dict[iw and 0x7fff]?.pf[iw shr 16]?.str ?: ""
    ///
    /// dictionary clean up
    ///
    fun drop()         = dict.removeLast()                  ///> remove last item
    fun forget(t: Int) = dict.subList(t, dict.size).clear() ///> forget words
    ///
    ///> find - Forth dictionary search 
    ///    @param  str  input string to be search against dictionary words
    ///    @return      Code found; null - if not found
    ///
    fun find(s: String, compile: Boolean): Code? {
        val n = dict.size - (if (compile) 2 else 1)     ///> compile to prevent recursive
        for (i in n downTo 0) {                         /// search array from tail to head
            val w = dict[i]
            if (s == w.name) return w
        }
        return null
    }
    fun compile(w: Code): Code {                        ///> compile a token into pf
        dict.last().pf.add(w)
        return w
    }
    fun bran(): Code = dict[dict.size - 2].pf.last()    ///> branching target
}
