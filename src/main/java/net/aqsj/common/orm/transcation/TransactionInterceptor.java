package net.aqsj.common.orm.transcation;

import net.aqsj.common.orm.transcation.meta.TransactionMeta;
import net.aqsj.common.orm.transcation.meta.TransactionPropagation;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;



public class TransactionInterceptor implements MethodInterceptor {




    protected List<Method> transactionalMethods=null;
    protected DataSource dataSource = null;
    public TransactionInterceptor(DataSource dataSource) {this.dataSource = dataSource;

    }

    public void setTransactionalMethods(List<Method> transactionalMethods) {
        this.transactionalMethods = transactionalMethods;
    }

    /**
     * 重写方法拦截在方法前和方法后加入业务
     * Object Object为由CGLib动态生成的代理类实例
     * Method Method为上文中实体类所调用的被代理的方法引用
     * Object[] params为参数，
     * MethodProxy MethodProxy为生成的代理类对方法的代理引用。
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] params,
                            MethodProxy proxy) throws Throwable {

        TransactionMeta transactionMeta = method.getDeclaredAnnotation(TransactionMeta.class);
        if (transactionMeta == null){
            return proxy.invokeSuper(obj, params);
        }


        TransactionContext transactionContext = TransactionContext.getCurrentTransactionContext();
        boolean newTransaction = false;
        if (transactionContext == null){
            transactionContext = TransactionContext.setupCurrentTransactionContext(dataSource,transactionMeta.isolation().isolationLvl());
            newTransaction = true;
        }

        try {
            Object result = proxy.invokeSuper(obj, params);

            if (newTransaction){
                commitTransaction(transactionContext);
            }
            return result;
        }catch (Throwable e){
            if (newTransaction){
                rollbackTransaction(transactionContext);
            }
            throw e;
        }finally {
            if (newTransaction){
                Connection connection = transactionContext.getCurrentConn();
                connection.close();
            }
        }
    }



    protected void commitTransaction(TransactionContext transactionContext) throws SQLException {
        if (transactionContext == null){return;}
        if (transactionContext.getCurrentConn() == null){return ;}
        transactionContext.getCurrentConn().commit();
    }
    protected void rollbackTransaction(TransactionContext transactionContext) throws SQLException {
        if (transactionContext == null){return;}
        if (transactionContext.getCurrentConn() == null){return ;}
        transactionContext.getCurrentConn().rollback();
    }
}