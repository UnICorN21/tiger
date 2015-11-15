package util

/**
  * Created by Huxley on 11/15/15.
  */
class Label(val i: Int) {
  override def equals(obj: scala.Any): Boolean =
    if (null == obj || !obj.isInstanceOf[Label]) false
    else i == obj.asInstanceOf[Label].i

  override def toString: String = s"L_$i"

  /**
    * For java caller currently.
    */
  def this() = {
    this(Label.count)
    Label.count += 1
  }
}

object Label {
  var count = 0

  def reset() = count = 0

  def apply() = {
    val ret = new Label(count)
    count += 1
    ret
  }
}
