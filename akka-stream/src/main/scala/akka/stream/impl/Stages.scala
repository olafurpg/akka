/**
 * Copyright (C) 2015-2018 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.stream.impl

import akka.annotation.InternalApi
import akka.stream.Attributes._
import akka.stream._

/**
 * INTERNAL API
 */
@InternalApi private[akka] object Stages {

  object DefaultAttributes {
    // reusable common attributes
    val IODispatcher: _root_.akka.stream.ActorAttributes.Dispatcher = ActorAttributes.IODispatcher
    val inputBufferOne: _root_.akka.stream.Attributes = inputBuffer(initial = 1, max = 1)

    // stage specific default attributes
    val fused: _root_.akka.stream.Attributes = name("fused")
    val materializedValueSource: _root_.akka.stream.Attributes = name("matValueSource")
    val map: _root_.akka.stream.Attributes = name("map")
    val log: _root_.akka.stream.Attributes = name("log")
    val filter: _root_.akka.stream.Attributes = name("filter")
    val filterNot: _root_.akka.stream.Attributes = name("filterNot")
    val collect: _root_.akka.stream.Attributes = name("collect")
    val recover: _root_.akka.stream.Attributes = name("recover")
    val mapAsync: _root_.akka.stream.Attributes = name("mapAsync")
    val mapAsyncUnordered: _root_.akka.stream.Attributes = name("mapAsyncUnordered")
    val grouped: _root_.akka.stream.Attributes = name("grouped")
    val groupedWithin: _root_.akka.stream.Attributes = name("groupedWithin")
    val groupedWeightedWithin: _root_.akka.stream.Attributes = name("groupedWeightedWithin")
    val limit: _root_.akka.stream.Attributes = name("limit")
    val limitWeighted: _root_.akka.stream.Attributes = name("limitWeighted")
    val sliding: _root_.akka.stream.Attributes = name("sliding")
    val take: _root_.akka.stream.Attributes = name("take")
    val drop: _root_.akka.stream.Attributes = name("drop")
    val takeWhile: _root_.akka.stream.Attributes = name("takeWhile")
    val dropWhile: _root_.akka.stream.Attributes = name("dropWhile")
    val scan: _root_.akka.stream.Attributes = name("scan")
    val scanAsync: _root_.akka.stream.Attributes = name("scanAsync")
    val fold: _root_.akka.stream.Attributes = name("fold")
    val foldAsync: _root_.akka.stream.Attributes = name("foldAsync")
    val reduce: _root_.akka.stream.Attributes = name("reduce")
    val intersperse: _root_.akka.stream.Attributes = name("intersperse")
    val buffer: _root_.akka.stream.Attributes = name("buffer")
    val conflate: _root_.akka.stream.Attributes = name("conflate")
    val batch: _root_.akka.stream.Attributes = name("batch")
    val batchWeighted: _root_.akka.stream.Attributes = name("batchWeighted")
    val expand: _root_.akka.stream.Attributes = name("expand")
    val statefulMapConcat: _root_.akka.stream.Attributes = name("statefulMapConcat")
    val detacher: _root_.akka.stream.Attributes = name("detacher")
    val groupBy: _root_.akka.stream.Attributes = name("groupBy")
    val prefixAndTail: _root_.akka.stream.Attributes = name("prefixAndTail")
    val split: _root_.akka.stream.Attributes = name("split")
    val concatAll: _root_.akka.stream.Attributes = name("concatAll")
    val processor: _root_.akka.stream.Attributes = name("processor")
    val processorWithKey: _root_.akka.stream.Attributes = name("processorWithKey")
    val identityOp: _root_.akka.stream.Attributes = name("identityOp")
    val delimiterFraming: _root_.akka.stream.Attributes = name("delimiterFraming")

    val initial: _root_.akka.stream.Attributes = name("initial")
    val completion: _root_.akka.stream.Attributes = name("completion")
    val idle: _root_.akka.stream.Attributes = name("idle")
    val idleTimeoutBidi: _root_.akka.stream.Attributes = name("idleTimeoutBidi")
    val delayInitial: _root_.akka.stream.Attributes = name("delayInitial")
    val idleInject: _root_.akka.stream.Attributes = name("idleInject")
    val backpressureTimeout: _root_.akka.stream.Attributes = name("backpressureTimeout")

    val merge: _root_.akka.stream.Attributes = name("merge")
    val mergePreferred: _root_.akka.stream.Attributes = name("mergePreferred")
    val mergePrioritized: _root_.akka.stream.Attributes = name("mergePrioritized")
    val flattenMerge: _root_.akka.stream.Attributes = name("flattenMerge")
    val recoverWith: _root_.akka.stream.Attributes = name("recoverWith")
    val broadcast: _root_.akka.stream.Attributes = name("broadcast")
    val balance: _root_.akka.stream.Attributes = name("balance")
    val zip: _root_.akka.stream.Attributes = name("zip")
    val zipN: _root_.akka.stream.Attributes = name("zipN")
    val zipWithN: _root_.akka.stream.Attributes = name("zipWithN")
    val zipWithIndex: _root_.akka.stream.Attributes = name("zipWithIndex")
    val unzip: _root_.akka.stream.Attributes = name("unzip")
    val concat: _root_.akka.stream.Attributes = name("concat")
    val orElse: _root_.akka.stream.Attributes = name("orElse")
    val repeat: _root_.akka.stream.Attributes = name("repeat")
    val unfold: _root_.akka.stream.Attributes = name("unfold")
    val unfoldAsync: _root_.akka.stream.Attributes = name("unfoldAsync")
    val delay: _root_.akka.stream.Attributes = name("delay")

    val terminationWatcher: _root_.akka.stream.Attributes = name("terminationWatcher")

    val publisherSource: _root_.akka.stream.Attributes = name("publisherSource")
    val iterableSource: _root_.akka.stream.Attributes = name("iterableSource")
    val cycledSource: _root_.akka.stream.Attributes = name("cycledSource")
    val futureSource: _root_.akka.stream.Attributes = name("futureSource")
    val futureFlattenSource: _root_.akka.stream.Attributes = name("futureFlattenSource")
    val tickSource: _root_.akka.stream.Attributes = name("tickSource")
    val singleSource: _root_.akka.stream.Attributes = name("singleSource")
    val emptySource: _root_.akka.stream.Attributes = name("emptySource")
    val maybeSource: _root_.akka.stream.Attributes = name("MaybeSource")
    val failedSource: _root_.akka.stream.Attributes = name("failedSource")
    val concatSource: _root_.akka.stream.Attributes = name("concatSource")
    val concatMatSource: _root_.akka.stream.Attributes = name("concatMatSource")
    val subscriberSource: _root_.akka.stream.Attributes = name("subscriberSource")
    val actorPublisherSource: _root_.akka.stream.Attributes = name("actorPublisherSource")
    val actorRefSource: _root_.akka.stream.Attributes = name("actorRefSource")
    val queueSource: _root_.akka.stream.Attributes = name("queueSource")
    val inputStreamSource: _root_.akka.stream.Attributes = name("inputStreamSource") and IODispatcher
    val outputStreamSource: _root_.akka.stream.Attributes = name("outputStreamSource") and IODispatcher
    val fileSource: _root_.akka.stream.Attributes = name("fileSource") and IODispatcher
    val unfoldResourceSource: _root_.akka.stream.Attributes = name("unfoldResourceSource") and IODispatcher
    val unfoldResourceSourceAsync: _root_.akka.stream.Attributes = name("unfoldResourceSourceAsync") and IODispatcher
    val asJavaStream: _root_.akka.stream.Attributes = name("asJavaStream") and IODispatcher
    val javaCollectorParallelUnordered: _root_.akka.stream.Attributes = name("javaCollectorParallelUnordered")
    val javaCollector: _root_.akka.stream.Attributes = name("javaCollector")

    val subscriberSink: _root_.akka.stream.Attributes = name("subscriberSink")
    val cancelledSink: _root_.akka.stream.Attributes = name("cancelledSink")
    val headSink: _root_.akka.stream.Attributes = name("headSink") and inputBufferOne
    val headOptionSink: _root_.akka.stream.Attributes = name("headOptionSink") and inputBufferOne
    val lastSink: _root_.akka.stream.Attributes = name("lastSink")
    val lastOptionSink: _root_.akka.stream.Attributes = name("lastOptionSink")
    val seqSink: _root_.akka.stream.Attributes = name("seqSink")
    val publisherSink: _root_.akka.stream.Attributes = name("publisherSink")
    val fanoutPublisherSink: _root_.akka.stream.Attributes = name("fanoutPublisherSink")
    val ignoreSink: _root_.akka.stream.Attributes = name("ignoreSink")
    val actorRefSink: _root_.akka.stream.Attributes = name("actorRefSink")
    val actorRefWithAck: _root_.akka.stream.Attributes = name("actorRefWithAckSink")
    val actorSubscriberSink: _root_.akka.stream.Attributes = name("actorSubscriberSink")
    val queueSink: _root_.akka.stream.Attributes = name("queueSink")
    val lazySink: _root_.akka.stream.Attributes = name("lazySink")
    val lazySource: _root_.akka.stream.Attributes = name("lazySource")
    val outputStreamSink: _root_.akka.stream.Attributes = name("outputStreamSink") and IODispatcher
    val inputStreamSink: _root_.akka.stream.Attributes = name("inputStreamSink") and IODispatcher
    val fileSink: _root_.akka.stream.Attributes = name("fileSink") and IODispatcher
    val fromJavaStream: _root_.akka.stream.Attributes = name("fromJavaStream")
  }

}
