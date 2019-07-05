package com.gameclick

import akka.actor.{Actor, ActorRef, Terminated}
import akka.persistence.PersistentActor

object GameClick {
  case object Join
  case class UpdateScore(value: String)
}

class GameClick extends Actor { //with PersistentActor {
  import GameClick._

  //override def persistenceId = "sample-id-1"
  var players: Set[ActorRef] = Set.empty

  def receive = {
    case Join =>
      players += sender()
      // we also would like to remove the user when its actor is stopped
      context.watch(sender())

    case Terminated(player) =>
      players -= player

    case msg: UpdateScore =>
      players.foreach(_ ! msg)
  }
}
