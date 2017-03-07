## A custom version

I try to refactor the whole compiler with scala and add more modern language features.

**Note**: I used `HashSet` in many visitors which provide no order guarantee in old versions.
Although it works fine with them but they deteriorate to bugs when meeting with scala code.
Therefore, it'd better be replaced with `LinkedHashSet` even in older versions.

**TODO**: Using llvm as the backend
