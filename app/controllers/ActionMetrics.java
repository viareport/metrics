package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import play.mvc.Controller;
import play.mvc.With;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

@With(AccessControl.class)
public class ActionMetrics extends Controller {
    @Perm("View Controller Metrics")
    public static void index() {
        MetricsRegistry registry = Metrics.defaultRegistry();
        HashMap<String, HashMap<String, List<String>>> controllers = new HashMap<String, HashMap<String, List<String>>>();
        for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : registry.groupedMetrics().entrySet()) {
            String[] nameTokens = entry.getKey().split("\\.");
            String controllerName = nameTokens[nameTokens.length - 1];
            SortedMap<MetricName, Metric> metricsMap = entry.getValue();
            
            HashMap<String, List<String>> metrics = new HashMap<String, List<String>>();
            for (Map.Entry<MetricName, Metric> controllerMetric: metricsMap.entrySet()) {
                List<String> values = new ArrayList<String>();
                String metricType = controllerMetric.getKey().getName();
                
                Timer timer = (Timer) controllerMetric.getValue();
                values.add(String.valueOf(timer.count()));
                values.add(String.valueOf(timer.mean()) + " ms");
                values.add(String.valueOf(timer.min()) + " ms");
                values.add(String.valueOf(timer.max()) + " ms");
                metrics.put(metricType, values);
            }
            controllers.put(controllerName, metrics);
        }
        render(controllers);
    }
}
