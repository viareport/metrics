package play.modules.metrics;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

public abstract class MetricsEnhancer extends Enhancer {
    
    public MetricsEnhancer() {
        super();
    }

    protected abstract boolean shoudInstrumentClass(CtClass ctClass) throws NotFoundException, ClassNotFoundException;

    protected abstract boolean shouldInstrumentMethod(CtMethod method);

    @Override
    public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
        if (applicationClass.name.equals(this.getClass().getName())) return;
        
        CtClass ctClass = makeClass(applicationClass);
        if (shoudInstrumentClass(ctClass)) {
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                if (shouldInstrumentMethod(method)) {
                    instrumentMethod(ctClass, method);
                }
            }
        }
        
        // Done.
        applicationClass.enhancedByteCode = ctClass.toBytecode();
        ctClass.defrost();
    }

    protected void instrumentMethod(CtClass ctClass, CtMethod method) throws CannotCompileException, NotFoundException {
        //rename old method to synthetic name, then duplicate the
        //  method with original name for use as interceptor
        String mname = method.getName();
        String newMethodImplementationName = getMethodImplementationName(mname);
        method.setName(newMethodImplementationName);
        CtMethod mnew = CtNewMethod.copy(method, mname, ctClass, null);
        
        //  start the body text generation by saving the start time
        //  to a local variable, then call the timed method; the
        //  actual code generated needs to depend on whether the
        //  timed method returns a value
        String timerName = ctClass.getName() + "." + mname ;
        String type = method.getReturnType().getName();
        StringBuffer body = new StringBuffer();
        body.append("{");
        if (!"void".equals(type)) {
            body.append(type + " result;\n");
        }
        body.append("try {\n");
        String metricGroup = "com.inativ.timer." + (ctClass.getPackageName() == null ? "" : ctClass.getPackageName());
        String metricType = ctClass.getSimpleName().replaceAll("\\$$", "");
        body.append("\nplay.modules.metrics.MetricsPlugin.enableTimer(" + 
                            "\"" + metricGroup + "\"" +
                            ", " + "\"" + metricType + "\"" +
                            ", " + "\"" + timerName + "\"" +
                            ");\n");
        body.append((!"void".equals(type) ? "result = " : "") + newMethodImplementationName + "($$);\n");
        body.append("} finally {\n");
        //  finish body text generation with call to print the timing
        //  information, and return saved value (if not void)
        body.append("play.modules.metrics.MetricsPlugin.stopTimer(\"" + timerName + "\");\n");
        body.append("}\n");
        if (!"void".equals(type)) {
            body.append("return result;\n");
        }
        body.append("}");
        
        //  replace the body of the interceptor method with generated
        //  code block and add it to class
        if (mname.contains("checkAccess")) {
            try {
                System.out.println(method.getAnnotations().toString() + "\n");
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(mnew.toString());
        }
        mnew.setBody(body.toString());
        ctClass.addMethod(mnew);
    }

    private String getMethodImplementationName(String mname) {
        return mname+"$impl";
    }
}