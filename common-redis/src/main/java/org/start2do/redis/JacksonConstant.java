package org.start2do.redis;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

public interface JacksonConstant {

    Class[] JPAAnnotation = {
        ManyToOne.class,
        ManyToMany.class,
        OneToOne.class,
        OneToMany.class,
        OneToOne.class

    };

}
