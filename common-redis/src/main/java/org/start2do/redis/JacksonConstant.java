package org.start2do.redis;


import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

public interface JacksonConstant {

    Class[] JPAAnnotation = {
        ManyToOne.class,
        ManyToMany.class,
        OneToOne.class,
        OneToMany.class,
        OneToOne.class

    };

}
