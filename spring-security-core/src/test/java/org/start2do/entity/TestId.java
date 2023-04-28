package org.start2do.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
public class TestId extends BaseModel2 {

    @Id
    @GeneratedValue(generator = "UUIDStr")
    private String id;
    private String name;

    public TestId(String name) {
        this.name = name;
    }
}
