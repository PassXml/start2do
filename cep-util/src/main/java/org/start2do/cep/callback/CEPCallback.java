package org.start2do.cep.callback;

import org.start2do.cep.dto.CEPResult;

@FunctionalInterface
public interface CEPCallback {
    void execute(CEPResult result);
}