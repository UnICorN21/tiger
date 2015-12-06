package cfg;

import cfg.Cfg.*;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Str;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm.*;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;
import codegen.C.Ast;
import util.Label;

import java.util.ArrayList;
import java.util.LinkedList;

// Traverse the C AST, and generate
// a control-flow graph.
public class TranslateVisitor implements codegen.C.Visitor {
  private String classId;
  private Type.T type; // type after translation
  private Operand.T operand;
  private Dec.T dec;
  // A dirty hack. Can hold stm, transfer, or label.
  private ArrayList<Object> stmOrTransfer;
  private util.Label entry;
  private LinkedList<Dec.T> newLocals;
  private Method.T method;
  private Class.T classs;
  private Vtable.T vtable;
  private MainMethod.T mainMethod;
  public Program.T program;

  public TranslateVisitor() {
    this.classId = null;
    this.type = null;
    this.dec = null;
    this.stmOrTransfer = new java.util.ArrayList<>();
    this.newLocals = new LinkedList<>();
    this.method = null;
    this.classs = null;
    this.vtable = null;
    this.mainMethod = null;
    this.program = null;
  }

  // /////////////////////////////////////////////////////
  // utility functions
  private java.util.LinkedList<Block.T> cookBlocks() {
    java.util.LinkedList<Block.T> blocks = new java.util.LinkedList<>();

    int i = 0;
    int size = this.stmOrTransfer.size();
    while (i < size) {
      util.Label label;
      BlockSingle b;
      LinkedList<Stm.T> stms = new LinkedList<>();
      Transfer.T transfer;

      if (!(this.stmOrTransfer.get(i) instanceof util.Label)) {
        new util.Bug();
      }
      label = (util.Label) this.stmOrTransfer.get(i++);
      while (i < size && this.stmOrTransfer.get(i) instanceof Stm.T) {
        stms.add((Stm.T) this.stmOrTransfer.get(i++));
      }

      if (this.stmOrTransfer.get(i) instanceof Transfer.T) {
        transfer = (Transfer.T) this.stmOrTransfer.get(i++);
      } else {
        Label wsl = (Label)this.stmOrTransfer.get(i);
        transfer = new Goto(wsl);
      }
      b = new BlockSingle(label, stms, transfer);
      blocks.add(b);
    }
    this.stmOrTransfer = new java.util.ArrayList<>();
    return blocks;
  }

  private void emit(Object obj) {
    this.stmOrTransfer.add(obj);
  }

  private String genVar() {
    String fresh = util.Temp.next();
    DecSingle dec = new DecSingle(new IntType(), fresh);
    this.newLocals.add(dec);
    return fresh;
  }

  private String genVar(Type.T ty) {
    String fresh = util.Temp.next();
    DecSingle dec = new DecSingle(ty, fresh);
    this.newLocals.add(dec);
    return fresh;
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Ast.Exp.Add e) {
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    Operand.T right = this.operand;
    String dst = genVar();
    emit(new Add(dst, new cfg.Cfg.Type.IntType(), left, right));
    this.operand = new Operand.Var(dst);
  }

  @Override
  public void visit(Ast.Exp.And e) {
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    Operand.T right = this.operand;
    String dst = genVar();
    emit(new Stm.And(dst, new Type.IntType(), left, right));
    this.operand = new Operand.Var(dst);
  }

  @Override
  public void visit(Ast.Exp.ArraySelect e) {
    e.array.accept(this);
    Operand.T array = this.operand;
    e.index.accept(this);
    Operand.T index = this.operand;
    String dst = genVar();
    emit(new Stm.ArraySelect(array, dst, index, new Type.IntType()));
    this.operand = new Operand.Var(dst);
  }

