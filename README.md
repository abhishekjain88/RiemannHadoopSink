RiemannHadoopSink
=================

Sends metrics to Riemann Servers

Compile:

    $ mvn clean package

The dependencies also need to be part of the jar, that is why a fat jar is required for the same.

Installation:

In your hadoop-env.sh file (usually in /etc/hadoop/conf/), add the location of the RiemannHadoopSink.jar file into the HADOOP_CLASSPATH

example: export HADOOP_CLASSPATH="/[path_to]/RiemannHadoopSink-1.0-SNAPSHOT.jar

Configuration:

In your hadoop-metrics2.properties file, add the following for all metrics

    *.sink.riemann.class=org.apache.hadoop.metrics2.riemann.RiemannContext
    # default sampling period
    *.period=10
    *.sink.riemann.server=localhost:5555
    #to distinguish the metric name based on the services
    namenode.sink.riemann.service=namenode
    datanode.sink.riemann.service=datanode
    jobtracker.sink.riemann.service=jobtracker
    tasktracker.sink.riemann.service=tasktracker
