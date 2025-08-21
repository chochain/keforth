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
    ///> create dictionary with given word list
    ///
    fun forget(t: Int) {
        dict.subList(t, dict.size).clear()              ///> forget words
    }
    ///
    ///> find - Forth dictionary search 
    ///    @param  str  input string to be search against dictionary words
    ///    @return      Code found; null - if not found
    ///
    fun find(s: String, compile: Boolean): Code? {
        val n = dict.size - (if (compile) 2 else 1)
        for (i in n downTo 0) {                         /// search array from tail to head
            val w = dict[i]
            if (s == w.name) return w
        }
        return null
    }
    fun compile(w: Code): Code {
        dict.tail().pf.add(w)
        return w
    }
    fun bran(): Code = dict.tail(2).pf.tail()
}
