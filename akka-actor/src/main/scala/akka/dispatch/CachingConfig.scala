/**
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.dispatch

import java.util
import java.util.concurrent.{ ConcurrentHashMap, TimeUnit }

import com.typesafe.config._

import scala.util.{ Failure, Success, Try }

/**
 * INTERNAL API
 */
private[akka] object CachingConfig {
  val emptyConfig: _root_.com.typesafe.config.Config = ConfigFactory.empty()

  sealed abstract trait PathEntry {
    val valid: Boolean
    val exists: Boolean
    val config: Config
  }
  final case class ValuePathEntry(valid: Boolean, exists: Boolean, config: Config = emptyConfig) extends PathEntry
  final case class StringPathEntry(valid: Boolean, exists: Boolean, config: Config, value: String) extends PathEntry

  val invalidPathEntry: _root_.akka.dispatch.CachingConfig.ValuePathEntry = ValuePathEntry(false, true)
  val nonExistingPathEntry: _root_.akka.dispatch.CachingConfig.ValuePathEntry = ValuePathEntry(true, false)
  val emptyPathEntry: _root_.akka.dispatch.CachingConfig.ValuePathEntry = ValuePathEntry(true, true)
}

/**
 * INTERNAL API
 *
 * A CachingConfig is a Config that wraps another Config and is used to cache path lookup and string
 * retrieval, which we happen to do a lot in some critical paths of the actor creation and mailbox
 * selection code.
 *
 * All other Config operations are delegated to the wrapped Config.
 */
