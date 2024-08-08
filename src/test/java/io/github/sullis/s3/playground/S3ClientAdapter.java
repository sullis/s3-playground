package io.github.sullis.s3.playground;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;


public class S3ClientAdapter {
  private static Object maybeReplaceArg(Object arg) {
    System.out.println("arg: " + arg.getClass().getName());
    System.out.println("arg.interfaces: " + Arrays.toString(arg.getClass().getInterfaces()));
    return arg;
  }

  public static S3AsyncClient adapt(final S3Client syncClient) {
    final InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object obj, Method method, Object[] args)
          throws Throwable {

        System.out.println("ARGS: " + Arrays.toString(args));

        Class<?>[] newParamTypes = new Class<?>[method.getParameterCount()];
        Object[] newArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
          newArgs[i] = maybeReplaceArg(args[i]);
          newParamTypes[i] = newArgs[i].getClass();
        }

        Method syncMethod = syncClient.getClass().getMethod(method.getName(), newParamTypes);
        syncMethod.setAccessible(true);
        System.out.println("method.name=" + method.getName());
        System.out.println("syncMethod.name=" + syncMethod.getName());
        Object syncMethodResult = syncMethod.invoke(syncClient, newArgs);
        System.out.println("syncMethodResult: " + syncMethodResult);
        return CompletableFuture.completedFuture(syncMethodResult);
      }
    };
    return (S3AsyncClient) Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        new Class[] { S3AsyncClient.class },
        handler);
  }
}
