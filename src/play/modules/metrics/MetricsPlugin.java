package play.modules.metrics;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.modules.metrics.MetricsPlugin.TimerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class MetricsPlugin extends PlayPlugin {
    public static class TimerFactory {
        
        private String appNameSpace;

        public TimerFactory(String appNameSpace) {
            this.appNameSpace = appNameSpace;
        }

        public Timer createTimer(String group, String type, String timerName) {
            return Metrics.newTimer(new MetricName(group, type, timerName), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        }
    }
    
    private final static Map<String, Timer> reponseTimers = new HashMap<String, Timer>();
    private final static Map<String, Timer> methodTimers = new HashMap<String, Timer>();
    protected static ThreadLocal<TimerContext> reponseTimerContext = new ThreadLocal<TimerContext>();
    protected static ThreadLocal<LinkedList<TimerContext>> methodTimerContexts = new ThreadLocal<LinkedList<TimerContext>>();
    protected static ThreadLocal<Integer> callDepth = new ThreadLocal<Integer>();
    
    private static TimerFactory TIMER_FACTORY;
    
    @Override
    public void onApplicationStart() {
        initTimerFactory();
//        System.out.println("Metrics starts");
    }

    private static void initTimerFactory() {
        String applicationName = Play.applicationPath.toString().replace("/", ".");
        TIMER_FACTORY = new TimerFactory(applicationName);
    }
    
    @Override
    public void beforeActionInvocation(Method actionMethod) {
        String actionName = actionMethod.getName();
        Timer responseTimer = getTimer("inativ.com.requests.timer", actionMethod.getDeclaringClass().getSimpleName(), actionName, reponseTimers);    
        reponseTimerContext.set(responseTimer.time());
    }

    private static Timer getTimer(String group, String type, String timerName, Map<String, Timer> timerRegistry) {
        Timer responseTimer = timerRegistry.get(timerName);
        
        if (responseTimer == null) {
            responseTimer =  getTimerFactory().createTimer(group, type, timerName);
            timerRegistry.put(timerName, responseTimer);
        }
        
        return responseTimer;
    }

    private static TimerFactory getTimerFactory() {
        if (TIMER_FACTORY == null) {
            initTimerFactory();
        }
        return TIMER_FACTORY;
    }
    
    @Override
    public void afterActionInvocation() {
        TimerContext responseTimerCtx = reponseTimerContext.get();
        
        if (responseTimerCtx != null)
            responseTimerCtx.stop();
    }
    
    @Override
    public void enhance(ApplicationClass applicationClass) throws Exception {
//        new MetricsModelEnhancer().enhanceThisClass(applicationClass);
//        new MetricAnnotationEnhancer().enhanceThisClass(applicationClass);
//        new MetricsAllEnhancer().enhanceThisClass(applicationClass);
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
        System.out.println(getIndentation(currentCallDepth) + "->Start::"+ timerName);
//        System.out.println("Activating metrics timer " + timerName);
        Timer timer = getTimer(group, type, timerName, methodTimers);
        LinkedList<TimerContext> timerContexts =  methodTimerContexts.get();
        if (timerContexts == null) {
            timerContexts = new LinkedList<TimerContext>();
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
        System.out.println(getIndentation(--currentCallDepth) + "->Stop::"+ timerName);
        LinkedList<TimerContext> timerContexts =  methodTimerContexts.get();
        if (timerContexts != null) {
            timerContexts.pop().stop();
//            System.out.println("Metrics timer stopped");
        }
        callDepth.set(currentCallDepth);
    }
}
