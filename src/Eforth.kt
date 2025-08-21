///
/// @file 
/// @brief - Kotlin eForth main
///
import java.io.*
import eforth.*

class Eforth(
    ins: InputStream,
    out: PrintStream
) : Runnable {                                              /// ooeforth
    companion object {
        const val APP_NAME = "ooeForth v2"
        
        @JvmStatic
        fun main(args: Array<String>) {                     /// main app
            Eforth(System.`in`, System.out).run()
        }
    }
    
    private val io: IO                                      ///< instantiate IO
    private val vm: VM                                      ///< eForth virtual machine

    init {
        io = IO(APP_NAME, ins, out)
        vm = VM(io)
    }
    
    override fun run() {
        vm.ok(true)                                         /// * prompt VM ready
        while (io.readline()) {                             /// * fetch from input 
            if (!vm.outer()) break                          /// * outer interpreter
        }
        io.pstr("\n$APP_NAME Done.\n")                      /// * exit prompt
    }
}
