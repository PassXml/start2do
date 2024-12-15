package org.start2do.ebean.util;

import io.ebean.Transaction;

public interface EntityHook<T> {

    default int sort() {
        return 0;
    }

    Class<T> getKey();


    default void insertBefore(T obj, Transaction transaction) {
    }


    default void insertAfter(T obj, Transaction transaction) {

    }

    default void updateBefore(T obj, Transaction transaction) {
    }


    default void updateAfter(T obj, Transaction transaction) {

    }


    default void postDelete(T obj, Transaction transaction) {

    }

    default void preDelete(T obj, Transaction transaction) {

    }

    default void preSoftDelete(T obj, Transaction transaction) {

    }

    default void preDeleteById(Object id, Transaction transaction) {

    }

    default void postSoftDelete(T obj, Transaction transaction) {

    }

}
