package com.alibaba.cloud.testsupport;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestTimeoutExtension implements  BeforeAllCallback, BeforeEachCallback, AfterAllCallback {
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
    
    }
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
    
        
    }
    
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        final Class<?> clazz = context.getRequiredTestClass();
        final TestExtend annotation = clazz.getAnnotation(
                TestExtend.class);
        ScheduledExecutorService singlonThread = Executors.newSingleThreadScheduledExecutor();
        while (!singlonThread.awaitTermination( annotation.time(), TimeUnit.MILLISECONDS)){
            singlonThread.shutdown();
        }
    }
}
