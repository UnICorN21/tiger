package cfg;

import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm.*;
import cfg.Cfg.Transfer;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;

import java.util.HashMap;

public class VisualVisitor implements Visitor {
  public StringBuffer strb;

  public VisualVisitor() {
    this.strb = new StringBuffer();
  }

  // ///////////////////////////////////////////////////
  private void emit(String s) {
    strb.append(s);
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(Int operand) {
    emit(Integer.toString(operand.i));
  }

  @Override
  public void visit(Var operand) {
    emit(operand.id);
  }

  // statements
  @Override
  public void visit(Add s) {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" + ");
    s.right.accept(this);
    emit(";");
  }

  @Override
  public void visit(Cfg.Stm.NewIntArray m) {

  }

  @Override
  public void visit(Cfg.Stm.And s) {

  }

  @Override
  public void visit(Cfg.Stm.ArraySelect s) {

  }

  @Override
  public void visit(Cfg.Stm.Length s) {

  }

  @Override
  public void visit(InvokeVirtual s) {
    emit(s.dst + " = " + s.obj);
    emit("->vptr->" + s.f + "(" + s.obj);
    for (Operand.T x : s.args) {
      emit(", ");
      x.accept(this);
    }
    emit(");");
  }

  @Override
  public void visit(Lt s) {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" < ");
    s.right.accept(this);
    emit(";");
  }

  @Override
  public void visit(Cfg.Stm.Gt s) {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" > ");
    s.right.accept(this);
    emit(";");
  }

  @Override
  public void visit(Move s) {
    emit(s.dst + " = ");
    s.src.accept(this);
    emit(";");
  }

  @Override
  public void visit(NewObject s) {
    emit(s.dst + " = ((struct " + s.c + "*)(Tiger_new (&" + s.c
        + "_vtable_, sizeof(struct " + s.c + "))));");
  }

  @Override
  public void visit(Print s) {
    emit("System_out_println (");
    s.arg.accept(this);
    emit(");");
  }

  @Override
  public void visit(Sub s) {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" - ");
    s.right.accept(this);
    emit(";");
  }

  @Override
  public void visit(Times s) {
    emit(s.dst + " = ");
    s.left.accept(this);
    emit(" * ");
    s.right.accept(this);
    emit(";");
  }

  // transfer
  @Override
  public void visit(If s) {
    emit("if (");
    s.operand.accept(this);
    emit(")\n");
    emit("  goto " + s.truee.toString() + ";\n");
    emit("else\n");
    emit("  goto " + s.falsee.toString() + ";\n");
  }

  @Override
  public void visit(Goto s) {
    emit("goto " + s.label.toString() + ";\n");
  }

  @Override
  public void visit(Return s) {
  }

  // type
  @Override
  public void visit(ClassType t) {
    emit("struct " + t.id + " *");
  }

  @Override
  public void visit(IntType t) {
  }

  @Override
  public void visit(IntArrayType t) {
  }

  // dec
  @Override
  public void visit(DecSingle d) {
  }

  // dec
  @Override
  public void visit(BlockSingle b) {
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    java.util.HashMap<util.Label, Block.T> map = new HashMap<>();
    for (Block.T block : m.blocks) {
      BlockSingle b = (BlockSingle) block;
      util.Label label = b.label;
      map.put(label, b);
    }

    util.Graph<Block.T> graph = new util.Graph<>(m.classId + "_"
        + m.id);

    for (Block.T block : m.blocks) {
      graph.addNode(block);
    }
    for (Block.T block : m.blocks) {
      BlockSingle b = (BlockSingle) block;
      Transfer.T transfer = b.transfer;
      if (transfer instanceof Transfer.Goto) {
        Transfer.Goto gotoo = (Transfer.Goto) transfer;
        Block.T to = map.get(gotoo.label);
        graph.addEdge(block, to);
      } else if (transfer instanceof Transfer.If) {
        Transfer.If iff = (If) transfer;
        Block.T truee = map.get(iff.truee);
        graph.addEdge(block, truee);
        Block.T falsee = map.get(iff.falsee);
        graph.addEdge(block, falsee);
      }
    }
    graph.visualize();
  }

  @Override
  public void visit(MainMethodSingle m) {
    java.util.HashMap<util.Label, Block.T> map = new HashMap<>();
    for (Block.T block : m.blocks) {
      Block.BlockSingle b = (Block.BlockSingle) block;
      util.Label label = b.label;
      map.put(label, b);
    }

    util.Graph<Block.T> graph = new util.Graph<>("Tiger_main");

    for (Block.T block : m.blocks) {
      graph.addNode(block);
    }
    for (Block.T block : m.blocks) {
      BlockSingle b = (BlockSingle) block;
      Transfer.T transfer = b.transfer;
      if (transfer instanceof Goto) {
        Transfer.Goto gotoo = (Transfer.Goto) transfer;
        Block.T to = map.get(gotoo.label);
        graph.addEdge(block, to);
      } else if (transfer instanceof Transfer.If) {
        Transfer.If iff = (Transfer.If) transfer;
        Block.T truee = map.get(iff.truee);
        graph.addEdge(block, truee);
        Block.T falsee = map.get(iff.falsee);
        graph.addEdge(block, falsee);
      }
    }
    graph.visualize();
  }

  // vtables
  @Override
  public void visit(VtableSingle v) { /* null */ }

  // class
  @Override
  public void visit(ClassSingle c) { /* null */ }

  // program
  @Override
  public void visit(ProgramSingle p) {
    // we'd like to output to a file, rather than the "stdout".
    for (cfg.Cfg.Method.T m : p.methods) {
      m.accept(this);
    }
    p.mainMethod.accept(this);
  }
}
