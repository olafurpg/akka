package akka.io

import akka.io.{ InetAddressDnsResolver, SimpleDnsManager }
import java.lang
class InetAddressDnsProvider extends DnsProvider {
  override def cache: Dns = new SimpleDnsCache()
  override def actorClass: lang.Class[InetAddressDnsResolver] = classOf[InetAddressDnsResolver]
  override def managerClass: lang.Class[SimpleDnsManager] = classOf[SimpleDnsManager]
}