  @Override
  public void visit(Ast.Exp.Call e) {
    e.retType.accept(this);
    String dst = genVar(this.type);
    String obj = null;
    e.exp.accept(this);
    Operand.T objOp = this.operand;
    if (objOp instanceof Var) {
      Var var = (Var) objOp;
      obj = var.id;
    } else {
      new util.Bug();
    }

    LinkedList<Operand.T> newArgs = new LinkedList<>();
    for (codegen.C.Ast.Exp.T x : e.args) {
      x.accept(this);
      newArgs.add(this.operand);
    }
    emit(new InvokeVirtual(dst, obj, e.id, newArgs));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.Id e) {
    this.operand = new Var(e.id);
  }

  @Override
  public void visit(Ast.Exp.Length e) {
    e.array.accept(this);
    Operand.T array = this.operand;
    String dst = genVar();
    emit(new Length(array, dst, new Type.IntType()));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.Lt e) {
    String dst = genVar();
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    emit(new Lt(dst, null, left, this.operand));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.Gt e) {
    String dst = genVar();
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    emit(new Gt(dst, null, left, this.operand));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.NewIntArray e) {
    e.exp.accept(this);
    Operand.T length = this.operand;
    emit(new NewIntArray(e.name, length));
    this.operand = new Var(e.name);
  }

  @Override
  public void visit(Ast.Exp.NewObject e) {
    String dst = genVar(new ClassType(e.classType));
    emit(new NewObject(dst, e.classType));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.Not e) {
  }

  @Override
  public void visit(Ast.Exp.Num e) {
    this.operand = new Int(e.num);
  }

  @Override
  public void visit(Ast.Exp.StringLiteral e) {
    this.operand = new Str(e.literal);
  }

  @Override
  public void visit(Ast.Exp.Sub e) {
    String dst = genVar();
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    emit(new Sub(dst, null, left, this.operand));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.This e) {
    this.operand = new Var("this");
  }

  @Override
  public void visit(Ast.Exp.Times e) {
    String dst = genVar();
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    emit(new Times(dst, null, left, this.operand));
    this.operand = new Var(dst);
  }

  // statements
  @Override
  public void visit(codegen.C.Ast.Stm.Assign s) {
    s.exp.accept(this);
    emit(new Move(s.id, null, this.operand));
  }

  @Override
  public void visit(codegen.C.Ast.Stm.AssignArray s) {
    s.index.accept(this);
    Operand.T index = this.operand;
    s.exp.accept(this);
    Operand.T exp = this.operand;
    emit(new AssignArray(s.id, null, index, exp));
  }

  @Override
  public void visit(codegen.C.Ast.Stm.Block s) {
    s.stms.stream().forEach(stm -> stm.accept(this));
  }

  @Override
  public void visit(codegen.C.Ast.Stm.If s) {
    util.Label tl = new util.Label(), fl = new util.Label(), el = new util.Label();
    s.condition.accept(this);
    emit(new If(this.operand, tl, fl));
    emit(fl);
    s.elsee.accept(this);
    emit(new Goto(el));
    emit(tl);
    s.thenn.accept(this);
    emit(new Goto(el));
    emit(el);
  }

  @Override
  public void visit(codegen.C.Ast.Stm.Print s) {
    s.exp.accept(this);
    emit(new Print(this.operand));
  }

  @Override
  public void visit(codegen.C.Ast.Stm.While s) {
    util.Label wl = new util.Label(), bsl = new util.Label(), wel = new util.Label();
    emit(wl);
    s.condition.accept(this);
    emit(new If(this.operand, bsl, wel));
    emit(bsl);
    s.body.accept(this);
    emit(new Goto(wl));
    emit(wel);
  }

  // type
  @Override
  public void visit(codegen.C.Ast.Type.ClassType t) {
    this.type = new ClassType(t.id);
  }

  @Override
  public void visit(codegen.C.Ast.Type.Int t) {
    this.type = new IntType();
  }

  @Override
  public void visit(codegen.C.Ast.Type.IntArray t) { this.type = new Type.IntArrayType(); }

  @Override
  public void visit(Ast.Type.StringType t) { this.type = new Type.StringType(); }

  // dec
  @Override
  public void visit(codegen.C.Ast.Dec.DecSingle d) {
    d.type.accept(this);
    this.dec = new DecSingle(this.type, d.id);
  }

