import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import java.time.YearMonth

object Cashback {

  case class Order(moneySpent: Int)
  case class NotEnoughCashback()
  case class CashbackAmount(amount: BigInt)

  // the id is a YearMonth, the id is going to be like 2022-01 or 2021-12
  def apply(userId: String)(state: CashbackAmount = CashbackAmount(0)): Behavior[Commands] = Behaviors.receiveMessage {
    case AddOrder(order, replyTo) =>
      replyTo.tell(())
      Cashback(userId)(state.copy(amount = state.amount + order.moneySpent))
    case RedeemCashback(amountToRedeem, replyTo) =>
      if (amountToRedeem <= state.amount) {
        replyTo.tell(Right(state.amount))
        Cashback(userId)(state.copy(amount = state.amount - amountToRedeem))
      }
      else {
        replyTo.tell(Left(NotEnoughCashback()))
        Behaviors.same
      }
    case HowMuchCashback(replyTo) =>
      replyTo.tell(state.amount)
      Behaviors.same
    case _ => Behaviors.ignore
  }


  sealed trait Commands

  case class AddOrder(order: Order, replyTo: ActorRef[Unit]) extends Commands

  case class HowMuchCashback(replyTo: ActorRef[BigInt]) extends Commands
  case class RedeemCashback(amountToRedeem: BigInt, replyTo: ActorRef[Either[NotEnoughCashback, BigInt]]) extends Commands


}
