package com.logger

import java.io.{PrintWriter, StringWriter}
import akka.actor.{Props, Actor}
import com.rabbitmq.client.{AMQP, Envelope, DefaultConsumer, Channel}
import org.apache.logging.log4j.LogManager


/**
  * Created by Sarunas G. on 10/05/17.
  */
class ListeningActor(channel: Channel, queue: String, f: (String) => Any) extends Actor {

  val logger = LogManager.getLogger("ListeningActor")

  def receive = {

    case _ => startReceiving
  }


  def startReceiving = {

    println("Listening for messages: ")

    val consumer = new DefaultConsumer(channel) {

      override def handleDelivery(consumerTag: String, envelope: Envelope,
                                  properties: AMQP.BasicProperties, body: Array[Byte]) {


        val message = new String(body, "UTF-8")
        val sw = new StringWriter
        val rmqCorrelationId = properties.getCorrelationId
        val rmqToReply = properties.getReplyTo
        val header = ""

        val acknowledgeActor = context.actorOf(Props(new AcknowledgeActor(channel, rmqToReply, rmqCorrelationId, header)))


        try {

          context.actorOf(Props(new Actor {

            def receive: PartialFunction[Any, Unit] = {

              case _: String => f(message)

                println(Thread.currentThread().getName)

                acknowledgeActor ! "ReplyToRPC"

                context.stop(self)

            }
          })) ! message

        } catch {

          case e: Exception => e.printStackTrace(new PrintWriter(sw))
            logger.error(sw.toString)
        } finally {
          logger.info("Message consumed successfully !")
        }


      }
    }

    channel.basicConsume(queue, true, consumer)


  }

}


class AcknowledgeActor(channel: Channel, queue: String = "rpc.default", rmqCorrelationId: String, header: String) extends Actor {


  def receive = {

    case "ReplyToRPC" =>

      val props = new AMQP.BasicProperties
      .Builder()
        .correlationId(rmqCorrelationId)
        .replyTo(rmqCorrelationId)
        .build()

      channel.basicPublish("", queue, props, header.getBytes("UTF-8"))
      println(Thread.currentThread().getName)
      context.stop(self)

  }

}