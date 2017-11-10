package net.aqsj.common.orm.transcation;

import net.sf.cglib.proxy.Enhancer;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ServiceProxyFactory {

    public static <T> T proxyServer(Class<T> serviceClass, DataSource dataSource){
        TransactionInterceptor interceptor = new TransactionInterceptor(dataSource);
        List<Method> methods = Arrays.asList(serviceClass.getDeclaredMethods());
        interceptor.setTransactionalMethods(methods);

        Enhancer enhancer =new Enhancer();
        enhancer.setSuperclass(serviceClass);
        enhancer.setCallback(interceptor);
        return serviceClass.cast(enhancer.create());
    }
}
