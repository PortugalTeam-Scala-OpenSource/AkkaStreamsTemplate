import akka.Done
import akka.actor.{ActorSystem, typed}

import scala.language.postfixOps
import akka.actor.typed.scaladsl.adapter._
import akka.pattern.ask
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import java.time.YearMonth
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.util.Random

object Main extends App {
  val config = ConfigFactory.load
  implicit val actorSystem = ActorSystem("Example", config)
  val typedActorSystem: typed.ActorSystem[Nothing] =
    actorSystem.toTyped
  implicit val ec: ExecutionContextExecutor = typedActorSystem.executionContext
  implicit val clusterSharding: ClusterSharding =
    ClusterSharding.apply(typedActorSystem)

  // asking someone requires a timeout if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds

  import infrastructure.actor.ShardedActor

  import OrdersPerYearMonth._

  val ordersPerYearMonths = {
    implicit val e = typedActorSystem
    ShardedActor[OrdersPerYearMonth.Commands]({ id =>
      OrdersPerYearMonth.apply(id)(0)
    })

  }
  /*def yearOrderIterator: Iterator[YearMonth] = new Iterator[YearMonth] {
      override def hasNext: Boolean = true

      override def next(): YearMonth =
        YearMonth.of(2021, Random.between(1, 12))
    }*/

  val yearOrderIterator =
    (1 to 1000).map(_ => YearMonth.of(2021, Random.between(1, 13))).iterator

  val done: Future[Done] = Source
    .fromIterator(() => yearOrderIterator)
    .mapAsync(10) { yearMonth =>
      val id = s"${yearMonth.getYear}-${yearMonth.getMonthValue}"
      println(s"Pushing ${id}")
      ordersPerYearMonths.ask(id)(replyTo => AddOrder(replyTo))
    }
    .runWith(Sink.ignore)

  for {
    _ <- done
    responseFromA <- ordersPerYearMonths.ask("2021-12")(replyTo =>
      HowManyOrders(replyTo)
    )
    responseFromB <- ordersPerYearMonths.ask("2021-1")(replyTo =>
      HowManyOrders(replyTo)
    )
  } yield {
    println(s"2021-12: ${responseFromA}")
    println(s"2022-1: ${responseFromB}")
  }

}
