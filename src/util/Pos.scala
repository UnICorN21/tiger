package util

/**
  * Created by Huxley on 11/15/15.
  */
case class Pos(lineRow: Int, lineCol: Int) {
  override def toString: String = s"line $lineRow, col $lineCol"
}