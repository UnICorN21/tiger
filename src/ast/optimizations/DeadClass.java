package ast.optimizations;

import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.*;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.*;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

import java.util.HashSet;
import java.util.LinkedList;

// Dead class elimination optimizations on an AST.

public class DeadClass implements ast.Visitor {
  private HashSet<String> set;
  private LinkedList<String> worklist;
  public Program.T program;

  public DeadClass() {
    this.set = new java.util.HashSet<>();
    this.worklist = new java.util.LinkedList<>();
    this.program = null;
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
    e.exp.accept(this);
    for (Exp.T arg : e.args) {
      arg.accept(this);
    }
  }

  @Override
  public void visit(False e) { /* null */ }

  @Override
  public void visit(Id e) {
    e.type.accept(this);
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
  public void visit(Exp.Gt e) {
    e.left.accept(this);
    e.right.accept(this);
  }

  @Override
  public void visit(NewIntArray e) {
    e.exp.accept(this);
  }

  @Override
  public void visit(NewObject e) {
    if (!this.set.contains(e.id)) {
      this.worklist.add(e.id);
      this.set.add(e.id);
    }
  }

  @Override
  public void visit(Not e) {
    e.exp.accept(this);
  }

  @Override
  public void visit(Num e) { /* null */ }

  @Override
  public void visit(StringLiteral e) { /* null */ }

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
    s.exp.accept(this);
  }

  @Override
  public void visit(AssignArray s) {
    s.index.accept(this);
    s.exp.accept(this);
  }

  @Override
  public void visit(Block s) {
    for (Stm.T x : s.stms)
      x.accept(this);
  }

  @Override
  public void visit(If s) {
    s.condition.accept(this);
    s.thenn.accept(this);
    s.elsee.accept(this);
  }

  @Override
  public void visit(Print s) {
    s.exp.accept(this);
  }

  @Override
  public void visit(While s) {
    s.condition.accept(this);
    s.body.accept(this);
  }

  // type
  @Override
  public void visit(Boolean t) { /* null */ }

  @Override
  public void visit(ClassType t) {
    if (!this.set.contains(t.id)) {
      this.set.add(t.id);
      this.worklist.add(t.id);
    }
  }

  @Override
  public void visit(Int t) { /* null */ }

  @Override
  public void visit(IntArray t) { /* null */ }

  // dec
  @Override
  public void visit(DecSingle d) {
    d.type.accept(this);
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    m.stms.stream().forEach(s -> s.accept(this));
    m.retExp.accept(this);
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    if (null != c.extendss && !this.set.contains(c.extendss)) {
      this.set.contains(c.extendss);
      this.worklist.contains(c.extendss);
    }
    c.decs.forEach(d -> d.accept(this));
    c.methods.forEach(m -> m.accept(this));
  }

  // main class
  @Override
  public void visit(MainClassSingle c) {
    c.stm.accept(this);
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    // we push the class name for mainClass onto the worklist
    MainClassSingle mainclass = (MainClassSingle) p.mainClass;
    this.set.add(mainclass.id);

    p.mainClass.accept(this);

    while (!this.worklist.isEmpty()) {
      String cid = this.worklist.removeFirst();

      for (ast.Ast.Class.T c : p.classes) {
        ClassSingle current = (ClassSingle) c;

        if (current.id.equals(cid)) {
          c.accept(this);
          break;
        }
      }
    }

    LinkedList<ast.Ast.Class.T> newClasses = new LinkedList<>();
    for (ast.Ast.Class.T classes : p.classes) {
      ClassSingle c = (ClassSingle) classes;
      if (this.set.contains(c.id))
        newClasses.add(c);
    }

    this.program = new ProgramSingle(p.mainClass, newClasses);
    
    if (control.Control.trace.equals("ast.DeadClass")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
  }
}
