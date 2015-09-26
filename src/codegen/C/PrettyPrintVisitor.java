package codegen.C;

import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.*;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm.*;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;
import control.Control;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PrettyPrintVisitor implements Visitor {
  private int indentLevel;
  private java.io.BufferedWriter writer;

  public PrettyPrintVisitor() {
    this.indentLevel = 2;
  }

  private void indent() {
    this.indentLevel += 2;
  }

  private void unIndent() {
    this.indentLevel -= 2;
  }

  private void printSpaces() {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s) {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s) {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    e.left.accept(this);
    this.say(" + ");
    e.right.accept(this);
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
    this.say("(" + e.assign + " = ");
    e.exp.accept(this);
    this.say(", ");
    this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
    int size = e.args.size();
    if (size == 0) {
      this.say("))");
      return;
    }
    for (Exp.T x : e.args) {
      this.say(", ");
      x.accept(this);
    }
    this.say("))");
  }

  @Override
  public void visit(Id e) {
    this.say(e.id);
  }

  @Override
  public void visit(Length e) {
    this.say("(*((");
    e.array.accept(this);
    this.say(")-1))");
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    this.say(" < ");
    e.right.accept(this);
  }

  @Override
  public void visit(Gt e) {
    e.left.accept(this);
    this.say(" > ");
    e.right.accept(this);
  }

  @Override
  public void visit(NewIntArray e) {
    this.say("Tiger_new_array(");
    e.exp.accept(this);
    this.say(")");
  }

  @Override
  public void visit(NewObject e) {
    this.say("(" + e.name + " = ((struct " + e.classType + "*)(Tiger_new (&" + e.classType
        + "_vtable_, sizeof(struct " + e.classType + ")))), " + e.name + ")");
  }

  @Override
  public void visit(Not e) {
    this.say("!");
    e.exp.accept(this);
  }

  @Override
  public void visit(Num e) {
    this.say(Integer.toString(e.num));
  }

  @Override
  public void visit(Sub e) {
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
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
    s.stms.stream().forEach(stm -> stm.accept(this));
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
    this.sayln("");
    this.printSpaces();
    this.sayln("else");
    this.indent();
    s.elsee.accept(this);
    this.sayln("");
    this.unIndent();
  }

  @Override
  public void visit(Print s) {
    this.printSpaces();
    this.say("System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
  }

  @Override
  public void visit(While s) {
    this.printSpaces();
    this.say("while (");
    s.condition.accept(this);
    this.sayln(")");
    s.body.accept(this);
  }

  // type
  @Override
  public void visit(ClassType t) {
    this.say("struct " + t.id + " *");
  }

  @Override
  public void visit(Int t) {
    this.say("int");
  }

  @Override
  public void visit(IntArray t) {
    this.say("int*");
  }

  // dec
  @Override
  public void visit(DecSingle d) {
    d.type.accept(this);
    this.sayln(" " + d.id);
  }

  private String genMemGCMap(List<Dec.T> list) {
    return list.stream().map(f -> {
      Ast.Type.T t = ((DecSingle)f).type;
      if (t instanceof ClassType || t instanceof IntArray) return "1";
      else return "0";
    }).reduce("", String::concat);
  }

  private void isayln(String content) {
    this.printSpaces();
    this.sayln(content);
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    // generate a gc frame struct
    String gcFrameName = String.format("struct %s_%s_gc_frame", m.classId, m.id);
    this.sayln(gcFrameName + " {");
    this.isayln("void *prev;");
    this.isayln("char *arguments_gc_map;");
    this.isayln("void *arguments_base_address;");
    this.isayln("int local_references_cnt;");
    m.locals.stream().map(l -> (DecSingle)l)
            .filter(d -> d.type instanceof ClassType || d.type instanceof IntArray)
            .forEach(d -> this.isayln(String.format("struct %s *%s;", d.type, d.id)));
    this.sayln("};\n");

    // generate the method body
    m.retType.accept(this);
    this.say(" " + m.classId + "_" + m.id + "(");
    int size = m.formals.size();
    for (Dec.T d : m.formals) {
      DecSingle dec = (DecSingle) d;
      size--;
      dec.type.accept(this);
      this.say(" " + dec.id);
      if (size > 0)
        this.say(", ");
    }
    this.sayln(")");
    this.sayln("{");

    // generate a gc stack frame and push it into the stack
    this.isayln(gcFrameName + " frame;");
    this.isayln("frame.prev = head;");
    this.isayln("head = &frame;");
    this.isayln(String.format("frame.arguments_gc_map = \"%s\";", genMemGCMap(m.formals)));
    this.isayln("frame.arguments_base_address = &this;");
    int localRefCnt = (int)Stream.of(genMemGCMap(m.locals)).filter(s -> s.equals("1")).count();
    this.isayln(String.format("frame.local_references_cnt = %s;", localRefCnt));
    this.isayln("");

    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      this.say("  ");
      dec.type.accept(this);
      this.say(" " + dec.id + ";\n");
    }
    this.sayln("");
    m.locals.stream().map(l -> (DecSingle) l)
            .filter(d -> d.type instanceof ClassType || d.type instanceof IntArray)
            .forEach(d -> this.isayln(String.format("frame.%s = &%s;", d.id, d.id)));
    if (0 != localRefCnt) this.sayln("");

    m.stms.stream().forEach(s -> s.accept(this));
    this.sayln("");

    // pop the gc frame
    this.isayln("head = frame.prev;");

    this.say("  return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("}\n");
  }

  @Override
  public void visit(MainMethodSingle m) {
    this.sayln("int Tiger_main ()");
    this.sayln("{");
    for (Dec.T dec : m.locals) {
      this.say("  ");
      DecSingle d = (DecSingle) dec;
      d.type.accept(this);
      this.say(" ");
      this.sayln(d.id + ";");
    }
    m.stm.accept(this);
    this.sayln("}");
  }

  // vtables
  @Override
  public void visit(VtableSingle v) {
    this.sayln("struct " + v.id + "_vtable {");
    this.isayln("char *gc_map;");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      t.ret.accept(this);
      this.sayln(" (*" + t.id + ")();");
    }
    this.sayln("};\n");
  }

  private void outputVtable(VtableSingle v) {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
    this.sayln("{");
    this.isayln(v.gcMap + ",");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.sayln("};\n");
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    this.sayln("struct " + c.id + " {");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    for (codegen.C.Tuple t : c.decs) {
      this.say("  ");
      t.type.accept(this);
      this.say(" ");
      this.sayln(t.id + ";");
    }
    this.sayln("};\n");
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = getOutputFileNames().get(0);

      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    this.sayln("#include \"../runtime/runtime.c\"\n");

    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!\n");

    this.sayln("// structures");
    for (codegen.C.Ast.Class.T c : p.classes) {
      c.accept(this);
    }

    this.sayln("// vtables structures");
    for (Vtable.T v : p.vtables) {
      v.accept(this);
    }
    this.sayln("");

    this.sayln("// a global pointer to GC stack");
    this.sayln("void *head;\n");

    this.sayln("// methods");
    for (Method.T m : p.methods) {
      m.accept(this);
    }

    this.sayln("// vtables");
    for (Vtable.T v : p.vtables) {
      outputVtable((VtableSingle) v);
    }
    this.sayln("");

    this.sayln("// main method");
    p.mainMethod.accept(this);
    this.sayln("");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public List<String> getOutputFileNames() {
    String outputName;
    if (Control.ConCodeGen.outputName != null)
      outputName = Control.ConCodeGen.outputName;
    else if (Control.ConCodeGen.fileName != null)
      outputName = Control.ConCodeGen.fileName + ".c";
    else
      outputName = "a.c";
    List<String> ret = new ArrayList<>(1);
    ret.add(outputName);
    return ret;
  }
}
