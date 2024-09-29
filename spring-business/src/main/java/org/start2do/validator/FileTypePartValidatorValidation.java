package org.start2do.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.codec.multipart.FilePart;
import org.start2do.util.FileUtil;

public class FileTypePartValidatorValidation implements ConstraintValidator<FileSuffixValidator, FilePart> {

    protected FileSuffixValidator validator;

    @Override
    public void initialize(FileSuffixValidator constraintAnnotation) {
        this.validator = constraintAnnotation;
    }

    @Override
    public boolean isValid(FilePart value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        //获取value的文件后缀,检查是否在valiator的value数组中
        String fileName = FileUtil.getSuffix(value.filename());
        for (String suffix : validator.value()) {
            if (suffix.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }
}
