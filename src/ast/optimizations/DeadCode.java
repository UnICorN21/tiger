package ast.optimizations;

import ast.Ast;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp.*;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.*;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

// Dead code elimination optimizations on an AST.

public class DeadCode implements ast.Visitor {
  private LinkedList<ast.Ast.Class.T> newClass;
  private ast.Ast.MainClass.T mainClass;
  public ast.Ast.Program.T program;

  private LinkedList<Ast.Stm.T> stms;
  private ast.Ast.Method.T method;
  private Set<String> mset;
  private Set<String> cset;
  
  public DeadCode() {
    this.newClass = new LinkedList<>();
    this.mainClass = null;
    this.program = null;

    this.stms = new LinkedList<>();
    this.method = null;
    this.mset = new HashSet<>();
    this.cset = new HashSet<>();
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(ArraySelect e) {
    e.array.accept(this);
    e.index.accept(this);
  }

  @Override
  public void visit(Call e) {
    e.args.stream().forEach(arg -> arg.accept(this));
  }

  @Override
  public void visit(False e) { /* null */ }

  @Override
  public void visit(Id e) {
    if (e.isField && !this.cset.contains(e.id)) this.cset.add(e.id);
    else if (!e.isField && !this.mset.contains(e.id)) this.mset.add(e.id);
  }

  @Override
  public void visit(Length e) {
    e.array.accept(this);
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(Gt e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(NewIntArray e) { /* null */ }

  @Override
  public void visit(NewObject e) { /* null */ }

  @Override
  public void visit(Not e) {
    e.exp.accept(this);
  }

  @Override
  public void visit(Num e) { /* null */ }

  @Override
  public void visit(Sub e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(This e) { /* null */ }

  @Override
  public void visit(Times e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(True e) { /* null */ }

  // statements
  @Override
  public void visit(Assign s) {
    if (s.isField && !this.cset.contains(s.id)) this.cset.add(s.id);
    else if (!s.isField && !this.mset.contains(s.id)) this.mset.add(s.id);
    this.stms.add(s);
  }

  @Override
  public void visit(AssignArray s) {
    if (s.isField && !this.cset.contains(s.id)) this.cset.add(s.id);
    else if (!s.isField && !this.mset.contains(s.id)) this.mset.add(s.id);
    this.stms.add(s);
  }

  @Override
  public void visit(Block s) {
    this.stms.add(s);
  }

  @Override
  public void visit(If s) {
    if (s.condition instanceof True) this.stms.add(s.thenn);
    else if (s.condition instanceof False) this.stms.add(s.elsee);
    else this.stms.add(s);
  }

  @Override
  public void visit(Print s) {
    s.exp.accept(this);
  }

  @Override
  public void visit(While s) {
    if (s.condition instanceof True) {
      System.out.println(String.format("Warning: while at will loop forever.", s.condition.pos));
    } else if (!(s.condition instanceof False)) this.stms.add(s);
  }

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
    this.mset.clear();
    this.stms.clear();
    m.stms.forEach(stm -> stm.accept(this));
    this.method = new MethodSingle(m.retType, m.id, m.formals, m.locals, this.stms, m.retExp);
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    LinkedList<Ast.Method.T> methods = new LinkedList<>();
    c.methods.stream().forEach(m -> {
      m.accept(this);
      methods.add(this.method);
    });
    LinkedList<Ast.Dec.T> decs = new LinkedList<>();
    c.decs.stream().map(d -> (DecSingle)d).forEach(d -> {
      if (this.cset.contains(d)) decs.add(d);
    });
    this.newClass.add(new ClassSingle(c.id, c.extendss, decs, methods));
  }

  // main class
  @Override
  public void visit(MainClassSingle c) {
    c.stm.accept(this);
  }

  // program
  @Override
  public void visit(ProgramSingle p) {

    p.mainClass.accept(this);
    p.classes.stream().forEach(c -> c.accept(this));
    this.program = new ProgramSingle(p.mainClass, this.newClass);

    if (control.Control.trace.equals("ast.DeadCode")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
  }
}
