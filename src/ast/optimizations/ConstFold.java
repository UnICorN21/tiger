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

// Constant folding optimizations on an AST.

public class ConstFold implements ast.Visitor {
  private Class.T newClass;
  private MainClass.T mainClass;
  public Program.T program;
  
  public ConstFold() {
    this.newClass = null;
    this.mainClass = null;
    this.program = null;
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
  }

  @Override
  public void visit(And e) {
  }

  @Override
  public void visit(ArraySelect e) {
  }

  @Override
  public void visit(Call e) {
  }

  @Override
  public void visit(False e) {
  }

  @Override
  public void visit(Id e) {
  }

  @Override
  public void visit(Length e) {
  }

  @Override
  public void visit(Lt e) {
  }

  @Override
  public void visit(Ast.Exp.Gt e) {
  }

  @Override
  public void visit(NewIntArray e) {
  }

  @Override
  public void visit(NewObject e) {
  }

  @Override
  public void visit(Not e) {
  }

  @Override
  public void visit(Num e) {
  }

  @Override
  public void visit(Sub e) {
  }

  @Override
  public void visit(This e) {
  }

  @Override
  public void visit(Times e) {
  }

  @Override
  public void visit(True e) {
  }

  // statements
  @Override
  public void visit(Assign s) {
  }

  @Override
  public void visit(AssignArray s) {
  }

  @Override
  public void visit(Block s) {
  }

  @Override
  public void visit(If s) {
  }

  @Override
  public void visit(Print s) {
  }

  @Override
  public void visit(While s) {
  }

  // type
  @Override
  public void visit(Boolean t) {
  }

  @Override
  public void visit(ClassType t) {
  }

  @Override
  public void visit(Int t) {
  }

  @Override
  public void visit(IntArray t) {
  }

  // dec
  @Override
  public void visit(DecSingle d) {
  }

  // method
  @Override
  public void visit(MethodSingle m) {
  }

  // class
  @Override
  public void visit(ClassSingle c) {
  }

  // main class
  @Override
  public void visit(MainClassSingle c) {
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    
 // You should comment out this line of code:
    this.program = p;

    if (control.Control.isTracing("ast.ConstFold")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
  }
}
