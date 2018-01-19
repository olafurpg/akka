/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.event.jul

import com.typesafe.config.ConfigFactory
import akka.actor.{ Actor, ActorLogging, Props }
import akka.testkit.AkkaSpec
import java.util.logging
import scala.util.control.NoStackTrace

object JavaLoggerSpec {

  val config: _root_.com.typesafe.config.Config = ConfigFactory.parseString("""
    akka {
      loglevel = INFO
      loggers = ["akka.event.jul.JavaLogger"]
      logging-filter = "akka.event.jul.JavaLoggingFilter"
    }""")

  class LogProducer extends Actor with ActorLogging {
    def receive: _root_.scala.PartialFunction[_root_.scala.Any, _root_.scala.Unit] = {
      case e: Exception ⇒
        log.error(e, e.getMessage)
      case (s: String, x: Int) ⇒
        log.info(s, x)
    }
  }

  class SimulatedExc extends RuntimeException("Simulated error") with NoStackTrace
}

class JavaLoggerSpec extends AkkaSpec(JavaLoggerSpec.config) {

  val logger: _root_.java.util.logging.Logger = logging.Logger.getLogger(classOf[JavaLoggerSpec.LogProducer].getName)
  logger.setUseParentHandlers(false) // turn off output of test LogRecords
  logger.addHandler(new logging.Handler {
    def publish(record: logging.LogRecord) {
      testActor ! record
    }

    def flush() {}
    def close() {}
  })

  val producer: _root_.akka.actor.ActorRef = system.actorOf(Props[JavaLoggerSpec.LogProducer], name = "log")

  "JavaLogger" must {

    "log error with stackTrace" in {
      producer ! new JavaLoggerSpec.SimulatedExc

      val record = expectMsgType[logging.LogRecord]

      record should not be (null)
      record.getMillis should not be (0)
      record.getThreadID should not be (0)
      record.getLevel should ===(logging.Level.SEVERE)
      record.getMessage should ===("Simulated error")
      record.getThrown.getClass should ===(classOf[JavaLoggerSpec.SimulatedExc])
      record.getSourceClassName should ===(classOf[JavaLoggerSpec.LogProducer].getName)
      record.getSourceMethodName should ===(null)
    }

    "log info without stackTrace" in {
      producer ! (("{} is the magic number", 3))

      val record = expectMsgType[logging.LogRecord]

      record should not be (null)
      record.getMillis should not be (0)
      record.getThreadID should not be (0)
      record.getLevel should ===(logging.Level.INFO)
      record.getMessage should ===("3 is the magic number")
      record.getThrown should ===(null)
      record.getSourceClassName should ===(classOf[JavaLoggerSpec.LogProducer].getName)
      record.getSourceMethodName should ===(null)
    }
  }

}
