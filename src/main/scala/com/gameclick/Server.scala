package com.gameclick

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.io.StdIn

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    /*val route =
      path("clickgame") {
        get {
          complete("Such HTTP response")
        }
      }*/

    val score = system.actorOf(Props(new GameClick), "score")

    def newPlayer(): Flow[Message, Message, NotUsed] = {
      // new connection - new user actor
      val playerActor = system.actorOf(Props(new Player(score)))
      val r = scala.util.Random

      val incomingMessages: Sink[Message, NotUsed] =
        Flow[Message].map {
          // transform websocket message to domain message
          case TextMessage.Strict(text) => Player.DecreaseScore(text)
          //case TextMessage.Strict(text) => Player.DecreaseScore(r.nextInt(10))
        }.to(Sink.actorRef[Player.DecreaseScore](playerActor, PoisonPill))

      val outgoingMessages: Source[Message, NotUsed] =
        Source.actorRef[Player.IncreaseScore](10, OverflowStrategy.fail)
          .mapMaterializedValue { outActor =>
            // give the player actor a way to send messages out
            playerActor ! Player.Connected(outActor)
            NotUsed
          }.map(
          // transform domain message to web socket message
          (outMsg: Player.IncreaseScore) => TextMessage(outMsg.value.toString))

      // then combine both to a flow
      Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
    }

    val route =
      path("gameclick") {
        get {
          handleWebSocketMessages(newPlayer())
        }
      }

    val binding = Await.result(Http().bindAndHandle(route, "127.0.0.1", 8080), 3.seconds)

    println("Started server at 127.0.0.1:8080, press enter to kill server")
    StdIn.readLine()
    system.terminate()
  }
}
