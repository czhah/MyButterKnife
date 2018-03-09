package com.zlf.ioc;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.google.auto.common.MoreElements.getPackage;


/**
 * @author chenzhuang
 * @time 2018/3/8 10:40
 * @class
 */

public class BindingSet {

    private final TypeName targetTypeName;
    private final ClassName bindingClassName;
    private final boolean isFinal;
    private final boolean isView;
    private final boolean isActivity;
    private final boolean isDialog;
    private final ImmutableList<ViewBinding> viewBindings;
    private final BindingSet parentBinding;
    private final ClassName UNBINDER = ClassName.get("com.zlf.ioc", "Unbinder");
    private final ClassName VIEW = ClassName.get("android.view", "View");

    static final ClassName UTILS = ClassName.get("com.zlf.ioc", "Utils");

    public BindingSet(TypeName targetTypeName, ClassName bindingClassName, boolean isFinal,
                      boolean isView, boolean isActivity, boolean isDialog,
                      ImmutableList<ViewBinding> viewBindings, BindingSet parentBinding) {
        this.isFinal = isFinal;
        this.targetTypeName = targetTypeName;
        this.bindingClassName = bindingClassName;
        this.isView = isView;
        this.isActivity = isActivity;
        this.isDialog = isDialog;
        this.viewBindings = viewBindings;
        this.parentBinding = parentBinding;
    }

    static Builder newBuilder(TypeElement enclosingElement) {
        //  <T>
        TypeMirror typeMirror = enclosingElement.asType();

        //  check isView、isActivity、isDialog
        boolean isView = false;
        boolean isActivity = true;
        boolean isDialog = false;

        TypeName targetType = TypeName.get(typeMirror);
        if(targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }

        String packageName = getPackage(enclosingElement).getQualifiedName().toString();
        String className = enclosingElement.getQualifiedName().toString().substring(
                packageName.length() + 1).replace('.', '$');
        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");
        boolean isFinal = enclosingElement.getModifiers().contains(Modifier.FINAL);

        return new Builder(targetType, bindingClassName, isFinal, isView, isActivity, isDialog);
    }

    public JavaFile brewJava() {
        return JavaFile.builder(bindingClassName.packageName(), createType())
                .addFileComment("Generated code from Butter Knife. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(Modifier.PUBLIC);
        //  add final

        //  add father
//        if(parentBinding != null) {
//        }else {
//        }
        builder.addSuperinterface(UNBINDER);

        //  add target
        builder.addField(targetTypeName, "target", Modifier.PRIVATE);

        if(isView) {
            //  add Constructor
//            builder.addMethod(createBindingConstructorForView());
        }else if(isActivity) {

        }else if(isDialog) {

        }

        builder.addMethod(createBindingConstructor());
        builder.addMethod(createBindingUnbindMethod());
        return builder.build();
    }

    private MethodSpec createBindingConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(targetTypeName, "target")
                .addParameter(VIEW, "source");

        //  this.target = target
        constructor.addStatement("this.target = target");
        constructor.addCode("\n");

        for (ViewBinding binding : viewBindings) {
            //  find bind View
            addViewBinding(constructor, binding);
        }

        return constructor.build();
    }

    private void addViewBinding(MethodSpec.Builder constructor, ViewBinding binding) {
        FieldViewBinding fieldBinding = binding.getFieldBinding();

        CodeBlock.Builder builder = CodeBlock.builder().add("target.$L = ", fieldBinding.getName());
        builder.add("$T.findOptionalViewAsType", UTILS);
        builder.add("(source, $L", binding.getId().code);
        builder.add(", $S", fieldBinding.getName());
        builder.add(", $T.class", fieldBinding.getRawType());
        builder.add(")");
        constructor.addStatement("$L", builder.build());
        return ;
    }

    private MethodSpec createBindingUnbindMethod() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("unbind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        result.addCode("\n");
//        result.addStatement("super.unbind()");
        return result.build();
    }

    static final class Builder {
        private final TypeName targetTypeName;
        private final ClassName bindingClassName;
        private final boolean isFinal;
        private final boolean isView;
        private final boolean isActivity;
        private final boolean isDialog;

        private BindingSet parentBinding;

        private final Map<Id, ViewBinding.Builder> viewIdMap = new LinkedHashMap<>();

        public Builder(TypeName targetTypeName, ClassName bindingClassName, boolean isFinal, boolean isView, boolean isActivity, boolean isDialog) {
            this.targetTypeName = targetTypeName;
            this.bindingClassName = bindingClassName;
            this.isFinal = isFinal;
            this.isView = isView;
            this.isActivity = isActivity;
            this.isDialog = isDialog;
        }

        void addField(Id id, FieldViewBinding binding) {
            getOrCreateViewBindings(id).setFieldBinding(binding);
        }

        private ViewBinding.Builder getOrCreateViewBindings(Id id) {
            ViewBinding.Builder viewId = viewIdMap.get(id);
            if (viewId == null) {
                viewId = new ViewBinding.Builder(id);
                viewIdMap.put(id, viewId);
            }
            return viewId;
        }

        BindingSet build() {
            ImmutableList.Builder<ViewBinding> viewBindings = ImmutableList.builder();
            for (ViewBinding.Builder builder : viewIdMap.values()) {
                viewBindings.add(builder.build());
            }
            return new BindingSet(targetTypeName, bindingClassName, isFinal, isView, isActivity, isDialog, viewBindings.build(), parentBinding);
        }

        public String findExistingBindingName(Id id) {

            return null;
        }
    }
}
