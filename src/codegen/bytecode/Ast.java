package codegen.bytecode;

import util.Label;

import java.util.LinkedList;

public class Ast {
  // ////////////////////////////////////////////////
  // type
  public static class Type {
    public static abstract class T implements codegen.bytecode.Acceptable {
      public abstract String desc();
    }

    public static class ClassType extends T {
      public String id;

      public ClassType(String id) {
        this.id = id;
      }

      @Override
      public String desc() {
        return String.format("L%s;", id);
      }

      @Override
      public String toString() {
        return this.id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Int extends T {
      public Int() { /* null */ }

      @Override
      public String desc() {
        return "I";
      }

      @Override
      public String toString() {
        return "@int";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IntArray extends T {
      public IntArray() { /* null */ }

      @Override
      public String desc() {
        return "[I";
      }

      @Override
      public String toString() {
        return "@int[]";
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
    public static abstract class T implements codegen.bytecode.Acceptable { /* null */ }

    public static class DecSingle extends T {
      public Type.T type;
      public String id;

      public DecSingle(Type.T type, String id) {
        this.type = type;
        this.id = id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of dec

  // ////////////////////////////////////////////////
  // statement
  public static class Stm {
    public static abstract class T implements codegen.bytecode.Acceptable { /* null */ }

    public static class Aload extends T {
      public int index;

      public Aload(int index) {
        this.index = index;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Areturn extends T {
      public Areturn() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class This extends T {
      public This() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Astore extends T {
      public int index;

      public Astore(int index) {
        this.index = index;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * Translate to `iconst_0`.
     */
    public static class False extends T {
      public False() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * Translate to `iconst_1`
     */
    public static class True extends T {
      public True() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Goto extends T {
      public Label l;

      public Goto(Label l) {
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ificmplt extends T {
      public Label l;

      public Ificmplt(Label l) {
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ificmpgt extends T {
      public Label l;

      public Ificmpgt(Label l) { this.l = l; }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Iflt extends T {
      public Label l;

      public Iflt(Label l) { this.l = l; }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ifgt extends T {
      public Label l;

      public Ifgt(Label l) { this.l = l; }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ifne extends T {
      public Label l;

      public Ifne(Label l) {
        this.l = l;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ifeq extends T {
      public Label l;

      public Ifeq(Label l) { this.l = l; }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Iaload extends T {
      public Iaload() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Iastore extends T {
      public Iastore() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Iload extends T {
      public int index;

      public Iload(int index) {
        this.index = index;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Imul extends T {
      public Imul() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IAnd extends T {
      public IAnd() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Invokevirtual extends T {
      public String f; // function name
      public String c; // class name
      public LinkedList<Type.T> at; // types of parameters
      public Type.T rt; // return type

      public Invokevirtual(String f, String c, LinkedList<Type.T> at, Type.T rt) {
        this.f = f;
        this.c = c;
        this.at = at;
        this.rt = rt;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ireturn extends T {
      public Ireturn() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Istore extends T {
      public int index;

      public Istore(int index) {
        this.index = index;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static  class IAdd extends T {
      public IAdd() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Isub extends T {
      public Isub() { /* null */ }

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
     * Stands for `ldc`/`bipush` instruction, but using the constant itself
     * other than the index of the constant pool.
     * It'll generate a `bipush` only if the value is in [-128, 127].
     */
    public static class Ldc extends T {
      public int i; // a constant number

      public Ldc(int i) {
        this.i = i;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * Stands for `new` instruction. For example, new #1, where #1 is the index in the run-time constant pool.
     */
    public static class New extends T {
      public String c;

      public New(String c) {
        this.c = c;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * The standard newarray instruction for jasmin.
     */
    public static class NewArray extends T {
      public String type; // will always be `int` in this application

      public NewArray() { this.type = "int"; }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    /**
     * Stands for a compensate of instructions.
     */
    public static class Print extends T {
      public Print() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class ArrayLength extends T {
      public ArrayLength() { /* null */ }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class PutField extends T {
      public String fieldspec;
      public String descriptor;

      public PutField(String fieldspec, String descriptor) {
        this.fieldspec = fieldspec;
        this.descriptor = descriptor;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class GetField extends T {
      public String fieldspce;
      public String descriptor;

      public GetField(String fieldspec, String descriptor) {
        this.fieldspce = fieldspec;
        this.descriptor = descriptor;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // debug info
    public static class Debug {

      public static abstract class D extends T { /* null */ }

      public static class Line extends D {
        public int num;

        public Line(int num) {
          this.num = num;
        }

        @Override
        public void accept(Visitor v) {
          v.visit(this);
        }
      }

      /**
       * Generate comments in jasmin.
       * For every statements in `Ast` package, there must be some comments to make things clear.
       */
      public static class Comment extends D {
        public String[] content;

        public Comment(String content) {
          this.content = content.split("\n");
        }

        @Override
        public void accept(Visitor v) {
          v.visit(this);
        }
      }
    }
  }// end of statement

  // ////////////////////////////////////////////////
  // method
  public static class Method {
    public static abstract class T implements codegen.bytecode.Acceptable { /* null */ }

    public static class MethodSingle extends T {
      public Type.T retType;
      public String id;
      public String classId;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Stm.T> stms;
      public int index; // number of index
      public int retExp;

      public MethodSingle(Type.T retType, String id, String classId,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Stm.T> stms, int retExp, int index) {
        this.retType = retType;
        this.id = id;
        this.classId = classId;
        this.formals = formals;
        this.locals = locals;
        this.stms = stms;
        this.retExp = retExp;
        this.index = index;
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
    public static abstract class T implements codegen.bytecode.Acceptable { /* null */ }

    public static class ClassSingle extends T {
      public String id;
      public String extendss; // null for non-existing "extends"
      public LinkedList<Dec.T> decs;
      public LinkedList<Method.T> methods;

      public ClassSingle(String id, String extendss,
          LinkedList<Dec.T> decs,
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
    public static abstract class T implements codegen.bytecode.Acceptable { /* null */ }

    public static class MainClassSingle extends T {
      public String id;
      public String arg;
      public LinkedList<Stm.T> stms;

      public MainClassSingle(String id, String arg,
          LinkedList<Stm.T> stms) {
        this.id = id;
        this.arg = arg;
        this.stms = stms;
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
    public static abstract class T implements codegen.bytecode.Acceptable { /* null */ }

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
