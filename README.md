## Lab3

----
### Status
Complete part A.
Complete part B.
Complete part C.

### Notes
#### Generate C code:
+ It's a kind of transform between two asts, which means one needs to define Ast firstly and using visitor pattern
to make changes.
+ Typically, a class in `MiniJava` will be translated to 2 structs. One is itself and another is its vtable with some
function pointers in it.
+ The implementation part about the class fields operations is error prone. For example, one may ignore to prefix
the `this->` symbol for a class field because of the vacate of checking whether an `Id` refers to a class field or not. (
Or force to add it as the prefix of `Id`s.)
+ For my personal perspective, the design of `MiniJava`'s AST has some problems. One, for example, is the waste of memory
in resolving the symbols and another is missing `Id` class in some sub trees' constructions.

#### Generate Java bytecode:
+ Same procedure with generating C code.
+ Can refer to [JVM Specification](http://docs.oracle.com/javase/specs/jvms/se8/html/index.html)'s first 4 chapters to
make a better understand of jasmin and the generation details.
+ Nevertheless to say, you should never trust the official code of this lab.

#### Generate Dalvik bytecode:
+ Dalvik is a typical register-based computer. Therefore, some translation procedures are subtle different from the above.
+ Again, never trust the official code. Some codes are copied from `Generate Java bytecode` lab and haven't be modified
at all.
+ My personal solution doesn't try to fix the situation when the arguments exceed 256 currently.(A registers allocator is required.)