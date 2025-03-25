package org.start2do.script.dto;

import java.io.ByteArrayOutputStream;
import javax.script.Bindings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.script.impl.nashornl.SystemConsole;

@Setter
@Getter
@Accessors(chain = true)
@AllArgsConstructor
public class BindingDto {

    private Bindings bindings;
    private ByteArrayOutputStream outputStream;
    private SystemConsole systemConsole;
}
