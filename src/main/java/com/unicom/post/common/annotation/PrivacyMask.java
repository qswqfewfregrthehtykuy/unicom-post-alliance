package com.unicom.post.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.unicom.post.common.enums.MaskType;
import com.unicom.post.common.serializer.PrivacyMaskSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 隐私脱敏注解，标注在需要脱敏的字段上
 * <p>
 * 配合 Jackson 序列化自动脱敏，无需手动调用 PrivacyUtils
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = PrivacyMaskSerializer.class)
public @interface PrivacyMask {
    MaskType value() default MaskType.PHONE;
}
