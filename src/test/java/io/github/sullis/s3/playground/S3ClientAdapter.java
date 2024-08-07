package io.github.sullis.s3.playground;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;


public class S3ClientAdapter {
  public static S3AsyncClient adapt(final S3Client syncClient) {
    final InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object obj, Method method, Object[] args)
          throws Throwable {
        Method syncMethod = syncClient.getClass().getMethod(method.getName(), method.getParameterTypes());
        System.out.println("method.name=" + method.getName());
        System.out.println("syncMethod.name=" + syncMethod.getName());
        Object syncMethodResult = syncMethod.invoke(syncClient, args);
        return CompletableFuture.completedFuture(syncMethodResult);
      }
    };
    return (S3AsyncClient) Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        new Class[] { S3AsyncClient.class },
        handler);
  }
}
