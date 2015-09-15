package util;

import ast.Ast;

/**
 * Created by Huxley on 9/15/15.
 */
public class VarInfo {
    public String id;
    public Ast.Type.T type;
    public Pos pos;
    public boolean used;

    public VarInfo(String id, Ast.Type.T type, Pos pos) {
        this.id = id;
        this.type = type;
        this.pos = pos;
        this.used = false;
    }
}
