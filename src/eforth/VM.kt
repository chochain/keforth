///
/// @file 
/// @brief - Virtual Machine class
///
package eforth

import java.io.*
import java.time.*
import java.util.*

typealias Xt    = (Code) -> Unit                         ///< Code lambda
typealias DU    = Int                                    ///< data unit
typealias FV<T> = ArrayList<T>                           ///< generic Forth Vector

class VM(val io: IO) {
    val dict: Dict
    ///
    ///> Forth stacks and dictionary
    ///
    val ss = Stack<DU>()
    val rs = Stack<DU>()
    ///
    ///> Forth internal variables
    ///
    var base    = 10                                     ///< numeric radix
    var run     = true                                   ///< VM execution flag
    var compile = false                                  ///< state: interpreter or compiling
    
    init {
        dict = Dict.getInstance()
        dictInit()                                       /// * populate dictionary
    }
    ///
    ///> Forth outer interpreter - process one line a time
    ///
    fun ok(stat: Boolean) {
        if (stat) io.mstat()
        if (io.loadDepth() > 0) return                   /// * skip when loading
        if (compile) io.pstr("> ")                       ///> compile mode prompt
        else {
            io.pstr("< ")
            io.ssDump(ss, base)
            io.pstr(">ok ")                              ///> * OK prompt (interpreter)
        }
    }
    fun outer(): Boolean {
        var idiom = io.nextToken()
        while (run && idiom != null) {                   ///> parse next token
            parse(idiom)
            idiom = io.nextToken()
        }
        ok(false)
        return run                                       ///> * return VM status
    }
    private fun parse(idiom: String) {                   ///> outer interpreter
        io.debug("find $idiom")
        val w = dict.find(idiom, compile)                ///< search dictionary
        if (w != null) {                                 ///> found word?
            io.debug(" => [${w.token}]${w.name}\n")
            if (!compile || w.immd) {                    ///> in interpreter mode?
                try { w.nest() }                         ///> * execute immediately
                catch (e: Exception) { io.err(e) }       ///> * just-in-case it failed
            } else dict.compile(w)                       ///> add to dictionary if in compile mode
            return
        } else io.debug(" => not found")
        
        ///> word not found, try as a number
        try {
            val n = idiom.toInt(base)                    ///> * try process as a number
            io.debug(" => $n\n")
            if (compile) {                               ///>> in compile mode 
                dict.compile(Code(_dolit,"lit", n))      ///> add to latest defined word
            } else ss.push(n)                            ///> or, add number to top of stack
        }
        catch (ex: NumberFormatException) {              ///> if it's not a number
            io.pstr("$idiom ?")                          ///> * show not found sign
            compile = false
        }
    }
    private fun word(existed: Boolean): Code? {
        val s = io.nextToken() ?: return null
        val w = dict.find(s, compile)
        return if (existed) {
            if (w == null) io.pstr("$s?")
            w
        } else {
            if (w != null) io.pstr("$s reDef?")
            Code(s)                                      ///> create new Code
        }
    }
    private fun word(): Code? = word(false)              ///> read token
    private fun tick(): Code? = word(true)               ///> find existed word
    ///
    ///> ALU functions (aka. macros)
    ///
    private fun BOOL(f: Boolean): DU = if (f) -1 else 0
    private fun UINT(v: DU): DU = v and 0x7fffffff
    private inline fun ALU(m: (DU) -> DU) {              ///> TOS = fn(TOS)
        val n = ss.pop(); ss.push(m(n))
    }
    private inline fun ALU(m: (DU, DU) -> DU) {          ///> TOS = fn(TOS, NOS)
        val n = ss.pop(); ss.push(m(ss.pop(), n))
    }
    ///
    ///> MMU macros
    ///
    private fun GETV(iw: DU): DU = dict[iw and 0x7fff].getVar(iw shr 16)
    private fun SETV(iw: DU, n: DU) {
        dict[iw and 0x7fff].setVar(iw shr 16, n)
        if (iw == 0) base = n                            /// * also update base
    }
    private fun IDX(): Int {                             ///< calculate String index
        return ((dict.last().pf.size - 1) shl 16) or dict.last().token
    }
    private fun STR(iw: DU): String? {
        if (iw < 0) return io.pad()
        return dict[iw and 0x7fff].pf[iw shr 16].str
    }
    ///
    ///> built-in words and macros
    ///
    private val _tmp:    Xt = { /* do nothing */ }
    private val _dolit:  Xt = { ss.push(it.qf[0]) }
    private val _dostr:  Xt = { 
        ss.push(it.token)
        ss.push(STR(it.token)?.length ?: 0)
    }
    private val _dotstr: Xt = { it.str?.let { io.pstr(it) } }
    private val _branch: Xt = { it.branch(ss) }
    private val _begin:  Xt = { it.begin(ss) }
    private val _for:    Xt = { it.dofor(rs) }
    private val _loop:   Xt = { it.loop(rs) }
    private val _tor:    Xt = { rs.push(ss.pop()) }
    private val _tor2:   Xt = { 
        rs.push(ss.pop())
        rs.push(ss.pop())
    }
    private val _dovar:  Xt = { ss.push(it.token) }
    private val _dodoes: Xt = {
        var hit = false
        for (w in dict[it.token].pf) {                   /// * scan through defining word
            if (w == it) hit = true                      /// does> ...
            else if (hit) dict.compile(w)                /// capture words
        }
        it.unnest()                                      /// exit nest
    }
    private fun ADDW(w: Code) { dict.compile(w) }
    private fun CODE(n: String, f: Xt) = dict.add(Code(n, false, f))
    private fun IMMD(n: String, f: Xt) = dict.add(Code(n, true,  f))
    private fun BRAN(pf: FV<Code>) {
        val t = dict.last(); pf += t.pf; t.pf.clear()
    }
    private fun CODEX(n: String): Code {
        val w = Code(n, false); dict.add(w); return w
    }
    ///
    ///> create dictionary - built-in words
    ///
/*    
    private var prim = mutableListOf<Code>(               /// * experimental, not used
        Code("bye") { run = false },
        Code("+")   { ALU { n, t -> n + t } },
        Immd("(")   { io.scan("\\)") }
    )
 */
    private fun allotBase() {
        val f: Xt = { ss.push(it.qf[0]) }                 /// * _dolit created after init
        val b = Code(f, "lit", base)                      ///< use dict[0] as base store
        b.token = 0
        ADDW(b)
    }        
    private fun dictInit() {
        CODE("bye")    { run = false }
        allotBase();                                      ///< use dict[0] as base store
        
        /// @defgroup ALU ops
        /// @{
        CODE("+")      { ALU { n, t -> n + t        } }
        CODE("-")      { ALU { n, t -> n - t        } }
        CODE("*")      { ALU { n, t -> n * t        } }
        CODE("/")      { ALU { n, t -> n / t        } }
        CODE("mod")    { ALU { n, t -> n % t        } }
        CODE("*/")     {
            val n = ss.pop()
            ss.push(ss.pop() * ss.pop() / n)
        }
        CODE("*/mod") {
            val n = ss.pop()
            val m = ss.pop() * ss.pop()
            ss.push(m % n)
            ss.push(m / n)
        }
        CODE("and")    { ALU { n, t -> n and t      } }
        CODE("or")     { ALU { n, t -> n or t       } }
        CODE("xor")    { ALU { n, t -> n xor t      } }
        CODE("abs")    { ALU { kotlin.math.abs(it)  } }
        CODE("negate") { ALU { -it                  } }
        CODE("invert") { ALU { UINT(it).inv()       } }
        CODE("rshift") { ALU { n, t -> n ushr t     } }
        CODE("lshift") { ALU { n, t -> n shl t      } }
        CODE("max")    { ALU { n, t -> maxOf(n, t)  } }
        CODE("min")    { ALU { n, t -> minOf(n, t)  } }
        CODE("2*")     { ALU { it * 2               } }
        CODE("2/")     { ALU { it / 2               } }
        CODE("1+")     { ALU { it + 1               } }
        CODE("1-")     { ALU { it - 1               } }
        /// @}
        /// @defgroup Logic ops
        /// @{
        CODE("0=")     { ALU { BOOL(it == 0)        } }
        CODE("0<")     { ALU { BOOL(it < 0)         } }
        CODE("0>")     { ALU { BOOL(it > 0)         } }
        CODE("=")      { ALU { n, t -> BOOL(n == t) } }
        CODE(">")      { ALU { n, t -> BOOL(n > t)  } }
        CODE("<")      { ALU { n, t -> BOOL(n < t)  } }
        CODE("<>")     { ALU { n, t -> BOOL(n != t) } }
        CODE(">=")     { ALU { n, t -> BOOL(n >= t) } }
        CODE("<=")     { ALU { n, t -> BOOL(n <= t) } }
        CODE("u<")     { ALU { n, t -> BOOL(UINT(n) < UINT(t)) } }
        CODE("u>")     { ALU { n, t -> BOOL(UINT(n) > UINT(t)) } }
        /// @}
        /// @defgroup Data Stack ops
        /// @{
        CODE("dup")    { ss.push(ss.peek())                }
        CODE("drop")   { ss.pop()                          }
        CODE("over")   { ss.push(ss[ss.size - 2])          }
        CODE("swap")   { ss.add(ss.size - 2, ss.pop())     }
        CODE("rot")    { ss.push(ss.removeAt(ss.size - 3)) }
        CODE("-rot")   {
            ss.push(ss.removeAt(ss.size - 3))
            ss.push(ss.removeAt(ss.size - 3))
        }
        CODE("pick")   {
            val i = ss.pop()
            val n = ss[ss.size - i - 1]
            ss.push(n)
        }
        CODE("roll")   {
            val i = ss.pop()
            val n = ss.removeAt(ss.size - i - 1)
            ss.push(n)
        }
        CODE("nip")    { ss.removeAt(ss.size - 2)               }
        CODE("?dup")   { if (ss.peek() != 0) ss.push(ss.peek()) }
        /// @}
        
        /// @defgroup Data Stack ops - double
        /// @{
        CODE("2dup")   { ss.addAll(ss.subList(ss.size - 2, ss.size)) }
        CODE("2drop")  { ss.pop(); ss.pop() }
        CODE("2swap")  {
            ss.push(ss.removeAt(ss.size - 4))
            ss.push(ss.removeAt(ss.size - 4))
        }
        CODE("2over")  { ss.addAll(ss.subList(ss.size - 4, ss.size - 2)) }
        /// @}
        /// @defgroup Return Stack ops
        /// @{
        CODE(">r")     { rs.push(ss.pop())  }
        CODE("r>")     { ss.push(rs.pop())  }
        CODE("r@")     { ss.push(rs.peek()) }
        CODE("i")      { ss.push(rs.peek()) }
        /// @}
        /// @defgroup Return Stack ops - Extra
        /// @{
        CODE("push")   { rs.push(ss.pop())  }
        CODE("pop")    { ss.push(rs.pop())  }
        /// @}
        /// @defgroup IO ops
        /// @{
        CODE("base")   { ss.push(0)                                }
        CODE("hex")    { dict[0].setVar(0, 16.also { base = it })  }
        CODE("decimal"){ dict[0].setVar(0, 10.also { base = it })  }
        CODE("cr")     { io.cr()                                   }
        CODE("bl")     { io.bl()                                   }
        CODE(".")      { io.dot(IO.OP.DOT, ss.pop(), base = base)  }
        CODE("u.")     { io.dot(IO.OP.UDOT, ss.pop(), base = base) }
        CODE(".r")     {
            val r = ss.pop()
            val n = ss.pop()
            io.dot(IO.OP.DOTR, n, r, base)
        }
        CODE("u.r")    {
            val r = ss.pop()
            val n = ss.pop()
            io.dot(IO.OP.UDOTR, n, r, base)
        }
        CODE("type")   {
            ss.pop()                                      /// drop len
            val iw = ss.pop()                             /// get index
            STR(iw)?.let { io.pstr(it) }
        }
        CODE("key")    { ss.push(io.key())            }
        CODE("emit")   { io.dot(IO.OP.EMIT, ss.pop()) }
        CODE("space")  { io.spaces(1)                 }
        CODE("spaces") { io.spaces(ss.pop())          }
        /// @}
        /// @defgroup Literal ops
        /// @{
        IMMD("(")      { io.scan("\\)")               }
        IMMD(".(")     { io.scan("\\)"); io.pad()?.let { io.pstr(it) } }
        IMMD("\\")     { io.scan("\n")                }
        IMMD("s\"")    {                                  /// -- w a
            val s = io.scan("\"") ?: return@IMMD
            if (compile) {
                val w = Code(_dostr, "s\"", s)
                ADDW(w)                                   /// literal=s
                w.token = IDX()
            } else {
                ss.push(-1)
                ss.push(s.length)                         /// use pad
            }
        }
        IMMD(".\"")    {
            val s = io.scan("\"") ?: return@IMMD
            if (!compile) io.pstr(s)
            else ADDW(Code(_dotstr, ".\"", s))            /// literal=s
        }
        /// @}
        /// @defgroup Branching ops
        /// @{
        IMMD("if")    {
            ADDW(Code(_branch, "if"))                     /// literal=s
            dict.add(Code(_tmp, ""))
        }
        IMMD("else")  {
            val b = dict.bran()
            BRAN(b.pf)
            b.stage = 1
        }
        IMMD("then")  {
            val b = dict.bran()                           ///< branching target
            val s = b.stage                               ///< branching state
            if (s == 0) {                                 /// * if..{pf}..then
                BRAN(b.pf)
                dict.removeLast()
            } else {                                      /// * else..{p1}..then, or
                BRAN(b.p1)                                /// * then..{p1}..next
                if (s == 1) dict.removeLast()             /// * if..else..then
            }
        }
        /// @}
        /// @defgroup Loops
        /// @{
        IMMD("begin") {
            ADDW(Code(_begin, "begin"))                   /// * branch target
            dict.add(Code(_tmp, ""))
        }
        IMMD("while") {
            val b = dict.bran()
            BRAN(b.pf)                                    /// * begin..{pf}..f.while
            b.stage = 2
        }
        IMMD("repeat") {
            val b = dict.bran()
            BRAN(b.p1)                                    /// * while..{p1}..repeat
            dict.removeLast()
        }
        IMMD("again") {
            val b = dict.bran()
            BRAN(b.pf)                                    /// * begin..{pf}..again
            dict.removeLast()
            b.stage = 1
        }
        IMMD("until") {
            val b = dict.bran()
            BRAN(b.pf)                                    /// * begin..{pf}..f.until
            dict.removeLast()
        }
        /// @}
        /// @defgroup FOR loops
        /// @{
        IMMD("for") {
            ADDW(Code(_tor, "\t"))
            ADDW(Code(_for, "for"))
            dict.add(Code(_tmp, ""))
        }
        IMMD("aft") {
            val b = dict.bran()
            BRAN(b.pf)
            b.stage = 3
        }
        IMMD("next") {
            val b = dict.bran()                           /// * for..{pf}..next, or
            BRAN(if (b.stage == 0) b.pf else b.p2)        /// * then..{p2}..next
            dict.removeLast()
        }
        /// @}
        /// @defgroup DO loops
        /// @{
        IMMD("do")   {
            ADDW(Code(_tor2, "\t"))                       ///< ( limit first -- )
            ADDW(Code(_loop, "do"))
            dict.add(Code(_tmp, ""))
        }
        CODE("leave"){ it.unnest() }                      /// * exit loop
        IMMD("loop") {
            val b = dict.bran()
            BRAN(b.pf)                                    /// * do..{pf}..loop
            dict.removeLast()
        }
        /// @}
        /// @defgroup Compiler ops
        /// @{
        CODE("[")    { compile = false }
        CODE("]")    { compile = true  }
        CODE(":")    {
            word()?.let { dict.add(it) }
            compile = true
        }
        IMMD(";")    { compile = false }
        CODE("variable") {
            word()?.let { w ->
                dict.add(w)
                val v = Code(_dovar, "var", 0)
                ADDW(v)
                v.token = IDX()
            }
        }
        CODE("constant") {                                /// n --
            word()?.let { w ->
                dict.add(w)
                val v = Code(_dolit, "lit", ss.pop())
                ADDW(v)
                v.token = IDX()
            }
        }
        CODE("postpone"){ tick()?.let { ADDW(it) }  }
        CODE("immediate") { dict.last().immediate() }
        CODE("exit")    { it.unnest()               }    /// marker to exit interpreter
        CODE("exec")    { dict[ss.pop()].nest() }
        CODE("create")  {
            word()?.let { w ->
                dict.add(w)
                val v = Code(_dovar, "var", 0)
                ADDW(v)
                v.token = IDX()
                v.qf.removeLast()
            }
        }
        IMMD("does>") {                                  /// n --
            val w = Code(_dodoes, "does>")
            ADDW(w)
            w.token = dict.last().token                  /// * point to new word
        }
        CODE("to") {                                     /// n -- , compile only
            tick()?.let { w -> w.setVar(0, ss.pop()) }
        }
        CODE("is") {                                     /// w -- , execute only
            tick()?.let { w ->
                val src = dict[ss.pop()]                 /// source word
                dict[w.token].pf = src.pf
            }
        }
        /// @}
        /// @defgroup Memory Access ops
        /// @{
        CODE("@")  { ss.push(GETV(ss.pop())) }           /// w -- n
        CODE("!")  {                                     /// n w --
            val iw = ss.pop()
            SETV(iw, ss.pop())
        }
        CODE("+!") {                                     /// n w --
            val iw = ss.pop()
            val n = GETV(iw) + ss.pop()
            SETV(iw, n)
        }
        CODE("?")  { io.dot(IO.OP.DOT, GETV(ss.pop())) } /// w --
        CODE(",")  { dict.last().comma(ss.pop())  }      /// n --
        CODE("cells") { /* backward compatible */ }      /// --
        CODE("allot") {                                  /// n --
            val n = ss.pop()
            val w = dict.last()
            repeat(n) { w.comma(0) }
        }
        CODE("th")    {                                  /// w i -- i_w
            val i = ss.pop() shl 16
            ss.push(i or ss.pop())                       /// i.e. 4 v 2 th !
        }
        /// @}
        /// @defgroup Debug ops
        /// @{
        CODE("here")  { ss.push(Code.fence)                 }
        CODE("'")     { tick()?.let { ss.push(it.token)   } }
        CODE(".s")    { io.ssDump(ss, base)                 }
        CODE("words") { io.words(dict)                      }
        CODE("see")   { tick()?.let { io.see(it, base, 0) } }
        CODE("clock") { ss.push(System.currentTimeMillis().toInt()) }
        CODE("rnd")   { ALU { io.rnd(it)                  } }
        CODE("depth") { ss.push(ss.size)                    }
        CODE("r")     { ss.push(rs.size)                    }
        IMMD("include")  {                               /// include an OS file
            io.nextToken()?.let { io.load(it, { outer() }) }
        } 
        CODE("included") {                               /// include a file (programmable)
            ss.pop()
            STR(ss.pop())?.let { io.load(it, { outer() }) }
        }
        CODE("ok")    { io.mstat() }
        CODE("ms")    {                                  /// n -- delay n ms
            try { Thread.sleep(ss.pop().toLong()) }
            catch (e: Exception) { io.err(e) }
        }
        CODE("forget") {
            val m = dict.find("boot", compile)
            tick()?.let { w ->
                dict.forget(maxOf(w.token, (m?.token ?: -1) + 1))
            }
        }
        CODE("boot")  {
            val t = dict.find("boot", compile)?.token?.plus(1) ?: 0
            dict.forget(t)
        }
        /// @}
    }
}
