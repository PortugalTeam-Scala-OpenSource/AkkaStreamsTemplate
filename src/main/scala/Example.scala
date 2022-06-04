object Example extends App {
  import java.time.YearMonth
  import scala.util.Try
  case class Order(name: String, initalDate: YearMonth)

  object Order {

    def apply(
        name: String,
        initialDate: String
    ): Option[Order] = {
      for {
        initialDate <- initialDate match {
          case s"$year-$month" =>
            Try(YearMonth.of(year.toInt, month.toInt)).toOption
          case _ => None
        }
      } yield Order(name, initialDate)
    }
  }

  val order: Option[Order] = Order.apply("name", "1993-12")
  val orderB: Order =
    Order("name", YearMonth.of(1993, 12))

  sealed trait MonthsSinceNow
  case object LessThanOneMonth extends MonthsSinceNow
  case object OneToThreeMonths extends MonthsSinceNow
  case object FourToSixMonths extends MonthsSinceNow
  case object SevenToTwelveMonths extends MonthsSinceNow
  case object MoreThanTwelveMonths extends MonthsSinceNow

  object MonthsSinceNow {
    def totalMonths(date: YearMonth): Int = {
      return date.getYear * 12 + date.getMonthValue
    }

    def apply(now: YearMonth, other: YearMonth): MonthsSinceNow = {
      val delta = totalMonths(now) - totalMonths(other)
      delta match {
        case delta if delta < 1                 => LessThanOneMonth
        case delta if delta == 1 || delta <= 3  => OneToThreeMonths
        case delta if delta == 4 || delta <= 6  => FourToSixMonths
        case delta if delta == 7 || delta <= 12 => SevenToTwelveMonths
        case delta if delta > 12                => MoreThanTwelveMonths
      }
    }
  }

  val `sequence of Order`: Seq[Order] =
    Seq(
      YearMonth.now().minusMonths(6),
      YearMonth.now().minusMonths(12),
      YearMonth.now().minusMonths(12)
    )
      .map(monthIndex =>
        Order(
          name = s"order-$monthIndex",
          initalDate = monthIndex
        )
      )

  println()
  println("sequence of Order")
  println("-----------------")
  `sequence of Order` foreach println
  /*
  sequence of Order
  -----------------
  Order(order-2021-12,2021-12)
  Order(order-2021-06,2021-06)
  Order(order-2021-06,2021-06)
   */

  val `sequence of MonthsSinceNow`: Seq[MonthsSinceNow] =
    `sequence of Order`
      .map { order =>
        MonthsSinceNow.apply(YearMonth.now(), order.initalDate)
      }

  println()
  println("sequence of MonthsSinceNow")
  println("--------------------------")
  `sequence of MonthsSinceNow` foreach println
  /*
  sequence of MonthsSinceNow
  --------------------------
  FourToSixMonths
  SevenToTwelveMonths
  SevenToTwelveMonths
   */

  val `monthsSinceNow to sequence of ocurrences`
      : Map[MonthsSinceNow, Seq[MonthsSinceNow]] =
    `sequence of MonthsSinceNow`.groupBy(identity)
  println()
  println("monthsSinceNow to sequence of ocurrences:")
  println("-----------------------------------------")
  `monthsSinceNow to sequence of ocurrences`
    .map { case (monthsSinceNow, amountOfOrders) =>
      s"$monthsSinceNow: [${amountOfOrders.mkString(",")}]"
    }
    .foreach(println)
  /*
  monthsSinceNow to sequence of ocurrences:
  -----------------------------------------
  FourToSixMonths: [FourToSixMonths]
  SevenToTwelveMonths: [SevenToTwelveMonths,SevenToTwelveMonths]
   */

  val `monthsSinceNow to amount of ocurrences`: Map[MonthsSinceNow, Int] =
    `monthsSinceNow to sequence of ocurrences`
      .map { case (monthsSinceNow, orders) => (monthsSinceNow, orders.size) }

  println()
  println("monthsSinceNow to amount of ocurrences")
  println("--------------------------------------")
  `monthsSinceNow to amount of ocurrences`
    .map { case (monthsSinceNow, amountOfOrders) =>
      s"$monthsSinceNow: $amountOfOrders"
    }
    .foreach(println)
  /*
  monthsSinceNow to amount of ocurrences
  --------------------------------------
  FourToSixMonths: 1
  SevenToTwelveMonths: 2
   */

}