  // vtable
  @Override
  public void visit(codegen.C.Ast.Vtable.VtableSingle v) {
    java.util.LinkedList<cfg.Ftuple> newTuples = new java.util.LinkedList<>();
    for (codegen.C.Ftuple t : v.ms) {
      t.ret.accept(this);
      Type.T ret = this.type;
      java.util.LinkedList<Dec.T> args = new java.util.LinkedList<>();
      for (codegen.C.Ast.Dec.T dec : t.args) {
        dec.accept(this);
        args.add(this.dec);
      }
      newTuples.add(new cfg.Ftuple(t.classs, ret, args, t.id));
    }
    this.vtable = new VtableSingle(v.id, v.gcMap, newTuples);
  }

  // class
  @Override
  public void visit(codegen.C.Ast.Class.ClassSingle c) {
    java.util.LinkedList<cfg.Tuple> newTuples = new java.util.LinkedList<cfg.Tuple>();
    for (codegen.C.Tuple t : c.decs) {
      t.type.accept(this);
      newTuples.add(new cfg.Tuple(t.classs, this.type, t.id));
    }
    this.classs = new ClassSingle(c.id, newTuples);
  }

  // method
  @Override
  public void visit(codegen.C.Ast.Method.MethodSingle m) {
    util.Label.reset();
    util.Temp.reset();
    this.newLocals = new java.util.LinkedList<>();

    m.retType.accept(this);
    Type.T retType = this.type;

    LinkedList<Dec.T> newFormals = new LinkedList<>();
    for (codegen.C.Ast.Dec.T c : m.formals) {
      c.accept(this);
      newFormals.add(this.dec);
    }

    LinkedList<Dec.T> locals = new LinkedList<>();
    for (codegen.C.Ast.Dec.T c : m.locals) {
      c.accept(this);
      locals.add(this.dec);
    }

    // a junk label
    util.Label entry = new util.Label();
    this.entry = entry;
    emit(entry);

    for (codegen.C.Ast.Stm.T s : m.stms)
      s.accept(this);

    m.retExp.accept(this);
    emit(new Return(this.operand));

    LinkedList<Block.T> blocks = cookBlocks();

    for (Dec.T d : this.newLocals)
      locals.add(d);

    this.method = new MethodSingle(retType, m.id, m.classId, newFormals,
        locals, blocks, entry, null, null);
  }

  // main method
  @Override
  public void visit(codegen.C.Ast.MainMethod.MainMethodSingle m) {
    util.Label.reset();
    util.Temp.reset();
    this.newLocals = new java.util.LinkedList<>();

    java.util.LinkedList<Dec.T> locals = new LinkedList<>();
    for (codegen.C.Ast.Dec.T c : m.locals) {
      c.accept(this);
      locals.add(this.dec);
    }

    util.Label entry = new util.Label();
    emit(entry);

    m.stm.accept(this);

    emit(new Transfer.Return(new Operand.Int(0)));

    java.util.LinkedList<Block.T> blocks = cookBlocks();
    for (Dec.T d : this.newLocals)
      locals.add(d);
    this.mainMethod = new MainMethodSingle(locals, blocks);
  }

  // program
  @Override
  public void visit(codegen.C.Ast.Program.ProgramSingle p) {
    java.util.LinkedList<Class.T> newClasses = new LinkedList<>();
    for (codegen.C.Ast.Class.T c : p.classes) {
      c.accept(this);
      newClasses.add(this.classs);
    }

    java.util.LinkedList<Vtable.T> newVtable = new LinkedList<>();
    for (codegen.C.Ast.Vtable.T v : p.vtables) {
      v.accept(this);
      newVtable.add(this.vtable);
    }

    LinkedList<Method.T> newMethods = new LinkedList<>();
    for (codegen.C.Ast.Method.T m : p.methods) {
      m.accept(this);
      newMethods.add(this.method);
    }

    p.mainMethod.accept(this);
    MainMethod.T newMainMethod = this.mainMethod;

    this.program = new ProgramSingle(newClasses, newVtable, newMethods,
        newMainMethod);
  }
}
