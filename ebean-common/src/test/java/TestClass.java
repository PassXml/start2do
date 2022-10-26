import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.validator.InDict;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class TestClass {

    @InDict(clazz = TestEnum.class)
    private String t1;
}
