package org.start2do.dto.req.mock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.awt.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MockDataImageReq {

    @NotNull
    @Min(0)
    private Integer height;
    @NotNull
    @Min(0)
    private Integer width;
    @Min(0)
    private Integer index = 0;
    private String text;
    private Color color = Color.WHITE;

    private String fontName = "宋体";
}
