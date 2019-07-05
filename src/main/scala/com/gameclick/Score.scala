package com.gameclick

import akka.actor.{Actor, ActorRef, Terminated}

object Score {
  case object Join
  case class ChatMessage(message: String)
}

class Score extends Actor {
  import Score._
  var users: Set[ActorRef] = Set.empty

  def receive = {
    case Join =>
      users += sender()
      // we also would like to remove the user when its actor is stopped
      context.watch(sender())

    case Terminated(user) =>
      users -= user

    case msg: ChatMessage =>
      users.foreach(_ ! msg)
  }
}
