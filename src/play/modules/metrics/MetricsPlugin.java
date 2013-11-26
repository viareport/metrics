package play.modules.metrics;

import java.util.LinkedList;

import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class MetricsPlugin extends PlayPlugin {

    public static final MetricRegistry PLUGIN_REGISTRY = new MetricRegistry();
    protected static ThreadLocal<LinkedList<Context>> methodTimerContexts = new ThreadLocal<LinkedList<Context>>();
    protected static ThreadLocal<Integer> callDepth = new ThreadLocal<Integer>();

    public static Timer getTimer(String namespaceToken, String ... namespaceTokens) {
        String timerName = PLUGIN_REGISTRY.name(namespaceToken, namespaceTokens);
        return PLUGIN_REGISTRY.timer(timerName);
    }
    
    @Override
    public void enhance(ApplicationClass applicationClass) throws Exception {
//        new MetricsModelEnhancer().enhanceThisClass(applicationClass);
          new MetricAnnotationEnhancer().enhanceThisClass(applicationClass);
          //new MetricsAllEnhancer().enhanceThisClass(applicationClass);
    }
    
    private static String getIndentation(int depth) {
        StringBuffer sBuf = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sBuf.append("-");
        }
        return sBuf.toString();
    }
    
    public static void enableTimer(String group, String type, String timerName) {
        int currentCallDepth = getCallDepth();
        //System.out.println(getIndentation(currentCallDepth) + "->Start::"+ timerName);
//        System.out.println("Activating metrics timer " + timerName);
        Timer timer = getTimer(group, type, timerName);
        LinkedList<Context> timerContexts =  methodTimerContexts.get();
        if (timerContexts == null) {
            timerContexts = new LinkedList<Context>();
            methodTimerContexts.set(timerContexts);
//            System.out.println("Metrics timer activated " + timerName);
        }
        
        timerContexts.addFirst(timer.time());
        callDepth.set(++currentCallDepth);
    }

    private static Integer getCallDepth() {
        Integer callDepthValue = callDepth.get();
        if (callDepthValue == null) {
            callDepthValue = new Integer(0);
            callDepth.set(callDepthValue);
        }
        return callDepthValue;
    }
    
    public static void stopTimer(String timerName) {
        int currentCallDepth = getCallDepth();
       // System.out.println(getIndentation(--currentCallDepth) + "->Stop::"+ timerName);
        LinkedList<Context> timerContexts =  methodTimerContexts.get();
        if (timerContexts != null) {
            timerContexts.pop().stop();
//            System.out.println("Metrics timer stopped");
        }
        callDepth.set(currentCallDepth);
    }

    public static MetricRegistry getRegistry() {
        return PLUGIN_REGISTRY;
    }

    public static void reset() {
        for (String timerName: PLUGIN_REGISTRY.getTimers().keySet()) {
            PLUGIN_REGISTRY.remove(timerName);
        }
    }
}
