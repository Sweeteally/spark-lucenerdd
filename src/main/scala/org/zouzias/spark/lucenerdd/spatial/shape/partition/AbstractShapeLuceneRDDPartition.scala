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
package org.zouzias.spark.lucenerdd.spatial.shape.partition

import org.zouzias.spark.lucenerdd.response.LuceneRDDResponsePartition
import org.zouzias.spark.lucenerdd.spatial.shape.ShapeLuceneRDD.PointType

import scala.reflect.ClassTag

private[shape] abstract class AbstractShapeLuceneRDDPartition[K, V] extends Serializable
  with AutoCloseable {

  protected implicit def kTag: ClassTag[K]
  protected implicit def vTag: ClassTag[V]

  def size: Long

  def iterator: Iterator[(K, V)]

  def isDefined(key: K): Boolean

  /**
   * Search for points within a circle
   *
   * @param center center of circle
   * @param radius radius of circle in kilometers (KM)
   * @param k number of points to return
   * @return
   */
  def circleSearch(center: PointType, radius: Double, k: Int, operationName: String)
  : LuceneRDDResponsePartition

  /**
   * Restricts the entries to those satisfying a predicate
   *
   * @param pred Predicate to filter on
   * @return
   */
  def filter(pred: (K, V) => Boolean): AbstractShapeLuceneRDDPartition[K, V]
}
