///
/// @file
/// @brief - IO module
///
package eforth

import java.io.*
import java.util.*
import kotlin.random.Random
///
///> console input/output
///
class IO(
    val name: String,
    i: InputStream,
    o: PrintStream
) {
    companion object { private const val DEBUG = false }
    enum class OP { CR, BL, EMIT, DOT, UDOT, DOTR, UDOTR, SPCS }

    private val rnd = Random                               ///< random number generator
    private val ins = FV<Scanner>()                        ///< input scanner stack
    private var tok: Scanner? = null                       ///< tokenizer
    private val out: PrintWriter                           ///< streaming output
    private var pad: String? = null                        ///< tmp storage

    init {
        ins.add(Scanner(i))                                ///< keep scanner on stack
        out = PrintWriter(o, true)
    }
    fun mstat() {
        val rt   = Runtime.getRuntime()
        val max  = rt.maxMemory() / 1024 / 1024            ///< heap size
        val tot  = rt.totalMemory() / 1024 / 1024          ///< JVM allocated
        val used = tot - rt.freeMemory() / 1024 / 1024     ///< used memory
        val free = max - used
        val pct  = 100.0 * free / max
        out.printf("\n%s, RAM %3.1f%% free (%d / %d MB)\n", name, pct, free, max)
    }
    fun readline(): Boolean {
        var tib: String? = null
        tok = if (ins.last().hasNextLine()) {             ///< create tokenizer
            tib = ins.last().nextLine()
            Scanner(tib)
        } else null
        
        if (loadDepth() > 0) tib?.let { debug("$it\n") }  ///< echo if needed
        return tok != null
    }
    fun pstr(s: String)   { out.print(s); out.flush() }
    fun pchr(n: Int)      { out.print(Character.toChars(n)) }
    fun debug(s: String)  { if (DEBUG) pstr(s) }
    fun err(e: Exception) {  e.printStackTrace() }
    fun nextToken(): String? {                             ///< fetch next token from in stream
        return if (tok?.hasNext() == true) tok!!.next() else null
    }
    fun scan(delim: String): String? {
        val d = tok?.delimiter()                           ///< keep delimiter (SPC)
        tok?.useDelimiter(delim); pad = nextToken()        /// * read to delimiter (into pad)
        tok?.useDelimiter(d);     nextToken()              /// * restore and skip off delim
        return if (pad != null) {
            pad!!.substring(1).also { pad = it }           /// * drop first char (a SPC)
        } else null
    }
    ///
    ///> IO methods
    ///
    fun key(): Int = nextToken()?.get(0)?.code ?: 0
    fun pad(): String? = pad
    fun itoa(n: Int, base: Int): String = Integer.toString(n, base)
    fun spaces(n: Int) { repeat(maxOf(1, n)) { pstr(" ") } }
    fun dot(op: OP, n: DU = 0, r: Int = 0, base: Int = 10) {
        when (op) {
            OP.CR   -> pstr("\n")
            OP.BL   -> pchr(0x20)
            OP.EMIT -> pchr(n)
            OP.DOT  -> pstr("${itoa(n, base)} ")
            OP.UDOT -> pstr(itoa(n and 0x7fffffff, base))
            OP.DOTR -> {
                val s = itoa(n, base)
                spaces(r - s.length)
                pstr(s)
            }
            OP.UDOTR -> {
                val s = itoa(n and 0x7fffffff, base)
                spaces(r - s.length)
                pstr(s)
            }
            OP.SPCS -> spaces(n)
        }
    }
    fun cr() = dot(OP.CR)
    fun bl() = dot(OP.BL)
    ///
    ///> ok - stack dump and OK prompt
    ///
    fun ssDump(ss: Stack<DU>, base: Int) {
        for (n in ss) pstr("${itoa(n, base)} ")
    }
    fun words(dict: Dict) {
        var sz = 0
        for (w in dict) {
            pstr("  ${w.name}")
            sz += (w.name?.length ?: 0) + 2                ///< width control
            if (sz > 64) {
                cr()
                sz = 0
            }
        }
        cr()
    }
    fun see(c: Code?, base: Int, dp: Int) {
        if (c == null) return
       
        val tab: (String) -> Unit = { s ->
            if (dp > 0) cr()
            repeat(dp) { pstr("  ") }
            pstr(s)
        }
        if (c.name!="\t") tab("${if (dp == 0) ": " else ""}${c.name} ")
        c.pf.forEach { w -> see(w, base, dp + 1) }
        if (c.p1.size > 0) {
            tab("( 1-- )"); c.p1.forEach { w -> see(w, base, dp + 1) }
        }
        if (c.p2.size > 0) {
            tab("( 2-- )"); c.p2.forEach { w -> see(w, base, dp + 1) }
        }
        if (c.qf.size > 0) {
            pstr(" \\ =");  c.qf.forEach { i -> pstr("${itoa(i, base)} ") }
        }
        c.str?.let { pstr(" \\ =\"$it\" ") }
        if (dp == 0) pstr("\n;\n")
    }
    ///
    ///> external modules
    ///
    fun rnd(t: Int): Int { return rnd.nextInt(t) }         /// ranged random number [0..t)
    fun loadDepth(): Int = ins.size - 1                    /// * depth or recursive loading
    fun load(fn: String, xt: () -> Boolean): Int {
        val tok0 = tok                                     /// * backup tokenizer
        var i = 0
        try {
            Scanner(File(fn)).use { sc ->                  ///< auto close scanner
                ins.add(sc)                                /// * keep input scanner
                while (readline()) {                       /// * load from file now
                    i++
                    if (!xt()) break
                }
            }
        } catch (e: IOException) {                         /// * just in case 
            err(e)
        } finally { ins.removeLast() }                     /// * restore scanner
        
        tok = tok0                                         /// * restore tokenizer
        return i
    }
}
