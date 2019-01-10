explore the subtleties of Spark’s actions and transformations. Recall that transformations describe data manipulations, but do not force execution. Actions, on the other hand, force an immediate execution of all upstream operations needed to produce the desired result. What will happen when an iterative program performs both actions and transformations in a loop? You will find out by exploring a program that computes PageRank with dangling pages for a simple graph. The program works with two data tables: Graph stores pairs (v1, v2), each
encoding an edge from some vertex v1 to another vertex v2. Ranks stores pairs
(v1, pr), encoding the PageRank pr for each vertex v1. To fill these tables with
data, create a graph that consists of k linear chains, each with k vertices.
Number the nodes from 1 to k2, chain by chain. Here k should be a parameter to
control problem size. The figure shows an example for k=3. When creating the
corresponding (v1, v2) pairs for Graph, make sure that all dangling pages are added to Graph with vertex 0 (zero) as the v2 value. For the Ranks table, include the k2 real vertices, each initially with pr = 1/k2. Also
   
add the dummy vertex 0 (zero) with pr=0. Since each vertex in the graph has only one or zero outlinks, we implement the following simplified PageRank algorithm with RDDs (you decide if you need regular RDDs or pair RDDs) in Spark Scala:
1. Given k, create RDDs Graph and Ranks for the k-by-k graph data as described above. You do not need to load the data from files but can directly generate it in your program. Make sure each RDD has at least two partitions. As an optional challenge, try to enforce that Graph and Ranks have the same Partitioner.
2. For i = 1 to 10
a. Join Graph with Ranks on v1 to create a new RDD of triples (v1, v2, pr).
b. Map each (v1, v2, pr) to (v2, pr) to create outgoing PR contributions. Let us call this
result Temp.
c. Group Temp by v2 and sum up the pr values in each group. Next, we handle the
dangling PR mass.
d. In Temp2, look up the pr value associated with v2=0 (the dummy vertex). Let us call this
value delta.
e. Add delta/k2 to the pr value of each record in Temp2, except for v2=0. Make sure that
this final result RDD “overwrites” Ranks, so that the next iteration works with it, instead
of the previous Ranks table.
f. Sum up all pr values in Ranks and check if that sum is close to 1.0. (It may not be exactly
1.0 due to numerical issues.)
3. Collect the top-100 (by pr value) tuples from the final Ranks RDD as a list at the driver.

Installation.

Please make sure the following compnents are installed:
- JDK 1.8
- Hadoop 2.9.1
- Maven
- AWS CLI (for EMR execution)

Environment Setup:

example bash alias configurations
Please add these configs to your ~/.bashrc file

export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export HADOOP_HOME=/usr/local/hadoop
export YARN_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

Explicitly set JAVA_HOME in $HADOOP_HOME/etc/hadoop/hadoop-env.sh:
export JAVA_HOME=/usr/lib/jvm/java-8-oracle

Execution of the program:

changing the job.name in make file with the objectname as per the program you want to run

All of the build & execution commands are organized in the Makefile.
1 Open command prompt.
2 Navigate to directory where project files unzipped.
3 Edit the Makefile to customize the environment at the top.
	Sufficient for standalone: hadoop.root, jar.name, local.input
	Other defaults acceptable for running standalone.

example makefile environment customization (customie this for your environment):

hadoop.root=/usr/local/hadoop
jar.name=twitter-foll-mr-1.0.jar
jar.path=target/${jar.name}
job.name=wc.TwitterFollAggregator
local.input=input
local.output=output
# Pseudo-Cluster Execution
hdfs.user.name=vaibhav
hdfs.input=input
hdfs.output=output
# AWS EMR Execution
aws.emr.release=emr-5.17.0
aws.region=us-east-1
aws.bucket.name=vaibhav-mrdemo
aws.subnet.id=subnet-395fd217
aws.input=input
aws.output=output
aws.log.dir=log
aws.num.nodes=10
aws.instance.type=m4.large


For Standalone Hadoop:
	make switch-standalone	 //set standalone Hadoop environment (execute once)
	make local

For AWS EMR Hadoop: (you must configure all the emr.* config parameters at top of Makefile)
	make upload-input-aws    //run only before first execution
	make aws		 //Executes on AWS.check for successful execution with web interface after completetion (aws.amazon.com)
	download-output-aws      //after successful execution & termination to download the output files
