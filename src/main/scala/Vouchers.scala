import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import java.time.YearMonth

object Vouchers {


  case class NotValidVoucher()
  case class VoucherId(id: String)
  case class AvailableVouchers(vouchers: Set[VoucherId])

  def apply(storeId: String)(state: AvailableVouchers = AvailableVouchers(Set.empty)): Behavior[Commands] = Behaviors.receiveMessage {
    case AddVoucher(voucherId, replyTo) =>
      replyTo.tell(())
      Vouchers(storeId)(state.copy(vouchers = state.vouchers + voucherId))
    case RedeemVoucher(voucherId, replyTo) =>
      if (state.vouchers contains voucherId) {
        replyTo.tell(Right())
        Vouchers(storeId)(state.copy(vouchers = state.vouchers - voucherId))
      }
      else {
        replyTo.tell(Left(NotValidVoucher()))
        Behaviors.same
      }
    case IsVoucherValid(voucherId, replyTo) =>
      replyTo.tell(state.vouchers contains voucherId)
      Behaviors.same
    case _ => Behaviors.ignore
  }


  sealed trait Commands

  case class AddVoucher(voucherId: VoucherId, replyTo: ActorRef[Unit]) extends Commands

  case class IsVoucherValid(voucherId: VoucherId, replyTo: ActorRef[Boolean]) extends Commands

  case class RedeemVoucher(voucherId: VoucherId, replyTo: ActorRef[Either[NotValidVoucher, Unit]]) extends Commands


}
