/**
 * Copyright (C) 2015-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.stream.scaladsl

import akka.stream.testkit.StreamSpec
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.stream.testkit.Utils._

import scala.util.control.NoStackTrace

class FlowRecoverSpec extends StreamSpec {

  val settings: _root_.akka.stream.ActorMaterializerSettings = ActorMaterializerSettings(system).withInputBuffer(initialSize = 1, maxSize = 1)

  implicit val materializer: _root_.akka.stream.ActorMaterializer = ActorMaterializer(settings)

  val ex: _root_.scala.`package`.RuntimeException with _root_.scala.util.control.NoStackTrace {} = new RuntimeException("ex") with NoStackTrace

  "A Recover" must {
    "recover when there is a handler" in assertAllStagesStopped {
      Source(1 to 4).map { a ⇒ if (a == 3) throw ex else a }
        .recover { case t: Throwable ⇒ 0 }
        .runWith(TestSink.probe[Int])
        .requestNext(1)
        .requestNext(2)
        .requestNext(0)
        .request(1)
        .expectComplete()
    }

    "failed stream if handler is not for such exception type" in assertAllStagesStopped {
      Source(1 to 3).map { a ⇒ if (a == 2) throw ex else a }
        .recover { case t: IndexOutOfBoundsException ⇒ 0 }
        .runWith(TestSink.probe[Int])
        .requestNext(1)
        .request(1)
        .expectError(ex)
    }

    "not influence stream when there is no exceptions" in assertAllStagesStopped {
      Source(1 to 3).map(identity)
        .recover { case t: Throwable ⇒ 0 }
        .runWith(TestSink.probe[Int])
        .request(3)
        .expectNextN(1 to 3)
        .expectComplete()
    }

    "finish stream if it's empty" in assertAllStagesStopped {
      Source.empty.map(identity)
        .recover { case t: Throwable ⇒ 0 }
        .runWith(TestSink.probe[Int])
        .request(1)
        .expectComplete()
    }
  }
}
