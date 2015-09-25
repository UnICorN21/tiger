package codegen.dalvik;

import codegen.dalvik.Ast.Class;
import codegen.dalvik.Ast.Class.ClassSingle;
import codegen.dalvik.Ast.*;
import codegen.dalvik.Ast.Dec.DecSingle;
import codegen.dalvik.Ast.MainClass.MainClassSingle;
import codegen.dalvik.Ast.Method.MethodSingle;
import codegen.dalvik.Ast.Program.ProgramSingle;
import codegen.dalvik.Ast.Stm.*;
import codegen.dalvik.Ast.Type.ClassType;
import codegen.dalvik.Ast.Type.Int;
import codegen.dalvik.Ast.Type.IntArray;

import java.util.ArrayList;
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
    say("    ");
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

  int paramCount = 0;

  private String paramRegister() {
    return "p" + ++paramCount;
  }

  private void resetParamRegister() { paramCount = 0; }

  private String classDesc(String id) { return String.format("L%s;", id); }

  private void biopInt(String op, String dsc, String left, String right) {
    this.isayln(String.format("%s-int %s, %s, %s", op, dsc, left, right));
  }

  // /////////////////////////////////////////////////////
  // statements
  @Override
  public void visit(ReturnObject s) {
    this.isayln("return-object");
  }

  @Override
  public void visit(Goto32 s) {
    this.isayln("goto/32 :" + s.l.toString());
  }

  @Override
  public void visit(Iflt s) {
    this.isayln("if-lt " + s.left + ", " + s.right + ", :" + s.l.toString());
  }

  @Override
  public void visit(Ifgt s) {
    this.isayln("if-gt " + s.left + ", " + s.right + ", :" + s.l.toString());
  }

  @Override
  public void visit(Ifne s) {
    this.isayln("if-ne :" + s.l.toString());
  }

  @Override
  public void visit(Mulint s) {
    biopInt("mul", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(Invokevirtual s) {
    this.say("    invoke-virtual {");
    int cnt = 0;
    for (String p: s.params) {
      if (cnt++ <= 0) this.say(p);
      else this.say(", " + p);
    }
    this.say("}, " + classDesc(s.c) + "->" + s.f + "(");
    for (Type.T t : s.at) {
      t.accept(this);
    }
    this.say(")");
    s.rt.accept(this);
    this.sayln("");
  }

  @Override
  public void visit(Return s) {
    this.isayln("return " + s.src);
  }

  @Override
  public void visit(Subint s) {
    biopInt("sub", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(Addint s) {
    biopInt("add", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(Divint s) {
    biopInt("div", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(Andint s) {
    biopInt("and", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(Orint s) {
    biopInt("or", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(Xorint s) {
    biopInt("xor", s.dst, s.src1, s.src2);
  }

  @Override
  public void visit(LabelJ s) {
    this.sayln(":" + s.l.toString());
  }

  @Override
  public void visit(Const s) {
    this.isayln("const " + s.dst + ", " + s.i);
  }

  @Override
  public void visit(NewInstance s) {
    this.isayln("new-instance " + s.dst + ", " + classDesc(s.c));
    this.isayln("invoke-direct {" + s.dst + "}, " + classDesc(s.c) + "-><init>()V");
  }

  @Override
  public void visit(NewArray s) {
    this.isayln("new-array " + s.dst + ", " + s.size + ", " + s.type);
  }

  @Override
  public void visit(ArrayLength s) {
    this.isayln("array-length " + s.dst + ", " + s.src);
  }

  @Override
  public void visit(Aget s) {
    this.isayln("aget " + s.dst + ", " + s.aryReg + ", " + s.idxReg);
  }

  @Override
  public void visit(Aput s) {
    this.isayln("aput " + s.src + ", " + s.aryReg + ", " + s.idxReg);
  }

  @Override
  public void visit(IGet s) {
    this.isayln("iget " + s.dst + ", " + s.objReg + ", " + s.type);
  }

  @Override
  public void visit(Iput s) {
    this.isayln("iput " + s.src + ", " + s.objReg + ", " + s.type);
  }

  @Override
  public void visit(Print s) {
    this.isayln("sget-object " + s.stream + ", "
        + "Ljava/lang/System;->out:Ljava/io/PrintStream;");
    this.isayln("invoke-virtual {" + s.stream + ", " + s.src
        + "}, Ljava/io/PrintStream;->println(I)V");
  }

  @Override
  public void visit(Ifnez s) {
    this.isayln("if-nez " + s.cond + ", :" + s.l.toString());
  }

  @Override
  public void visit(Ifeqz s) {
    this.isayln("if-eqz " + s.cond + ", :" + s.l.toString());
  }

  @Override
  public void visit(Move16 s) {
    this.isayln("move/16 " + s.left + ", " + s.right);
  }

  @Override
  public void visit(Moveobject16 s) {
    this.isayln("move-object/16 " + s.left + s.right);
  }

  // //////////////////////////////////////////////////////
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

  // //////////////////////////////////////////////////
  // dec
  @Override
  public void visit(DecSingle d) {
  }

  // //////////////////////////////////////////////////
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
    this.isayln(".registers " + m.regCnt);
    m.formals.stream().map(f -> (DecSingle)f).forEach(f -> this.isayln(String.format(".param %s, \"%s\"", f.reg, f.id)));
    this.isayln(".prologue\n");
    m.locals.stream().map(f -> (DecSingle)f).forEach(f -> this.isayln(String.format(".local %s, \"%s\":%s", f.reg, f.id, f.type.desc())));
    this.isayln("");
    m.stms.forEach(s -> s.accept(this));

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
    this.sayln("# This is automatically generated by the Tiger compiler.");
    this.sayln("# Do NOT modify!\n");

    this.sayln(".class public " + classDesc(c.id));
    if (c.extendss == null)
      this.sayln(".super Ljava/lang/Object;\n");
    else
      this.sayln(".super " + classDesc(c.extendss));

    // fields
    for (Dec.T d : c.decs) {
      DecSingle dd = (DecSingle) d;
      this.say(".field public " + dd.id);
      dd.type.accept(this);
      this.sayln("");
    }

    // methods
    this.sayln(".method public constructor <init>()V");
    this.isayln(".registers 1");
    this.isayln(".prologue\n");
    this.isayln(".line 1");
    if (c.extendss == null)
      this.isayln("invoke-direct {p0}, Ljava/lang/Object;-><init>()V");
    else
      this.isayln("invoke-direct {p0}, " + c.extendss + "/<init>()V");
    this.isayln("return-void");
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

    this.sayln("# This is automatically generated by the Tiger compiler.");
    this.sayln("# Do NOT modify!\n");

    this.sayln(".class public " + classDesc(c.id));
    this.sayln(".super Ljava/lang/Object;\n");
    this.sayln(".method public static main([Ljava/lang/String;)V");
    this.isayln(".registers " + c.regCnt);
    this.isayln(String.format(".param p0, \"%s\"", c.arg));
    this.isayln(".prologue");
    for (Stm.T s : c.stms)
      s.accept(this);
    this.isayln("return-void");
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

  private String generateFilename(String classname) {
    String ret = classname + ".smali";
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
