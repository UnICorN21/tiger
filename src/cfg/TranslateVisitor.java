package cfg;

import cfg.Cfg.*;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand.Int;
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
      transfer = (Transfer.T) this.stmOrTransfer.get(i++);
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
  public void visit(codegen.C.Ast.Exp.Add e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.And e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.ArraySelect e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Call e) {
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
  public void visit(codegen.C.Ast.Exp.Id e) {
    this.operand = new Var(e.id);
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Length e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Lt e) {
    String dst = genVar();
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    emit(new Lt(dst, null, left, this.operand));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(Ast.Exp.Gt e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.NewIntArray e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.NewObject e) {
    String dst = genVar(new ClassType(e.classType));
    emit(new NewObject(dst, e.classType));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Not e) {
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Num e) {
    this.operand = new Int(e.num);
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Sub e) {
    String dst = genVar();
    e.left.accept(this);
    Operand.T left = this.operand;
    e.right.accept(this);
    emit(new Sub(dst, null, left, this.operand));
    this.operand = new Var(dst);
  }

  @Override
  public void visit(codegen.C.Ast.Exp.This e) {
    this.operand = new Var("this");
  }

  @Override
  public void visit(codegen.C.Ast.Exp.Times e) {
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
  }

  @Override
  public void visit(codegen.C.Ast.Stm.Block s) {
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
  public void visit(codegen.C.Ast.Type.IntArray t) {
  }

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
    this.vtable = new VtableSingle(v.id, newTuples);
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
