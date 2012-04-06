package play.modules.metrics;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class MetricsModelEnhancer extends MetricsEnhancer {
    
    @Override
    protected boolean shoudInstrumentClass(CtClass ctClass) throws NotFoundException, ClassNotFoundException {
        //Enhance only JPA entities models
        return ctClass.subtypeOf(classPool.get("play.db.jpa.Model")) && hasAnnotation(ctClass, "javax.persistence.Entity");
    }

    @Override
    protected boolean shouldInstrumentMethod(CtMethod method) {
        String mname = method.getName();
        return mname.startsWith("find") || mname.equals("filter") || mname.equals("count") || mname.equals("all");
    }
}
