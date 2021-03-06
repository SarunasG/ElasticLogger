package rabbitmq


import java.io.{PrintWriter, StringWriter}
import javax.net.ssl.SSLContext

import akka.actor.{ActorSystem, Props}
import com.logger.ListeningActor
import com.rabbitmq.client._
import org.apache.logging.log4j.LogManager

import scala.collection.immutable.NumericRange.Exclusive
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Sarunas G. on 10/05/17.
  */
object RabbitMQConnection {


  val logger = LogManager.getLogger("RabbitMQConnection")
  val system = ActorSystem("RabbitMqActorSystem")
  private val connection = None: Option[Connection]
  val sw = new StringWriter()

  def setupListener(receivingChannel: Channel, queue: String, f: (String) => Any): Unit = {

    system.scheduler.scheduleOnce(1 seconds,
      system.actorOf(Props(new ListeningActor(receivingChannel, queue, f))), "")
  }


  def getConnection(userName: String, userPassword: String, hostname: String, sslStatus: Boolean = false): Option[Connection] = {

    connection match {

      case None =>

        val factory = new ConnectionFactory()
        if (sslStatus) {

          factory.setUri(s"amqps://$userName:$userPassword@$hostname")
          val supportedProtocols = SSLContext.getDefault.getSupportedSSLParameters.getProtocols
          factory.useSslProtocol(ConnectionFactory.computeDefaultTlsProcotol(supportedProtocols))

        } else {

          factory.setUri(s"amqp://$userName:$userPassword@$hostname")
        }

        logger.info("The RabbitMQ Connection factory has been successfully created")
        try{
          val rmqConnection = Some(factory.newConnection)
          rmqConnection.get.addShutdownListener(new ShutdownListener() {
             def shutdownCompleted(cause : ShutdownSignalException): Unit = {

              logger.error(cause + "\n" + cause.getReason)
            }
          })
          rmqConnection
        } catch {

          case ex: Exception => ex.printStackTrace(new PrintWriter(sw))
            logger.error(sw.toString)
            System.exit(-1)
            None
        }



      case _ => connection


    }

  }

  def getChannel(connection: Connection, exchange: String,
                 queueName: String, routingKey: String, exchangeType: String,
                 durable: Boolean, exclusive: Boolean, autoDelete: Boolean): Channel = {


    val channel = connection.createChannel

    channel.exchangeDeclare(exchange, exchangeType, durable, autoDelete, null)
    channel.queueDeclare(queueName, durable, exclusive, autoDelete, null)
    channel.queueBind(queueName, exchange, routingKey)

    channel
  }


}
