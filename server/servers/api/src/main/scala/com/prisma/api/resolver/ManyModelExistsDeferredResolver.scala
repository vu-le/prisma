package com.prisma.api.resolver

import com.prisma.api.connector.mysql.database.DataResolver
import com.prisma.api.resolver.DeferredTypes.{ManyModelExistsDeferred, OrderedDeferred, OrderedDeferredFutureResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManyModelExistsDeferredResolver(dataResolver: DataResolver) {
  def resolve(orderedDeferreds: Vector[OrderedDeferred[ManyModelExistsDeferred]]): Vector[OrderedDeferredFutureResult[Boolean]] = {
    val deferreds = orderedDeferreds.map(_.deferred)

    DeferredUtils.checkSimilarityOfModelDeferredsAndThrow(deferreds)

    val headDeferred = deferreds.head
    val model        = headDeferred.model
    val args         = headDeferred.args

    // all deferred have the same return value
    val futureDataItems = Future.successful(dataResolver.resolveByModel(model, args))

    val results = orderedDeferreds.map {
      case OrderedDeferred(deferred, order) =>
        OrderedDeferredFutureResult[Boolean](futureDataItems.flatMap(identity).map(_.items.nonEmpty), order)
    }

    results
  }
}
