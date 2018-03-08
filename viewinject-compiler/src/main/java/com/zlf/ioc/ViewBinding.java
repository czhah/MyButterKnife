package com.zlf.ioc;

/**
 * @author chenzhuang
 * @time 2018/3/8 17:14
 * @class
 */

final class ViewBinding {

    private final Id id;
    private final FieldViewBinding fieldBinding;

    public ViewBinding(Id id, FieldViewBinding fieldBinding) {
        this.id = id;
        this.fieldBinding = fieldBinding;
    }

    public Id getId() {
        return id;
    }

    public FieldViewBinding getFieldBinding() {
        return fieldBinding;
    }

    public static final class Builder {
        private final Id id;

//        private final Map<ListenerClass, Map<ListenerMethod, Set<MethodViewBinding>>> methodBindings =
//                new LinkedHashMap<>();
        FieldViewBinding fieldBinding;

        Builder(Id id) {
            this.id = id;
        }

//        public boolean hasMethodBinding(ListenerClass listener, ListenerMethod method) {
//            Map<ListenerMethod, Set<MethodViewBinding>> methods = methodBindings.get(listener);
//            return methods != null && methods.containsKey(method);
//        }
//
//        public void addMethodBinding(ListenerClass listener, ListenerMethod method,
//                                     MethodViewBinding binding) {
//            Map<ListenerMethod, Set<MethodViewBinding>> methods = methodBindings.get(listener);
//            Set<MethodViewBinding> set = null;
//            if (methods == null) {
//                methods = new LinkedHashMap<>();
//                methodBindings.put(listener, methods);
//            } else {
//                set = methods.get(method);
//            }
//            if (set == null) {
//                set = new LinkedHashSet<>();
//                methods.put(method, set);
//            }
//            set.add(binding);
//        }
//
        public void setFieldBinding(FieldViewBinding fieldBinding) {
            if (this.fieldBinding != null) {
                throw new AssertionError();
            }
            this.fieldBinding = fieldBinding;
        }

        public ViewBinding build() {
            return new ViewBinding(id, fieldBinding);
        }
    }
}
