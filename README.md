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



Troubleshooting protocol buffers version mismatch:

Depending on the Hadoop version, there could be a scenario where the following exception trace could be seen:

    java.lang.VerifyError: class com.aphyr.riemann.Proto$Msg overrides final method getUnknownFields.()Lcom/google/protobuf/UnknownFieldSet;
    at java.lang.ClassLoader.defineClass1(Native Method)
    at java.lang.ClassLoader.defineClass(ClassLoader.java:800)
    at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)
    at java.net.URLClassLoader.defineClass(URLClassLoader.java:449)
    at java.net.URLClassLoader.access$100(URLClassLoader.java:71)
    at java.net.URLClassLoader$1.run(URLClassLoader.java:361)
    at java.net.URLClassLoader$1.run(URLClassLoader.java:355)
    at java.security.AccessController.doPrivileged(Native Method)
    at java.net.URLClassLoader.findClass(URLClassLoader.java:354)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:425)
    at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:308)
    at java.lang.ClassLoader.loadClass(ClassLoader.java:358)
    at com.aphyr.riemann.client.TcpTransport.<clinit>(TcpTransport.java:34)
    at com.aphyr.riemann.client.RiemannClient.tcp(RiemannClient.java:36)
    at org.apache.hadoop.metrics2.riemann.RiemannContext.init(RiemannContext.java:44)
    at org.apache.hadoop.metrics2.impl.MetricsConfig.getPlugin(MetricsConfig.java:199)
    at org.apache.hadoop.metrics2.impl.MetricsSystemImpl.newSink(MetricsSystemImpl.java:478)
    at org.apache.hadoop.metrics2.impl.MetricsSystemImpl.configureSinks(MetricsSystemImpl.java:450)
    at org.apache.hadoop.metrics2.impl.MetricsSystemImpl.configure(MetricsSystemImpl.java:429)
    at org.apache.hadoop.metrics2.impl.MetricsSystemImpl.start(MetricsSystemImpl.java:180)
    at org.apache.hadoop.metrics2.impl.MetricsSystemImpl.init(MetricsSystemImpl.java:156)
    at org.apache.hadoop.metrics2.lib.DefaultMetricsSystem.init(DefaultMetricsSystem.java:54)
    at org.apache.hadoop.metrics2.lib.DefaultMetricsSystem.initialize(DefaultMetricsSystem.java:50)
    at org.apache.hadoop.hdfs.server.namenode.NameNode.createNameNode(NameNode.java:1176)
    at org.apache.hadoop.hdfs.server.namenode.NameNode.main(NameNode.java:1241)
    
This is due the fact that the Riemann Client uses Google Protocol Buffers to transmit the data to Riemann. It seems that there is a mismatch in the version of the protobuf library that some of Hadoop's versions use with that which Riemann uses. Riemann expects the version to be 2.5.0.

To fix the above problem simply remove the older version of the protobuf jar from the hadoop classpath (For me it was at : /usr/lib/hadoop/lib/protobuf-java-2.4.1.jar) and place the new one. Can be downloaded here: http://www.java2s.com/Code/JarDownload/protobuf/protobuf-java-2.5.0.jar.zip
