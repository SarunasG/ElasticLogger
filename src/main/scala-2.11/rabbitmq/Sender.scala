package rabbitmq

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Connection
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLContext

import helpers.ApplicationConfig

import scala.util.Try


/**
  * Created by Sarunas G. on 15/03/17.
  */
object Sender {


  def main(args: Array[String]) {

    val configPath = Try(args(0))
    val conf = new ApplicationConfig(configPath.getOrElse("./src/main/resources/application.conf"))
    import conf._

    val QUEUE_NAME = queueName

    val factory = new ConnectionFactory


    if (sslStatus) {
      val supportedProtocols = SSLContext.getDefault.getSupportedSSLParameters.getProtocols
      factory.useSslProtocol(ConnectionFactory.computeDefaultTlsProcotol(supportedProtocols))
      factory.setUri(s"amqps://$userName:$userPassword@$hostname")
    } else{
      factory.setUri(s"amqp://$userName:$userPassword@$hostname")
    }

    var connection = None: Option[Connection]
    try {

      connection = Some(factory.newConnection())
      println("Connection status: " + connection.get.isOpen)


      if (!connectionTestOnly) {

        val channel = connection.get.createChannel()
        //channel.queueDeclare(QUEUE_NAME, false, false, false, null)
        channel.exchangeDeclare(queueName, exchangeType, false, false, null )

        val message = s"Message has been sent to $QUEUE_NAME"

        channel.basicPublish(exchange, exchangeType, null, message.getBytes)

        println("Message sent from sender to queue: " + message)

        channel.close()
      }

      connection.get.close()
      println("Connection status: " + connection.get.isOpen)


    } catch {

      case ex: IOException =>
        println("RabbitMQ server is Down !")
        println(ex.getMessage)

      case ex: TimeoutException => ex.printStackTrace()
    }
  }

}
