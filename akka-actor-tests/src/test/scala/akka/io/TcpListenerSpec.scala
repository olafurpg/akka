/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.io

import java.net.Socket
import java.nio.channels.{ SelectableChannel, SocketChannel }
import java.nio.channels.SelectionKey.OP_ACCEPT
import scala.concurrent.duration._
import akka.actor._
import akka.testkit.{ TestProbe, TestActorRef, AkkaSpec, EventFilter }
import akka.io.TcpListener.{ RegisterIncoming, FailedRegisterIncoming }
import akka.io.SelectionHandler._
import akka.testkit.SocketUtil
import Tcp._

class TcpListenerSpec extends AkkaSpec("""
    akka.io.tcp.batch-accept-limit = 2
    akka.actor.serialize-creators = on
    """) {

  "A TcpListener" must {

    "register its ServerSocketChannel with its selector" in new TestSetup(pullMode = false)

    "let the Bind commander know when binding is completed" in new TestSetup(pullMode = false) {
      listener ! new ChannelRegistration {
        def disableInterest(op: Int) = ()
        def enableInterest(op: Int) = ()
        def cancel() = ()
      }
      bindCommander.expectMsgType[Bound]
    }

    "accept acceptable connections and register them with its parent" in new TestSetup(pullMode = false) {
      bindListener()

      attemptConnectionToEndpoint()
      attemptConnectionToEndpoint()
      attemptConnectionToEndpoint()

      // since the batch-accept-limit is 2 we must only receive 2 accepted connections
      listener ! ChannelAcceptable

      expectWorkerForCommand
      expectWorkerForCommand
      selectorRouter.expectNoMsg(100.millis)
      interestCallReceiver.expectMsg(OP_ACCEPT)

      // and pick up the last remaining connection on the next ChannelAcceptable
      listener ! ChannelAcceptable
      expectWorkerForCommand
    }

    "continue to accept connections after a previous accept" in new TestSetup(pullMode = false) {
      bindListener()

      attemptConnectionToEndpoint()
      listener ! ChannelAcceptable
      expectWorkerForCommand
      selectorRouter.expectNoMsg(100.millis)
      interestCallReceiver.expectMsg(OP_ACCEPT)

      attemptConnectionToEndpoint()
      listener ! ChannelAcceptable
      expectWorkerForCommand
      selectorRouter.expectNoMsg(100.millis)
      interestCallReceiver.expectMsg(OP_ACCEPT)
    }

    "not accept connections after a previous accept until read is reenabled" in new TestSetup(pullMode = true) {
      bindListener()

      attemptConnectionToEndpoint()
      expectNoMsg(100.millis)

      listener ! ResumeAccepting(batchSize = 1)
      listener ! ChannelAcceptable
      expectWorkerForCommand
      selectorRouter.expectNoMsg(100.millis)
      interestCallReceiver.expectMsg(OP_ACCEPT)

      // No more accepts are allowed now
      interestCallReceiver.expectNoMsg(100.millis)

      listener ! ResumeAccepting(batchSize = 2)
      interestCallReceiver.expectMsg(OP_ACCEPT)

      attemptConnectionToEndpoint()
      listener ! ChannelAcceptable
      expectWorkerForCommand
      selectorRouter.expectNoMsg(100.millis)
      // There is still one token remaining, accepting
      interestCallReceiver.expectMsg(OP_ACCEPT)

      attemptConnectionToEndpoint()
      listener ! ChannelAcceptable
      expectWorkerForCommand
      selectorRouter.expectNoMsg(100.millis)

      // Tokens are depleted now
      interestCallReceiver.expectNoMsg(100.millis)
    }

    "react to Unbind commands by replying with Unbound and stopping itself" in new TestSetup(pullMode = false) {
      bindListener()

      val unbindCommander: _root_.akka.testkit.TestProbe = TestProbe()
      unbindCommander.send(listener, Unbind)

      unbindCommander.expectMsg(Unbound)
      parent.expectTerminated(listener)
    }

    "drop an incoming connection if it cannot be registered with a selector" in new TestSetup(pullMode = false) {
      bindListener()

      attemptConnectionToEndpoint()

      listener ! ChannelAcceptable
      val channel: _root_.java.nio.channels.SocketChannel = expectWorkerForCommand

      EventFilter.warning(pattern = "selector capacity limit", occurrences = 1) intercept {
        listener ! FailedRegisterIncoming(channel)
        awaitCond(!channel.isOpen)
      }
    }
  }

  val counter: _root_.scala.collection.Iterator[_root_.scala.Int] = Iterator.from(0)

  class TestSetup(pullMode: Boolean) {
    val handler: _root_.akka.testkit.TestProbe = TestProbe()
    val handlerRef: _root_.akka.actor.ActorRef = handler.ref
    val bindCommander: _root_.akka.testkit.TestProbe = TestProbe()
    val parent: _root_.akka.testkit.TestProbe = TestProbe()
    val selectorRouter: _root_.akka.testkit.TestProbe = TestProbe()
    val endpoint: _root_.java.net.InetSocketAddress = SocketUtil.temporaryServerAddress()

    var registerCallReceiver: _root_.akka.testkit.TestProbe = TestProbe()
    var interestCallReceiver: _root_.akka.testkit.TestProbe = TestProbe()

    private val parentRef = TestActorRef(new ListenerParent(pullMode))

    registerCallReceiver.expectMsg(if (pullMode) 0 else OP_ACCEPT)

    def bindListener() {
      listener ! new ChannelRegistration {
        def enableInterest(op: Int): Unit = interestCallReceiver.ref ! op
        def disableInterest(op: Int): Unit = interestCallReceiver.ref ! -op
        def cancel(): Unit = ()
      }
      bindCommander.expectMsgType[Bound]
    }

    def attemptConnectionToEndpoint(): Unit = new Socket(endpoint.getHostName, endpoint.getPort)

    def listener: _root_.akka.actor.ActorRef = parentRef.underlyingActor.listener

    def expectWorkerForCommand: SocketChannel =
      selectorRouter.expectMsgPF() {
        case WorkerForCommand(RegisterIncoming(chan), commander, _) ⇒
          chan.isOpen should ===(true)
          commander should ===(listener)
          chan
      }

    private class ListenerParent(pullMode: Boolean) extends Actor with ChannelRegistry {
      val listener: _root_.akka.actor.ActorRef = context.actorOf(
        props = Props(classOf[TcpListener], selectorRouter.ref, Tcp(system), this, bindCommander.ref,
          Bind(handler.ref, endpoint, 100, Nil, pullMode)).withDeploy(Deploy.local),
        name = "test-listener-" + counter.next())
      parent.watch(listener)
      def receive: Receive = {
        case msg ⇒ parent.ref forward msg
      }
      override def supervisorStrategy: _root_.akka.actor.SupervisorStrategy = SupervisorStrategy.stoppingStrategy

      def register(channel: SelectableChannel, initialOps: Int)(implicit channelActor: ActorRef): Unit =
        registerCallReceiver.ref.tell(initialOps, channelActor)
    }
  }

}
