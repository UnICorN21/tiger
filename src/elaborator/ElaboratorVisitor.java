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
import util.Pos;

import java.util.LinkedList;

public class ElaboratorVisitor implements ast.Visitor {
  private static final Type.Int TYPE_INT = new Type.Int();
  private static final Type.IntArray TYPE_INTARRAY = new Type.IntArray();
  private static final Type.Boolean TYPE_BOOLEAN = new Type.Boolean();

  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public Type.T type; // type of the expression being elaborated

  private int errorCnt = 0;
  private int warnCnt = 0;

  public ElaboratorVisitor() {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
  }

  private void error(String msg, Pos pos) {
    ++errorCnt;
    System.out.println(String.format("%s at %s.", msg, pos));
  }

  private void error(Type.T found, Type.T excepted, Pos pos) {
    error(String.format("Except %s, found %s", excepted, found), pos);
  }

  private void warn(String msg) {
    ++warnCnt;
    System.out.println(String.format("Warning: %s", msg));
  }

  private void warnUnused(String id, Pos pos) {
    warn(String.format("variable %s at %s never used", id, pos));
  }

  private void report() {
    System.out.println("Parse finished.");
    System.out.println(String.format("Found %d error(s), %d warn(s).\n", errorCnt, warnCnt));
    if (0 < errorCnt) System.exit(1);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    e.left.accept(this);
    Type.T tl = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(tl.toString())) error(tl, this.type, e.right.pos);
    this.type = TYPE_INT;
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN, e.left.pos);
    e.right.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN, e.right.pos);
    this.type = TYPE_BOOLEAN;
  }

  @Override
  public void visit(ArraySelect e) {
    e.index.accept(this);
    if (!this.type.toString().equals("@int")) error(this.type, TYPE_INT, e.index.pos);
    e.array.accept(this);
    if (!this.type.toString().equals("@int[]")) error(this.type, TYPE_INTARRAY, e.array.pos);
    this.type = TYPE_INT;
  }

  @Override
  public void visit(Call e) {
    Type.T leftty;
    Type.ClassType ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty instanceof ClassType) {
      // check whether can find the correspond class
      ty = (ClassType) leftty;
      if (null == this.classTable.get(ty.toString())) {
        error("Can't resolve class named " + ty.toString(), e.exp.pos);
        return;
      }
      e.type = ty.id;
    } else error("Can't call methods on a no class object", e.exp.pos);
    MethodType mty = this.classTable.getm(ty.id, e.id);
    java.util.LinkedList<Type.T> declaredArgTypes = new java.util.LinkedList<>();
    mty.argsType.forEach(dec -> declaredArgTypes.add(((DecSingle)dec).type));
    java.util.LinkedList<Type.T> argsty = new LinkedList<>();
    for (Exp.T a : e.args) {
      a.accept(this);
      argsty.addLast(this.type);
    }
    if (mty.argsType.size() > argsty.size()) error("Not enough arguments", e.pos);
    else if (mty.argsType.size() < argsty.size()) error("More arguments than excepted", e.pos);
    for (int i = 0; i < argsty.size(); i++) {
      Type.T methodArgType = null;
      if (i < mty.argsType.size()) methodArgType = ((DecSingle) mty.argsType.get(i)).type;
      Type.T paramArgType = argsty.get(i);
      if (null != paramArgType && paramArgType instanceof ClassType) {
        // loop to check with type hierarchy
        while (null != methodArgType && !methodArgType.toString().equals(paramArgType.toString())) {
          String parent = this.classTable.get(paramArgType.toString()).extendss;
          if (null != parent) paramArgType = new Type.ClassType(parent);
          else error(paramArgType, methodArgType, e.pos);
        }
      } else if (null != paramArgType && !paramArgType.toString().equals(paramArgType.toString())) error(paramArgType, methodArgType, e.pos);
    }
    this.type = mty.retType;
    // the following two types should be the declared types.
    e.at = declaredArgTypes;
    e.rt = this.type;
  }

  @Override
  public void visit(False e) {
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(Id e) {
    // first look up the classType in method table
    Type.T type = this.methodTable.get(e.id);
    // if search failed, then s.classType must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this classType as a field classType, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null) error("Can't resolve variable " + e.id, e.pos);
    this.type = type;
    // record this type on this node for future use.
    e.type = type;
  }

  @Override
  public void visit(Length e) {
    e.array.accept(this);
    if (!this.type.toString().equals("@int[]")) error(this.type, TYPE_INTARRAY, e.array.pos);
    this.type = TYPE_INT;
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    Type.T ty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(ty.toString())) error(this.type, ty, e.right.pos);
    this.type = TYPE_BOOLEAN;
  }

  @Override
  public void visit(Gt e) {
    e.left.accept(this);
    Type.T ty = this.type;
    e.right.accept(this);
    if (!this.type.toString().equals(ty.toString())) error(this.type, ty, e.right.pos);
    this.type = TYPE_BOOLEAN;
  }

  @Override
  public void visit(NewIntArray e) {
    e.exp.accept(this);
    if (!this.type.toString().equals("@int")) error(this.type, TYPE_INT, e.exp.pos);
    this.type = TYPE_INTARRAY;
  }

  @Override
  public void visit(NewObject e) {
    this.type = new Type.ClassType(e.id);
  }

  @Override
  public void visit(Not e) {
    e.exp.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN, e.exp.pos);
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
    if (!this.type.toString().equals(leftty.toString())) error(this.type, leftty, e.right.pos);
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
    if (!this.type.toString().equals(leftty.toString())) error(this.type, leftty, e.right.pos);
    this.type = TYPE_INT;
  }

  @Override
  public void visit(True e) {
    this.type = TYPE_BOOLEAN;
  }

  // statements
  @Override
  public void visit(Assign s) {
    // first look up the classType in method table
    Type.T type = this.methodTable.get(s.id);
    // if search failed, then s.classType must be a class field
    if (type == null) {
      s.isField = true;
      type = this.classTable.get(this.currentClass, s.id);
    }
    if (type == null) error("Can't resolve variable " + s.id, s.exp.pos);
    s.exp.accept(this);
    s.type = this.type;
    if (null != type && !s.type.toString().equals(type.toString())) error(this.type, type, s.exp.pos);
  }

  @Override
  public void visit(AssignArray s) {
    Type.T leftArrayType = this.methodTable.get(s.id);
    if (null == leftArrayType) {
      s.isField = true;
      leftArrayType = this.classTable.get(this.currentClass, s.id);
    }
    if (null == leftArrayType) error("Can't resolve variable " + s.id, s.index.pos);
    s.index.accept(this);
    if (!this.type.toString().equals("@int")) error(this.type, TYPE_INT, s.index.pos);
    s.exp.accept(this);
    if (!this.type.toString().equals("@int")) error(this.type, TYPE_INT, s.exp.pos);
  }

  @Override
  public void visit(Block s) {
    for (Stm.T stm: s.stms) stm.accept(this);
  }

  @Override
  public void visit(If s) {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN, s.condition.pos);
    s.thenn.accept(this);
    s.elsee.accept(this);
  }

  @Override
  public void visit(Print s) {
    s.exp.accept(this);
    if (!this.type.toString().equals("@int")) error(this.type, TYPE_INT, s.exp.pos);
  }

  @Override
  public void visit(While s) {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean")) error(this.type, TYPE_BOOLEAN, s.condition.pos);
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

    this.methodTable.getUnusedVars().forEach(info -> warnUnused(info.id, info.pos));
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

    // step 3: report the result
    report();

  }
}
