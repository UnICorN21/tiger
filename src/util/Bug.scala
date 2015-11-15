package util

/**
  * Bug stands for a compiler bug.
  * Created by Huxley on 11/14/15.
  *
  * Add annotation for convenient usage in java.
  * And it'll throw error automatically in java (which will be removed when all bugs are from scala).
  */
@throws[Error]
class Bug(java: Boolean = false, msg: String = "Compiler Error") extends Error(msg) {
  /**
    * Constructor for java caller.
    * @return Unit
    */
  def this() = {
    this(false, "Compiler Error")
    throw this
  }
}

object Bug {
  def apply = new Bug(false)
  def apply(msg: String) = new Bug(false, msg)
}

