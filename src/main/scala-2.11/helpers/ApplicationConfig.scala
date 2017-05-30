package helpers

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File

/**
  * Created by Sarunas G. on 23/03/17.
  */
final class ApplicationConfig(configPath : String) {


  val myConfigFile = new File(configPath)

  val cfg: Config = ConfigFactory.load(ConfigFactory.parseFile(myConfigFile))

   val config: Config = cfg.getConfig("rabbitmq-config")
   val configElk: Config = cfg.getConfig("elastic-config")


  val sslStatus: Boolean = config.getString("ssl.status").toBoolean
  val connectionTestOnly: Boolean = config.getString("connectionTestOnly").toBoolean
  val sslOneWay: Boolean = config.getString("ssl.oneway").toBoolean
  val trustStore: String = config.getString("ssl.truststore")
  val trustStorePassword: String = config.getString("ssl.trustStorePassword")
  val keystore: String = config.getString("ssl.keystore")
  val keystorePassword: String = config.getString("ssl.keystorePassword")
  val hostname: String = config.getString("hostname")
  val port: String =config.getString("port")
  val exchangeType: String = config.getString("exchangetype")
  val queueName: String = config.getString("queuename")
  val exchange: String = config.getString("exchange")
  val routingKey: String = config.getString("routingkey")
  val userName: String = config.getString("rabbitmq.username")
  val userPassword: String = config.getString("rabbitmq.password")
  val durable = config.getBoolean("rabbitmq.durable")
  val exclusive = config.getBoolean("rabbitmq.exclusive")
  val autoDelete = config.getBoolean("rabbitmq.autodelete")

  val elasticHostname: String = configElk.getString("elastic.hostname")
  val elasticPort: Int = configElk.getInt("elastic.port")
  val elasticUserName: String = configElk.getString("elastic.username")
  val elasticPassword: String = configElk.getString("elastic.password")
  val elasticIndex: String = configElk.getString("elastic.indexname")

}