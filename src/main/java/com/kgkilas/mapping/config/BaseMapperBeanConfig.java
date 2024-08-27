package com.kgkilas.mapping.config;

import com.kgkilas.mapping.annotation.MapperBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * Configuration class that scans for classes annotated with @MapperBean and registers them as beans.
 */
@Configuration
public class BaseMapperBeanConfig implements org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor {

    private final String basePackage;

    public BaseMapperBeanConfig(@Value("${mapper.base.package}") String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(MapperBean.class));

        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);

        for (BeanDefinition beanDefinition : candidateComponents) {
            try {
                Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                if (beanClass.isAnnotationPresent(MapperBean.class)) {
                    registerBeansInPackage(scanner, registry, beanClass.getPackage().getName());
                }
            } catch (ClassNotFoundException e) {
                throw new BeansException("Failed to process MapperBean annotation", e) {};
            }
        }
    }

    private void registerBeansInPackage(ClassPathScanningCandidateComponentProvider scanner, BeanDefinitionRegistry registry, String packageName) throws ClassNotFoundException {
        Set<BeanDefinition> packageComponents = scanner.findCandidateComponents(packageName);
        for (BeanDefinition definition : packageComponents) {
            GenericBeanDefinition genericBeanDefinition = (GenericBeanDefinition) definition;
            Class<?> beanClass = Class.forName(genericBeanDefinition.getBeanClassName());
            genericBeanDefinition.setBeanClass(beanClass);
            registry.registerBeanDefinition(beanClass.getSimpleName(), genericBeanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No implementation needed
    }
}