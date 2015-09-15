package elaborator;

import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.*;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp.*;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.*;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;

import java.util.LinkedList;

public class ElaboratorVisitor implements ast.Visitor {
  private static final Type.Int TYPE_INT = new Type.Int();
  private static final Type.IntArray TYPE_INTARRAY = new Type.IntArray();
  private static final Type.Boolean TYPE_BOOLEAN = new Type.Boolean();

  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public Type.T type; // type of the expression being elaborated

  public ElaboratorVisitor() {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
  }

  private void error() {
    System.out.println("type mismatch");
    System.exit(1);
  }

  private void error(Type.T excepted, Type.T found) {
    System.out.println(String.format("Except %s, found %s", excepted, found));
  }

  private void warn(String msg) {
    System.out.println(msg);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    e.left.accept(this);
    Type.T tl = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(tl.toString())) error(tl, this.type);
    this.type = TYPE_INT;
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN);
    e.right.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN);
    this.type = TYPE_BOOLEAN;
  }

  @Override
  public void visit(ArraySelect e) {
    e.index.accept(this);
    if (!this.type.toString().equals("@int")) error();
    e.array.accept(this);
    if (!this.type.toString().equals("@int[]")) error();
    this.type = TYPE_INT;
  }

  @Override
  public void visit(Call e) {
    Type.T leftty;
    Type.ClassType ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty instanceof ClassType) {
      ty = (ClassType) leftty;
      e.type = ty.id;
    } else error();
    MethodType mty = this.classTable.getm(ty.id, e.id);
    java.util.LinkedList<Type.T> argsty = new LinkedList<>();
    for (Exp.T a : e.args) {
      a.accept(this);
      argsty.addLast(this.type);
    }
    if (mty.argsType.size() != argsty.size()) error();
    for (int i = 0; i < argsty.size(); i++) {
      Type.T methodArgType = ((DecSingle) mty.argsType.get(i)).type;
      Type.T paramArgType = argsty.get(i);
      if (paramArgType instanceof ClassType) {
        // loop to check with type hierarchy
        while (!methodArgType.toString().equals(paramArgType.toString())) {
          String parent = this.classTable.get(paramArgType.toString()).extendss;
          if (null != parent) paramArgType = new Type.ClassType(parent);
          else error();
        }
      } else if (!paramArgType.toString().equals(paramArgType.toString())) error();
    }
    this.type = mty.retType;
    e.at = argsty;
    e.rt = this.type;
  }

  @Override
  public void visit(False e) {
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(Id e) {
    // first look up the id in method table
    Type.T type = this.methodTable.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null) error();
    this.type = type;
    // record this type on this node for future use.
    e.type = type;
  }

  @Override
  public void visit(Length e) {
    e.array.accept(this);
    if (!this.type.toString().equals("@int[]")) error();
    this.type = TYPE_INT;
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    Type.T ty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(ty.toString()))
      error();
    this.type = TYPE_BOOLEAN;
  }

  @Override
  public void visit(NewIntArray e) {
    e.exp.accept(this);
    if (!this.type.toString().equals("@int")) error();
    this.type = TYPE_INTARRAY;
  }

  @Override
  public void visit(NewObject e) {
    this.type = new Type.ClassType(e.id);
  }

  @Override
  public void visit(Not e) {
    e.exp.accept(this);
    if (!this.type.toString().equals("@boolean")) error();
    this.type = TYPE_BOOLEAN;
  }

  @Override
  public void visit(Num e) {
    this.type = TYPE_INT;
  }

  @Override
  public void visit(Sub e) {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString()))
      error();
    this.type = TYPE_INT;
  }

  @Override
  public void visit(This e) {
    this.type = new Type.ClassType(this.currentClass);
  }

  @Override
  public void visit(Times e) {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(leftty.toString())) error();
    this.type = TYPE_INT;
  }

  @Override
  public void visit(True e) {
    this.type = TYPE_BOOLEAN;
  }

  // statements
  @Override
  public void visit(Assign s) {
    // first look up the id in method table
    Type.T type = this.methodTable.get(s.id);
    // if search failed, then s.id must be a class field
    if (type == null)
      type = this.classTable.get(this.currentClass, s.id);
    if (type == null) error();
    s.exp.accept(this);
    s.type = this.type;
    if (!s.type.toString().equals(type.toString())) error();
  }

  @Override
  public void visit(AssignArray s) {
    Type.T leftArrayType = this.methodTable.get(s.id);
    if (null == leftArrayType) leftArrayType = this.classTable.get(this.currentClass, s.id);
    if (null == leftArrayType) error();
    s.index.accept(this);
    if (!this.type.toString().equals("@int")) error();
    s.exp.accept(this);
    if (!this.type.toString().equals("@int")) error();
  }

  @Override
  public void visit(Block s) {
    for (Stm.T stm: s.stms) stm.accept(this);
  }

  @Override
  public void visit(If s) {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean")) error();
    s.thenn.accept(this);
    s.elsee.accept(this);
  }

  @Override
  public void visit(Print s) {
    s.exp.accept(this);
    if (!this.type.toString().equals("@int")) error();
  }

  @Override
  public void visit(While s) {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean")) error();
    s.body.accept(this);
  }

  // type
  @Override
  public void visit(Type.Boolean t) {
    this.type = t;
  }

  @Override
  public void visit(Type.ClassType t) {
    this.type = t;
  }

  @Override
  public void visit(Type.Int t) {
    this.type = t;
  }

  @Override
  public void visit(Type.IntArray t) {
    this.type = t;
  }

  // dec
  @Override
  public void visit(Dec.DecSingle d) {
    this.classTable.put(currentClass, d.id, d.type);
  }

  // method
  @Override
  public void visit(Method.MethodSingle m) {
    // construct the method table
    this.methodTable.put(m.formals, m.locals);

    if (ConAst.elabMethodTable) {
      System.out.println("MethodTable of " + m.id);
      this.methodTable.dump();
      System.out.println();
    }

    for (Stm.T s : m.stms)
      s.accept(this);
    m.retExp.accept(this);

    this.methodTable.clear();
  }

  // class
  @Override
  public void visit(Class.ClassSingle c) {
    this.currentClass = c.id;

    for (Method.T m : c.methods) {
      m.accept(this);
    }
  }

  // main class
  @Override
  public void visit(MainClass.MainClassSingle c) {
    this.currentClass = c.id;
    // "main" has an argument "arg" of type "String[]", but
    // one has no chance to use it. So it's safe to skip it...

    c.stm.accept(this);
  }

  // ////////////////////////////////////////////////////////
  // step 1: build class table
  // class table for Main class
  private void buildMainClass(MainClass.MainClassSingle main) {
    this.classTable.put(main.id, new ClassBinding(null));
  }

  // class table for normal classes
  private void buildClass(ClassSingle c) {
    this.classTable.put(c.id, new ClassBinding(c.extendss));
    for (Dec.T dec : c.decs) {
      Dec.DecSingle d = (Dec.DecSingle) dec;
      this.classTable.put(c.id, d.id, d.type);
    }
    for (Method.T method : c.methods) {
      MethodSingle m = (MethodSingle) method;
      this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
    }
  }

  // step 1: end
  // ///////////////////////////////////////////////////

  // program
  @Override
  public void visit(ProgramSingle p) {
    // ////////////////////////////////////////////////
    // step 1: build a symbol table for class (the class table)
    // a class table is a mapping from class names to class bindings
    // classTable: className -> ClassBinding{extends, fields, methods}
    buildMainClass((MainClass.MainClassSingle) p.mainClass);
    for (Class.T c : p.classes) {
      buildClass((ClassSingle) c);
    }

    // we can double check that the class table is OK!
    if (control.Control.ConAst.elabClassTable) {
      this.classTable.dump();
    }

    // ////////////////////////////////////////////////
    // step 2: elaborate each class in turn, under the class table
    // built above.
    p.mainClass.accept(this);
    for (Class.T c : p.classes) {
      c.accept(this);
    }

  }
}
