package org.start2do.util;

import lombok.experimental.UtilityClass;
import org.start2do.dto.MethodImplementationException;
import reactor.core.publisher.Mono;

@UtilityClass
public class FunctionUtil {

    public Mono MethodImplementationException() {
        return Mono.error(new MethodImplementationException());
    }
}
