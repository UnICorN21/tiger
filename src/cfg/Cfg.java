package cfg;

import java.util.LinkedList;

public class Cfg {
  // //////////////////////////////////////////////////
  // type
  public static class Type {
    public static abstract class T implements cfg.Acceptable { /* null */ }

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
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IntType extends T {
      public IntType() { /* null */ }

      @Override
      public String toString() {
        return "@int";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IntArrayType extends T {
      public IntArrayType() { /* null */ }

      @Override
      public String toString() {
        return "@int[]";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class StringType extends T {
      public StringType() { /* null */ }

      @Override
      public String toString() {
        return "@String";
      }

      @Override
      public void accept(Visitor v) { v.visit(this); }
    }

  }// end of type

  // //////////////////////////////////////////////////
  // dec
  public static class Dec {
    public static abstract class T implements cfg.Acceptable { /* null */ }

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

  // //////////////////////////////////////////////////
  // Operand
  public static class Operand {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class Int extends T {
      public int i;

      public Int(int i) {
        this.i = i;
      }

      @Override
      public String toString() {
        return String.valueOf(i);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Str extends T {
      public String literal;

      public Str(String literal) { this.literal = literal; }

      @Override
      public String toString() { return literal; }

      @Override
      public void accept(Visitor v) { v.visit(this); }
    }

    public static class Var extends T {
      public String id;

      public Var(String id) {
        this.id = id;
      }

      @Override
      public String toString() {
        return id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of operand

  // //////////////////////////////////////////////////
  // statement
  public static class Stm {
    public static abstract class T implements cfg.Acceptable {
      public String dst;
    }

    public static class Add extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Add(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public String toString() {
        return String.format("%s = %s + %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class And extends T {
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public And(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.left = left;
        this.right = right;
        this.ty = ty;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return String.format("%s = %s && %s", dst, left, right);
      }
    }

    public static class ArraySelect extends T {
      public Type.T ty;
      public Operand.T array;
      public Operand.T index;

      public ArraySelect(Operand.T array, String dst, Operand.T index, Type.T ty) {
        this.array = array;
        this.dst = dst;
        this.index = index;
        this.ty = ty;
      }

      @Override
      public String toString() {
        return String.format("%s = %s[%s]", dst, array, index);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Length extends T {
      public Type.T ty;
      public Operand.T array;

      public Length(Operand.T array, String dst, Type.T ty) {
        this.array = array;
        this.dst = dst;
        this.ty = ty;
      }

      @Override
      public String toString() {
        return String.format("%s = %s.length", dst, array);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class InvokeVirtual extends T {
      public String obj;
      public String f;
      // type of the destination variable
      public java.util.LinkedList<Operand.T> args;

      public InvokeVirtual(String dst, String obj, String f,
          LinkedList<Operand.T> args) {
        this.dst = dst;
        this.obj = obj;
        this.f = f;
        this.args = args;
      }

      @Override
      public String toString() {
        String ret = String.format("%s = %s.%s(", dst, obj, f);
        for (int i = 0; i < args.size(); ++i) {
          if (i > 0) ret += ", ";
          ret += args.get(i);
        }
        ret += ")";
        return ret;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Lt extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Lt(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public String toString() {
        return String.format("%s = %s < %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Le extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Le(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public String toString() {
        return String.format("%s = %s <= %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Gt extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Gt(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.left = left;
        this.right = right;
        this.ty = ty;
      }

      @Override
      public String toString() {
        return String.format("%s = %s > %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Ge extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Ge(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.left = left;
        this.right = right;
        this.ty = ty;
      }

      @Override
      public String toString() {
        return String.format("%s = %s >= %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Eq extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Eq(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.left = left;
        this.right = right;
        this.ty = ty;
      }

      @Override
      public String toString() {
        return String.format("%s = %s == %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class AssignArray extends T {
      public Type.T ty;
      public Operand.T index;
      public Operand.T exp;

      public AssignArray(String dst, Type.T ty, Operand.T index, Operand.T exp) {
        this.dst = dst;
        this.ty = ty;
        this.index = index;
        this.exp = exp;
      }

      @Override
      public String toString() {
        return String.format("%s[%s] = %s", dst, exp, index);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Move extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T src;

      public Move(String dst, Type.T ty, Operand.T src) {
        this.dst = dst;
        this.ty = ty;
        this.src = src;
      }

      @Override
      public String toString() {
        return dst + " = " + src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class NewObject extends T {
      // type of the destination variable
      public String c;

      public NewObject(String dst, String c) {
        this.dst = dst;
        this.c = c;
      }

      @Override
      public String toString() {
        return String.format("%s = new %s()", dst, c);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class NewIntArray extends T {
      public Operand.T length;

      public NewIntArray(String dst, Operand.T length) {
        this.dst = dst;
        this.length = length;
      }

      @Override
      public String toString() {
        return dst + " = new int[" + length + "]";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Print extends T {
      public Operand.T arg;

      public Print(Operand.T arg) {
        this.dst = null;
        this.arg = arg;
      }

      @Override
      public String toString() {
        return String.format("System.out.println(%s)", arg);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Sub extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Sub(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public String toString() {
        return String.format("%s = %s - %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Times extends T {
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Times(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public String toString() {
        return String.format("%s = %s * %s", dst, left, right);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of statement

  // //////////////////////////////////////////////////
  // transfer
  public static class Transfer {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class Goto extends T {
      public util.Label label;

      public Goto(util.Label label) {
        this.label = label;
      }

      @Override
      public String toString() {
        return "Goto " + label;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class If extends T {
      public Operand.T operand;
      public util.Label truee;
      public util.Label falsee;

      public If(Operand.T operand, util.Label truee, util.Label falsee) {
        this.operand = operand;
        this.truee = truee;
        this.falsee = falsee;
      }

      @Override
      public String toString() {
        return String.format("If %s goto %s else goto %s", operand, truee, falsee);
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class Return extends T {
      public Operand.T operand;

      public Return(Operand.T operand) {
        this.operand = operand;
      }

      @Override
      public String toString() {
        return "return " + operand;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of transfer

  // //////////////////////////////////////////////////
  // block
  public static class Block {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class BlockSingle extends T {
      public util.Label label;
      public LinkedList<Stm.T> stms;
      public Transfer.T transfer;

      // used for topo-sort
      public LinkedList<BlockSingle> in;
      public LinkedList<BlockSingle> out;

      public BlockSingle(util.Label label, LinkedList<Stm.T> stms,
          Transfer.T transfer) {
        this.label = label;
        this.stms = stms;
        this.transfer = transfer;

        this.in = new LinkedList<>();
        this.out = new LinkedList<>();
      }

      @Override
      public boolean equals(Object o) {
        if (o == null || !(o instanceof BlockSingle))
          return false;

        BlockSingle ob = (BlockSingle) o;
        return this.label.equals(ob.label);
      }

      @Override
      public String toString() {
        StringBuffer strb = new StringBuffer();
        strb.append(this.label.toString() + ":\\n");
        stms.stream().forEach(s -> strb.append(s + "\\n"));
        strb.append(transfer);
        return strb.toString();
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of block

  // //////////////////////////////////////////////////
  // method
  public static class Method {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class MethodSingle extends T {
      public Type.T retType;
      public String id;
      public String classId;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Block.T> blocks;
      public util.Label entry;
      public util.Label exit;
      public Operand.T retValue;

      // used to avoid duplicate calculation
      public boolean reverseTopoOrder;

      public MethodSingle(Type.T retType, String id, String classId,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Block.T> blocks, util.Label entry, util.Label exit,
          Operand.T retValue) {
        this.retType = retType;
        this.id = id;
        this.classId = classId;
        this.formals = formals;
        this.locals = locals;
        this.blocks = blocks;
        this.entry = entry;
        this.exit = exit;
        this.retValue = retValue;
        reverseTopoOrder = false;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of method

  // //////////////////////////////////////////////////
  // main method
  public static class MainMethod {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class MainMethodSingle extends T {
      public LinkedList<Dec.T> locals;
      public LinkedList<Block.T> blocks;

      public boolean reverseTopoOrder;

      public MainMethodSingle(LinkedList<Dec.T> locals,
          LinkedList<Block.T> blocks) {
        this.locals = locals;
        this.blocks = blocks;
        reverseTopoOrder = false;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of main method

  // //////////////////////////////////////////////////
  // vtable
  public static class Vtable {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class VtableSingle extends T {
      public String id; // name of the class
      public String gcMap; // class gc map
      public LinkedList<cfg.Ftuple> ms; // all methods

      public VtableSingle(String id, String gcMap, LinkedList<cfg.Ftuple> ms) {
        this.id = id;
        this.gcMap = gcMap;
        this.ms = ms;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }

  // //////////////////////////////////////////////////
  // class
  public static class Class {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class ClassSingle extends T {
      public String id;
      public LinkedList<cfg.Tuple> decs;

      public ClassSingle(String id, LinkedList<cfg.Tuple> decs) {
        this.id = id;
        this.decs = decs;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// enf of clazz

  // //////////////////////////////////////////////////
  // program
  public static class Program {
    public static abstract class T implements cfg.Acceptable { /* null */ }

    public static class ProgramSingle extends T {
      public LinkedList<Class.T> classes;
      public LinkedList<Vtable.T> vtables;
      public LinkedList<Method.T> methods;
      public MainMethod.T mainMethod;

      public ProgramSingle(LinkedList<Class.T> classes,
          LinkedList<Vtable.T> vtables, LinkedList<Method.T> methods,
          MainMethod.T mainMethod) {
        this.classes = classes;
        this.vtables = vtables;
        this.methods = methods;
        this.mainMethod = mainMethod;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of program
}
