package com.zlf.ioc;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author chenzhuang
 * @time 2018/3/8 9:42
 * @class
 */
@AutoService(Processor.class)
public class ViewInjectProcessor2 extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    static final String VIEW_TYPE = "android.view.View";
    private static final String NULLABLE_ANNOTATION_NAME = "Nullable";

    private final Map<QualifiedId, Id> symbols = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for(Class<? extends Annotation> annotations : getSupportedAnnotations()) {
            types.add(annotations.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(Bind.class);
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet> bindingMap = findAndParseTargets(roundEnv);
        for(Map.Entry<TypeElement, BindingSet> entry : bindingMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingSet binding = entry.getValue();
            JavaFile javaFile = binding.brewJava();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet.Builder> builderMap = new LinkedHashMap<>();
        Set<TypeElement> erasedTargetNames = new LinkedHashSet<>();

        //  add Annotation Bind.class
        for(Element element : roundEnv.getElementsAnnotatedWith(Bind.class)) {
            //  不太清楚ButterKnife为什么不添加判断
//            if(!SuperficialValidation.validateElement(element)) continue;
            parseBindView(element, builderMap, erasedTargetNames);
        }

        Deque<Map.Entry<TypeElement, BindingSet.Builder>> entries = new ArrayDeque<>(builderMap.entrySet());
        Map<TypeElement, BindingSet> bindingMap = new LinkedHashMap<>();
        while(!entries.isEmpty()) {
            Map.Entry<TypeElement, BindingSet.Builder> entry = entries.removeFirst();

            TypeElement type = entry.getKey();
            BindingSet.Builder builder = entry.getValue();

            TypeElement parentType = findParentType(type, erasedTargetNames);
            if(parentType == null) {
                bindingMap.put(type, builder.build());
            }else {
                //  dispatch father
            }

        }
        printMessage("bindingMap size:"+bindingMap.size());

        return bindingMap;
    }

    private TypeElement findParentType(TypeElement type, Set<TypeElement> erasedTargetNames) {

        return null;
    }

    private void parseBindView(Element element, Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        boolean hasError = isInaccessibleViaGeneratedCode(Bind.class, "fields", element)
                || isBindingInWrongPackage(Bind.class, element);

        //  T
        TypeMirror typeMirror = element.asType();
        if(typeMirror.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) typeMirror;
            typeMirror = typeVariable.getUpperBound();
        }

        Name qualifiedName = enclosingElement.getQualifiedName();
        Name simpleName = element.getSimpleName();
        if(!isSubtypeOfType(typeMirror, VIEW_TYPE) && !isInterface(typeMirror)) {
            //  check class and interface
            if (typeMirror.getKind() == TypeKind.ERROR) {
                note(element, "@%s field with unresolved type (%s) "
                                + "must elsewhere be generated as a View or interface. (%s.%s)",
                        Bind.class.getSimpleName(), typeMirror, qualifiedName, simpleName);
            } else {
                error(element, "@%s fields must extend from View or be an interface. (%s.%s)",
                        Bind.class.getSimpleName(), qualifiedName, simpleName);
                hasError = true;
            }
        }

        if(hasError) {
            return ;
        }
        int id = element.getAnnotation(Bind.class).value();
        QualifiedId qualifiedId = elementToQualifiedId(element, id);
        BindingSet.Builder builder = builderMap.get(enclosingElement);
        if(builder != null) {
            String exitingBindinggName = builder.findExistingBindingName(getId(qualifiedId));
        }else {
            builder = getOrCreateBindingBuilder(builderMap, enclosingElement);
        }

        String name = simpleName.toString();
        TypeName type = TypeName.get(typeMirror);
        boolean required = isFieldRequired(element);

        builder.addField(getId(qualifiedId), new FieldViewBinding(name, type, required));
        erasedTargetNames.add(enclosingElement);
    }

    private boolean isFieldRequired(Element element) {
        //  false
        return !hasAnnotationWithName(element, NULLABLE_ANNOTATION_NAME);
    }

    private boolean hasAnnotationWithName(Element element, String simpleName) {

        return false;
    }

    private Id getId(QualifiedId qualifiedId) {
        if(symbols.get(qualifiedId) == null) {
            symbols.put(qualifiedId, new Id(qualifiedId.id));
        }
        return symbols.get(qualifiedId);
    }

    private BindingSet.Builder getOrCreateBindingBuilder(Map<TypeElement, BindingSet.Builder> builderMap, TypeElement enclosingElement) {
        //  两次非空判断
        BindingSet.Builder builder = builderMap.get(enclosingElement);
        if(builder == null) {
            builder = BindingSet.newBuilder(enclosingElement);
            builderMap.put(enclosingElement, builder);
        }
        return builder;
    }

    private QualifiedId elementToQualifiedId(Element element, int id) {
        return new QualifiedId(elementUtils.getPackageOf(element), id);
    }

    private boolean isInterface(TypeMirror typeMirror) {
        return typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.INTERFACE;
    }

    private boolean isSubtypeOfType(TypeMirror typeMirror, String viewType) {
        //  class is View
        if(isTypeEqual(typeMirror, viewType)) {
            return true;
        }
        if(typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if(typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for(int i = 0; i < typeArguments.size(); i++) {
                if(i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            printMessage("isSubtypeOfType :"+typeString.toString());
            if(typeString.toString().equals(viewType)) {
                return true;
            }
        }

        Element element = declaredType.asElement();
        if(!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if(isSubtypeOfType(superType, viewType)) {
            return true;
        }

        for(TypeMirror interfaceType : typeElement.getInterfaces()) {
            if(isSubtypeOfType(interfaceType, viewType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTypeEqual(TypeMirror typeMirror, String viewType) {
        return viewType.equals(typeMirror.toString());
    }

    private boolean isBindingInWrongPackage(Class<? extends Annotation> annotationClass, Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();
        if (qualifiedName.startsWith("android.")) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith("java.")) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        return false;
    }

    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass, String fields, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        //  get private final static
        Set<Modifier> modifiers = element.getModifiers();
        if(modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            error(element, "@%s %s must not be private or static. (%s.%s)", annotationClass.getSimpleName(), fields, enclosingElement.getQualifiedName());
            hasError = true;
        }

        //  check package、class、interface、method、field...
        if(enclosingElement.getKind() != ElementKind.CLASS) {
            error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), fields, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), fields, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }
        return hasError;
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if(args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(kind, message, element);
    }

    private void printMessage(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

}
