package com.logger

import java.io.{PrintWriter, StringWriter}

import elastic.ElasticConnection
import helpers.{ApplicationConfig, DateConverter}
import io.searchbox.core.Index
import org.apache.log4j.BasicConfigurator
import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.xcontent.XContentFactory._
import play.api.libs.json.{JsValue, Json}
import rabbitmq.RabbitMQConnection

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import java.util.Date


/**
  * Created by Sarunas G. on 10/02/17.
  */
object Controls {

  private val usage =
    """Usage: FrontLoggingService-assembly-xx.jar <application.conf_path>
      application.conf_path - path to the application.conf file on disk"""

  def main(args: Array[String]): Unit = {

    if(args.length !=1){
      System.err.println(usage)
      System.exit(-1)

    }

    BasicConfigurator.configure()

    val configPath = Try(args(0))
    val conf = new ApplicationConfig(configPath.getOrElse("./src/main/resources/application.conf"))
    import conf._
    val logger = LogManager.getLogger(Controls.getClass.getName)
    val sw = new StringWriter

    val rabbitMQConnection = RabbitMQConnection
      .getConnection(userName, userPassword, hostname, sslStatus).get
    val rabbitMQChannel = RabbitMQConnection
      .getChannel(rabbitMQConnection, exchange, queueName, routingKey, exchangeType, durable, exclusive, autoDelete)

    val elkClient = ElasticConnection.getHttpElkClient(elasticHostname, elasticPort, elasticUserName, elasticPassword)

    logger.info("Application has Started")

    val writeToElastic = (str: String) => {


      def parseJsonObject(str: String): Try[JsValue] = {
        Try(Json.parse(str.trim))
      }

      parseJsonObject(str) match {


        case Success(jsonObject) => executeObject(jsonObject)
        case Failure(ex) => println(s"issues with parsing object :  ${ex.getMessage}")
          ex.printStackTrace(new PrintWriter(sw))
          logger.error(sw.toString)

      }


      def executeObject(jsonObject: JsValue): Unit = {

        val correlationId = (jsonObject \ "logerItem" \ "correlationId").as[String]
        val listOfEvents = (jsonObject \ "logerItem" \ "events").as[List[String]]
        val timeStamp = DateConverter.returnCurrentDateFromMillis(System.currentTimeMillis())

        val source = jsonBuilder()
          .startObject()
          .field("@timestamp", new Date)
          .startObject("fields")
          .field("correlationId", correlationId)
          .field("events", listOfEvents)
          .endObject()
          .endObject()
          .string()

        val index = new Index.Builder(source).index(s"$elasticIndex-$timeStamp")
          .`type`("frontEnLogs")
          .id(java.util.UUID.randomUUID.toString)
          .build()

        try {
          elkClient.execute(index)

        } catch {
          case ex: Exception => ex.printStackTrace(new PrintWriter(sw))
            logger.error(sw.toString)

        }
      }
    }

    try {

      RabbitMQConnection.setupListener(rabbitMQChannel, queueName, writeToElastic)
    } catch {
      case ex: Exception => ex.printStackTrace(new PrintWriter(sw))
        logger.error(sw.toString)

    }

    sys.addShutdownHook{

      println("Caught ˆC, Abborting ....")
      println("Clossing Connections ...")

    }

  }

}
