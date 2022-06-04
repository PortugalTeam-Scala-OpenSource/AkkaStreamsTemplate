import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import java.time.YearMonth

object Stock {

  case class Order(productAmount: Int)

  case class NotEnoughStock()

  case class AvailableProduct(amount: BigInt)

  // the id is a YearMonth, the id is going to be like 2022-01 or 2021-12
  def apply(userId: String)(state: AvailableProduct = AvailableProduct(0)): Behavior[Commands] = Behaviors.receiveMessage {
    case AddStock(order, replyTo) =>
      replyTo.tell(())
      Stock(userId)(state.copy(amount = state.amount + order.productAmount))
    case BuyProductRequest(amountToRedeem, replyTo) =>
      if (amountToRedeem <= state.amount) {
        replyTo.tell(Right(state.amount))
        Stock(userId)(state.copy(amount = state.amount - amountToRedeem))
      }
      else {
        replyTo.tell(Left(NotEnoughStock()))
        Behaviors.same
      }
    case HowMuchStock(replyTo) =>
      replyTo.tell(state.amount)
      Behaviors.same
    case _ => Behaviors.ignore
  }


  sealed trait Commands

  case class AddStock(order: Order, replyTo: ActorRef[Unit]) extends Commands

  case class HowMuchStock(replyTo: ActorRef[BigInt]) extends Commands

  case class BuyProductRequest(amountToRedeem: BigInt, replyTo: ActorRef[Either[NotEnoughStock, BigInt]]) extends Commands


}
