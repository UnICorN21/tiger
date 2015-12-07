package codegen.dalvik;

import codegen.dalvik.Ast.Class;
import codegen.dalvik.Ast.Class.ClassSingle;
import codegen.dalvik.Ast.*;
import codegen.dalvik.Ast.Dec.DecSingle;
import codegen.dalvik.Ast.MainClass.MainClassSingle;
import codegen.dalvik.Ast.Method.MethodSingle;
import codegen.dalvik.Ast.Program.ProgramSingle;
import codegen.dalvik.Ast.Stm.*;
import util.Label;
import util.Temp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

// Given a Java AST, translate it into Dalvik bytecode.

public class TranslateVisitor implements ast.Visitor {
  private String classId;
  private Type.T type; // type after translation
  private Dec.T dec;
  // these two fields are expression-related: after
  // translating an expression, we get the expression's
  // type "etype" and the expression name "evar";
  private Type.T etype;
  private String evar;

  private LinkedList<DecSingle> tmpVars;
  private LinkedList<Stm.T> stms;
  private Map<String, DecSingle> classLookupTable;
  private Map<String, DecSingle> methodLookupTable;
  private Method.T method;
  private Class.T clazz;
  private MainClass.T mainClass;
  public Program.T program;

  public TranslateVisitor() {
    this.classId = null;
    this.type = null;
    this.dec = null;
    this.tmpVars = new LinkedList<>();
    this.etype = null;
    this.evar = null;
    this.stms = new LinkedList<>();
    this.classLookupTable = new HashMap<>();
    this.methodLookupTable = new HashMap<>();
    this.method = null;
    this.clazz = null;
    this.mainClass = null;
    this.program = null;
  }

  // utility functions
  private void emitDec(Type.T ty, String id) {
    this.tmpVars.addLast(new DecSingle(ty, id));
  }

  private void emit(Stm.T s) {
    this.stms.add(s);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.Ast.Exp.Add e) {
    e.left.accept(this);
    String left = this.evar;
    e.right.accept(this);
    String right = this.evar;
    String reg = Temp.next();
    emit(new Addint(reg, left, right));
  }

  @Override
  public void visit(ast.Ast.Exp.And e) {
    e.left.accept(this);
    String left = this.evar;
    e.right.accept(this);
    String right = this.evar;
    String reg = Temp.next();
    emit(new Andint(reg, left, right));
  }

  @Override
  public void visit(ast.Ast.Exp.ArraySelect e) {
    e.array.accept(this);
    String aryReg = this.evar;
    e.index.accept(this);
    String idxReg = this.evar;
    String reg = Temp.next();
    emit(new Aget(aryReg, idxReg, reg));
  }

  @Override
  public void visit(ast.Ast.Exp.Call e) {
    LinkedList<String> regs = new LinkedList<>();
    e.exp.accept(this);
    regs.add(this.evar);
    for (ast.Ast.Exp.T x : e.args) {
      x.accept(this);
      regs.add(this.evar);
    }
    e.rt.accept(this);
    Type.T rt = this.type;
    java.util.LinkedList<Type.T> at = new LinkedList<>();
    for (ast.Ast.Type.T t : e.at) {
      t.accept(this);
      at.add(this.type);
    }
    emit(new Invokevirtual(e.id, e.type, at, rt, regs));
  }

  @Override
  public void visit(ast.Ast.Exp.False e) {
    this.evar = Temp.next();
    this.etype = new Type.Int();
    emit(new Const(this.evar, 0));
  }

  @Override
  public void visit(ast.Ast.Exp.Id e) {
    DecSingle ds = methodLookupTable.get(e.id);
    if (null != ds) {
      this.evar = ds.reg;
      this.etype = ds.type;
    } else {
      ds = classLookupTable.get(e.id);
      this.evar = String.format("L%s;->%s:%s", classId, ds.id, ds.type.desc());
      this.etype = ds.type;
    }
  }

  @Override
  public void visit(ast.Ast.Exp.Length e) {
    e.array.accept(this);
    String reg = Temp.next();
    emit(new ArrayLength(reg, this.evar));
  }

  @Override
  public void visit(ast.Ast.Exp.Lt e) {
    Label tl = new Label(), fl = new Label(), el = new Label();
    e.left.accept(this);
    String lname = this.evar;
    e.right.accept(this);
    String rname = this.evar;
    String newname = util.Temp.next();
    this.evar = newname;
    this.etype = new Type.Int();
    emit(new Iflt(lname, rname, tl));
    emit(new LabelJ(fl));
    emit(new Const(newname, 0));
    emit(new Goto32(el));
    emit(new LabelJ(tl));
    emit(new Const(newname, 1));
    emit(new Goto32(el));
    emit(new LabelJ(el));
  }

