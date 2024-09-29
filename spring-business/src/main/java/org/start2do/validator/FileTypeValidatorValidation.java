package org.start2do.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.util.FileUtil;

public class FileTypeValidatorValidation implements ConstraintValidator<FileSuffixValidator, MultipartFile> {

    protected FileSuffixValidator validator;

    @Override
    public void initialize(FileSuffixValidator constraintAnnotation) {
        this.validator = constraintAnnotation;
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String fileName = FileUtil.getSuffix(value.getOriginalFilename());
        for (String suffix : validator.value()) {
            if (suffix.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

}
