package com.gameclick

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import akka.persistence.PersistentActor

sealed trait Msg
object Player {
  case class Connected(outgoing: ActorRef)
  case class DecreaseScore(value: String)
  case class IncreaseScore(value: String)
  case class GetScore(value: Int)
}

class Player(centralGame: ActorRef) extends Actor { //} with PersistentActor {
  import Player._

  //override def persistenceId = "player-name"
  var currentScore: Int = 0

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    centralGame ! GameClick.Join

    {
      case DecreaseScore(value) =>
        //currentScore -= value
        centralGame ! GameClick.UpdateScore(value)

      case GameClick.UpdateScore(value) =>
        //currentScore += value
        outgoing ! IncreaseScore(value)

      case GetScore =>
        System.out.println("First: " + currentScore)
        System.out.println("Second: " + (sender() ! currentScore))
        sender() ! currentScore
    }
  }

}

