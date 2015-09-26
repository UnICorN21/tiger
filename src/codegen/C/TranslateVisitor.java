package codegen.C;

import codegen.C.Ast.Class;
import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.*;
import codegen.C.Ast.Exp.*;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Vtable.VtableSingle;

import java.util.LinkedList;

// Given a Java ast, translate it into a C ast and outputs it.

public class TranslateVisitor implements ast.Visitor {
  private ClassTable table;
  private String classId;
  private Type.T type; // type after translation
  private Dec.T dec;
  private Stm.T stm;
  private Exp.T exp;
  private Method.T method;
  private LinkedList<Dec.T> tmpVars;
  private LinkedList<Class.T> classes;
  private LinkedList<Vtable.T> vtables;
  private LinkedList<Method.T> methods;
  private MainMethod.T mainMethod;
  public Program.T program;

  public TranslateVisitor() {
    this.table = new ClassTable();
    this.classId = null;
    this.type = null;
    this.dec = null;
    this.stm = null;
    this.exp = null;
    this.method = null;
    this.classes = new LinkedList<>();
    this.vtables = new LinkedList<>();
    this.methods = new LinkedList<>();
    this.mainMethod = null;
    this.program = null;
  }

  // //////////////////////////////////////////////////////
  //
  public String genId() {
    return util.Temp.next();
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.Ast.Exp.Add e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Exp.Add(left, right);
  }

  @Override
  public void visit(ast.Ast.Exp.And e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Exp.Add(left, right);
  }

  @Override
  public void visit(ast.Ast.Exp.ArraySelect e) {
    e.array.accept(this);
    Exp.T array = this.exp;
    e.index.accept(this);
    Exp.T index = this.exp;
    this.exp = new Exp.ArraySelect(array, index);
  }

  @Override
  public void visit(ast.Ast.Exp.Call e) {
    e.exp.accept(this);
    String newid = this.genId();
    this.tmpVars.add(new Dec.DecSingle(new Type.ClassType(e.type), newid));
    Exp.T exp = this.exp;
    LinkedList<Exp.T> args = new LinkedList<>();
    for (ast.Ast.Exp.T x : e.args) {
      x.accept(this);
      args.add(this.exp);
    }
    this.exp = new Call(newid, exp, e.id, args);
  }

  @Override
  public void visit(ast.Ast.Exp.False e) {
    this.exp = new Num(0);
  }

  @Override
  public void visit(ast.Ast.Exp.Id e) {
    if (e.isField) this.exp = new Id(String.format("this->%s", e.id));
    else this.exp = new Id(e.id);
  }

  @Override
  public void visit(ast.Ast.Exp.Length e) {
    e.array.accept(this);
    this.exp = new Exp.Length(this.exp);
  }

  @Override
  public void visit(ast.Ast.Exp.Lt e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Lt(left, right);
  }

  @Override
  public void visit(ast.Ast.Exp.Gt e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Gt(left, right);
  }

  @Override
  public void visit(ast.Ast.Exp.NewIntArray e) {
    e.exp.accept(this);
    this.exp = new Exp.NewIntArray(this.exp);
  }

  @Override
  public void visit(ast.Ast.Exp.NewObject e) {
    this.exp = new NewObject(e.id);
  }

  @Override
  public void visit(ast.Ast.Exp.Not e) {
    e.exp.accept(this);
    this.exp = new Exp.Not(this.exp);
  }

  @Override
  public void visit(ast.Ast.Exp.Num e) {
    this.exp = new Num(e.num);
  }

  @Override
  public void visit(ast.Ast.Exp.Sub e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Sub(left, right);
  }

  @Override
  public void visit(ast.Ast.Exp.This e) {
    this.exp = new This();
  }

  @Override
  public void visit(ast.Ast.Exp.Times e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Times(left, right);
  }

  @Override
  public void visit(ast.Ast.Exp.True e) {
    this.exp = new Num(1);
  }

  // //////////////////////////////////////////////
  // statements
  @Override
  public void visit(ast.Ast.Stm.Assign s) {
    s.exp.accept(this);
    this.stm = new Assign(s.isField ? "this->" + s.id: s.id, this.exp);
  }

  @Override
  public void visit(ast.Ast.Stm.AssignArray s) {
    s.exp.accept(this);
    Exp.T exp = this.exp;
    s.index.accept(this);
    Exp.T index = this.exp;
    this.stm = new Stm.AssignArray(s.isField ? "this->" + s.id : s.id, index, exp);
  }

  @Override
  public void visit(ast.Ast.Stm.Block s) {
    LinkedList<Stm.T> stms = new LinkedList<>();
    s.stms.stream().forEach(stm -> {
      stm.accept(this);
      stms.add(this.stm);
    });
    this.stm = new Stm.Block(stms);
  }

  @Override
  public void visit(ast.Ast.Stm.If s) {
    s.condition.accept(this);
    Exp.T condition = this.exp;
    s.thenn.accept(this);
    Stm.T thenn = this.stm;
    s.elsee.accept(this);
    Stm.T elsee = this.stm;
    this.stm = new If(condition, thenn, elsee);
  }

  @Override
  public void visit(ast.Ast.Stm.Print s) {
    s.exp.accept(this);
    this.stm = new Print(this.exp);
  }

