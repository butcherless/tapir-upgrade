package com.cmartin.learn

import akka.http.scaladsl.server.Route
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.endpoint
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.Future

trait ErrorHandlingApi {

  sealed trait InfoError
  case class BadRequestError(message: String) extends InfoError
  case class NotFoundError(message: String) extends InfoError
  case class ConflictError(message: String) extends InfoError
  case class Unknown(message: String) extends InfoError

  val errorEndpoint: Endpoint[Int, InfoError, String, Any] =
    endpoint.get
      .in("users" / path[Int]("id"))
      .out(jsonBody[String])
      .errorOut(
        oneOf[InfoError](
          //oneOfDefaultMapping(jsonBody[Unknown].description("unknown")),

          oneOfMappingFromMatchType(
            StatusCode.BadRequest,
            jsonBody[BadRequestError].description("bad_request")
          ),
          oneOfMappingFromMatchType(
            StatusCode.NotFound,
            jsonBody[NotFoundError].description("not_found")
          ),
          oneOfMappingFromMatchType(
            StatusCode.Conflict,
            jsonBody[ConflictError].description("conflict")
          ),
          /* WORKAROUND
             - comment the oneOfDefaultMapping below and
             - uncomment the oneOfDefaultMapping above
          */
          oneOfDefaultMapping(jsonBody[Unknown].description("unknown"))
        )
      )

  lazy val route: Route =
    AkkaHttpServerInterpreter
      .toRoute(errorEndpoint)(id =>
        Future.successful(
          id match {
            case 400 => Left(BadRequestError("bad-request-message"))
            case 404 => Left(NotFoundError("not-found-message"))
            case 409 => Left(ConflictError("conflict-message"))
            case 500 => Left(Unknown("default-message"))
            case _   => Right("Ok")
          }
        )
      )

}

object ErrorHandlingApi extends ErrorHandlingApi
