package ast;

import util.Pos;

import java.util.LinkedList;

public class Ast {

  // ///////////////////////////////////////////////////////////
  // type
  public static class Type {
    public static abstract class T implements ast.Acceptable {
      // boolean: -1
      // int: 0
      // int[]: 1
      // class: 2
      // string: 3
      // Such that one can easily tell who is who
      public abstract int getNum();
    }

    // boolean
    public static class Boolean extends T {
      public Boolean() { /* null */ }

      @Override
      public String toString() {
        return "@boolean";
      }

      @Override
      public int getNum() {
        return -1;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // class
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
      public int getNum() {
        return 2;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // int
    public static class Int extends T {
      public Int() { /* null */ }

      @Override
      public String toString() {
        return "@int";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public int getNum() {
        return 0;
      }
    }

    // int[]
    public static class IntArray extends T {
      public IntArray() { /* null */ }

      @Override
      public String toString() {
        return "@int[]";
      }

      @Override
      public int getNum() {
        return 1;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class StringType extends T { // Not use `String` to avoid name conflict.
      public StringType() { /* null */ }

      @Override
      public String toString() {
        return "@String";
      }

      @Override
      public int getNum() {
        return 3;
      }

      @Override
      public void accept(Visitor v) { v.visit(this); }
    }
  }

  // ///////////////////////////////////////////////////
  // declaration
  public static class Dec {
    public static abstract class T implements ast.Acceptable {
      public Pos pos;
      protected T(Pos pos) { this.pos = pos; }
    }

    public static class DecSingle extends T {
      public Type.T type;
      public String id;

      public DecSingle(Type.T type, String id, Pos pos) {
        super(pos);
        this.type = type;
        this.id = id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }

  // /////////////////////////////////////////////////////////
  // expression
  public static class Exp {
    public static abstract class T implements ast.Acceptable {
      public Pos pos;
      protected T(Pos pos) {
        this.pos = pos;
      }
    }

    // +
    public static class Add extends T {
      public T left;
      public T right;

      public Add(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // and
    public static class And extends T {
      public T left;
      public T right;

      public And(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // ArraySelect
    public static class ArraySelect extends T {
      public T array;
      public T index;

      public ArraySelect(T array, T index, Pos pos) {
        super(pos);
        this.array = array;
        this.index = index;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // Call
    public static class Call extends T {
      public T exp;
      public String id;
      public java.util.LinkedList<T> args;
      public String type; // type of first field "exp"
      public java.util.LinkedList<Type.T> at; // arg's type
      public Type.T rt;

      public Call(T exp, String id, java.util.LinkedList<T> args, Pos pos) {
        this(pos, args, null, exp, id, null, null);
      }

      public Call(Pos pos, LinkedList<T> args, LinkedList<Type.T> at, T exp, String id, Type.T rt, String type) {
        super(pos);
        this.args = args;
        this.at = at;
        this.exp = exp;
        this.id = id;
        this.rt = rt;
        this.type = type;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // False
    public static class False extends T {
      public False(Pos pos) { super(pos); }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // Id
    public static class Id extends T {
      public String id; // name of the classType
      public Type.T type; // type of the classType
      public boolean isField; // whether or not this is a class field

      public Id(String id, Pos pos) {
        super(pos);
        this.id = id;
        this.type = null;
        this.isField = false;
      }

      public Id(String id, Type.T type, boolean isField, Pos pos) {
        super(pos);
        this.id = id;
        this.type = type;
        this.isField = isField;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // length
    public static class Length extends T {
      public T array;

      public Length(T array, Pos pos) {
        super(pos);
        this.array = array;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // <
    public static class Lt extends T {
      public T left;
      public T right;

      public Lt(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // <=
    public static class Le extends T {
      public T left;
      public T right;

      public Le(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // >
    public static class Gt extends T {
      public T left;
      public T right;

      public Gt(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // >=
    public static class Ge extends T {
      public T left;
      public T right;

      public Ge(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // ==
    public static class Eq extends T {
      public T left;
      public T right;

      public Eq(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    // new int [e]
    public static class NewIntArray extends T {
      public T exp;

      public NewIntArray(T exp, Pos pos) {
        super(pos);
        this.exp = exp;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // new A();
    public static class NewObject extends T {
      public String id;

      public NewObject(String id, Pos pos) {
        super(pos);
        this.id = id;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // !
    public static class Not extends T {
      public T exp;

      public Not(T exp, Pos pos) {
        super(pos);
        this.exp = exp;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // number
    public static class Num extends T {
      public int num;

      public Num(int num, Pos pos) {
        super(pos);
        this.num = num;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // string literal
    public static class StringLiteral extends T {
      public String literal;

      public StringLiteral(String literal, Pos pos) {
        super(pos);
        this.literal = literal;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // -
    public static class Sub extends T {
      public T left;
      public T right;

      public Sub(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // this
    public static class This extends T {
      public This(Pos pos) { super(pos); }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // *
    public static class Times extends T {
      public T left;
      public T right;

      public Times(T left, T right, Pos pos) {
        super(pos);
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // True
    public static class True extends T {
      public True(Pos pos) { super(pos); }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

  }
  // end of expression

  // /////////////////////////////////////////////////////////
  // statement
  public static class Stm {
    public static abstract class T implements ast.Acceptable { /* null */ }

    // assign
    public static class Assign extends T {
      public String id;
      public Exp.T exp;
      public Type.T type; // type of the classType
      public boolean isField; // is classType a class field.

      public Assign(String id, Exp.T exp) {
        this(exp, id, false, null);
      }

      public Assign(Exp.T exp, String id, boolean isField, Type.T type) {
        this.exp = exp;
        this.id = id;
        this.isField = isField;
        this.type = type;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // assign-array
    public static class AssignArray extends T {
      public String id;
      public Exp.T index;
      public Exp.T exp;
      public boolean isField;

      public AssignArray(String id, Exp.T index, Exp.T exp) {
        this(exp, id, index, false);
      }

      public AssignArray(Exp.T exp, String id, Exp.T index, boolean isField) {
        this.exp = exp;
        this.id = id;
        this.index = index;
        this.isField = isField;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // block
    public static class Block extends T {
      public java.util.LinkedList<T> stms;

      public Block(java.util.LinkedList<T> stms) {
        this.stms = stms;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // if
    public static class If extends T {
      public Exp.T condition;
      public T thenn;
      public T elsee;

      public If(Exp.T condition, T thenn, T elsee) {
        this.condition = condition;
        this.thenn = thenn;
        this.elsee = elsee;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // Print
    public static class Print extends T {
      public Exp.T exp;

      public Print(Exp.T exp) {
        this.exp = exp;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

    // while
    public static class While extends T {
      public Exp.T condition;
      public T body;

      public While(Exp.T condition, T body) {
        this.condition = condition;
        this.body = body;
      }

      @Override
      public void accept(ast.Visitor v) {
        v.visit(this);
      }
    }

  }// end of statement

  // /////////////////////////////////////////////////////////
  // method
  public static class Method {
    public static abstract class T implements ast.Acceptable { /* null */ }

    public static class MethodSingle extends T {
      public Type.T retType;
      public String id;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Stm.T> stms;
      public Exp.T retExp;

      public MethodSingle(Type.T retType, String id,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Stm.T> stms, Exp.T retExp) {
        this.retType = retType;
        this.id = id;
        this.formals = formals;
        this.locals = locals;
        this.stms = stms;
        this.retExp = retExp;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }

  // class
  public static class Class {
    public static abstract class T implements ast.Acceptable { /* null */ }

    public static class ClassSingle extends T {
      public String id;
      public String extendss; // null for non-existing "extends"
      public java.util.LinkedList<Dec.T> decs;
      public java.util.LinkedList<ast.Ast.Method.T> methods;

      public ClassSingle(String id, String extendss,
          java.util.LinkedList<Dec.T> decs,
          java.util.LinkedList<ast.Ast.Method.T> methods) {
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
  }

  // main class
  public static class MainClass {
    public static abstract class T implements ast.Acceptable { /* null */ }

    public static class MainClassSingle extends T {
      public String id;
      public String arg;
      public Stm.T stm;

      public MainClassSingle(String id, String arg, Stm.T stm) {
        this.id = id;
        this.arg = arg;
        this.stm = stm;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }

  // whole program
  public static class Program {
    public static abstract class T implements ast.Acceptable { /* null */ }

    public static class ProgramSingle extends T {
      public MainClass.T mainClass;
      public LinkedList<Class.T> classes;

      public ProgramSingle(MainClass.T mainClass, LinkedList<Class.T> classes) {
        this.mainClass = mainClass;
        this.classes = classes;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }
}
