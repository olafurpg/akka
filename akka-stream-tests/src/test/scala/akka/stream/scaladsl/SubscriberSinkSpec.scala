/**
 * Copyright (C) 2014-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.stream.scaladsl

import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import akka.stream.testkit._
import akka.stream.testkit.Utils._

class SubscriberSinkSpec extends StreamSpec {

  val settings: _root_.akka.stream.ActorMaterializerSettings = ActorMaterializerSettings(system)
    .withInputBuffer(initialSize = 2, maxSize = 16)

  implicit val materializer: _root_.akka.stream.ActorMaterializer = ActorMaterializer(settings)

  "A Flow with SubscriberSink" must {

    "publish elements to the subscriber" in assertAllStagesStopped {
      val c = TestSubscriber.manualProbe[Int]()
      Source(List(1, 2, 3)).to(Sink.fromSubscriber(c)).run()
      val s = c.expectSubscription()
      s.request(3)
      c.expectNext(1)
      c.expectNext(2)
      c.expectNext(3)
      c.expectComplete()
    }
  }

}
