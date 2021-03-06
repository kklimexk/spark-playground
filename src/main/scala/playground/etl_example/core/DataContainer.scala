package playground.etl_example.core

import org.apache.spark.sql.SparkSession
import playground.etl_example.core.DataContainer.DatasetName

import scala.language.higherKinds

private[core] trait DataContainer[F[_], A] {
  protected def inputDataset(implicit sparkSession: SparkSession): F[A]

  protected def mapDataset(inputDataset: F[A])(implicit sparkSession: SparkSession): Map[DatasetName, F[_]]

  protected def combineDatasets(inputDatasets: Map[DatasetName, F[_]])
                               (implicit sparkSession: SparkSession): Option[Map[DatasetName, F[_]]] = None

  protected def writePath: String =
    throw new NotImplementedError(s"writePath is not defined!")

  protected def writeFunc(datasets: Map[DatasetName, F[_]], writePath: String): Unit =
    throw new NotImplementedError("writeFunc is not implemented!")
}

object DataContainer {

  trait DatasetName

  def run[F[_], A](c: DataContainer[F, A], write: Boolean = false)
                  (implicit sparkSession: SparkSession): Map[DatasetName, F[_]] = {
    val mapped = c.mapDataset(c.inputDataset)
    val maybeCombined = c.combineDatasets(mapped).getOrElse(mapped)
    //side effect
    if (write) c.writeFunc(maybeCombined, c.writePath)

    maybeCombined
  }
}
