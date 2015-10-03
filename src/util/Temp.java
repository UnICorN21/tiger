package util;

import control.Control;

public class Temp {
  private static int count = 0;

  private static int ccnt = 0;
  private static int acnt = 0;

  private static Control.ConCodeGen.Kind_t kind = Control.ConCodeGen.codegen;

  private Temp() { /* null */ }

  public static void reset() {
    count = 0;
  }

  public static void resetTokenCnt() { ccnt = 0; acnt = 0; }

  public static int getCount() { return count; }

  // Factory pattern
  public static String next() {
    switch(kind) {
      case C: case Bytecode:
        return "x_" + (count++);
      case Dalvik:
        return "v" + (count++);
    }
    return null;
  }

  /**
   * Generate an inner representation of a class object or array.
   * Only using in C generation currently.
   * @param h the head of the generated name, which should be `c` for class and `a` for array.
   * @return a generated name such as c$1
   */
  public static String nextToken(String h) {
    switch (h) {
      case "a": return String.format("a$%s", acnt++);
      case "c": return String.format("c$%s", ccnt++);
    }
    return null;
  }
}
