///
/// @file 
/// @brief - Virtual Machine class
///
package com.keforth.eforth;

import com.keforth.*;

public interface JavaCallback {
    public enum PostType {
        LOG, DEBUG, PRINT, FORTH, JAVA
    }
    void onPost(PostType tid, String msg);
}
