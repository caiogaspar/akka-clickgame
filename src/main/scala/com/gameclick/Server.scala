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

    val score = system.actorOf(Props(new GameClick), "score")

    def newPlayer(): Flow[Message, Message, NotUsed] = {
      // new connection - new user actor
      val playerActor = system.actorOf(Props(new Player(score)))
      val r = scala.util.Random

      val incomingMessages: Sink[Message, NotUsed] =
        Flow[Message].map {
          // transform websocket message to domain message
          case TextMessage.Strict(playerName) => Player.IncreaseScore(GameAction(playerName, r.nextInt(10)))
        }.to(Sink.actorRef[Player.IncreaseScore](playerActor, PoisonPill))

      val outgoingMessages: Source[Message, NotUsed] =
        Source.actorRef[Player.DecreaseScore](10, OverflowStrategy.fail)
          .mapMaterializedValue { outActor =>
            // give the player actor a way to send messages out
            playerActor ! Player.Connected(outActor)
            NotUsed
          }.map(
            // transform domain message to web socket message
            (outMsg: Player.DecreaseScore) => TextMessage(outMsg.value.playerName + "," + outMsg.value.points + "," + outMsg.value.currentScore)
          )

      // then combine both to a flow
      Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
    }

    val routeNewPlayer =
      path("newplayer") {
        get {
          complete("")
        }
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
