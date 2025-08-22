///
/// @file 
/// @brief - generic List class - extended ArrayList
///
package eforth

open class FV<T> : ArrayList<T>() {         ///< eForth ArrayList (i.e. vector)
    fun tail(): T = get(size - 1)
    fun tail(offset: Int): T = get(size - offset)
}
