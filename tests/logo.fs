\ keForth Logo demo
: st s" logo st" java ;
: fd s" logo fd %d" java ;
: rt s" logo rt %d" java ;
: pc s" logo pc %d" java ;
: ts s" logo ts %d" java ;
: tt s" logo tt 'Forth'" java ;
: one >r r@ 2* pc r@ 6 * fd 35 rt r> 10 + ts tt ;
: demo st 0 do i one loop ;
words
60 demo
