package io.circe.optics

import io.circe.{ Decoder, Encoder, Json, JsonNumber, JsonObject }
import io.circe.optics.JsonObjectOptics._
import io.circe.optics.JsonOptics._
import monocle.{ Fold, Iso, Optional, Prism, Traversal }
import monocle.function.{ At, FilterIndex, Index }
import scala.language.dynamics

final case class JsonPath(json: Optional[Json, Json]) extends Dynamic {
  final def `null`: Optional[Json, Unit] = json.andThen(jsonNull)
  final def boolean: Optional[Json, Boolean] = json.andThen(jsonBoolean)
  final def byte: Optional[Json, Byte] = json.andThen(jsonByte)
  final def short: Optional[Json, Short] = json.andThen(jsonShort)
  final def int: Optional[Json, Int] = json.andThen(jsonInt)
  final def long: Optional[Json, Long] = json.andThen(jsonLong)
  final def bigInt: Optional[Json, BigInt] = json.andThen(jsonBigInt)
  final def double: Optional[Json, Double] = json.andThen(jsonDouble)
  final def bigDecimal: Optional[Json, BigDecimal] = json.andThen(jsonBigDecimal)
  final def number: Optional[Json, JsonNumber] = json.andThen(jsonNumber)
  final def string: Optional[Json, String] = json.andThen(jsonString)
  final def arr: Optional[Json, Vector[Json]] = json.andThen(jsonArray)
  final def obj: Optional[Json, JsonObject] = json.andThen(jsonObject)

  final def at(field: String): Optional[Json, Option[Json]] =
    obj.andThen(At.at(field))

  final def selectDynamic(field: String): JsonPath =
    JsonPath(obj.andThen(Index.index(field)))

  final def applyDynamic(field: String)(index: Int): JsonPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonPath = index(i)

  final def index(i: Int): JsonPath =
    JsonPath(arr.andThen(Index.index(i)(Index.vectorIndex[Json])))

  final def each: JsonTraversalPath =
    JsonTraversalPath(json.andThen(jsonDescendants))

  final def filterByIndex(p: Int => Boolean): JsonTraversalPath =
    JsonTraversalPath(arr.andThen(FilterIndex.filterIndex(p)(FilterIndex.vectorFilterIndex[Json])))

  final def filterByField(p: String => Boolean): JsonTraversalPath =
    JsonTraversalPath(obj.andThen(FilterIndex.filterIndex(p)))

  final def filterUnsafe(p: Json => Boolean): JsonPath =
    JsonPath(json.andThen(UnsafeOptics.select(p)))

  final def filter(p: Json => Boolean): JsonFoldPath =
    JsonFoldPath(filterUnsafe(p).json.asFold)

  final def as[A](implicit decode: Decoder[A], encode: Encoder[A]): Optional[Json, A] =
    json.andThen(UnsafeOptics.parse)

  final def atAs[A](field: String)(implicit decode: Decoder[A], encode: Encoder[A]): Optional[Json, Option[A]] =
    at(field).andThen(UnsafeOptics.optionParse)
}

object JsonPath {
  final val root: JsonPath = JsonPath(Iso.id)
}

final case class JsonTraversalPath(json: Traversal[Json, Json]) extends Dynamic {
  final def `null`: Traversal[Json, Unit] = json.andThen(jsonNull)
  final def boolean: Traversal[Json, Boolean] = json.andThen(jsonBoolean)
  final def byte: Traversal[Json, Byte] = json.andThen(jsonByte)
  final def short: Traversal[Json, Short] = json.andThen(jsonShort)
  final def int: Traversal[Json, Int] = json.andThen(jsonInt)
  final def long: Traversal[Json, Long] = json.andThen(jsonLong)
  final def bigInt: Traversal[Json, BigInt] = json.andThen(jsonBigInt)
  final def double: Traversal[Json, Double] = json.andThen(jsonDouble)
  final def bigDecimal: Traversal[Json, BigDecimal] = json.andThen(jsonBigDecimal)
  final def number: Traversal[Json, JsonNumber] = json.andThen(jsonNumber)
  final def string: Traversal[Json, String] = json.andThen(jsonString)
  final def arr: Traversal[Json, Vector[Json]] = json.andThen(jsonArray)
  final def obj: Traversal[Json, JsonObject] = json.andThen(jsonObject)

  final def at(field: String): Traversal[Json, Option[Json]] =
    obj.andThen(At.at(field))

  final def selectDynamic(field: String): JsonTraversalPath =
    JsonTraversalPath(obj.andThen(Index.index(field)))

  final def applyDynamic(field: String)(index: Int): JsonTraversalPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonTraversalPath = index(i)

  final def index(i: Int): JsonTraversalPath =
    JsonTraversalPath(arr.andThen(Index.index(i)(Index.vectorIndex[Json])))

  final def each: JsonTraversalPath =
    JsonTraversalPath(json.andThen(jsonDescendants))

  final def filterByIndex(p: Int => Boolean): JsonTraversalPath =
    JsonTraversalPath(arr.andThen(FilterIndex.filterIndex(p)(FilterIndex.vectorFilterIndex[Json])))

  final def filterByField(p: String => Boolean): JsonTraversalPath =
    JsonTraversalPath(obj.andThen(FilterIndex.filterIndex(p)))

  final def filterUnsafe(p: Json => Boolean): JsonTraversalPath =
    JsonTraversalPath(json.andThen(UnsafeOptics.select(p)))

  final def filter(p: Json => Boolean): JsonFoldPath =
    JsonFoldPath(filterUnsafe(p).json.asFold)

  final def as[A](implicit decode: Decoder[A], encode: Encoder[A]): Traversal[Json, A] =
    json.andThen(UnsafeOptics.parse)

  final def atAs[A](field: String)(implicit decode: Decoder[A], encode: Encoder[A]): Traversal[Json, Option[A]] =
    at(field).andThen(UnsafeOptics.optionParse)
}

