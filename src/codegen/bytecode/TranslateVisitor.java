package codegen.bytecode;

import codegen.bytecode.Ast.Class;
import codegen.bytecode.Ast.Class.ClassSingle;
import codegen.bytecode.Ast.*;
import codegen.bytecode.Ast.Dec.DecSingle;
import codegen.bytecode.Ast.MainClass.MainClassSingle;
import codegen.bytecode.Ast.Method.MethodSingle;
import codegen.bytecode.Ast.Program.ProgramSingle;
import codegen.bytecode.Ast.Stm.*;
import codegen.bytecode.Ast.Type.Int;
import util.Label;

import java.util.Hashtable;
import java.util.LinkedList;

// Given a Java ast, translate it into Java bytecode.
public class TranslateVisitor implements ast.Visitor {
  private String classId;
  private int index;
  private Hashtable<String, Integer> indexTable;
  private Type.T type; // type after translation
  private Dec.T dec;
  private LinkedList<Stm.T> stms;
  private Method.T method;
  private Class.T classs;
  private MainClass.T mainClass;
  public Program.T program;

  public TranslateVisitor() {
    this.classId = null;
    this.indexTable = null;
    this.type = null;
    this.dec = null;
    this.stms = new LinkedList<>();
    this.method = null;
    this.classs = null;
    this.mainClass = null;
    this.program = null;
  }

  private void emit(Stm.T s) {
    this.stms.add(s);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.Ast.Exp.Add e) {
    e.left.accept(this);
    e.right.accept(this);
    emit(new IAdd());
  }

  @Override
  public void visit(ast.Ast.Exp.And e) {
    e.left.accept(this);
    e.right.accept(this);
    emit(new IAnd());
  }

  @Override
  public void visit(ast.Ast.Exp.ArraySelect e) {
    // push arrayref, index into the operand stack
    e.array.accept(this);
    e.index.accept(this);
  }

  @Override
  public void visit(ast.Ast.Exp.Call e) {
    e.exp.accept(this);
    for (ast.Ast.Exp.T x : e.args) {
      x.accept(this);
    }
    e.rt.accept(this);
    Type.T rt = this.type;
    LinkedList<Type.T> at = new LinkedList<>();
    for (ast.Ast.Type.T t : e.at) {
      t.accept(this);
      at.add(this.type);
    }
    emit(new Invokevirtual(e.id, e.type, at, rt));
  }

  @Override
  public void visit(ast.Ast.Exp.False e) {
    emit(new False());
  }

  @Override
  public void visit(ast.Ast.Exp.Id e) {
    int index = this.indexTable.get(e.id);
    ast.Ast.Type.T type = e.type;
    if (type.getNum() > 0)// a reference
      emit(new Aload(index));
    else
      emit(new Iload(index));
    // but what about this is a field?
  }

  @Override
  public void visit(ast.Ast.Exp.Length e) {
    e.array.accept(this);
    emit(new ArrayLength());
  }

  @Override
  public void visit(ast.Ast.Exp.Lt e) {
    Label tl = new Label(), fl = new Label(), el = new Label();
    e.left.accept(this);
    e.right.accept(this);
    emit(new Ificmplt(tl));
    emit(new LabelJ(fl));
    emit(new False());
    emit(new Goto(el));
    emit(new LabelJ(tl));
    emit(new True());
    emit(new Goto(el));
    emit(new LabelJ(el));
  }

  @Override
  public void visit(ast.Ast.Exp.NewIntArray e) {
    e.exp.accept(this);
    emit(new NewArray());
  }

  @Override
  public void visit(ast.Ast.Exp.NewObject e) {
    emit(new New(e.id));
  }

  @Override
  public void visit(ast.Ast.Exp.Not e) {
  }

  @Override
  public void visit(ast.Ast.Exp.Num e) {
    emit(new Ldc(e.num));
  }

  @Override
  public void visit(ast.Ast.Exp.Sub e) {
    e.left.accept(this);
    e.right.accept(this);
    emit(new Isub());
  }

  @Override
  public void visit(ast.Ast.Exp.This e) {
    emit(new Aload(0));
  }

  @Override
  public void visit(ast.Ast.Exp.Times e) {
    e.left.accept(this);
    e.right.accept(this);
    emit(new Imul());
  }

  @Override
  public void visit(ast.Ast.Exp.True e) {
    emit(new True());
  }

