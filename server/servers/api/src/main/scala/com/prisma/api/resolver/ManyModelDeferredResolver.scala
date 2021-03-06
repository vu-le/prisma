package com.prisma.api.resolver

import com.prisma.api.connector.mysql.database._
import com.prisma.api.resolver.DeferredTypes._

import scala.concurrent.ExecutionContext.Implicits.global

class ManyModelDeferredResolver(resolver: DataResolver) {
  def resolve(orderedDeferreds: Vector[OrderedDeferred[ManyModelDeferred]]): Vector[OrderedDeferredFutureResult[RelayConnectionOutputType]] = {
    val deferreds = orderedDeferreds.map(_.deferred)

    DeferredUtils.checkSimilarityOfModelDeferredsAndThrow(deferreds)

    val headDeferred          = deferreds.head
    val model                 = headDeferred.model
    val args                  = headDeferred.args
    val futureResolverResults = resolver.resolveByModel(model, args)

    orderedDeferreds.map {
      case OrderedDeferred(deferred, order) =>
        OrderedDeferredFutureResult(futureResolverResults.map(mapToConnectionOutputType(_, deferred)), order)
    }
  }

  def mapToConnectionOutputType(input: ResolverResult, deferred: ManyModelDeferred): RelayConnectionOutputType = {
    DefaultIdBasedConnection(
      PageInfo(
        hasNextPage = input.hasNextPage,
        hasPreviousPage = input.hasPreviousPage,
        input.items.headOption.map(_.id),
        input.items.lastOption.map(_.id)
      ),
      input.items.map(x => DefaultEdge(x, x.id)),
      ConnectionParentElement(None, None, deferred.args)
    )
  }
}
