package util;

/**
 * Created by Huxley on 9/15/15.
 */
public class Pos {
    public int lineRow;
    public int lineCol;
    public Pos(int row, int col) {
        lineRow = row;
        lineCol = col;
    }

    @Override
    public String toString() {
        return String.format("line %s, col %s", lineRow, lineCol);
    }
}
