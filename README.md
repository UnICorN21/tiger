## Lab3

----
### Status
Complete part A.

### Notes
#### Generate C code:
+ It's a kind of transform between two asts, which means one needs to define Ast firstly and using visitor pattern
to make changes.
+ Typically, a class in `MiniJava` will be translated to 2 structs. One is itself and another is its vtable with some
function pointers in it.