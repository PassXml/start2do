package mapTest;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.start2do.ebean.dict.IDictItem;

@Mapper
public interface TestDtoMapper {


    TestDtoMapper INSTANCE = Mappers.getMapper(TestDtoMapper.class);

    default String toString(IDictItem item) {
        return item.value();
    }

    default Test toString(String s) {
        return Test.get(s);
    }

    TestDto toTestDto(TestDto2 dto2);

    TestDto2 toTestDto23(TestDto dto);
}
