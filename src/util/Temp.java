package util;

import control.Control;

public class Temp {
  private static int count = 0;
  private static Control.ConCodeGen.Kind_t kind = Control.ConCodeGen.codegen;

  private Temp() { /* null */ }

  public static void reset() {
    count = 0;
  }

  public static int getCount() { return count; }

  // Factory pattern
  public static String next() {
    switch(kind) {
      case C: Bytecode:
        return "x_" + (Temp.count++);
      case Dalvik:
        return "v" + (Temp.count++);
    }
    return null;
  }
}