  @Override
  public void visit(ast.Ast.Stm.While s) {
    s.condition.accept(this);
    Exp.T cond = this.exp;
    s.body.accept(this);
    Stm.T body = this.stm;
    this.stm = new Stm.While(cond, body);
  }

  // ///////////////////////////////////////////
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

  // ////////////////////////////////////////////////
  // dec
  @Override
  public void visit(ast.Ast.Dec.DecSingle d) {
    d.type.accept(this);
    this.dec = new codegen.C.Ast.Dec.DecSingle(this.type, d.id);
  }

  // method
  @Override
  public void visit(ast.Ast.Method.MethodSingle m) {
    this.tmpVars = new LinkedList<>();
    m.retType.accept(this);
    Type.T newRetType = this.type;
    LinkedList<Dec.T> newFormals = new LinkedList<>();
    newFormals.add(new Dec.DecSingle(
        new ClassType(this.classId), "this"));
    for (ast.Ast.Dec.T d : m.formals) {
      d.accept(this);
      newFormals.add(this.dec);
    }
    LinkedList<Dec.T> locals = new LinkedList<>();
    for (ast.Ast.Dec.T d : m.locals) {
      d.accept(this);
      locals.add(this.dec);
    }
    LinkedList<Stm.T> newStm = new LinkedList<>();
    for (ast.Ast.Stm.T s : m.stms) {
      s.accept(this);
      newStm.add(this.stm);
    }
    this.tmpVars.stream().forEach(locals::add);
    m.retExp.accept(this);
    Exp.T retExp = this.exp;
    this.method = new MethodSingle(newRetType, this.classId, m.id,
        newFormals, locals, newStm, retExp);
  }

  // class
  @Override
  public void visit(ast.Ast.Class.ClassSingle c) {
    ClassBinding cb = this.table.get(c.id);
    this.classes.add(new ClassSingle(c.id, cb.fields));
    this.vtables.add(new VtableSingle(c.id, cb.methods));
    this.classId = c.id;
    c.methods.stream().forEach(m -> {
      m.accept(this);
      this.methods.add(this.method);
    });
  }

  // main class
  @Override
  public void visit(ast.Ast.MainClass.MainClassSingle c) {
    ClassBinding cb = this.table.get(c.id);
    Class.T newc = new ClassSingle(c.id, cb.fields);
    this.classes.add(newc);
    this.vtables.add(new VtableSingle(c.id, cb.methods));

    this.tmpVars = new LinkedList<>();

    c.stm.accept(this);
    this.mainMethod = new MainMethodSingle(
        this.tmpVars, this.stm);
  }

  // /////////////////////////////////////////////////////
  // the first pass
  public void scanMain(ast.Ast.MainClass.T m) {
    this.table.init(((ast.Ast.MainClass.MainClassSingle) m).id, null);
    // this is a special hacking in that we don't want to
    // enter "main" into the table.
  }

  public void scanClasses(LinkedList<ast.Ast.Class.T> cs) {
    // put empty chuncks into the table
    for (ast.Ast.Class.T c : cs) {
      ast.Ast.Class.ClassSingle cc = (ast.Ast.Class.ClassSingle) c;
      this.table.init(cc.id, cc.extendss);
    }

    // put class fields and methods into the table
    for (ast.Ast.Class.T c : cs) {
      ast.Ast.Class.ClassSingle cc = (ast.Ast.Class.ClassSingle) c;
      LinkedList<Dec.T> newDecs = new LinkedList<>();
      for (ast.Ast.Dec.T dec : cc.decs) {
        dec.accept(this);
        newDecs.add(this.dec);
      }
      this.table.initDecs(cc.id, newDecs);

      // all methods
      java.util.LinkedList<ast.Ast.Method.T> methods = cc.methods;
      for (ast.Ast.Method.T mthd : methods) {
        ast.Ast.Method.MethodSingle m = (ast.Ast.Method.MethodSingle) mthd;
        LinkedList<Dec.T> newArgs = new LinkedList<>();
        for (ast.Ast.Dec.T arg : m.formals) {
          arg.accept(this);
          newArgs.add(this.dec);
        }
        m.retType.accept(this);
        Type.T newRet = this.type;
        this.table.initMethod(cc.id, newRet, newArgs, m.id);
      }
    }

    // calculate all inheritance information
    for (ast.Ast.Class.T c : cs) {
      ast.Ast.Class.ClassSingle cc = (ast.Ast.Class.ClassSingle) c;
      this.table.inherit(cc.id);
    }
  }

  public void scanProgram(ast.Ast.Program.T p) {
    ast.Ast.Program.ProgramSingle pp = (ast.Ast.Program.ProgramSingle) p;
    scanMain(pp.mainClass);
    scanClasses(pp.classes);
  }

  // end of the first pass
  // ////////////////////////////////////////////////////

  // program
  @Override
  public void visit(ast.Ast.Program.ProgramSingle p) {
    // The first pass is to scan the whole program "p", and
    // to collect all information of inheritance.
    scanProgram(p);

    // do translations
    p.mainClass.accept(this);
    for (ast.Ast.Class.T classs : p.classes) {
      classs.accept(this);
    }
    this.program = new ProgramSingle(this.classes, this.vtables,
        this.methods, this.mainMethod);
  }
}
