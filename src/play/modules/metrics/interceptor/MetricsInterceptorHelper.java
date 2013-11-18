package play.modules.metrics.interceptor;

import com.codahale.metrics.Timer;
import org.apache.log4j.Logger;
import play.modules.metrics.MetricsPlugin;

import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Set;

public class MetricsInterceptorHelper {
    protected static ThreadLocal<Set<Timer.Context>> reponseTimerContext = new ThreadLocal<Set<Timer.Context>>();


    public static void startTimer(String  namespaceToken, String ... namespaceTokens) {
        Set<Timer.Context> responseTimerCtx = getContexts();
        Timer overallTimer = MetricsPlugin.getTimer(namespaceToken, namespaceTokens);
        if (! responseTimerCtx.contains(overallTimer)) {
            responseTimerCtx.add(overallTimer.time());
        }
        reponseTimerContext.set(responseTimerCtx);
    }

    private static Set<Timer.Context> getContexts() {
        Set<Timer.Context> contexts = reponseTimerContext.get();
        if (contexts == null) {
            contexts = new HashSet<Timer.Context>();
        }
        return contexts;
    }


    public static void resetRequestTimers() {
        Set<Timer.Context> responseTimerCtx = getContexts();

        if (responseTimerCtx != null) {
            for (Timer.Context timerCtx: responseTimerCtx) {
                timerCtx.stop();
            }
            reponseTimerContext.set(null);
        } else {
            Logger.getLogger(MetricsInterceptorHelper.class).error("No context found when resetting timers");
        }
    }
}