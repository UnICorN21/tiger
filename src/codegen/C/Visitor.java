package codegen.C;

import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Exp.*;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm.*;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable.VtableSingle;

public interface Visitor {
  // expressions
  void visit(Add e);

  void visit(And e);

  void visit(ArraySelect e);

  void visit(Call e);

  void visit(Id e);

  void visit(Length e);

  void visit(Lt e);

  void visit(NewIntArray e);

  void visit(NewObject e);

  void visit(Not e);

  void visit(Num e);

  void visit(Sub e);

  void visit(This e);

  void visit(Times e);

  // statements
  void visit(Assign s);

  void visit(AssignArray s);

  void visit(Block s);

  void visit(If s);

  void visit(Print s);

  void visit(While s);

  // type
  void visit(ClassType t);

  void visit(Int t);

  void visit(IntArray t);

  // dec
  void visit(DecSingle d);

  // method
  void visit(MethodSingle m);

  // main method
  void visit(MainMethodSingle m);

  // vtable
  void visit(VtableSingle v);

  // class
  void visit(ClassSingle c);

  // program
  void visit(ProgramSingle p);
}
