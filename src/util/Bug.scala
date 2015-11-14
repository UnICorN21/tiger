package util

/**
  * Created by Huxley on 11/14/15.
  *
  * Add annotation for convenient usage in java.
  * And it'll throw error automatically in java (which will be removed when all bugs are from scala).
  */
@throws[Error]
class Bug(java: Boolean = false, msg: String = "Compiler Error") {
  val err: Throwable = new Error(msg)
  if (java) throw err

  /**
    * Constructor for java caller.
    * @return Unit
    */
  def this() = this(false, "Compiler Error")
}

object Bug extends Throwable {
  self: Throwable =>
  def apply = new Bug(false)
  def apply(msg: String) = new Bug(false, msg)
}
