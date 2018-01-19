package akka.io

class InetAddressDnsProvider extends DnsProvider {
  override def cache: Dns = new SimpleDnsCache()
  override def actorClass: _root_.java.lang.Class[_root_.akka.io.InetAddressDnsResolver] = classOf[InetAddressDnsResolver]
  override def managerClass: _root_.java.lang.Class[_root_.akka.io.SimpleDnsManager] = classOf[SimpleDnsManager]
}
