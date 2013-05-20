package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import play.modules.metrics.MetricsPlugin;
import play.mvc.Controller;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class ActionMetrics extends Controller {
    public static void index() {
       MetricRegistry registry = MetricsPlugin.getRegistry();
       HashMap<String, HashMap<String, List<String>>> controllers = new HashMap<String, HashMap<String, List<String>>>();
       for (Map.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
           String[] nameTokens = entry.getKey().split("\\.");
           String controllerName = nameTokens[nameTokens.length - 1];
           
           HashMap<String, List<String>> metrics = controllers.get(controllerName);
           if (metrics == null) {
               metrics = Maps.newHashMap();
               controllers.put(controllerName, metrics);
           }
           Timer timer = entry.getValue();
           List<String> values = Lists.newArrayList();
           values.add(String.valueOf(timer.getCount()));
           values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMean(), TimeUnit.NANOSECONDS)) + " ms");
           values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMin(), TimeUnit.NANOSECONDS)) + " ms");
           values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMax(), TimeUnit.NANOSECONDS)) + " ms");
           metrics.put(entry.getKey(), values);
           
           controllers.put(controllerName, metrics);
       }
       render(controllers);
    }
}
