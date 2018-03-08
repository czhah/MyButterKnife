package com.zlf.ioc;

import javax.lang.model.element.PackageElement;

/**
 * @author chenzhuang
 * @time 2018/3/8 14:43
 * @class
 */

final class QualifiedId {
    final PackageElement packageElement;
    final int id;

    public QualifiedId(PackageElement packageElement, int id) {
        this.packageElement = packageElement;
        this.id = id;
    }

    @Override public String toString() {
        return "QualifiedId{packageName='" + packageElement + "', id=" + id + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QualifiedId)) return false;
        QualifiedId other = (QualifiedId) o;
        return id == other.id
                && packageElement.equals(other.packageElement);
    }

    @Override public int hashCode() {
        int result = packageElement.hashCode();
        result = 31 * result + id;
        return result;
    }
}