  @Override
  public void visit(ast.Ast.Exp.Le e) {
    // TODO
  }

  @Override
  public void visit(ast.Ast.Exp.Gt e) {
    Label tl = new Label(), fl = new Label(), el = new Label();
    e.left.accept(this);
    String lname = this.evar;
    e.right.accept(this);
    String rname = this.evar;
    String newname = util.Temp.next();
    this.evar = newname;
    this.etype = new Type.Int();
    emit(new Ifgt(lname, rname, tl));
    emit(new LabelJ(fl));
    emit(new Const(newname, 0));
    emit(new Goto32(el));
    emit(new LabelJ(tl));
    emit(new Const(newname, 1));
    emit(new Goto32(el));
    emit(new LabelJ(el));
  }

  @Override
  public void visit(ast.Ast.Exp.Eq e) {
    // TODO
  }

  @Override
  public void visit(ast.Ast.Exp.Ge e) {
    // TODO
  }

  @Override
  public void visit(ast.Ast.Exp.NewIntArray e) {
    e.exp.accept(this);
    String reg = Temp.next();
    emit(new NewArray(reg, this.evar, "[I"));
    this.evar = reg;
    this.type = new Type.IntArray();
    emitDec(this.type, this.evar);
  }

  @Override
  public void visit(ast.Ast.Exp.NewObject e) {
    String newname = Temp.next();
    this.evar = newname;
    this.etype = new Type.ClassType(e.id);
    emit(new NewInstance(newname, e.id));
    emitDec(this.etype, this.evar);
  }

  @Override
  public void visit(ast.Ast.Exp.Not e) {
    e.exp.accept(this);
    String reg = Temp.next();
    emit(new Const(reg, 1));
    emit(new Xorint(this.evar, this.evar, reg));
  }

  @Override
  public void visit(ast.Ast.Exp.Num e) {
    this.evar = Temp.next();
    this.etype = new Type.Int();
    emitDec(this.type, this.evar);
    emit(new Const(this.evar, e.num));
  }

  @Override
  public void visit(ast.Ast.Exp.StringLiteral e) {
    // TODO
    System.err.println("Not support String yet.");
    System.exit(1);
  }

  @Override
  public void visit(ast.Ast.Exp.Sub e) {
    e.left.accept(this);
    String left = this.evar;
    e.right.accept(this);
    String right = this.evar;
    this.evar = Temp.next();
    this.etype = new Type.Int();
    emit(new Subint(this.evar, left, right));
  }

  @Override
  public void visit(ast.Ast.Exp.This e) {
    this.evar = "p0";
    this.etype = new Type.ClassType(classId);
  }

  @Override
  public void visit(ast.Ast.Exp.Times e) {
    e.left.accept(this);
    String left = this.evar;
    e.right.accept(this);
    String right = this.evar;
    this.evar = Temp.next();
    this.etype = new Type.Int();
    emit(new Mulint(this.evar, left, right));
  }

  @Override
  public void visit(ast.Ast.Exp.True e) {
    this.evar = Temp.next();
    this.etype = new Type.Int();
    emit(new Const(this.evar, 1));
  }

  // ///////////////////////////////////////////////////
  // statements
  @Override
  public void visit(ast.Ast.Stm.Assign s) {
    s.exp.accept(this);
    String right = this.evar;
    s.type.accept(this);
    Type.T ty = this.type;
    DecSingle ds = null;
    if (s.isField) {
      ds = classLookupTable.get(s.id);
      emit(new Iput(right, "p0", ds.type.desc()));
    } else {
      ds = methodLookupTable.get(s.id);
      if (ty instanceof Type.Int) {
        emit(new Move16(ds.reg, right));
      } else {
        emit(new Moveobject16(ds.reg, right));
      }
    }
  }

  @Override
  public void visit(ast.Ast.Stm.AssignArray s) {
    s.exp.accept(this);
    String valReg = this.evar;
    s.index.accept(this);
    String idxReg = this.evar;
    DecSingle ds;
    if (s.isField) {
      ds = classLookupTable.get(s.id);
      String reg = Temp.next();
      emit(new IGet(reg, "p0", String.format("L%s;->%s:[I", classId, s.id)));
      emit(new Aput(valReg, reg, idxReg));
    } else {
      ds = methodLookupTable.get(s.id);
      emit(new Aput(ds.reg, idxReg, valReg));
    }
  }

  @Override
  public void visit(ast.Ast.Stm.Block s) {
    s.stms.forEach(stm -> stm.accept(this));
  }

