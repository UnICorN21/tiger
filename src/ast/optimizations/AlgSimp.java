package ast.optimizations;

import ast.Ast;
import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp.*;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.*;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

import java.util.LinkedList;

// Algebraic simplification optimizations on an AST.

public class AlgSimp implements ast.Visitor {
  private Class.T newClass;
  private MainClass.T mainClass;
  public Program.T program;

  private Ast.Exp.T exp;
  private Ast.Stm.T stm;
  private Ast.Method.T method;
  
  public AlgSimp() {
    this.newClass = null;
    this.mainClass = null;
    this.program = null;
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    e.left.accept(this);
    Ast.Exp.T left = this.exp;
    e.right.accept(this);
    Ast.Exp.T right = this.exp;
    if (left instanceof Num && 0 == ((Num)left).num) this.exp = right;
    else if (right instanceof Num && 0 == ((Num)right).num) this.exp = left;
    else this.exp = new Add(left, right, e.pos);
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    Ast.Exp.T left = this.exp;
    e.right.accept(this);
    Ast.Exp.T right = this.exp;
    if (left instanceof False || right instanceof False) this.exp = new False(e.pos);
    else if (left instanceof True) this.exp = right;
    else if (right instanceof True) this.exp = left;
    else this.exp = new And(left, right, e.pos);
  }

  @Override
  public void visit(ArraySelect e) {
    e.index.accept(this);
    Ast.Exp.T index = this.exp;
    e.array.accept(this);
    Ast.Exp.T array = this.exp;
    this.exp = new ArraySelect(array, index, e.pos);
  }

  @Override
  public void visit(Call e) {
    e.exp.accept(this);
    Ast.Exp.T exp = this.exp;
    this.exp = new Call(e.pos, e.args, e.at, exp, e.id, e.rt, e.type);
  }

  @Override
  public void visit(False e) {
    this.exp = e;
  }

  @Override
  public void visit(Id e) {
    this.exp = e;
  }

  @Override
  public void visit(Length e) {
    e.array.accept(this);
    this.exp = new Length(this.exp, e.pos);
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    Ast.Exp.T left = this.exp;
    e.right.accept(this);
    Ast.Exp.T right = this.exp;
    this.exp = new Lt(left, right, e.pos);
  }

  @Override
  public void visit(Ast.Exp.Gt e) {
    e.left.accept(this);
    Ast.Exp.T left = this.exp;
    e.right.accept(this);
    Ast.Exp.T right = this.exp;
    this.exp = new Gt(left, right, e.pos);
  }

  @Override
  public void visit(NewIntArray e) {
    e.exp.accept(this);
    this.exp = new NewIntArray(this.exp, e.pos);
  }

  @Override
  public void visit(NewObject e) {
    this.exp = e;
  }

  @Override
  public void visit(Not e) {
    e.exp.accept(this);
    this.exp = new Not(this.exp, e.pos);
  }

  @Override
  public void visit(Num e) {
    this.exp = e;
  }

  @Override
  public void visit(Sub e) {
    e.left.accept(this);
    Ast.Exp.T left = this.exp;
    e.right.accept(this);
    Ast.Exp.T right = this.exp;
    if (right instanceof Num && 0 == ((Num)right).num) this.exp = left;
    else this.exp = new Sub(left, right, e.pos);
  }

  @Override
  public void visit(This e) {
    this.exp = e;
  }

  @Override
  public void visit(Times e) {
    e.left.accept(this);
    Ast.Exp.T left = this.exp;
    e.right.accept(this);
    Ast.Exp.T right = this.exp;
    if (left instanceof Num) {
      Num l = (Num)left;
      if (0 == l.num) this.exp = new Num(0, e.pos);
      else if (1 == l.num) this.exp = right;
      else this.exp = new Times(left, right, e.pos);
    } else if (right instanceof Num) {
      Num r = (Num)right;
      if (0 == r.num) this.exp = new Num(0, e.pos);
      else if (1 == r.num) this.exp = left;
      else this.exp = new Times(left, right, e.pos);
    } else this.exp = new Times(left, right, e.pos);
  }

  @Override
  public void visit(True e) {
    this.exp = e;
  }

  /////////////////////////////////////////
  // statements
  @Override
  public void visit(Assign s) {
    s.exp.accept(this);
    this.stm = new Assign(this.exp, s.id, s.isField, s.type);
  }

  @Override
  public void visit(AssignArray s) {
    s.exp.accept(this);
    Ast.Exp.T exp = this.exp;
    s.index.accept(this);
    Ast.Exp.T index = this.exp;
    this.stm = new AssignArray(exp, s.id, index, s.isField);
  }

  @Override
  public void visit(Block s) {
    LinkedList<Ast.Stm.T> stms = new LinkedList<>();
    s.stms.stream().forEach(stm -> {
      stm.accept(this);
      stms.add(this.stm);
    });
    this.stm = new Block(stms);
  }

  @Override
  public void visit(If s) {
    s.condition.accept(this);
    Ast.Exp.T cond = this.exp;
    s.thenn.accept(this);
    Ast.Stm.T thenn = this.stm;
    s.elsee.accept(this);
    Ast.Stm.T elsee = this.stm;
    this.stm = new If(cond, thenn, elsee);
  }

  @Override
  public void visit(Print s) {
    s.exp.accept(this);
    this.stm = new Print(this.exp);
  }

  @Override
  public void visit(While s) {
    s.condition.accept(this);
    Ast.Exp.T cond = this.exp;
    s.body.accept(this);
    Ast.Stm.T body = this.stm;
    this.stm = new While(cond, body);
  }

  // types and dec can't accept this opt(It even won't be invoked.) and thus the following will do nothing.

  // type
  @Override
  public void visit(Boolean t) { /* null */ }

  @Override
  public void visit(ClassType t) { /* null */ }

  @Override
  public void visit(Int t) { /* null */ }

  @Override
  public void visit(IntArray t) { /* null */ }

  // dec
  @Override
  public void visit(DecSingle d) { /* null */ }

  // method
  @Override
  public void visit(MethodSingle m) {
    LinkedList<Ast.Stm.T> stms = new LinkedList<>();
    m.stms.stream().forEach(s -> {
      s.accept(this);
      stms.add(this.stm);
    });
    m.retExp.accept(this);
    Ast.Exp.T retExp = this.exp;
    this.method = new MethodSingle(m.retType, m.id, m.formals, m.locals, stms, retExp);
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    LinkedList<Ast.Method.T> methods = new LinkedList<>();
    c.methods.stream().forEach(m -> {
      m.accept(this);
      methods.add(this.method);
    });
    this.newClass = new ClassSingle(c.id, c.extendss, c.decs, methods);
  }

  // main class
  @Override
  public void visit(MainClassSingle c) {
    c.stm.accept(this);
    this.mainClass = new MainClassSingle(c.id, c.arg, this.stm);
  }

  // program
  @Override
  public void visit(ProgramSingle p) {

    LinkedList<Class.T> classes = new LinkedList<>();
    p.mainClass.accept(this);
    p.classes.stream().forEach(c -> {
      c.accept(this);
      classes.add(this.newClass);
    });
    this.program = new ProgramSingle(this.mainClass, classes);

    if (control.Control.trace.equals("ast.AlgSimp")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
  }
}
