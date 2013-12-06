package controllers;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import play.modules.metrics.MetricsPlugin;
import play.mvc.Controller;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class ShowMetrics extends Controller {
    private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat( "###.##" );
    
    public static void index() {
       MetricRegistry registry = MetricsPlugin.getRegistry();
       HashMap<String, HashMap<String, List<String>>> controllers = new HashMap<String, HashMap<String, List<String>>>();
       for (Map.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
           String controllerName = entry.getKey().substring(0, entry.getKey().lastIndexOf('.'));
           
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
           String fullMethodName = entry.getKey();
           metrics.put(String.valueOf(fullMethodName.substring(fullMethodName.lastIndexOf(".") + 1)), values);
           controllers.put(controllerName, metrics);
       }
       render(controllers);
    }
    
    public static void csv() {
        StringBuilder content = new StringBuilder();
        
        //CSV HEADERS
        content.append(Joiner.on(", ").join(new String[] {"Timer", "Count", "Mean", "Median", "Std dev","Min", "Max"}));
        content.append("\n");
        
        MetricRegistry registry = MetricsPlugin.getRegistry();
        HashMap<String, HashMap<String, List<String>>> controllers = new HashMap<String, HashMap<String, List<String>>>();
        for (Map.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            String controllerName = entry.getKey().substring(0, entry.getKey().lastIndexOf('.'));
            
            HashMap<String, List<String>> metrics = controllers.get(controllerName);
            if (metrics == null) {
                metrics = Maps.newHashMap();
                controllers.put(controllerName, metrics);
            }
            Timer timer = entry.getValue();
            List<String> values = Lists.newArrayList();
            values.add(entry.getKey());
            values.add(String.valueOf(timer.getCount()));
            values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMean(), TimeUnit.NANOSECONDS)));
            values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMedian(), TimeUnit.NANOSECONDS)));
            values.add(DECIMAL_FORMATTER.format(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getStdDev(), TimeUnit.NANOSECONDS)));
            values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMin(), TimeUnit.NANOSECONDS)));
            values.add(String.valueOf(TimeUnit.MILLISECONDS.convert((long) timer.getSnapshot().getMax(), TimeUnit.NANOSECONDS)));
            
            content.append(Joiner.on(",").join(values));
            content.append("\n");
        }
        
        renderBinary(new ByteArrayInputStream(content.toString().getBytes()), "metrics.csv", "text/csv", true);
     }
    
}
