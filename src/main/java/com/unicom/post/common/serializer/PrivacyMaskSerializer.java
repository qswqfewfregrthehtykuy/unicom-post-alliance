package com.unicom.post.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.unicom.post.common.annotation.PrivacyMask;
import com.unicom.post.common.enums.MaskType;
import com.unicom.post.common.utils.PrivacyUtils;

import java.io.IOException;

/**
 * 隐私脱敏 Jackson 序列化器
 * <p>
 * 根据字段上的 {@link PrivacyMask} 注解自动选择脱敏策略
 */
public class PrivacyMaskSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private MaskType maskType;

    /** Jackson 反射创建实例时需要无参构造 */
    public PrivacyMaskSerializer() {
        this.maskType = MaskType.PHONE;
    }

    private PrivacyMaskSerializer(MaskType maskType) {
        this.maskType = maskType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        String masked = PrivacyUtils.mask(value, maskType.name());
        gen.writeString(masked);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return this;
        }
        PrivacyMask annotation = property.getAnnotation(PrivacyMask.class);
        if (annotation == null) {
            // 也检查 getter 方法上的注解
            annotation = property.getContextAnnotation(PrivacyMask.class);
        }
        if (annotation != null) {
            return new PrivacyMaskSerializer(annotation.value());
        }
        return this;
    }
}
