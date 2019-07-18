package com.gameclick

import java.util.stream.Collectors

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

  var name: String = ""

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    centralGame ! GameClick.Join

    {
      case IncreaseScore(value) => {
        name = value.split(",").head
        currentScore += value.split(",").last.toInt
        centralGame ! GameClick.UpdateScore(
          name.concat(",").concat(value.split(",").last).concat(",").concat(currentScore.toString)
        )
      }

      case GameClick.UpdateScore(value) => {
        currentScore = currentScore - (if (name != value.split(",").head) 1 else 0)
        outgoing ! DecreaseScore(name.concat(",").concat(value.split(",").last).concat(",").concat(currentScore.toString))
      }


      case GetScore => {
        System.out.println("First: " + currentScore)
        System.out.println("Second: " + (sender() ! currentScore))
        sender() ! currentScore
      }

    }
  }

}