  // ///////////////////////////////////////////////////
  // statements
  @Override
  public void visit(ast.Ast.Stm.Assign s) {
    emit(new Debug.Line(s.exp.pos.lineRow));
    s.exp.accept(this);
    int index = this.indexTable.get(s.id);
    ast.Ast.Type.T type = s.type;
    if (type.getNum() > 0)
      emit(new Astore(index));
    else
      emit(new Istore(index));
  }

  @Override
  public void visit(ast.Ast.Stm.AssignArray s) {
    emit(new Debug.Line(s.exp.pos.lineRow));
    // TODO
  }

  @Override
  public void visit(ast.Ast.Stm.Block s) {
    // TODO
  }

  @Override
  public void visit(ast.Ast.Stm.If s) {
    emit(new Debug.Line(s.condition.pos.lineRow));
    Label tl = new Label(), fl = new Label(), el = new Label();
    s.condition.accept(this);

    emit(new Ifne(tl));
    emit(new LabelJ(fl));
    s.elsee.accept(this);
    emit(new Goto(el));
    emit(new LabelJ(tl));
    s.thenn.accept(this);
    emit(new Goto(el));
    emit(new LabelJ(el));
  }

  @Override
  public void visit(ast.Ast.Stm.Print s) {
    emit(new Debug.Line(s.exp.pos.lineRow));
    s.exp.accept(this);
    emit(new Print());
  }

  @Override
  public void visit(ast.Ast.Stm.While s) {
    emit(new Debug.Line(s.condition.pos.lineRow));
    // TODO
  }

  // type
  @Override
  public void visit(ast.Ast.Type.Boolean t) {
    this.type = new Int();
  }

  @Override
  public void visit(ast.Ast.Type.ClassType t) {
    this.type = new Type.ClassType(t.id);
  }

  @Override
  public void visit(ast.Ast.Type.Int t) {
    this.type = new Int();
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
    this.indexTable.put(d.id, index++);
  }

  // method
  @Override
  public void visit(ast.Ast.Method.MethodSingle m) {
    // record, in a hash table, each var's index
    // this index will be used in the load store operation
    this.index = 1;
    this.indexTable = new Hashtable<>();

    m.retType.accept(this);
    Type.T newRetType = this.type;
    LinkedList<Dec.T> newFormals = new LinkedList<>();
    for (ast.Ast.Dec.T d : m.formals) {
      d.accept(this);
      newFormals.add(this.dec);
    }
    LinkedList<Dec.T> locals = new java.util.LinkedList<>();
    for (ast.Ast.Dec.T d : m.locals) {
      d.accept(this);
      locals.add(this.dec);
    }
    this.stms = new LinkedList<Stm.T>();
    for (ast.Ast.Stm.T s : m.stms) {
      s.accept(this);
    }

    // return statement is specially treated
    m.retExp.accept(this);

    if (m.retType.getNum() > 0)
      emit(new Areturn());
    else
      emit(new Ireturn());

    this.method = new MethodSingle(newRetType, m.id, this.classId, newFormals,
        locals, this.stms, 0, this.index);
  }

  // class
  @Override
  public void visit(ast.Ast.Class.ClassSingle c) {
    this.classId = c.id;
    LinkedList<Dec.T> newDecs = new LinkedList<>();
    for (ast.Ast.Dec.T dec : c.decs) {
      dec.accept(this);
      newDecs.add(this.dec);
    }
    LinkedList<Method.T> newMethods = new LinkedList<>();
    for (ast.Ast.Method.T m : c.methods) {
      m.accept(this);
      newMethods.add(this.method);
    }
    this.classs = new ClassSingle(c.id, c.extendss, newDecs, newMethods);
  }

  // main class
  @Override
  public void visit(ast.Ast.MainClass.MainClassSingle c) {
    c.stm.accept(this);
    this.mainClass = new MainClassSingle(c.id, c.arg, this.stms);
    this.stms = new LinkedList<>();
  }

  // program
  @Override
  public void visit(ast.Ast.Program.ProgramSingle p) {
    // do translations
    p.mainClass.accept(this);

    LinkedList<Class.T> newClasses = new LinkedList<>();
    for (ast.Ast.Class.T classes : p.classes) {
      classes.accept(this);
      newClasses.add(this.classs);
    }
    this.program = new ProgramSingle(this.mainClass, newClasses);
  }
}