final case class JsonFoldPath(json: Fold[Json, Json]) extends Dynamic {
  final def `null`: Fold[Json, Unit] = json.andThen(jsonNull)
  final def boolean: Fold[Json, Boolean] = json.andThen(jsonBoolean)
  final def byte: Fold[Json, Byte] = json.andThen(jsonByte)
  final def short: Fold[Json, Short] = json.andThen(jsonShort)
  final def int: Fold[Json, Int] = json.andThen(jsonInt)
  final def long: Fold[Json, Long] = json.andThen(jsonLong)
  final def bigInt: Fold[Json, BigInt] = json.andThen(jsonBigInt)
  final def double: Fold[Json, Double] = json.andThen(jsonDouble)
  final def bigDecimal: Fold[Json, BigDecimal] = json.andThen(jsonBigDecimal)
  final def number: Fold[Json, JsonNumber] = json.andThen(jsonNumber)
  final def string: Fold[Json, String] = json.andThen(jsonString)
  final def arr: Fold[Json, Vector[Json]] = json.andThen(jsonArray)
  final def obj: Fold[Json, JsonObject] = json.andThen(jsonObject)

  final def at(field: String): Fold[Json, Option[Json]] =
    obj.andThen(At.at(field))

  final def selectDynamic(field: String): JsonFoldPath =
    JsonFoldPath(obj.andThen(Index.index(field)))

  final def applyDynamic(field: String)(index: Int): JsonFoldPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonFoldPath = index(i)

  final def index(i: Int): JsonFoldPath =
    JsonFoldPath(arr.andThen(Index.index(i)))

  final def each: JsonFoldPath =
    JsonFoldPath(json.andThen(jsonDescendants))

  final def filterByIndex(p: Int => Boolean): JsonFoldPath =
    JsonFoldPath(arr.andThen(FilterIndex.filterIndex(p)))

  final def filterByField(p: String => Boolean): JsonFoldPath =
    JsonFoldPath(obj.andThen(FilterIndex.filterIndex(p)))

  final def filter(p: Json => Boolean): JsonFoldPath =
    JsonFoldPath(json.andThen(UnsafeOptics.select(p)))

  final def as[A](implicit decode: Decoder[A], encode: Encoder[A]): Fold[Json, A] =
    json.andThen(UnsafeOptics.parse)

  final def atAs[A](field: String)(implicit decode: Decoder[A], encode: Encoder[A]): Fold[Json, Option[A]] =
    at(field).andThen(UnsafeOptics.optionParse)
}

object UnsafeOptics {

  /**
   * Decode a value at the current location.
   *
   * Note that this operation is not lawful, since decoding is not injective (as noted by Julien
   * Truffaut). It is provided here for convenience, but may change in future versions.
   */
  def parse[A](implicit decode: Decoder[A], encode: Encoder[A]): Prism[Json, A] =
    Prism[Json, A](decode.decodeJson(_) match {
      case Right(a) => Some(a)
      case Left(_)  => None
    })(encode(_))

  /**
   * Decode a value at the current location.
   * But give Option[A] instead of A in order to treat non-exist field as None
   *
   * Note that this operation is not lawful, since the same reason as above
   * It is provided here for convenience, but may change in future versions.
   */
  final val keyMissingNone: Option[None.type] = Some(None)
  def optionParse[A](implicit decode: Decoder[A], encode: Encoder[A]): Prism[Option[Json], Option[A]] =
    Prism[Option[Json], Option[A]] {
      case Some(json) =>
        decode.decodeJson(json) match {
          case Right(a) => Some(Some(a))
          case Left(_)  => None
        }
      case None => keyMissingNone
    }(_.map(encode(_)))

  /**
   * Select if a value matches a predicate
   *
   * Note that this operation is not lawful because the predicate could be invalidated with set or modify.
   * However `select(_.a > 10) andThen b` is safe because once we zoom into `b`, we cannot change `a` anymore.
   */
  def select[A](p: A => Boolean): Prism[A, A] =
    Prism[A, A](a => if (p(a)) Some(a) else None)(Predef.identity)
}
