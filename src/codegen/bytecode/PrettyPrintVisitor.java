package codegen.bytecode;

import codegen.bytecode.Ast.Class;
import codegen.bytecode.Ast.Class.ClassSingle;
import codegen.bytecode.Ast.*;
import codegen.bytecode.Ast.Dec.DecSingle;
import codegen.bytecode.Ast.MainClass.MainClassSingle;
import codegen.bytecode.Ast.Method.MethodSingle;
import codegen.bytecode.Ast.Program.ProgramSingle;
import codegen.bytecode.Ast.Stm.*;
import codegen.bytecode.Ast.Type.ClassType;
import codegen.bytecode.Ast.Type.Int;
import codegen.bytecode.Ast.Type.IntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrettyPrintVisitor implements Visitor {
  private java.io.BufferedWriter writer;
  private List<String> filenames;

  public PrettyPrintVisitor() { filenames = new ArrayList<>(); }

  private void sayln(String s) {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void isayln(String s) {
    say("\t");
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
  // statements
  @Override
  public void visit(This s) {
    this.isayln("aload_0");
  }

  @Override
  public void visit(Aload s) {
    this.isayln("aload " + s.index);
  }

  @Override
  public void visit(Areturn s) {
    this.isayln("areturn");
  }

  @Override
  public void visit(Astore s) {
    this.isayln("astore " + s.index);
  }

  @Override
  public void visit(Goto s) {
    this.isayln("goto " + s.l.toString());
  }

  @Override
  public void visit(Ificmplt s) {
    this.isayln("if_icmplt " + s.l.toString());
  }

  @Override
  public void visit(Ificmpgt s) {
    this.isayln("if_icmpgt " + s.l.toString());
  }

  @Override
  public void visit(Ifgt s) {
    this.isayln("ifgt " + s.l.toString());
  }

  @Override
  public void visit(Iflt s) {
    this.isayln("iflt " + s.l.toString());
  }

  @Override
  public void visit(Ifne s) {
    this.isayln("ifne " + s.l.toString());
  }

  @Override
  public void visit(Ifeq s) {
    this.isayln("ifeq " + s.l.toString());
  }

  @Override
  public void visit(Iload s) {
    if (s.index <= 3) this.isayln("iload_" + s.index);
    else this.isayln("iload " + s.index);
  }

  @Override
  public void visit(Istore s) {
    if (s.index <= 3) this.isayln("istore_" + s.index);
    else this.isayln("istore " + s.index);
  }

  @Override
  public void visit(Iaload s) {
    this.isayln("iaload");
  }

  @Override
  public void visit(Iastore s) {
    this.isayln("iastore");
  }

  @Override
  public void visit(Imul s) {
    this.isayln("imul");
  }

  @Override
  public void visit(Invokevirtual s) {
    this.say("    invokevirtual " + s.c + "/" + s.f + "(");
    for (Type.T t : s.at) {
      t.accept(this);
    }
    this.say(")");
    s.rt.accept(this);
    this.sayln("");
  }

  @Override
  public void visit(Ireturn s) {
    this.isayln("ireturn");
  }

  @Override
  public void visit(Isub s) {
    this.isayln("isub");
  }

  @Override
  public void visit(LabelJ s) {
    this.sayln(s.l.toString() + ":");
  }

  @Override
  public void visit(Ldc s) {
    this.isayln("ldc " + s.i);
  }

  @Override
  public void visit(New s) {
    this.isayln("new " + s.c);
    this.isayln("dup");
    this.isayln("invokespecial " + s.c + "/<init>()V");
  }

  @Override
  public void visit(Print s) {
    this.isayln("getstatic java/lang/System/out Ljava/io/PrintStream;");
    this.isayln("swap");
    this.isayln("invokevirtual java/io/PrintStream/println(I)V");
  }

  @Override
  public void visit(ArrayLength s) {
    this.isayln("arraylength");
  }

  @Override
  public void visit(False s) {
    this.isayln("iconst_0");
  }

  @Override
  public void visit(IAdd s) {
    this.isayln("iadd");
  }

  @Override
  public void visit(IAnd s) {
    this.isayln("iand");
  }

  @Override
  public void visit(NewArray s) {
    this.isayln("newarray " + s.type);
  }

  @Override
  public void visit(True s) {
    this.isayln("iconst_1");
  }

  @Override
  public void visit(GetField s) {
    this.isayln(String.format("getfield %s %s", s.fieldspce, s.descriptor));
  }

  @Override
  public void visit(PutField s) {
    this.isayln(String.format("putfield %s %s", s.fieldspec, s.descriptor));
  }

  // type
  @Override
  public void visit(ClassType t) {
    this.say("L" + t.id + ";");
  }

  @Override
  public void visit(Int t) {
    this.say("I");
  }

  @Override
  public void visit(IntArray t) {
    this.say("[I");
  }

  // dec
  @Override
  public void visit(DecSingle d) { /* null */ }

  // method
  @Override
  public void visit(MethodSingle m) {
    this.say(".method public " + m.id + "(");
    for (Dec.T d : m.formals) {
      DecSingle dd = (DecSingle) d;
      dd.type.accept(this);
    }
    this.say(")");
    m.retType.accept(this);
    this.sayln("");
    this.sayln(".limit stack 4096");
    this.sayln(".limit locals " + (m.index + 1));

    for (Stm.T s : m.stms)
      s.accept(this);

    this.sayln(".end method");
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    // Every class must go into its own class file.
    try {
      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(generateFilename(c.id))));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // header
    this.sayln("; This is automatically generated by the Tiger compiler.");
    this.sayln("; Do NOT modify!\n");

    this.sayln(".class public " + c.id);
    if (c.extendss == null)
      this.sayln(".super java/lang/Object\n");
    else
      this.sayln(".super " + c.extendss);

    // fields
    for (Dec.T d : c.decs) {
      DecSingle dd = (DecSingle) d;
      this.say(String.format(".field public %s ", dd.id));
      dd.type.accept(this);
      this.sayln("");
    }

    // methods
    this.sayln(".method public <init>()V");
    this.isayln("aload 0");
    if (c.extendss == null)
      this.isayln("invokespecial java/lang/Object/<init>()V");
    else
      this.isayln("invokespecial " + c.extendss + "/<init>()V");
    this.isayln("return");
    this.sayln(".end method\n\n");

    for (Method.T m : c.methods) {
      m.accept(this);
    }

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // main class
  @Override
  public void visit(MainClassSingle c) {
    // Every class must go into its own class file.
    try {
      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(generateFilename(c.id))));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    this.sayln("; This is automatically generated by the Tiger compiler.");
    this.sayln("; Do NOT modify!\n");

    this.sayln(".class public " + c.id);
    this.sayln(".super java/lang/Object\n");
    this.sayln(".method public static main([Ljava/lang/String;)V");
    this.isayln(".limit stack 4096");
    this.isayln(".limit locals 2");
    for (Stm.T s : c.stms)
      s.accept(this);
    this.isayln("return");
    this.sayln(".end method");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // program
  @Override
  public void visit(ProgramSingle p) {

    p.mainClass.accept(this);

    for (Class.T c : p.classes) {
      c.accept(this);
    }
  }

  @Override
  public void visit(Debug.Line l) {
    this.sayln(String.format(".line %d", l.num));
  }

  @Override
  public void visit(Debug.Comment c) {
    Arrays.stream(c.content).forEach(line -> this.sayln(" ; " + line));
  }

  private String generateFilename(String classname) {
    String ret = classname + ".j";
    filenames.add(ret);
    return ret;
  }

  /**
   * Get all generated filenames.
   * Should be invoked after the whole pass.
   */
  public List<String> getOutputFileNames() {
    return this.filenames;
  }
}