private[akka] class CachingConfig(_config: Config) extends Config {

  import CachingConfig._

  private val (config: Config, entryMap: ConcurrentHashMap[String, PathEntry]) = _config match {
    case cc: CachingConfig ⇒ (cc.config, cc.entryMap)
    case _                 ⇒ (_config, new ConcurrentHashMap[String, PathEntry])
  }

  private def getPathEntry(path: String): PathEntry = entryMap.get(path) match {
    case null ⇒
      val ne = Try { config.hasPath(path) } match {
        case Failure(e)     ⇒ invalidPathEntry
        case Success(false) ⇒ nonExistingPathEntry
        case _ ⇒
          Try { config.getValue(path) } match {
            case Failure(e) ⇒
              emptyPathEntry
            case Success(v) ⇒
              if (v.valueType() == ConfigValueType.STRING)
                StringPathEntry(true, true, v.atKey("cached"), v.unwrapped().asInstanceOf[String])
              else
                ValuePathEntry(true, true, v.atKey("cached"))
          }
      }

      entryMap.putIfAbsent(path, ne) match {
        case null ⇒ ne
        case e    ⇒ e
      }

    case e ⇒ e
  }

  def checkValid(reference: Config, restrictToPaths: String*) {
    config.checkValid(reference, restrictToPaths: _*)
  }

  def root(): _root_.com.typesafe.config.ConfigObject = config.root()

  def origin(): _root_.com.typesafe.config.ConfigOrigin = config.origin()

  def withFallback(other: ConfigMergeable): _root_.akka.dispatch.CachingConfig = new CachingConfig(config.withFallback(other))

  def resolve(): _root_.com.typesafe.config.Config = resolve(ConfigResolveOptions.defaults())

  def resolve(options: ConfigResolveOptions): _root_.com.typesafe.config.Config = {
    val resolved = config.resolve(options)
    if (resolved eq config) this
    else new CachingConfig(resolved)
  }

  def hasPath(path: String): _root_.scala.Boolean = {
    val entry = getPathEntry(path)
    if (entry.valid)
      entry.exists
    else // run the real code to get proper exceptions
      config.hasPath(path)
  }

  def hasPathOrNull(path: String): Boolean = config.hasPathOrNull(path)

  def isEmpty: _root_.scala.Boolean = config.isEmpty

  def entrySet(): _root_.java.util.Set[_root_.java.util.Map.Entry[_root_.java.lang.String, _root_.com.typesafe.config.ConfigValue]] = config.entrySet()

  def getBoolean(path: String): _root_.scala.Boolean = config.getBoolean(path)

  def getNumber(path: String): _root_.java.lang.Number = config.getNumber(path)

  def getInt(path: String): _root_.scala.Int = config.getInt(path)

  def getLong(path: String): _root_.scala.Long = config.getLong(path)

  def getDouble(path: String): _root_.scala.Double = config.getDouble(path)

  def getString(path: String): _root_.java.lang.String = {
    getPathEntry(path) match {
      case StringPathEntry(_, _, _, string) ⇒
        string
      case e ⇒ e.config.getString("cached")
    }
  }

  def getObject(path: String): _root_.com.typesafe.config.ConfigObject = config.getObject(path)

  def getConfig(path: String): _root_.com.typesafe.config.Config = config.getConfig(path)

  def getAnyRef(path: String): _root_.java.lang.Object = config.getAnyRef(path)

  def getValue(path: String): _root_.com.typesafe.config.ConfigValue = config.getValue(path)

  def getBytes(path: String): _root_.java.lang.Long = config.getBytes(path)

  def getMilliseconds(path: String): _root_.java.lang.Long = config.getDuration(path, TimeUnit.MILLISECONDS)

  def getNanoseconds(path: String): _root_.java.lang.Long = config.getDuration(path, TimeUnit.NANOSECONDS)

  def getList(path: String): _root_.com.typesafe.config.ConfigList = config.getList(path)

  def getBooleanList(path: String): _root_.java.util.List[_root_.java.lang.Boolean] = config.getBooleanList(path)

  def getNumberList(path: String): _root_.java.util.List[_root_.java.lang.Number] = config.getNumberList(path)

  def getIntList(path: String): _root_.java.util.List[_root_.java.lang.Integer] = config.getIntList(path)

  def getLongList(path: String): _root_.java.util.List[_root_.java.lang.Long] = config.getLongList(path)

  def getDoubleList(path: String): _root_.java.util.List[_root_.java.lang.Double] = config.getDoubleList(path)

  def getStringList(path: String): _root_.java.util.List[_root_.java.lang.String] = config.getStringList(path)

  def getObjectList(path: String): java.util.List[_ <: com.typesafe.config.ConfigObject] = config.getObjectList(path)

  def getConfigList(path: String): java.util.List[_ <: com.typesafe.config.Config] = config.getConfigList(path)

  def getAnyRefList(path: String): java.util.List[_] = config.getAnyRefList(path)

  def getBytesList(path: String): _root_.java.util.List[_root_.java.lang.Long] = config.getBytesList(path)

  def getMillisecondsList(path: String): _root_.java.util.List[_root_.java.lang.Long] = config.getDurationList(path, TimeUnit.MILLISECONDS)

  def getNanosecondsList(path: String): _root_.java.util.List[_root_.java.lang.Long] = config.getDurationList(path, TimeUnit.NANOSECONDS)

  def withOnlyPath(path: String): _root_.akka.dispatch.CachingConfig = new CachingConfig(config.withOnlyPath(path))

  def withoutPath(path: String): _root_.akka.dispatch.CachingConfig = new CachingConfig(config.withoutPath(path))

  def atPath(path: String): _root_.akka.dispatch.CachingConfig = new CachingConfig(config.atPath(path))

  def atKey(key: String): _root_.akka.dispatch.CachingConfig = new CachingConfig(config.atKey(key))

  def withValue(path: String, value: ConfigValue): _root_.akka.dispatch.CachingConfig = new CachingConfig(config.withValue(path, value))

  def getDuration(path: String, unit: TimeUnit): _root_.scala.Long = config.getDuration(path, unit)

  def getDurationList(path: String, unit: TimeUnit): _root_.java.util.List[_root_.java.lang.Long] = config.getDurationList(path, unit)

  def getDuration(path: String): java.time.Duration = config.getDuration(path)

  def getDurationList(path: String): _root_.java.util.List[_root_.java.time.Duration] = config.getDurationList(path)

  def getPeriod(path: String): _root_.java.time.Period = config.getPeriod(path)

  def getTemporal(path: String): _root_.java.time.temporal.TemporalAmount = config.getTemporal(path)

  def getIsNull(path: String): Boolean = config.getIsNull(path)

  def getMemorySize(path: String): _root_.com.typesafe.config.ConfigMemorySize = config.getMemorySize(path)

  def getMemorySizeList(path: String): _root_.java.util.List[_root_.com.typesafe.config.ConfigMemorySize] = config.getMemorySizeList(path)

  def isResolved(): _root_.scala.Boolean = config.isResolved()

  def resolveWith(source: Config, options: ConfigResolveOptions): _root_.com.typesafe.config.Config = config.resolveWith(source, options)

  def resolveWith(source: Config): _root_.com.typesafe.config.Config = config.resolveWith(source)

  override def getEnumList[T <: Enum[T]](enumClass: Class[T], path: String): util.List[T] = config.getEnumList(enumClass, path)

  override def getEnum[T <: Enum[T]](enumClass: Class[T], path: String): T = config.getEnum(enumClass, path)

}

