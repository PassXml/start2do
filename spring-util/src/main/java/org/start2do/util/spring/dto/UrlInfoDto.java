package org.start2do.util.spring.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UrlInfoDto {

    /**
      *
     */
    private String urlName;
    private String beanName;
    private String methodName;

    private Set<String> urls;

    public UrlInfoDto(String beanName, String methodName, Set<String> urls) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.urls = urls;
    }
}
