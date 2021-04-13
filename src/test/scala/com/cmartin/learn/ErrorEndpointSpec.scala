package com.cmartin.learn

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ErrorEndpointSpec
    extends AnyFlatSpec
    with Matchers
    with ScalatestRouteTest {

  behavior of "ErrorEndpoint API"

  it should "respond OK" in {
    // G I V E N
    val id = 1
    Get(s"users/$id") ~>
      // W H E N
      ErrorHandlingApi.route ~>
      // T H E N
      check {
        status shouldBe StatusCodes.OK
        info(entityAs[String])
      }
  }

  it should "respond BadRequest" in {
    // G I V E N
    val id = 400
    Get(s"users/$id") ~>
      // W H E N
      ErrorHandlingApi.route ~>
      // T H E N
      check {
        status shouldBe StatusCodes.BadRequest
        info(entityAs[String])
      }
  }

  it should "respond NotFound" in {
    // G I V E N
    val id = 404
    Get(s"users/$id") ~>
      // W H E N
      ErrorHandlingApi.route ~>
      // T H E N
      check {
        status shouldBe StatusCodes.NotFound
        info(entityAs[String])
      }
  }

  it should "respond Conflict" in {
    // G I V E N
    val id = 409
    Get(s"users/$id") ~>
      // W H E N
      ErrorHandlingApi.route ~>
      // T H E N
      check {
        status shouldBe StatusCodes.Conflict
        info(entityAs[String])
      }
  }

  it should "respond Unknown" in {
    // G I V E N
    val id = 500
    Get(s"users/$id") ~>
      // W H E N
      ErrorHandlingApi.route ~>
      // T H E N
      check {
        status shouldBe StatusCodes.BadRequest
        info(entityAs[String])
      }
  }

}
