Spark Twitter aggregator
 We only work with the edges data from http://socialcomputing.asu.edu/datasets/Twitter Like in the previous assignment, your programs should output the number of followers for each user, returning output formatted like this, each user and follower count in a different line:
(userID1, number_of_followers_this_user_has) (userID2, number_of_followers_this_user_has)

Installation.

Please make sure the following compnents are installed:
- JDK 1.8
- Scala 2.11.12
- Hadoop 2.9.1
- Spark 2.3.1 (without bundled Hadoop)
- Maven
- AWS CLI (for EMR execution)

Environment Setup:

example bash alias configurations
Please add these configs to your ~/.bashrc file

export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export HADOOP_HOME=/usr/local/hadoop
export YARN_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export SCALA_HOME=/usr/share/scala-2.11
export SPARK_HOME=/usr/lib/spark
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$SCALA_HOME/bin:$SPARK_HOME/bin
export SPARK_DIST_CLASSPATH=$(hadoop classpath)

Explicitly set JAVA_HOME in $HADOOP_HOME/etc/hadoop/hadoop-env.sh:
export JAVA_HOME=/usr/lib/jvm/java-8-oracle

Execution of the program:

All of the build & execution commands are organized in the Makefile.
1 Open command prompt in the project folder.
2 Navigate to directory where project files unzipped.
3 Edit the Makefile to customize the environment at the top.
	Sufficient for standalone: hadoop.root, jar.name, local.input
	Other defaults acceptable for running standalone.

example makefile environment configuration (customie this for your environment)

spark.root=/usr/lib/spark
hadoop.root=/usr/local/hadoop
app.name=Twitter Followers
jar.name=twitter-foll.jar
maven.jar.name=spark-demo-1.0.jar
job.name=wc.TwitterFollowersMain
local.master=local[4]
local.input=input
local.output=output
# Pseudo-Cluster Execution
hdfs.user.name=vaibhav
hdfs.input=input
hdfs.output=output
# AWS EMR Execution
aws.emr.release=emr-5.17.0
aws.bucket.name=vaibhav-sparkdemo
aws.subnet.id=subnet-395fd217
aws.input=input
aws.output=output
aws.log.dir=log
aws.num.nodes=5
aws.instance.type=m4.large



For Standalone Hadoop execution enter the following in the terminal
	make switch-standalone	 //set standalone Hadoop environment (execute once)
	make local

For AWS EMR Hadoop: (you must configure all the emr.* config parameters at top of Makefile)
	make upload-input-aws    //run only before first execution
	make aws		 //Executes on AWS.check for successful execution with web interface after completetion (aws.amazon.com)
	download-output-aws      //after successful execution & termination to download the output files
