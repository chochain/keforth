///
/// @file 
/// @brief - generic List class - extended ArrayList
///
package eforth

class FV<T> : ArrayList<T>() {                 ///< eForth ArrayList (i.e. vector)
    fun head(): T = get(0)
    fun tail(): T = get(size - 1)
    fun tail(offset: Int): T = get(size - offset)
    
    fun merge(lst: FV<T>): FV<T> {
        addAll(lst)
        return this
    }
    
    fun drop(): FV<T> {
        removeAt(size - 1)
        return this
    }
}
