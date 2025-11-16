\ keForth Logo demo
: st s" logo st" java ;
: fd s" logo fd %d" java ;
: rt s" logo rt %d" java ;
: pc s" logo pc %d" java ;
: ts s" logo ts %d" java ;
: tt s" logo tt 'Forth'" java ;
: one dup 2* pc 5 * fd 35 rt tt ;
: demo 0 do i one loop ;
\ 10 demo
