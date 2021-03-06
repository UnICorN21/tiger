package ast;

import ast.Ast.Class.ClassSingle;
import ast.Ast.*;
import ast.Ast.Exp.*;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Stm.*;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

public class PrettyPrintVisitor implements Visitor {
  private int indentLevel;

  public PrettyPrintVisitor()
  {
    this.indentLevel = 4;
  }

  private void indent()
  {
    this.indentLevel += 2;
  }

  private void unIndent()
  {
    this.indentLevel -= 2;
  }

  private void printSpaces() {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s)
  {
    System.out.println(s);
  }

  private void say(String s)
  {
    System.out.print(s);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    this.say("(");
    e.left.accept(this);
    this.say(" + ");
    e.right.accept(this);
    this.say(")");
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    this.say(" && ");
    e.right.accept(this);
  }

  @Override
  public void visit(ArraySelect e) {
    e.array.accept(this);
    this.say("[");
    e.index.accept(this);
    this.say("]");
  }

  @Override
  public void visit(Call e) {
    e.exp.accept(this);
    this.say("." + e.id + "(");
    for (int i = 0; i < e.args.size(); ++i) {
      e.args.get(i).accept(this);
      if (i + 1 < e.args.size()) this.say(", ");
    }
    this.say(")");
  }

  @Override
  public void visit(False e) {
    this.say("false");
  }

  @Override
  public void visit(Id e) {
    this.say(e.id);
  }

  @Override
  public void visit(Length e) {
    e.array.accept(this);
    this.say(".length");
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    this.say(" < ");
    e.right.accept(this);
  }

  @Override
  public void visit(Le e) {
    e.left.accept(this);
    this.say(" <= ");
    e.right.accept(this);
  }

  @Override
  public void visit(Gt e) {
    e.left.accept(this);
    this.say(" > ");
    e.right.accept(this);
  }

  @Override
  public void visit(Ge e) {
    e.left.accept(this);
    this.say(" >= ");
    e.right.accept(this);
  }

  @Override
  public void visit(Eq e) {
    e.left.accept(this);
    this.say(" == ");
    e.right.accept(this);
  }

  @Override
  public void visit(NewIntArray e) {
    this.say("new int[");
    e.exp.accept(this);
    this.say("]");
  }

  @Override
  public void visit(NewObject e) {
    this.say("new " + e.id + "()");
  }

  @Override
  public void visit(Not e) {
    this.say("!");
    e.exp.accept(this);
  }

  @Override
  public void visit(Num e) { System.out.print(e.num);}

  public void visit(StringLiteral e) { System.out.print(e.literal); }

  @Override
  public void visit(Sub e) {
    this.say("(");
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
    this.say(")");
  }

  @Override
  public void visit(This e) {
    this.say("this");
  }

  @Override
  public void visit(Times e) {
    e.left.accept(this);
    this.say(" * ");
    e.right.accept(this);
  }

  @Override
  public void visit(True e) {
    this.say("true");
  }

  // statements
  @Override
  public void visit(Assign s) {
    this.printSpaces();
    this.say(s.id + " = ");
    s.exp.accept(this);
    this.sayln(";");
  }

  @Override
  public void visit(AssignArray s) {
    this.printSpaces();
    this.say(s.id + "[");
    s.index.accept(this);
    this.say("] = ");
    s.exp.accept(this);
    this.sayln(";");
  }

  @Override
  public void visit(Block s) {
    this.printSpaces();
    this.sayln("{");
    this.indent();
    for (Stm.T stm: s.stms) {
      stm.accept(this);
    }
    this.unIndent();
    this.printSpaces();
    this.sayln("}");
  }

  @Override
  public void visit(If s) {
    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.thenn.accept(this);
    this.unIndent();
    this.printSpaces();
    this.sayln("else");
    this.indent();
    s.elsee.accept(this);
    this.unIndent();
  }

  @Override
  public void visit(Print s) {
    this.printSpaces();
    this.say("System.out.println (");
    s.exp.accept(this);
    this.sayln(");");
  }

  @Override
  public void visit(While s) {
    this.printSpaces();
    this.say("while(");
    s.condition.accept(this);
    this.sayln(")");
    s.body.accept(this);
  }

  // type
  @Override
  public void visit(Boolean t) {
    this.say("boolean");
  }

  @Override
  public void visit(ClassType t) {
    this.say(t.id);
  }

  @Override
  public void visit(Int t) {
    this.say("int");
  }

  @Override
  public void visit(IntArray t) {
    this.say("int[]");
  }

  @Override
  public void visit(Type.StringType t) {
    this.say("string");
  }

  // dec
  @Override
  public void visit(Dec.DecSingle d) {
    this.printSpaces();
    d.type.accept(this);
    this.sayln(d.id + ";");
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    this.say("  public ");
    m.retType.accept(this);
    this.say(" " + m.id + "(");
    for (int i = 0; i < m.formals.size(); ++i) {
      Dec.DecSingle dec = (Dec.DecSingle)m.formals.get(i);
      dec.type.accept(this);
      this.say(" " + dec.id);
      if (i + 1 < m.formals.size()) this.say(", ");
    }
    this.sayln(")");
    this.sayln("  {");

    for (Dec.T d : m.locals) {
      Dec.DecSingle dec = (Dec.DecSingle) d;
      this.say("    ");
      dec.type.accept(this);
      this.say(" " + dec.id + ";\n");
    }
    this.sayln("");
    for (Stm.T s : m.stms)
      s.accept(this);
    this.say("    return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("  }");
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
    this.say("class " + c.id);
    if (c.extendss != null)
      this.sayln(" extends " + c.extendss);
    else
      this.sayln("");

    this.sayln("{");

    for (Dec.T d : c.decs) {
      Dec.DecSingle dec = (Dec.DecSingle) d;
      this.say("  ");
      dec.type.accept(this);
      this.say(" ");
      this.sayln(dec.id + ";");
    }
    for (Method.T mthd : c.methods)
      mthd.accept(this);
    this.sayln("}");
  }

  // main class
  @Override
  public void visit(MainClass.MainClassSingle c) {
    this.sayln("class " + c.id);
    this.sayln("{");
    this.sayln("  public static void main (String [] " + c.arg + ")");
    this.sayln("  {");
    c.stm.accept(this);
    this.sayln("  }");
    this.sayln("}");
  }

  // program
  @Override
  public void visit(Program.ProgramSingle p) {
    p.mainClass.accept(this);
    this.sayln("");
    for (ast.Ast.Class.T classs : p.classes) {
      classs.accept(this);
    }
    System.out.println("\n\n");
  }
}
