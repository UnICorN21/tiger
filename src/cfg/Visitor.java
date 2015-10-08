package cfg;

import cfg.Cfg.Block.BlockSingle;
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
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;

public interface Visitor {
  // operand
  default void visit(Int o) { /* null */ }

  default void visit(Var o) { /* null */ }

  // type
  default void visit(ClassType t) { /* null */ }

  default void visit(IntType t) { /* null */ }

  default void visit(IntArrayType t) { /* null */ }

  // dec
  default void visit(DecSingle d) { /* null */ }

  // transfer
  default void visit(If t) { /* null */ }

  default void visit(Goto t) { /* null */ }

  default void visit(Return t) { /* null */ }

  // statement:
  default void visit(Add m) { /* null */ }

  default void visit(Cfg.Stm.And s) { /* null */ }

  default void visit(Cfg.Stm.ArraySelect s) { /* null */ }

  default void visit(Cfg.Stm.Length s) { /* null */ }

  default void visit(InvokeVirtual m) { /* null */ }

  default void visit(Lt s) { /* null */ }

  default void visit(Gt s) { /* null */ }

  default void visit(AssignArray s) { /* null */ }

  default void visit(Move m) { /* null */ }
  
  default void visit(NewObject m) { /* null */ }

  default void visit(Cfg.Stm.NewIntArray m) { /* null */ }
  
  default void visit(Print m) { /* null */ }

  default void visit(Sub m) { /* null */ }

  default void visit(Times m) { /* null */ }

  // block
  default void visit(BlockSingle b) { /* null */ }

  // vtable
  default void visit(VtableSingle v) { /* null */ }

  // class
  default void visit(ClassSingle c) { /* null */ }

  // method
  void visit(MethodSingle m);

  // main method
  void visit(MainMethodSingle c);

  // program
  void visit(ProgramSingle p);
}
