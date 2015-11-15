/**
  * Created by Huxley on 11/15/15.
  */
package object util {
  /**
    * String interpolation for Bug class.
    * It adds '\n' at the end automatically.
    * Usage: bug"..."
    */
  implicit class BugString(val sc: StringContext) extends AnyVal {
    def bug(args: Any*) = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      val msg = new StringBuffer(strings.next)
      while (strings.hasNext) {
        msg append expressions.next
        msg append strings.next
      }
      Bug(msg.toString + '\n')
    }
  }
}
