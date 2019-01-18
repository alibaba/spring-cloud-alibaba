package org.springframework.cloud.alibaba.cloud.examples;

import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.event.EventListener;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

/**
 * @author xiaojing
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener(ServiceBeanExportedEvent.class)
    public void onServiceBeanExportedEvent(ServiceBeanExportedEvent event) {
        ServiceBean serviceBean = event.getServiceBean();
    }

    @EventListener(InstancePreRegisteredEvent.class)
    public void onInstancePreRegisteredEvent(InstancePreRegisteredEvent event) throws JsonProcessingException {
        Registration registration = event.getRegistration();
        Map<String, ServiceBean> serviceBeansMap = beanFactory.getBeansOfType(ServiceBean.class);
        Map<String, String> metaData = registration.getMetadata();
        String serviceBeansJson = objectMapper.writeValueAsString(services(serviceBeansMap));
        metaData.put("serviceBeans", serviceBeansJson);
    }

    public Map<String, Map<String, Object>> services(Map<String, ServiceBean> serviceBeansMap) {

        Map<String, Map<String, Object>> servicesMetadata = new LinkedHashMap<>(serviceBeansMap.size());

        for (Map.Entry<String, ServiceBean> entry : serviceBeansMap.entrySet()) {

            String serviceBeanName = entry.getKey();

            ServiceBean serviceBean = entry.getValue();

            Map<String, Object> serviceBeanMetadata = resolveBeanMetadata(serviceBean);

            Object service = resolveServiceBean(serviceBeanName, serviceBean);

            if (service != null) {
                // Add Service implementation class
                serviceBeanMetadata.put("serviceClass", service.getClass().getName());
            }

            servicesMetadata.put(serviceBeanName, serviceBeanMetadata);

        }

        return servicesMetadata;

    }

    protected Map<String, Object> resolveBeanMetadata(final Object bean) {

        final Map<String, Object> beanMetadata = new LinkedHashMap<>();

        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

                Method readMethod = propertyDescriptor.getReadMethod();

                if (readMethod != null && isSimpleType(propertyDescriptor.getPropertyType())) {

                    String name = Introspector.decapitalize(propertyDescriptor.getName());
                    Object value = readMethod.invoke(bean);

                    if (value != null) {
                        beanMetadata.put(name, value);
                    }
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return beanMetadata;

    }

    private Object resolveServiceBean(String serviceBeanName, ServiceBean serviceBean) {

        int index = serviceBeanName.indexOf("#");

        if (index > -1) {

            Class<?> interfaceClass = serviceBean.getInterfaceClass();

            String serviceName = serviceBeanName.substring(index + 1);

            if (beanFactory.containsBean(serviceName)) {
                return beanFactory.getBean(serviceName, interfaceClass);
            }

        }

        return null;

    }

    private static boolean isSimpleType(Class<?> type) {
        return isPrimitiveOrWrapper(type)
                || type == String.class
                || type == BigDecimal.class
                || type == BigInteger.class
                || type == Date.class
                || type == URL.class
                || type == Class.class
                ;
    }
}
