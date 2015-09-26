package codegen.bytecode;

public interface Acceptable {
  void accept(Visitor v);
}
