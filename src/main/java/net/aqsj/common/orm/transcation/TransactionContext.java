package net.aqsj.common.orm.transcation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionContext {
    public static ThreadLocal<TransactionContext> transactionContexts = new ThreadLocal<TransactionContext>();
    public static TransactionContext getCurrentTransactionContext(){
        TransactionContext transactionContext = transactionContexts.get();
        return transactionContext;
    }
    public static TransactionContext setupCurrentTransactionContext(DataSource dataSource, int isolation) throws SQLException {
        TransactionContext transactionContext = new TransactionContext();

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(isolation);

        transactionContext.currentConn = connection;

        transactionContexts.set(transactionContext);
        return transactionContext;
    }

    private Connection currentConn;

    public TransactionContext() {}

    public Connection getCurrentConn() {
        return currentConn;
    }
}