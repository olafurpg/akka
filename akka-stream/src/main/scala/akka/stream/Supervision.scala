/**
 * Copyright (C) 2015-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.stream

import akka.japi.{ function ⇒ japi }

object Supervision {
  sealed trait Directive

  /**
   * Scala API: The stream will be completed with failure if application code for processing an element
   * throws an exception.
   */
  case object Stop extends Directive

  /**
   * Java API: The stream will be completed with failure if application code for processing an element
   * throws an exception.
   */
  def stop: _root_.akka.stream.Supervision.Stop.type = Stop

  /**
   * Scala API: The element is dropped and the stream continues if application code for processing
   * an element throws an exception.
   */
  case object Resume extends Directive

  /**
   * Java API: The element is dropped and the stream continues if application code for processing
   * an element throws an exception.
   */
  def resume: _root_.akka.stream.Supervision.Resume.type = Resume

  /**
   * Scala API: The element is dropped and the stream continues after restarting the stage
   * if application code for processing an element throws an exception.
   * Restarting a stage means that any accumulated state is cleared. This is typically
   * performed by creating a new instance of the stage.
   */
  case object Restart extends Directive

  /**
   * Java API: The element is dropped and the stream continues after restarting the stage
   * if application code for processing an element throws an exception.
   * Restarting a stage means that any accumulated state is cleared. This is typically
   * performed by creating a new instance of the stage.
   */
  def restart: _root_.akka.stream.Supervision.Restart.type = Restart

  type Decider = Function[Throwable, Directive]

  /**
   * Scala API: [[Decider]] that returns [[Stop]] for all exceptions.
   */
  val stoppingDecider: Decider with japi.Function[Throwable, Directive] =
    new Decider with japi.Function[Throwable, Directive] {
      override def apply(e: Throwable): _root_.akka.stream.Supervision.Stop.type = Stop
    }

  /**
   * Java API: Decider function that returns [[#stop]] for all exceptions.
   */
  val getStoppingDecider: japi.Function[Throwable, Directive] = stoppingDecider

  /**
   * Scala API: [[Decider]] that returns [[Resume]] for all exceptions.
   */
  val resumingDecider: Decider with japi.Function[Throwable, Directive] =
    new Decider with japi.Function[Throwable, Directive] {
      override def apply(e: Throwable): _root_.akka.stream.Supervision.Resume.type = Resume
    }

  /**
   * Java API: Decider function that returns [[#resume]] for all exceptions.
   */
  val getResumingDecider: japi.Function[Throwable, Directive] = resumingDecider

  /**
   * Scala API: [[Decider]] that returns [[Restart]] for all exceptions.
   */
  val restartingDecider: Decider with japi.Function[Throwable, Directive] =
    new Decider with japi.Function[Throwable, Directive] {
      override def apply(e: Throwable): _root_.akka.stream.Supervision.Restart.type = Restart
    }

  /**
   * Java API: Decider function that returns [[#restart]] for all exceptions.
   */
  val getRestartingDecider: japi.Function[Throwable, Directive] = restartingDecider

}
