package cfg.optimizations;

import cfg.Cfg;
import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm;
import cfg.Cfg.Stm.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ReachingDefinition implements cfg.Visitor {
  // defs for methods
  private HashMap<String, HashSet<Stm.T>> defs;

  // gen, kill for one statement
  private HashSet<Stm.T> oneStmGen;
  private HashSet<Stm.T> oneStmKill;

  // gen, kill for statements
  private HashMap<Stm.T, HashSet<Stm.T>> stmGen;
  private HashMap<Stm.T, HashSet<Stm.T>> stmKill;

  // gen, kill for blocks
  private HashMap<Block.T, HashSet<Stm.T>> blockGen;
  private HashMap<Block.T, HashSet<Stm.T>> blockKill;

  // in, out for blocks
  private HashMap<Block.T, HashSet<Stm.T>> blockIn;
  private HashMap<Block.T, HashSet<Stm.T>> blockOut;

  // in, out for statements
  public HashMap<Stm.T, HashSet<Stm.T>> stmIn;
  public HashMap<Stm.T, HashSet<Stm.T>> stmOut;

  // in, out for transfers
  public HashMap<Cfg.Transfer.T, HashSet<Stm.T>> transferIn;

  enum RDStepKind {
    Step0("step0"), Step1("step1"), Step2("step2"), Step3("step3"), Step4("step4");
    private String name;

    RDStepKind(String str) {
      this.name = str;
    }
  }

  private RDStepKind currentStep;

  public ReachingDefinition() {
    this.defs = new HashMap<>();

    this.oneStmGen = new HashSet<>();
    this.oneStmKill = new HashSet<>();

    this.stmGen = new HashMap<>();
    this.stmKill = new HashMap<>();

    this.blockGen = new HashMap<>();
    this.blockKill = new HashMap<>();

    this.blockIn = new HashMap<>();
    this.blockOut = new HashMap<>();

    this.stmIn = new HashMap<>();
    this.stmOut = new HashMap<>();

    this.transferIn = new HashMap<>();
  }

  public HashMap<T, HashSet<T>> getStmIn() {
    return stmIn;
  }

  public HashMap<Cfg.Transfer.T, HashSet<T>> getTransferIn() {
    return transferIn;
  }

  // statements
  @Override
  public void visit(Add s) {
    processStm(s);
  }

  @Override
  public void visit(Stm.NewIntArray s) {
    processStm(s);
  }

  @Override
  public void visit(Stm.And s) {
    processStm(s);
  }

  @Override
  public void visit(Stm.Length s) {
    processStm(s);
  }

  @Override
  public void visit(InvokeVirtual s) {
    processStm(s);
  }

  @Override
  public void visit(Lt s) {
    processStm(s);
  }

  @Override
  public void visit(ArraySelect s) {
    processStm(s);
  }

  @Override
  public void visit(Stm.Gt s) {
    processStm(s);
  }

  @Override
  public void visit(Le s) {
    processStm(s);
  }

  @Override
  public void visit(Ge s) {
    processStm(s);
  }

  @Override
  public void visit(Eq s) {
    processStm(s);
  }

  @Override
  public void visit(Move s) {
    processStm(s);
  }

  @Override
  public void visit(NewObject s) {
    processStm(s);
  }

  @Override
  public void visit(Sub s) {
    processStm(s);
  }

  @Override
  public void visit(Times s) {
    processStm(s);
  }

  private void processStm(Stm.T s) {
    switch (currentStep) {
      case Step0:
        HashSet<Stm.T> defset = this.defs.getOrDefault(s.dst, new HashSet<>());
        defset.add(s);
        this.defs.put(s.dst, defset);
        break;
      case Step1:
        this.oneStmGen.add(s);
        this.oneStmKill.addAll(this.defs.get(s.dst));
        this.oneStmKill.remove(s);
        break;
      default: break;
    }
  }

  private void trace(BlockSingle b) {
    StringBuffer buffer = new StringBuffer();
    switch (currentStep) {
      case Step0:
        this.defs.forEach((var, set) -> {
          buffer.append("defset for variable " + var + ":\n");
          set.stream().forEach(e -> buffer.append("\t" + e + "\n"));
        });
        break;
      case Step1:
        for (Stm.T stm: b.stms) {
          buffer.append("\ngen, kill for statement " + stm + ":");
          buffer.append("\n\tgen: ");
          this.stmGen.get(stm).forEach(e -> buffer.append(e + ", "));
          buffer.append("\n\tkill: ");
          this.stmKill.get(stm).forEach(e -> buffer.append(e + ", "));
        }
        break;
      case Step2:
        buffer.append("\ngen, kill for block " + b.label + ":");
        buffer.append("\n\tgen: ");
        this.blockGen.get(b).forEach(e -> buffer.append(e + ", "));
        buffer.append("\n\tkill: ");
        this.blockKill.get(b).forEach(e -> buffer.append(e + ", "));
        break;
      case Step3:
        buffer.append("\nin, out for block " + b.label + ":");
        buffer.append("\n\tin: ");
        this.blockIn.get(b).forEach(e -> buffer.append(e + ", "));
        buffer.append("\n\tout: ");
        this.blockOut.get(b).forEach(e -> buffer.append(e + ", "));
        break;
      case Step4:
        for (Stm.T stm: b.stms) {
          buffer.append("\nin, out for statement " + stm + ":");
          buffer.append("\n\tin: ");
          this.stmIn.get(stm).forEach(e -> buffer.append(e + ", "));
          buffer.append("\n\tout: ");
          this.stmOut.get(stm).forEach(e -> buffer.append(e + ", "));
        }
        break;
      default: break;
    }
    System.out.print(buffer);
  }

  private void step1(BlockSingle b) {
    for (Stm.T stm: b.stms) {
      this.oneStmGen = new HashSet<>();
      this.oneStmKill = new HashSet<>();
      stm.accept(this);
      this.stmGen.put(stm, this.oneStmGen);
      this.stmKill.put(stm, this.oneStmKill);
    }
  }

  private void step2(BlockSingle b) {
    HashSet<Stm.T> blockGen = new HashSet<>(), blockKill = new HashSet<>();
    for (Stm.T stm: b.stms) {
      if (stm instanceof Print) continue;
      HashSet<Stm.T> defset = this.defs.get(stm.dst);
      Set<T> staleGen = blockGen.stream()
              .filter(defset::contains)
              .collect(Collectors.toSet());
      blockGen.removeAll(staleGen);
      blockGen.addAll(this.stmGen.get(stm));
      blockKill.addAll(defset);
      blockKill.remove(stm);
    }
    this.blockGen.put(b, blockGen);
    this.blockKill.put(b, blockKill);
  }

  private boolean step3(BlockSingle b) {
    HashSet<Stm.T> blockIn = new HashSet<>(), blockOut = new HashSet<>();
    b.in.stream().forEach(p -> {
      HashSet<Stm.T> pout = this.blockOut.get(p);
      if (null != pout) blockIn.addAll(pout);
    });
    blockOut.addAll(this.blockGen.get(b));
    blockOut.addAll(blockIn.stream()
            .filter(e -> !this.blockKill.get(b).contains(e))
            .collect(Collectors.toSet()));
    HashSet<Stm.T> oldBlockIn = this.blockIn.get(b);
    if (null != oldBlockIn && blockIn.equals(oldBlockIn)) return true;
    else {
      this.blockIn.put(b, blockIn);
      this.blockOut.put(b, blockOut);
      return false;
    }
  }

  private void step4(BlockSingle b) {
    if (0 == b.stms.size()) this.transferIn.put(b.transfer, this.blockIn.get(b));
    for (int i = 0; i < b.stms.size(); ++i) {
      HashSet<Stm.T> stmIn = new HashSet<>(), stmOut = new HashSet<>();
      Stm.T stm = b.stms.get(i);
      if (0 == i) stmIn.addAll(this.blockIn.get(b));
      else stmIn.addAll(this.stmOut.get(b.stms.get(i-1)));
      if (b.stms.size()-1 == i) stmOut.addAll(this.blockOut.get(b));
      else {
        stmOut.addAll(this.stmGen.get(stm));
        stmOut.addAll(stmIn.stream()
                .filter(e -> !this.stmKill.get(stm).contains(e))
                .collect(Collectors.toSet()));
      }
      this.stmIn.put(stm, stmIn);
      this.stmOut.put(stm, stmOut);
      this.transferIn.put(b.transfer, stmOut);
    }
  }

  // block
  @Override
  public void visit(BlockSingle b) {
    switch (currentStep) {
      case Step0:
        b.stms.forEach(stm -> stm.accept(this));
        break;
      case Step1: step1(b); break;
      case Step2: step2(b); break;
      case Step4: step4(b); break;
      default: break;
    }
    if (control.Control.isTracing("rd." + currentStep.name)) trace(b);
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    defs.clear();

    currentStep = RDStepKind.Step0;
    m.blocks.forEach(b -> b.accept(this));

    currentStep = RDStepKind.Step1;
    m.blocks.forEach(b -> b.accept(this));

    currentStep = RDStepKind.Step2;
    m.blocks.forEach(b -> b.accept(this));

    currentStep = RDStepKind.Step3;
    Collections.reverse(m.blocks);
    while (true) {
      boolean fixed = true;
      for (Block.T b: m.blocks) fixed = step3((BlockSingle)b) && fixed;
      if (fixed) break;
    }
    if (control.Control.isTracing("rd." + currentStep.name)) {
      m.blocks.forEach(b -> trace((BlockSingle)b));
    }

    currentStep = RDStepKind.Step4;
    m.blocks.forEach(b -> b.accept(this));
  }

  @Override
  public void visit(MainMethodSingle m) {
    defs.clear();

    currentStep = RDStepKind.Step0;
    m.blocks.forEach(b -> b.accept(this));

    currentStep = RDStepKind.Step1;
    m.blocks.forEach(b -> b.accept(this));

    currentStep = RDStepKind.Step2;
    m.blocks.forEach(b -> b.accept(this));

    currentStep = RDStepKind.Step3;
    Collections.reverse(m.blocks);
    while (true) {
      boolean fixed = true;
      for (Block.T b: m.blocks) fixed = step3((BlockSingle)b) && fixed;
      if (fixed) break;
    }
    if (control.Control.isTracing("rd." + currentStep.name)) {
      m.blocks.forEach(b -> trace((BlockSingle)b));
    }

    currentStep = RDStepKind.Step4;
    m.blocks.forEach(b -> b.accept(this));
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    p.mainMethod.accept(this);
    p.methods.stream().forEach(m -> m.accept(this));
  }
}
