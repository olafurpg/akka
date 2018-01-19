/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.stream.scaladsl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import akka.stream.testkit.{ StreamSpec, TestSubscriber }
import org.reactivestreams.Subscriber
import org.scalatest.Matchers

class FlowAppendSpec extends StreamSpec with River {

  val settings: _root_.akka.stream.ActorMaterializerSettings = ActorMaterializerSettings(system)
  implicit val materializer: _root_.akka.stream.ActorMaterializer = ActorMaterializer(settings)

  "Flow" should {
    "append Flow" in riverOf[String] { subscriber ⇒
      val flow = Flow[Int].via(otherFlow)
      Source(elements).via(flow).to(Sink.fromSubscriber(subscriber)).run()
    }

    "append Sink" in riverOf[String] { subscriber ⇒
      val sink = Flow[Int].to(otherFlow.to(Sink.fromSubscriber(subscriber)))
      Source(elements).to(sink).run()
    }
  }

  "Source" should {
    "append Flow" in riverOf[String] { subscriber ⇒
      Source(elements)
        .via(otherFlow)
        .to(Sink.fromSubscriber(subscriber)).run()
    }

    "append Sink" in riverOf[String] { subscriber ⇒
      Source(elements)
        .to(otherFlow.to(Sink.fromSubscriber(subscriber)))
        .run()
    }
  }

}

trait River { self: Matchers ⇒

  val elements: _root_.scala.collection.immutable.Range.Inclusive = 1 to 10
  val otherFlow: _root_.akka.stream.scaladsl.Flow[_root_.scala.Int, _root_.java.lang.String, _root_.akka.NotUsed] = Flow[Int].map(_.toString)

  def riverOf[T](flowConstructor: Subscriber[T] ⇒ Unit)(implicit system: ActorSystem): _root_.akka.stream.testkit.TestSubscriber.ManualProbe#Self = {
    val subscriber = TestSubscriber.manualProbe[T]()

    flowConstructor(subscriber)

    val subscription = subscriber.expectSubscription()
    subscription.request(elements.size)
    elements.foreach { el ⇒
      subscriber.expectNext() shouldBe el.toString
    }
    subscription.request(1)
    subscriber.expectComplete()
  }
}
