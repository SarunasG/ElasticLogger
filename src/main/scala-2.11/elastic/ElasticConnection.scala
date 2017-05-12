package elastic

import java.net.InetAddress

import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.config.HttpClientConfig.Builder
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient


/**
  * Created by Sarunas G. on 10/05/17.
  */
object ElasticConnection {


  def getClient(hostname: String, port: Int, clusterName: String = "Elasticsearch"): TransportClient = {

    val settings = Settings.builder()
      .put("cluster.name", clusterName)
      .put("client.transport.sniff", true)
      .build()

    new PreBuiltTransportClient(settings)
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), port))

  }

  def getHttpElkClient(hostname: String,
                       port: Int,
                       username: String,
                       password: String,
                       clusterName: String = "elasticsearch"): JestClient = {

    val factory = new JestClientFactory()
    factory.setHttpClientConfig(
      new HttpClientConfig.Builder(s"http://$hostname:$port")
        .defaultCredentials(username, password)
        .multiThreaded(true)
        .build())

    factory.getObject

  }


}
