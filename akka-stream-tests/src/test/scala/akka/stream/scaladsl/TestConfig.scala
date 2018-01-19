/*
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.stream.scaladsl

object TestConfig {
  val numberOfTestsToRun: _root_.scala.Int = System.getProperty("akka.stream.test.numberOfRandomizedTests", "10").toInt
  val RandomTestRange: _root_.scala.collection.immutable.Range.Inclusive = 1 to numberOfTestsToRun
}
