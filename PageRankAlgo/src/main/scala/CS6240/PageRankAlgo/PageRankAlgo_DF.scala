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





object PageRankMainDF {
  
    def main(args: Array[String]) {
      val logger: org.apache.log4j.Logger = LogManager.getRootLogger
    
      val conf = new SparkConf().setAppName("Page Rank")
      val sc = new SparkContext(conf)
      val sqlContext = new SQLContext(sc)
      val spark = SparkSession.builder().appName("Page Rank").master("local").getOrCreate

      import sqlContext.implicits._
      val iterations = 6
      
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

 
   
    
     val Graph = graphMap.toSeq.toDF("v1","v2").persist()
     var Ranks = rankMap.toSeq.toDF("v1","pr")

 
      var delta = 0.0

      for(i <- 1 to iterations)
        
      {
          val zeroNode = Seq((0,0.0)).toDF()
        
//         // zeroNode.show()
          Ranks.union(zeroNode)
        
     
        val df = Graph.join(Ranks, Seq("v1")).select($"v2",$"pr".alias("pageRank"))
        
        val Temp = Ranks.join(df, $"v1" === $"v2", "left_outer").select("v1","pageRank")
                        .na.fill(0.0, Seq("pageRank"))
                        
        val Temp2 = Temp.groupBy($"v1").agg(sum($"pageRank").alias("pageRank"))
        
                
        val delta = Temp2.filter(Temp2("v1").equalTo(0)).select("pageRank").head().getDouble(0)
          
        Ranks = Temp2.filter(Temp2("v1") !== 0).select($"v1",$"pageRank" + delta/(k*k) as ("pr")).union(zeroNode)
        

        
      }

      
      var top100 = Ranks.sort(desc("pr")).take(100)
 
      top100.foreach(println) 
      
      Ranks.show()
      
     Ranks.rdd.saveAsTextFile(args(1))

   
  }
}