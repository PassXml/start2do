package org.start2do.util.validator.validList;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidListValidator implements ConstraintValidator<ValidList, List<?>> {

    @Autowired
    // 注入标准的校验器
    private Validator validator;

    @Override
    public boolean isValid(List<?> list, ConstraintValidatorContext context) {
        if (list == null || list.isEmpty()) {
            // 或者根据业务返回 false
            return true;
        }
        // 遍历列表，对每个对象进行校验
        for (Object obj : list) {
            // 使用注入的 validator 对 DTO 进行单独校验
            // 如果任何一个对象校验失败，整个校验就失败
            if (!validator.validate(obj).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
