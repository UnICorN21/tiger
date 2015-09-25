package codegen.dalvik;

import util.Label;

import java.util.LinkedList;

public class Ast {
  // ////////////////////////////////////////////////
  // type
  public static class Type {
    public static abstract class T implements codegen.dalvik.Acceptable {
      public abstract String desc();
    }

    public static class ClassType extends T {
      public String id;

      public ClassType(String id) {
        this.id = id;
      }

      @Override
      public String toString() {
        return this.id;
      }

      @Override
      public String desc() {
        return String.format("L%s;", id);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Int extends T {
      public Int() { /* null */ }

      @Override
      public String toString() {
        return "@int";
      }

      @Override
      public String desc() {
        return "I";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IntArray extends T {
      public IntArray() { /* null */ }

      @Override
      public String toString() {
        return "@int[]";
      }

      @Override
      public String desc() {
        return "[I";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of type

  // ////////////////////////////////////////////////
  // dec
  public static class Dec {
    public static abstract class T implements codegen.dalvik.Acceptable { /* null */ }

    public static class DecSingle extends T {
      public Type.T type;
      public String id;
      public String reg;

      public DecSingle(Type.T type, String id) {
        this.type = type;
        this.id = id;
      }

      public void setReg(String reg) { this.reg = reg; }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of dec

  // ////////////////////////////////////////////////
  // statement
  public static class Stm {
    public static abstract class T implements codegen.dalvik.Acceptable { /* null */ }

    /**
     * const vAA, #+BBBBBBBB
     * Move the given literal value into the specified register.
     */
    public static class Const extends T {
      public String dst;
      public int i;

      public Const(String dst, int i) {
        this.dst = dst;
        this.i = i;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * goto/32 +AAAAAAAA
     * Unconditionally jump to the indicated instruction.
     */
    public static class Goto32 extends T {
      public Label l;

      public Goto32(Label l) {
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * iflt vA, vB, #+CCCCCCCC
     * Jump to the indicated instruction if vA < vB.
     */
    public static class Iflt extends T {
      public String left, right;
      public Label l;

      public Iflt(String left, String right, Label l) {
        this.left = left;
        this.right = right;
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * ifgt vA, vB, #+CCCCCCCC
     */
    public static class Ifgt extends T {
      public String left, right;
      public Label l;

      public Ifgt(String left, String right, Label l) {
        this.l = l;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * ifne vA, vB, #+CCCCCCCC
     * Jump to the indicated instruction if vA != vB.
     */
    public static class Ifne extends T {
      String left, right;
      public Label l;

      public Ifne(String left, String right, Label l) {
        this.left = left;
        this.right = right;
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * ifnez vA, #+BBBBBBBB
     * Jump to the indicated instruction if vA != 0.
     */
    public static class Ifnez extends T {
      public String cond;
      public Label l;

      public Ifnez(String cond, Label l) {
        this.cond = cond;
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * ifez vA, #+BBBBBBBB
     */
    public static class Ifeqz extends T {
      public String cond;
      public Label l;

      public Ifeqz(String cond, Label l) {
        this.cond = cond;
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * invoke-virtual {arguments-list} method-descriptor
     * Invoke the instance method.
     * The first argument in the arguments-list must be p0.
     */
    public static class Invokevirtual extends T {
      public String f; // function id
      public String c; // class id
      public LinkedList<Type.T> at; // formals' types
      public Type.T rt; // return type
      public LinkedList<String> params;

      public Invokevirtual(String f, String c, LinkedList<Type.T> at, Type.T rt, LinkedList<String> params) {
        this.f = f;
        this.c = c;
        this.at = at;
        this.rt = rt;
        this.params = params;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class LabelJ extends T {
      public util.Label l;

      public LabelJ(util.Label l) {
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * move/16 vAAAA, vBBBB
     * Move the contents of one non-object register to another.
     */
    public static class Move16 extends T {
      public String left, right;

      public Move16(String left, String right) {
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * move-object/16 vAAAA, vBBBB
     * Move the contents of one object-bearing register to another.
     */
    public static class Moveobject16 extends T {
      public String left, right;

      public Moveobject16(String left, String right) {
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * mul-int vAA, vBB, vCC
     * Perform the multiply operation on the two source registers, storing the result in the first source register.
     */
    public static class Mulint extends T {
      public String dst, src1, src2;
      
      public Mulint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * new-instance vAA, type@BBBB
     * Construct a new instance of the indicated type, storing a reference to it in the destination.
     * The type must refer to a non-array class.
     */
    public static class NewInstance extends T {
      public String dst;
      public String c;

      public NewInstance(String dst, String c) {
        this.dst = dst;
        this.c = c;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * new-array vA, vB, type@CCCC
     */
    public static class NewArray extends T {
      public String dst;
      public String size;
      public String type;

      public NewArray(String dst, String size, String type) {
        this.dst = dst;
        this.size = size;
        this.type = type;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * array-length vA, vB
     */
    public static class ArrayLength extends T {
      public String dst;
      public String src;

      public ArrayLength(String dst, String src) {
        this.dst = dst;
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * aput vAA, vBB, vCC
     */
    public static class Aput extends T {
      public String aryReg;
      public String idxReg;
      public String src;

      public Aput(String aryReg, String idxReg, String src) {
        this.aryReg = aryReg;
        this.src = src;
        this.idxReg = idxReg;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Aget extends T {
      public String aryReg;
      public String idxReg;
      public String dst;

      public Aget(String aryReg, String idxReg, String dst) {
        this.aryReg = aryReg;
        this.dst = dst;
        this.idxReg = idxReg;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Print extends T {
      public String stream;
      public String src;
      
      public Print(String stream, String src) {
        this.stream = stream;
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * return vAA
     * Return from a single-width (32-bit) non-object value-returning method.
     */
    public static class Return extends T {
      String src;
      
      public Return(String src) {
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * return-object vAA
     * Return from an object-returning method.
     */
    public static class ReturnObject extends T {
      public String src;
      
      public ReturnObject(String src) {
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * sub-int vAA, vBB, vCC
     */
    public static class Subint extends T {
      public String dst, src1, src2;
      
      public Subint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * add-int vAA, vBB, vCC
     */
    public static class Addint extends T {
      public String dst, src1, src2;

      public Addint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * div-int vAA, vBB, vCC
     */
    public static class Divint extends T {
      public String dst, src1, src2;

      public Divint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }


    /**
     * and-int vAA, vBB, vCC
     */
    public static class Andint extends T {
      public String dst, src1, src2;

      public Andint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Xorint extends T {
      public String dst, src1, src2;

      public Xorint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Orint extends T {
      public String dst, src1, src2;

      public Orint(String dst, String src1, String src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Iput extends T {
      public String src;
      public String objReg;
      public String type;

      public Iput(String src, String objReg, String type) {
        this.objReg = objReg;
        this.src = src;
        this.type = type;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IGet extends T {
      public String dst;
      public String objReg;
      public String type;

      public IGet(String dst, String objReg, String type) {
        this.dst = dst;
        this.objReg = objReg;
        this.type = type;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of statement

  // ////////////////////////////////////////////////
  // method
  public static class Method {
    public static abstract class T implements codegen.dalvik.Acceptable { /* null */ }

    public static class MethodSingle extends T {
      public Type.T retType;
      public String id;
      public String classId;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Stm.T> stms;
      public int index; // number of index
      public int retExp;
      public int regCnt;

      public MethodSingle(Type.T retType, String id, String classId,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Stm.T> stms, int retExp, int index, int regCnt) {
        this.retType = retType;
        this.id = id;
        this.classId = classId;
        this.formals = formals;
        this.locals = locals;
        this.stms = stms;
        this.retExp = retExp;
        this.index = index;
        if (regCnt > 256)
          System.out.println("Warning: method " + classId + "." +id + " allocates more than 256 registers");
        this.regCnt = regCnt;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of method

  // ////////////////////////////////////////////////
  // class
  public static class Class {
    public static abstract class T implements codegen.dalvik.Acceptable { /* null */ }

    public static class ClassSingle extends T {
      public String id;
      public String extendss; // null for non-existing "extends"
      public LinkedList<Dec.T> decs;
      public LinkedList<Method.T> methods;

      /**
       * Constructor
       * @param id class name including its path
       * @param extendss class parent including its path
       * @param decs fields in the class
       * @param methods methods in the class
       */
      public ClassSingle(String id, String extendss, LinkedList<Dec.T> decs,
          LinkedList<Method.T> methods) {
        this.id = id;
        this.extendss = extendss;
        this.decs = decs;
        this.methods = methods;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of class

  // ////////////////////////////////////////////////
  // main class
  public static class MainClass {
    public static abstract class T implements codegen.dalvik.Acceptable { /* null */ }

    public static class MainClassSingle extends T {
      public String id;
      public String arg;
      public LinkedList<Stm.T> stms;
      public int regCnt;

      public MainClassSingle(String id, String arg, LinkedList<Stm.T> stms, int regCnt) {
        this.id = id;
        this.arg = arg;
        this.stms = stms;
        this.regCnt = regCnt;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of main class

  // ////////////////////////////////////////////////
  // program
  public static class Program {
    public static abstract class T implements codegen.dalvik.Acceptable { /* null */ }

    public static class ProgramSingle extends T {
      public MainClass.T mainClass;
      public LinkedList<Class.T> classes;

      public ProgramSingle(MainClass.T mainClass,
          java.util.LinkedList<Class.T> classes) {
        this.mainClass = mainClass;
        this.classes = classes;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of program
}
