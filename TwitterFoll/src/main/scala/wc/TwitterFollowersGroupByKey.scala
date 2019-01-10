package wc

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.log4j.LogManager
import org.apache.log4j.Level

object TwitterFollowersGroupByKey {
  
    def main(args: Array[String]) {
      val logger: org.apache.log4j.Logger = LogManager.getRootLogger
      if (args.length != 2) {
        logger.error("Usage:\nwc.TwitterFollowersMain <input dir> <output dir>")
        System.exit(1)
      }
    
      val conf = new SparkConf().setAppName("Twitter Followers")
      val sc = new SparkContext(conf)
    
      val textFile = sc.textFile(args(0))
      // splits the input line based on delimiter "," extracts the second value
      // assigns count 1 for each occurrence of the value and totals the count for 
      // each value
      val counts = textFile.map(line => line.split(",").last)
                   .map(word => (word, 1))
                   .groupByKey()
                   .map({case (x,y) => (x,y.sum)})
      println(counts.toDebugString)
      counts.saveAsTextFile(args(1))
    
  }
}
