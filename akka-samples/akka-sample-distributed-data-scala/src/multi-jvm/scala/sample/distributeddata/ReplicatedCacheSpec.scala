package sample.distributeddata

import scala.concurrent.duration._
import akka.cluster.Cluster
import akka.cluster.ddata.DistributedData
import akka.cluster.ddata.Replicator.GetReplicaCount
import akka.cluster.ddata.Replicator.ReplicaCount
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.testkit._
import com.typesafe.config.ConfigFactory

object ReplicatedCacheSpec extends MultiNodeConfig {
  val node1 = role("node-1")
  val node2 = role("node-2")
  val node3 = role("node-3")

  commonConfig(ConfigFactory.parseString("""
    akka.loglevel = INFO
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.log-dead-letters-during-shutdown = off
    """))
}

class ReplicatedCacheSpecMultiJvmNode1 extends ReplicatedCacheSpec
class ReplicatedCacheSpecMultiJvmNode2 extends ReplicatedCacheSpec
class ReplicatedCacheSpecMultiJvmNode3 extends ReplicatedCacheSpec

class ReplicatedCacheSpec
    extends MultiNodeSpec(ReplicatedCacheSpec) with STMultiNodeSpec
    with ImplicitSender {
  import ReplicatedCacheSpec._
  import ReplicatedCache._

  override def initialParticipants = roles.size

  val cluster         = Cluster(system)
  val replicatedCache = system.actorOf(ReplicatedCache.props)

  def join(from: RoleName, to: RoleName): Unit = {
    runOn(from) {
      cluster join node(to).address
    }
    enterBarrier(from.name + "-joined")
  }

  "Demo of a replicated cache" must {
    "join cluster" in within(20.seconds) {
      join(node1, node1)
      join(node2, node1)
      join(node3, node1)

      awaitAssert {
        DistributedData(system).replicator ! GetReplicaCount
        expectMsg(ReplicaCount(roles.size))
      }
      enterBarrier("after-1")
    }

    "replicate cached entry" in within(10.seconds) {
      runOn(node1) {
        replicatedCache ! PutInCache("key1", "A")
      }

      awaitAssert {
        val probe = TestProbe()
        replicatedCache.tell(GetFromCache("key1"), probe.ref)
        probe.expectMsg(Cached("key1", Some("A")))
      }

      enterBarrier("after-2")
    }

    "replicate many cached entries" in within(10.seconds) {
      runOn(node1) {
        for (i ← 100 to 200) replicatedCache ! PutInCache("key" + i, i)
      }

      awaitAssert {
        val probe = TestProbe()
        for (i ← 100 to 200) {
          replicatedCache.tell(GetFromCache("key" + i), probe.ref)
          probe.expectMsg(Cached("key" + i, Some(i)))
        }
      }

      enterBarrier("after-3")
    }

    "replicate evicted entry" in within(15.seconds) {
      runOn(node1) {
        replicatedCache ! PutInCache("key2", "B")
      }

      awaitAssert {
        val probe = TestProbe()
        replicatedCache.tell(GetFromCache("key2"), probe.ref)
        probe.expectMsg(Cached("key2", Some("B")))
      }
      enterBarrier("key2-replicated")

      runOn(node3) {
        replicatedCache ! Evict("key2")
      }

      awaitAssert {
        val probe = TestProbe()
        replicatedCache.tell(GetFromCache("key2"), probe.ref)
        probe.expectMsg(Cached("key2", None))
      }

      enterBarrier("after-4")
    }

    "replicate updated cached entry" in within(10.seconds) {
      runOn(node2) {
        replicatedCache ! PutInCache("key1", "A2")
        replicatedCache ! PutInCache("key1", "A3")
      }

      awaitAssert {
        val probe = TestProbe()
        replicatedCache.tell(GetFromCache("key1"), probe.ref)
        probe.expectMsg(Cached("key1", Some("A3")))
      }

      enterBarrier("after-5")
    }
  }
}
