package CS6240.ShortestPath

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.log4j.LogManager
import org.apache.log4j.Level
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession





object ShortestPathDF {
  
    def main(args: Array[String]) {
      val logger: org.apache.log4j.Logger = LogManager.getRootLogger
    
      val conf = new SparkConf().setAppName("Page Rank")
      val sc = new SparkContext(conf)
      val sqlContext = new SQLContext(sc)
      val spark = SparkSession.builder().appName("Page Rank").master("local").getOrCreate

      import sqlContext.implicits._
      val iterations = 9
      
      val source = "1"
      
      val textFile = sc.textFile(args(0))
      
      
       val graphRdd = textFile
       .map{case(line) => (line.split(" ")(0),if 
           (line.split(" ").size > 1) 
            line.split(" ")(1)
       else
         "")}
       .map{case(line) => (line._1,line._2.split(",").toList)}
       
       

       val graph = graphRdd.toDF("node","adjList").persist();
       
       val sourceadjacentNodesList = graph.filter(graph("node")
                                          .equalTo(source))
                                          .select("adjList")
                                          .rdd.map(r => r(0)
                                          .asInstanceOf[Seq[String]]).collect()(0)
                                          
       var distinctNodes = graphRdd.map(x => x._2)
                                   .flatMap(x => x)
                                    .distinct()
                                    .filter(x => !x.equals(""))
                                    .map{case(x) => 
                                      if(x.equals(source))
                                      {
                                        (x,0)
                                      }
                                      else if(sourceadjacentNodesList.exists(node => node.equals(x)))
                                      {
                                        (x,1)
                                      }
                                      else
                                      {
                                        (x,99999999)
                                      }
                                    }.toDF("node","distance")                                   
                                          
                                          
     
           
            for(i <- 1 to iterations)      
         {
          // Use Accumulator instead to determine when last iteration is reached
          distinctNodes = graph.join(distinctNodes, Seq("node"))
          
          distinctNodes.show()

          // Remember the shortest of the distances found
         }       
         distinctNodes.collect().foreach(println)
         
         distinctNodes.rdd.saveAsTextFile(args(1))  
  
  }
}