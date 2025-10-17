///
/// @file 
/// @brief - Virtual Machine class
///
package com.keforth.eforth;

import com.keforth.*;

public interface JavaCallback {
    public enum PostType {
        LOG, FORTH, JAVA
    }
    void onPost(PostType tid, String msg);
}
