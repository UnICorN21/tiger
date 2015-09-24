package codegen.dalvik;

import codegen.dalvik.Ast.Class.ClassSingle;
import codegen.dalvik.Ast.Dec.DecSingle;
import codegen.dalvik.Ast.MainClass.MainClassSingle;
import codegen.dalvik.Ast.Method.MethodSingle;
import codegen.dalvik.Ast.Program.ProgramSingle;
import codegen.dalvik.Ast.Stm.*;
import codegen.dalvik.Ast.Type.ClassType;
import codegen.dalvik.Ast.Type.Int;
import codegen.dalvik.Ast.Type.IntArray;

public interface Visitor {
  // statements
  void visit(ReturnObject s);

  void visit(Goto32 s);

  void visit(Iflt s);

  void visit(Ifne s);
  
  void visit(Ifnez s);

  void visit(Mulint s);

  void visit(Return s);

  void visit(Subint s);

  void visit(Invokevirtual s);

  void visit(LabelJ s);
  
  void visit(Move16 s);
  
  void visit(Moveobject16 s);

  void visit(Const s);

  void visit(Print s);

  void visit(NewInstance s);

  // type
  void visit(ClassType t);

  void visit(Int t);

  void visit(IntArray t);

  // dec
  void visit(DecSingle d);

  // method
  void visit(MethodSingle m);

  // class
  void visit(ClassSingle c);

  // main class
  void visit(MainClassSingle c);

  // program
  void visit(ProgramSingle p);
}
