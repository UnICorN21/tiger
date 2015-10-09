package cfg.optimizations;

import cfg.Cfg;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Program;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm.Print;
import cfg.Cfg.Stm.T;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class DeadCode implements cfg.Visitor {
  private HashMap<Cfg.Stm.T, HashSet<String>> livenessOut;

  private Cfg.MainMethod.T mainMethod;
  private Cfg.Method.T method;
  private Cfg.Block.T block;

  public Program.T program;

  public DeadCode() {
    this.program = null;
    this.mainMethod = null;
    this.method = null;
    this.block = null;
  }

  // setters
  public void setLivenessOut(HashMap<T, HashSet<String>> livenessOut) {
    this.livenessOut = livenessOut;
  }

  // block
  @Override
  public void visit(BlockSingle b) {
    LinkedList<Cfg.Stm.T> stms = new LinkedList<>();
    stms.addAll(b.stms.stream()
            .filter(stm -> stm instanceof Print || stm instanceof Cfg.Stm.InvokeVirtual || this.livenessOut.get(stm).contains(stm.dst))
            .collect(Collectors.toList()));
    this.block = new BlockSingle(b.label, stms, b.transfer);
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    LinkedList<Cfg.Block.T> blocks = new LinkedList<>();
    m.blocks.stream().forEach(b -> {
      b.accept(this);
      blocks.add(this.block);
    });
    this.method = new MethodSingle(m.retType, m.id, m.classId, m.formals, m.locals, blocks, m.entry, m.exit, m.retValue);
  }

  @Override
  public void visit(MainMethodSingle m) {
    LinkedList<Cfg.Block.T> blocks = new LinkedList<>();
    m.blocks.stream().forEach(b -> {
      b.accept(this);
      blocks.add(this.block);
    });
    this.mainMethod = new MainMethodSingle(m.locals, blocks);
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    LinkedList<Cfg.Method.T> methods = new LinkedList<>();

    p.mainMethod.accept(this);
    p.methods.stream().forEach(m -> {
      m.accept(this);
      methods.add(this.method);
    });

    this.program = new ProgramSingle(p.classes, p.vtables, methods, this.mainMethod);
  }
}
