package mapTest;

public class MapConvertTest {

    public static void main(String[] args) {
        TestDto2 dto2 = new TestDto2();
        dto2.setHello(Test.V2);
        TestDto dto = TestDtoMapper.INSTANCE.toTestDto(dto2);
        System.out.println(dto.getHello());
        dto.setHello("0");
        TestDto2 dto1 = TestDtoMapper.INSTANCE.toTestDto23(dto);
        System.out.println(dto1.getHello().getLabel());
    }
}
