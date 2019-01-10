package CS6240.PageRankAlgo

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.log4j.LogManager
import org.apache.log4j.Level
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import scala.collection.mutable.ListBuffer


object PageRankMain {
  
    def main(args: Array[String]) {
      val logger: org.apache.log4j.Logger = LogManager.getRootLogger
    
      val conf = new SparkConf().setAppName("Page Rank")
      val sc = new SparkContext(conf)
      val sqlContext = new SQLContext(sc)
      val spark = SparkSession.builder().appName("Page Rank").master("local").getOrCreate

      import sqlContext.implicits._
      val iterations = 10
      
      var nodeList = new ListBuffer[(Double)]()

      
      var k = 100
      var graphMap = scala.collection.mutable.Map[Int, Int]()
      for (i <- 1 to k*k) {
       val j = i+1
       if(i%k == 0)
       {
         graphMap +=(i -> 0)
       }
       else
       {
        graphMap += (i -> j)
       }
      }
     
      var pr = 1.toDouble/(k*k)
      
      var rankMap = scala.collection.mutable.Map[Int, Double]()
      for (i <- 1 to k*k) {

              rankMap +=(i -> pr)
   
      }
      rankMap +=(0 -> 0)

 
      val Graph = sc.parallelize(graphMap.toSeq).groupByKey().persist()
            
      var Ranks = sc.parallelize(rankMap.toSeq)
      
      var delta = 0.0

      for(i <- 1 to iterations)
        
      {
     
        val initalJoin = Graph.join(Ranks)
                               .flatMap({case (v1, (v2, pr)) 
                        => v2.map(v2 => (v2, pr))})
       
                        val Temp = Ranks.leftOuterJoin(initalJoin)
                      .map(x => (x._1,x._2._2.getOrElse(0.0)))
                  
     val Temp2 = Temp.reduceByKey((x,y) => (x+y))
          
     if(Temp2.lookup(0).size != 0)
     {
      delta = Temp2.filter(x => x._1 == 0).first._2
     }
     Ranks = Temp2.filter(x => x._1 != 0).map(x => (x._1,x._2 + delta/(k*k)))
     .union(sc.parallelize(Seq((0, 0.0))))
     
     var sum = Ranks.map(_._2).sum()
     
            nodeList += sum

    }
    
     val output = Ranks.map(_._2).sum()
     
     val inRange = output match 
     {
          case x if (0.6 <= x && x < 1.5) => true
          case _ => false
      }
     
     val finalRanks = Ranks.sortBy(_._2, false).take(100)
     
     println(Ranks.toDebugString)
      
      finalRanks.foreach(println)
     
     //  nodeList.foreach(println)
     // println(output)
    Ranks.saveAsTextFile(args(1))
  
  }
}