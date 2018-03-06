package com.zlf.ioc;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author chenzhuang
 * @time 2018/2/26 15:20
 * @class
 */

public class ProxyInfo {

    private String packageName;
    private String proxyClassName;
    private TypeElement typeElement;

    public static final String PROXY = "ViewInject";

    public Map<Integer, VariableElement> injectVariables = new HashMap<>();

    public ProxyInfo(Elements elements, TypeElement classElement) {
        this.typeElement = classElement;
        PackageElement packageElement = elements.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = ClassValidator.getClassName(classElement, packageName);
        //  com.zlf.ioc
        this.packageName = packageName;
        //  className  MainActivity$$ViewInject
        this.proxyClassName = className + "$$" + PROXY;
    }

    public String generateJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Generated code. Do not modify!\n")
                .append("package ")
                .append(packageName)
                .append(";\n\n")
                .append("import com.zlf.ioc.*;\n")
                .append('\n')
                .append("public class ")
                .append(proxyClassName)
                .append(" implements "+ProxyInfo.PROXY + "<" + typeElement.getQualifiedName() + ">")
                .append(" {\n");

        generateMethods(sb);
        sb.append('\n').append("}\n");
        return sb.toString();
    }

    private void generateMethods(StringBuilder sb) {
        sb.append("@Override\n").append("public void inject("+typeElement.getQualifiedName()+" host, Object source ){\n");

        for(int id: injectVariables.keySet()) {
            VariableElement element = injectVariables.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            sb.append(" if(source instanceof android.app.Activity){\n")
                    .append("host."+name)
                    .append(" = ")
                    .append("("+type+")(((android.app.Activity)source).findViewById("+id+"));\n")
                    .append("\n}else{\n")
                    .append("host."+name)
                    .append(" = ")
                    .append("("+type+")(((android.view.View)source).findViewById("+id+"));")
                    .append("\n}");
        }
        sb.append(" }\n");
    }

    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
}
