package org.start2do.dto.permission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionDto {

    private Set<String> urls;
    private boolean defaultPass;

    public PermissionDto(Set<String> urls, boolean defaultPass) {
        this.urls = urls;
        this.defaultPass = defaultPass;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        PermissionDto that = (PermissionDto) o;
        return defaultPass == that.defaultPass && Objects.equals(urls, that.urls);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(urls);
        result = 31 * result + Boolean.hashCode(defaultPass);
        return result;
    }
}
