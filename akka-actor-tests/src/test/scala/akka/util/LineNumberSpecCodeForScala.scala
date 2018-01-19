/**
 * Copyright (C) 2014-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.util

/*
 * IMPORTANT: do not change this file, the line numbers are verified in LineNumberSpec!
 */

object LineNumberSpecCodeForScala {

  val oneline: _root_.scala.Predef.String ⇒ _root_.scala.Unit = (s: String) ⇒ println(s)

  val twoline: _root_.scala.Predef.String ⇒ _root_.scala.Int = (s: String) ⇒ {
    println(s)
    Integer.parseInt(s)
  }

  val partial: PartialFunction[String, Unit] = {
    case "a" ⇒
    case "b" ⇒
  }
}
