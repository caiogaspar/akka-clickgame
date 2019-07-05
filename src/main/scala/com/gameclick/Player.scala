package com.gameclick

import akka.actor.{Actor, ActorRef}

object Player {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
}

class Player(score: ActorRef) extends Actor {
  import Player._

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    score ! Score.Join

    {
      case IncomingMessage(text) =>
        score ! Score.ChatMessage(text)

      case Score.ChatMessage(text) =>
        outgoing ! OutgoingMessage(text)
    }
  }

}