  @Override
  public void visit(ast.Ast.Stm.If s) {
    Label tl = new Label(), fl = new Label(), el = new Label();

    s.condition.accept(this);
    String evar = this.evar;
    emit(new Ifnez(evar, tl));
    emit(new LabelJ(fl));
    s.elsee.accept(this);
    emit(new Goto32(el));
    emit(new LabelJ(tl));
    s.thenn.accept(this);
    emit(new Goto32(el));
    emit(new LabelJ(el));
  }

  @Override
  public void visit(ast.Ast.Stm.Print s) {
    String newname = Temp.next();
    s.exp.accept(this);
    emit(new Print(newname, this.evar));
  }

  @Override
  public void visit(ast.Ast.Stm.While s) {
    Label cl = new Label(), el = new Label();

    emit(new LabelJ(cl));
    s.condition.accept(this);
    String condReg = this.evar;
    emit(new Ifeqz(condReg, el));
    s.body.accept(this);
    emit(new Goto32(cl));
    emit(new LabelJ(el));
  }

  // ////////////////////////////////////////////////////////
  // type
  @Override
  public void visit(ast.Ast.Type.Boolean t) {
    this.type = new Type.Int();
  }

  @Override
  public void visit(ast.Ast.Type.ClassType t) {
    this.type = new Type.ClassType(t.id);
  }

  @Override
  public void visit(ast.Ast.Type.Int t) {
    this.type = new Type.Int();
  }

  @Override
  public void visit(ast.Ast.Type.IntArray t) {
    this.type = new Type.IntArray();
  }

  // dec
  @Override
  public void visit(ast.Ast.Dec.DecSingle d) {
    d.type.accept(this);
    this.dec = new DecSingle(this.type, d.id);
  }

  // method
  @Override
  public void visit(ast.Ast.Method.MethodSingle m) {
    Temp.reset();
    methodLookupTable.clear();
    // There are two passes here:
    // In the 1st pass, the method is translated
    // into a three-address-code like intermediate representation.
    m.retType.accept(this);
    Type.T newRetType = this.type;
    LinkedList<Dec.T> newFormals = new LinkedList<>();
    int formalCnt = 0;
    for (ast.Ast.Dec.T d : m.formals) {
      d.accept(this);
      DecSingle ds = (DecSingle)this.dec;
      ds.setReg("p" + ++formalCnt);
      newFormals.add(ds);
      methodLookupTable.put(ds.id, ds);
    }
    LinkedList<Dec.T> locals = new LinkedList<>();
    for (ast.Ast.Dec.T d : m.locals) {
      d.accept(this);
      DecSingle ds = (DecSingle)this.dec;
      ds.setReg(Temp.next());
      locals.add(ds);
      methodLookupTable.put(ds.id, ds);
    }
    this.stms = new LinkedList<>();
    for (ast.Ast.Stm.T s : m.stms) {
      s.accept(this);
    }

    // return statement is specially treated
    m.retExp.accept(this);
    String retName = this.evar;

    if (m.retType.getNum() > 0)
      emit(new ReturnObject(retName));
    else
      emit(new Return(retName));
    // TODO
    // in the second pass, rename all method
    // parameters according to the "p"-convention; and
    // rename all method locals according to the "v"-convention.
    // cook the final method.
    this.method = new MethodSingle(newRetType, m.id, this.classId, newFormals,
        locals, this.stms, 0, 0, Temp.getCount());
  }

  // ///////////////////////////////////////////////////////////////
  // class
  @Override
  public void visit(ast.Ast.Class.ClassSingle c) {
    this.classId = c.id;
    LinkedList<Dec.T> newDecs = new LinkedList<>();
    for (ast.Ast.Dec.T dec : c.decs) {
      dec.accept(this);
      newDecs.add(this.dec);
      DecSingle ds = (DecSingle)this.dec;
      classLookupTable.put(ds.id, ds);
    }
    LinkedList<Method.T> newMethods = new LinkedList<>();
    for (ast.Ast.Method.T m : c.methods) {
      m.accept(this);
      newMethods.add(this.method);
    }
    this.clazz = new ClassSingle(c.id, c.extendss, newDecs, newMethods);
  }

  // /////////////////////////////////////////////////////////////////
  // main class
  @Override
  public void visit(ast.Ast.MainClass.MainClassSingle c) {
    Temp.reset();
    c.stm.accept(this);
    this.mainClass = new MainClassSingle(c.id, c.arg, this.stms, Temp.getCount());
    this.stms = new LinkedList<>();
  }

  // program
  @Override
  public void visit(ast.Ast.Program.ProgramSingle p) {
    // do translations
    p.mainClass.accept(this);

    LinkedList<Class.T> newClasses = new LinkedList<>();
    for (ast.Ast.Class.T classs : p.classes) {
      classs.accept(this);
      newClasses.add(this.clazz);
    }
    this.program = new ProgramSingle(this.mainClass, newClasses);
  }
}