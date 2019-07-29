package com.gameclick

import java.util.stream.Collectors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import akka.persistence.PersistentActor

sealed trait Msg

object Player {
  case class Connected(outgoing: ActorRef)
  case class DecreaseScore(value: GameAction)
  case class IncreaseScore(value: GameAction)
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
      case IncreaseScore(gameAction) => {
        name = gameAction.playerName
        currentScore += gameAction.points
        centralGame ! GameClick.UpdateScore(
          GameAction(name, gameAction.points, currentScore)
        )
      }

      case GameClick.UpdateScore(gameAction) => {
        currentScore = currentScore - (if (name != gameAction.playerName) 1 else 0)
        outgoing ! DecreaseScore(GameAction(name, gameAction.points, currentScore))
      }


      case GetScore => {
        System.out.println("First: " + currentScore)
        System.out.println("Second: " + (sender() ! currentScore))
        sender() ! currentScore
      }

    }
  }

}

