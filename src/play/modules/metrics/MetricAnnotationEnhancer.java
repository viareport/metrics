package play.modules.metrics;

import java.util.Arrays;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class MetricAnnotationEnhancer extends MetricsEnhancer {

    @Override
    protected boolean shouldInstrumentMethod(CtMethod method) {
        boolean hasMethodTimer = false;
        try {
            for (Object annotation : method.getAnnotations()) {
                if (annotation instanceof MethodTimer) {
                    hasMethodTimer = true;
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            //TODO log error
        }
        return hasMethodTimer;
    }

    @Override
    protected boolean shoudInstrumentClass(CtClass ctClass) throws NotFoundException, ClassNotFoundException {
        return true;
    }
    
}
