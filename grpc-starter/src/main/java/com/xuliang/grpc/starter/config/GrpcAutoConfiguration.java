package com.xuliang.grpc.starter.config;


import com.xuliang.grpc.starter.annotation.GrpcService;
import com.xuliang.grpc.starter.annotation.GrpcServiceScan;
import com.xuliang.grpc.starter.binding.GrpcServiceProxy;
import com.xuliang.grpc.starter.client.GrpcClient;
import com.xuliang.grpc.starter.server.GrpcServer;
import com.xuliang.grpc.starter.service.CommonService;
import com.xuliang.grpc.starter.service.SerializeService;
import com.xuliang.grpc.starter.service.impl.ProtoStuffSerializeService;
import com.xuliang.grpc.starter.util.ClassNameUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author xuliang
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {


    private final AbstractApplicationContext applicationContext;

    private final GrpcProperties grpcProperties;

    public GrpcAutoConfiguration(AbstractApplicationContext applicationContext, GrpcProperties grpcProperties) {
        this.applicationContext = applicationContext;
        this.grpcProperties = grpcProperties;
    }


    /**
     * 全局 RPC 序列化/反序列化 protostuff
     */
    @Bean
    @ConditionalOnMissingBean(SerializeService.class)
    public SerializeService serializeService() {
        return new ProtoStuffSerializeService();
    }


    /**
     * PRC 服务调用
     */
    @Bean
    public CommonService commonService(SerializeService serializeService) {
        return new CommonService(applicationContext, serializeService);
    }


    /**
     * RPC 服务端
     */
    @Bean
    @ConditionalOnMissingBean(GrpcServer.class)
    @ConditionalOnProperty(value = "spring.grpc.enable", havingValue = "true")
    public GrpcServer grpcServer(CommonService commonService) throws Exception {
        GrpcServer server = new GrpcServer(grpcProperties, commonService);
        server.start();
        return server;
    }

    /**
     * RPC 客户端
     */
    @Bean
    @ConditionalOnMissingBean(GrpcClient.class)
    public GrpcClient grpcClient(SerializeService serializeService) {
        GrpcClient client = new GrpcClient(grpcProperties, serializeService);
        client.init();
        return client;
    }

    /**
     * 自动扫描@GrpcService 注解的接口，生成动态代理类，注入到 Spring 容器
     */
    public static class ExternalGrpcServiceScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
            ClassPathBeanDefinitionScanner scanner = new ClassPathGrpcServiceScanner(beanDefinitionRegistry);
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AnnotationTypeFilter(GrpcService.class));
            Set<BeanDefinition> beanDefinitions = scanPackages(annotationMetadata, scanner);
            /*注册动态代理，由于spring 容器中管理的都是对象的动态代理，默认此处使用的是 cglib动态代理*/
            ProxyUtil.registerBeans(beanFactory, beanDefinitions);
        }

        /**
         * 包扫描
         */
        private Set<BeanDefinition> scanPackages(AnnotationMetadata importingClassMetadata, ClassPathBeanDefinitionScanner scanner) {
            List<String> packages = new ArrayList<>();
            Map<String, Object> annotationAttributes =
                    importingClassMetadata.getAnnotationAttributes(GrpcServiceScan.class.getCanonicalName());
            if (annotationAttributes != null) {
                String[] basePackages = (String[]) annotationAttributes.get("packages");
                if (basePackages.length > 0) {
                    packages.addAll(Arrays.asList(basePackages));
                }
            }
            Set<BeanDefinition> beanDefinitions = new HashSet<>();
            if (CollectionUtils.isEmpty(packages)) {
                return beanDefinitions;
            }
            packages.forEach(pack -> beanDefinitions.addAll(scanner.findCandidateComponents(pack)));
            return beanDefinitions;
        }
    }

    protected static class ClassPathGrpcServiceScanner extends ClassPathBeanDefinitionScanner {

        ClassPathGrpcServiceScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
        }

    }


    /**
     * grpc代理工具类
     */
    protected static class ProxyUtil {
        static void registerBeans(BeanFactory beanFactory, Set<BeanDefinition> beanDefinitions) {
            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();
                if (StringUtils.isEmpty(className)) {
                    continue;
                }
                try {
                    // 创建代理类
                    Class<?> target = Class.forName(className);
                    Object invoker = new Object();
                    InvocationHandler invocationHandler = new GrpcServiceProxy<>(target, invoker);
                    Object proxy = Proxy.newProxyInstance(GrpcService.class.getClassLoader(), new Class[]{target}, invocationHandler);

                    // 注册到 Spring 容器
                    String beanName = ClassNameUtils.beanName(className);
                    ((DefaultListableBeanFactory) beanFactory).registerSingleton(beanName, proxy);
                    log.info("注册Spring容器，GrpcService Bean Name:{} ，Get Proxy:{}", beanName, proxy);
                } catch (Exception e) {
                    log.warn("exception : " + e);
                }
            }
        }
    }
}
