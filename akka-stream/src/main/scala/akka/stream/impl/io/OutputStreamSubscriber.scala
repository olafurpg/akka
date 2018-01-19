/**
 * Copyright (C) 2015-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.stream.impl.io

import java.io.OutputStream

import akka.Done
import akka.actor.{ ActorLogging, Deploy, Props }
import akka.annotation.InternalApi
import akka.stream.actor.{ ActorSubscriberMessage, WatermarkRequestStrategy }
import akka.stream.IOResult
import akka.util.ByteString

import scala.concurrent.Promise
import scala.util.{ Failure, Success }

/** INTERNAL API */
@InternalApi private[akka] object OutputStreamSubscriber {
  def props(os: OutputStream, completionPromise: Promise[IOResult], bufSize: Int, autoFlush: Boolean): _root_.akka.actor.Props = {
    require(bufSize > 0, "buffer size must be > 0")
    Props(classOf[OutputStreamSubscriber], os, completionPromise, bufSize, autoFlush).withDeploy(Deploy.local)
  }

}

/** INTERNAL API */
@InternalApi private[akka] class OutputStreamSubscriber(os: OutputStream, completionPromise: Promise[IOResult], bufSize: Int, autoFlush: Boolean)
  extends akka.stream.actor.ActorSubscriber
  with ActorLogging {

  override protected val requestStrategy: _root_.akka.stream.actor.WatermarkRequestStrategy = WatermarkRequestStrategy(highWatermark = bufSize)

  private var bytesWritten: Long = 0

  def receive: _root_.scala.PartialFunction[_root_.scala.Any, _root_.scala.Unit] = {
    case ActorSubscriberMessage.OnNext(bytes: ByteString) ⇒
      try {
        // blocking write
        os.write(bytes.toArray)
        bytesWritten += bytes.length
        if (autoFlush) os.flush()
      } catch {
        case ex: Exception ⇒
          completionPromise.success(IOResult(bytesWritten, Failure(ex)))
          cancel()
      }

    case ActorSubscriberMessage.OnError(ex) ⇒
      log.error(ex, "Tearing down OutputStreamSink due to upstream error, wrote bytes: {}", bytesWritten)
      completionPromise.success(IOResult(bytesWritten, Failure(ex)))
      context.stop(self)

    case ActorSubscriberMessage.OnComplete ⇒
      context.stop(self)
      os.flush()
  }

  override def postStop(): Unit = {
    try {
      if (os ne null) os.close()
    } catch {
      case ex: Exception ⇒
        completionPromise.success(IOResult(bytesWritten, Failure(ex)))
    }

    completionPromise.trySuccess(IOResult(bytesWritten, Success(Done)))
    super.postStop()
  }
}
