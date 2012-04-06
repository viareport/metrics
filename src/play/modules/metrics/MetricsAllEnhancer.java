package play.modules.metrics;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;


public class MetricsAllEnhancer extends MetricsEnhancer {

    @Override
    protected boolean shoudInstrumentClass(CtClass ctClass) throws NotFoundException, ClassNotFoundException {
        String packageName = ctClass.getPackageName();
        return !ctClass.isAnnotation() && !ctClass.isInterface() && packageName != null && (packageName.contains("controllers")
                    && ! ctClass.getName().contains("AccessControl"));
    }

    @Override
    protected boolean shouldInstrumentMethod(CtMethod method) {
        String annotationPattern = "play.mvc.Before";
        boolean shouldInstrument = false;
        
        try {
            shouldInstrument = ! hasAnnotation(method, annotationPattern);
        } catch (ClassNotFoundException e) {
            // TODO logs
        }
        
        return shouldInstrument;
    }
}
