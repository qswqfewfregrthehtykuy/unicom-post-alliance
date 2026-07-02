package com.unicom.post.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

public class CustomBeanNameGenerator extends AnnotationBeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        // 获取完整类名
        String beanClassName = definition.getBeanClassName();
        if (beanClassName == null) {
            return super.generateBeanName(definition, registry);
        }

        // 只处理Mapper接口，按包名加类名生成唯一Bean名称
        if (beanClassName.contains(".mapper.")) {
            // 例如: com.unicom.post.modules.system.mapper.SysOperationLogMapper
            // 生成: system_SysOperationLogMapper
            String packagePart = beanClassName.substring(0, beanClassName.lastIndexOf('.'));
            String simpleName = beanClassName.substring(beanClassName.lastIndexOf('.') + 1);
            String packageKey = packagePart.substring(packagePart.lastIndexOf('.') + 1);
            return packageKey + "_" + simpleName;
        }
        return super.generateBeanName(definition, registry);
    }
}