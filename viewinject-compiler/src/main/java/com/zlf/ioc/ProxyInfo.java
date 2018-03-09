package com.zlf.ioc;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
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
    private ClassName targetTypeName;

    private static final ClassName InterfaceName = ClassName.get("com.zlf.ioc", "ViewInject");
    private static final ClassName VIEW = ClassName.get("android.view", "View");

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
        // BaseActivity
        TypeMirror typeMirror = classElement.asType();
        TypeName targetType = TypeName.get(typeMirror);
        if (targetType instanceof ParameterizedTypeName) {
            this.targetTypeName = ((ParameterizedTypeName) targetType).rawType;
        }
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

    public JavaFile brewJava() {
        return JavaFile.builder(packageName, createType())
                .addFileComment("Generated code from Butter Knife. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(getProxyClassFullName())
                .addModifiers(Modifier.PUBLIC);

//        ClassName superinterface = ClassName.bestGuess("com.zs.javapoet.TestClass");
        builder.addSuperinterface(ParameterizedTypeName.get(InterfaceName, targetTypeName))
                .superclass(ClassName.bestGuess("com.zlf.ioc.ViewInject"));

        if(targetTypeName != null) {
            builder.addField(targetTypeName, "target", Modifier.PRIVATE);
        }

//        builder.addMethod(createBindingConstructorForActivity());

        builder.addMethod(createBindingConstructor());
        return builder.build();
    }

    private MethodSpec createInjectMethod() {
        MethodSpec.Builder injectMethod = MethodSpec.methodBuilder("")
                .addModifiers(Modifier.PUBLIC);

        injectMethod.addParameter(targetTypeName, "target");

        injectMethod.addParameter(VIEW, "source");

        for(int id: injectVariables.keySet()) {
            VariableElement element = injectVariables.get(id);
            addViewBinding(injectMethod, element, id);
        }

        return injectMethod.build();
    }

    private MethodSpec createBindingConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        constructor.addParameter(targetTypeName, "target");

        constructor.addParameter(VIEW, "source");

        for(int id: injectVariables.keySet()) {
            VariableElement element = injectVariables.get(id);
            addViewBinding(constructor, element, id);
        }

        return constructor.build();
    }

    private void addViewBinding(MethodSpec.Builder result, VariableElement element, int id) {
        String name = element.getSimpleName().toString();
        String type = element.asType().toString();
//        target.mToolbar = (Toolbar)Utils.findOptionalViewAsType(source, 2131558545, "field \'mToolbar\'", Toolbar.class);
// Optimize the common case where there's a single binding directly to a field.
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("target.$L = ", name);

        builder.add("($T) ", type);
        builder.add("source.findViewById($L)", id);
        result.addStatement("$L", builder.build());
        return;
    }

    private MethodSpec createBindingConstructorForActivity() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
//                .addParameter(targetTypeName, "target");
        builder.addStatement("this(target, target.getWindow().getDecorView())");
        return builder.build();
    }
    //Error:Lambda coming from jar file need their interfaces on the classpath to be compiled, unknown interfaces are java.util.function.Supplier
}
