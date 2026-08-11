package cn.yanque.commons.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link EnumValue} 注解校验器。
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {

    private Set<String> enumValues;
    private boolean ignoreCase;

    @Override
    public void initialize(EnumValue annotation) {
        ignoreCase = annotation.ignoreCase();
        enumValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .map(this::normalize)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        // 是否允许 null 应由 @NotNull 或 @NotBlank 控制。
        return value == null || enumValues.contains(normalize(value.toString()));
    }

    private String normalize(String value) {
        return ignoreCase ? value.toUpperCase(java.util.Locale.ROOT) : value;
    }
}
