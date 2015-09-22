package codegen.bytecode;

import codegen.bytecode.Ast.Class.ClassSingle;
import codegen.bytecode.Ast.Dec.DecSingle;
import codegen.bytecode.Ast.MainClass.MainClassSingle;
import codegen.bytecode.Ast.Method.MethodSingle;
import codegen.bytecode.Ast.Program.ProgramSingle;
import codegen.bytecode.Ast.Stm.*;
import codegen.bytecode.Ast.Type.ClassType;
import codegen.bytecode.Ast.Type.Int;
import codegen.bytecode.Ast.Type.IntArray;

public interface Visitor {
  // statements
  void visit(This s);

  void visit(Aload s);

  void visit(Areturn s);

  void visit(Astore s);

  void visit(False s);

  void visit(True s);

  void visit(Goto s);

  void visit(Ificmplt s);

  void visit(Ificmpgt s);

  void visit(Iflt s);

  void visit(Ifgt s);

  void visit(Ifne s);

  void visit(Ifeq s);

  void visit(Iload s);

  void visit(Imul s);

  void visit(IAnd s);

  void visit(Ireturn s);

  void visit(Istore s);

  void visit(IAdd s);

  void visit(Isub s);

  void visit(Invokevirtual s);

  void visit(LabelJ s);

  void visit(Ldc s);

  void visit(Print s);

  void visit(New s);

  void visit(NewArray s);

  void visit(ArrayLength s);

  void visit(PutField s);

  void visit(GetField s);

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

  // debug
  void visit(Debug.Line l);

  void visit(Debug.Comment c);
}
