package sttp.tapir.server.play

import play.api.mvc.RequestHeader
import play.utils.UriEncoding
import sttp.model.{Header, Method, QueryParams, Uri}
import sttp.tapir.model.{ConnectionInfo, ServerRequest}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import scala.collection.immutable._

private[play] class PlayServerRequest(request: RequestHeader) extends ServerRequest {
  override lazy val method: Method = Method(request.method.toUpperCase)
  override def protocol: String = request.version
  override lazy val uri: Uri = Uri.unsafeParse(request.uri)
  private val splitHost = request.host.split(":")
  private val local = if (splitHost.size > 1) Some(new InetSocketAddress(splitHost.head, splitHost.last.toInt)) else None
  override lazy val connectionInfo: ConnectionInfo = ConnectionInfo(local, None, Some(request.secure))
  override lazy val headers: Seq[Header] = request.headers.headers.map { case (k, v) => Header(k, v) }.toList
  override lazy val queryParameters: QueryParams = QueryParams.fromMultiMap(request.queryString)
  override lazy val pathSegments: List[String] =
    request.path.dropWhile(_ == '/').split("/").toList.map(UriEncoding.decodePathSegment(_, StandardCharsets.UTF_8))
  override def underlying: Any = request
}
