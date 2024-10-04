package com.example

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*

import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.source.FromIteratorFunction
import org.apache.flink.util.Collector

import scala.jdk.CollectionConverters.*

import java.util.Arrays

case class Bar(id: String, baz: Option[String])
case class Foo(bar: Option[Bar])
case class Far(id: String, bar: Option[Bar])

object FoosSource:
  private val data =
    Array(
      Foo(Some(Bar("a", Some("b")))),
      Foo(Some(Bar("b", Some("b")))),
      Foo(Some(Bar("c", Some("b")))),
      Foo(None)
    )

  def iterator: FromIteratorFunction[Foo] =
    FromIteratorFunction[Foo](
      (new Iterator[Foo] with Serializable:
        var rows = data.iterator

        override def hasNext: Boolean = rows.hasNext

        override def next(): Foo =
          Thread.sleep(100)
          val next = rows.next

          // Going to the first element again
          if !hasNext then rows = data.iterator

          next
      ).asJava
    )

class FakeProcessFunction extends KeyedProcessFunction[String, Foo, Far]:
  override def processElement(
      event: Foo,
      ctx: KeyedProcessFunction[String, Foo, Far]#Context,
      out: Collector[Far]
  ): Unit =
    out.collect(
      Far(event.bar.map(_.id).getOrElse("empty") + "-mapped", event.bar)
    )

@main def foosStream =
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  env
    .addSource(FoosSource.iterator)
    .keyBy(_.bar.map(_.id).getOrElse("empty"))
    .process(FakeProcessFunction())
    .print()

  env.execute("FoosStream")
