/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zouzias.spark.lucenerdd.response

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.types.{IntegerType, StringType}
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import org.zouzias.spark.lucenerdd.{LuceneRDD, LuceneRDDKryoRegistrator}
import org.zouzias.spark.lucenerdd._
import org.zouzias.spark.lucenerdd.testing.FavoriteCaseClass

class LuceneRDDResponseSpec extends FlatSpec with Matchers
  with BeforeAndAfterEach
  with SharedSparkContext {

  override val conf = LuceneRDDKryoRegistrator.registerKryoClasses(new SparkConf().
    setMaster("local[*]").
    setAppName("test").
    set("spark.ui.enabled", "false").
    set("spark.app.id", appID))

  def randomString(length: Int): String = scala.util.Random.alphanumeric.take(length).mkString

  var luceneRDD: LuceneRDD[_] = _

  override def afterEach() {
    luceneRDD.close()
  }

  "LuceneRDDResponseSpec.take(k)" should "return exactly k elements" in {
    val array = Array("aaa", "bbb", "ccc", "ddd", "eee")
    val rdd = sc.parallelize(array)
    luceneRDD = LuceneRDD(rdd)
    val result = luceneRDD.query("*:*", 10)
    result.take(2).length should be (2)
  }

  "LuceneRDDResponseSpec.collect()" should "return all elements" in {
    val array = Array("aaa", "bbb", "ccc", "ddd", "eee")
    val rdd = sc.parallelize(array)
    luceneRDD = LuceneRDD(rdd)
    val result = luceneRDD.query("*:*", 10)
    result.collect().length should be (array.length)
  }

  "LuceneRDDResponseSpec.toDF()" should "convert to DataFrame" in {
    val sqlContext = SparkSession.builder().getOrCreate().sqlContext
    val elem = Array("fear", "death", "water", "fire", "house")
      .zipWithIndex.map{ case (str, index) =>
      FavoriteCaseClass(str, index, 10L, 10e-6F, s"${str}@gmail.com")}
    val rdd = sc.parallelize(elem)
    luceneRDD = LuceneRDD(rdd)
    val result = luceneRDD.query("*:*", 10).toDF(1.0)(sqlContext)
    val schema = result.schema

    schema.nonEmpty should equal(true)
    schema.fieldNames.contains("name") should equal(true)
    schema.fieldNames.contains("age") should equal(true)
    schema.fieldNames.contains("myLong") should equal(true)
    schema.fieldNames.contains("myFloat") should equal(true)
    schema.fieldNames.contains("email") should equal(true)

    // Extra auxiliary fields that must exist on the DataFrame
    schema.fieldNames.contains("__docid__") should equal(true)
    schema.fieldNames.contains("__score__") should equal(true)
    schema.fieldNames.contains("__shardIndex__") should equal(true)


    schema.fields(schema.fieldIndex("name")).dataType should equal(StringType)
    schema.fields(schema.fieldIndex("age")).dataType should equal(IntegerType)
    schema.fields(schema.fieldIndex("myLong")).dataType should
      equal(org.apache.spark.sql.types.LongType)
    schema.fields(schema.fieldIndex("myFloat")).dataType should
      equal(org.apache.spark.sql.types.FloatType)
    schema.fields(schema.fieldIndex("email")).dataType should equal(StringType)
  }
}
