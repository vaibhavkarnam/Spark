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
import scala.collection.mutable.ListBuffer


object ShortestPathMain {
  
    def extractVertices(nodes : (String,(List[String],Int))): List[(String, Int)] = {
  
    var nodeList = new ListBuffer[(String,Int)]()
      for (vertices <- nodes._2._1)
      {
        if(!vertices.equals(""))
        {
        val node = (vertices,nodes._2._2 + 1)
        nodeList += node
        }
      }
    
      val node = (nodes._1,nodes._2._2)
      
      nodeList += node
          
      return nodeList.toList
    }
  
    
    def main(args: Array[String]) {
      val logger: org.apache.log4j.Logger = LogManager.getRootLogger
      if (args.length != 2) {
        logger.error("Usage:\nwc.ShortestPathMain <input dir> <output dir>")
        System.exit(1)
    }
    
      
      val conf = new SparkConf().setAppName("Shortest Path")
      val sc = new SparkContext(conf)

      val iterations = 9
      val source = "1";
      
      
       val textFile = sc.textFile(args(0))
       
       val graph = textFile
       .map{case(line) => (line.split(" ")(0),if 
           (line.split(" ").size > 1) 
            line.split(" ")(1)
       else
         "")}
       .map{case(line) => (line._1,line._2.split(",").toList)}
       
       graph.persist()
       
       //stores the source adjacency list
      
       val sourceadjacentNodesList = graph.filter(x => x._1.equals(source))
                                          .map(x => x._2).collect()(0)
       
       
       //creates the pairRDD(node,distance) for each of the distinct nodes and nodes which are in 
       // adjacency list of source will have distance 1 and rest all of them will have distance 0
       var nodesWithDistance = graph.flatMap(x => x._2).filter(x => !x.equals(""))
                                    .distinct()
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
                                    }
       
       
                                                                     
//                                    
       for(i <- 1 to iterations)      
         {
          // Use Accumulator instead to determine when last iteration is reached
          nodesWithDistance = graph.join(nodesWithDistance)
          
                                   .flatMap{case(n,(adjList, currentDistanceOfN))
                                     => extractVertices(n,(adjList, currentDistanceOfN))}
                                   .reduceByKey((x, y) => Math.min(x, y))
          // Remember the shortest of the distances found
         }                             
       
          // adjacentNodesList.foreach(println)
       
       
         nodesWithDistance.collect().foreach(println)
         
         nodesWithDistance.saveAsTextFile(args(1))
    }
       
}
     
      

