package org.apache.hadoop.metrics2.riemann;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.metrics2.AbstractMetric;
import org.apache.hadoop.metrics2.MetricsRecord;
import org.apache.hadoop.metrics2.MetricsSink;
import org.apache.hadoop.metrics2.MetricsTag;
import org.apache.hadoop.metrics2.util.Servers;
import org.apache.hadoop.net.NetUtils;

import com.aphyr.riemann.Proto.Event;
import com.aphyr.riemann.client.RiemannClient;

@SuppressWarnings("unused")
public class RiemannContext implements MetricsSink {

  private RiemannClient riemannClient;
  private static final String SERVER_NAME_PROPERTY = "server";
  private static final String SERVICE_NAME_PROPERTY = "service";
  private static final int DEFAULT_RIEMANN_PORT = 5555;
  Queue<List<Event>> metricsQueue = new LinkedBlockingDeque<List<Event>>();
  public final Log LOG = LogFactory.getLog(this.getClass());
  private String serviceName;

  @Override
  public void init(SubsetConfiguration conf) {
    LOG.info("Initializing the Riemann Sink");
    InetSocketAddress socketAddress = NetUtils.createSocketAddr(conf.getString
        (SERVER_NAME_PROPERTY), DEFAULT_RIEMANN_PORT);
    try {
      riemannClient = RiemannClient.tcp(socketAddress.getHostName(),socketAddress.getPort());
      riemannClient.connect();
    } catch (Exception e) {
      LOG.error("Failed to create Riemann Client");
    }
    serviceName = conf.getString(SERVICE_NAME_PROPERTY, "hadoop");
  }

  @Override
  public void putMetrics(MetricsRecord metricsRecord) {
    String hostname = null;
    for (MetricsTag tag: metricsRecord.tags()){
      if (tag.name().equals("Hostname")){
        hostname = tag.value();
      } else {
        hostname = "noHostnameListed";
      }
    }

    //To avoid domain name hierarchy to interfere with graphite metric hierarchy
    hostname = hostname.replace('.', '-');

    long tm = System.currentTimeMillis() / 1000;
    // Graphite doesn't handle milliseconds
    List<Event> events = new ArrayList<Event>();
    StringBuilder metricName = new StringBuilder();

    for (AbstractMetric metric : metricsRecord.metrics()) {
      metricName.append(serviceName).
      append(".").
      append(metric.name()).
      append(".").
      append(metric.type().name());

      events.add(riemannClient.event().
          host(hostname).
          service(metricName.toString()).
          metric(metric.value().doubleValue()).
          build());

      metricName = new StringBuilder();
    }

    metricsQueue.add(events);
  }

  @Override
  public void flush() {
    try {
      while (metricsQueue.size() > 0){
        if (riemannClient.aSendEventsWithAck(metricsQueue.remove())
            .deref(2, java.util.concurrent.TimeUnit.SECONDS) == null) {
          LOG.error("Failed to send some events to Riemann");
        }
      }
    } catch (Exception e) {
      LOG.error(e);
    }
  }
}
