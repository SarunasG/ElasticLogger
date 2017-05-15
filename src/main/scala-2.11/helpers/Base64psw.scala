package helpers

import org.apache.commons.codec.binary.{ Base64 => ApacheBase64 }

/**
  * Created by Sarunas G on 15/05/17.
  */
object Base64psw {

  def decode(encoded : String) = new String(ApacheBase64.decodeBase64(encoded.getBytes))
  def encode(decoded : String) = new String(ApacheBase64.encodeBase64(decoded.getBytes))

}
