package net.aqsj.common.orm.transcation.meta;

import java.sql.Connection;

public enum TransactionIsolation {

    /**
     * 使用后端数据库默认的隔离级别
     */
    ISOLATION_NONE(Connection.TRANSACTION_NONE),


    /**
     * 最低的隔离级别，允许读取尚未提交的数据变更，可能会导致脏读、幻读或不可重复读
     */
    ISOLATION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /**
     * 允许读取并发事务已经提交的数据，可以阻止脏读，但是幻读或不可重复读仍有可能发生
     */
    ISOLATION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /**
     * 对同一字段的多次读取结果都是一致的，除非数据是被本身事务自己所修改，可以阻止脏读和不可重复读，但幻读仍有可能发生
     */ISOLATION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),


    /**
     * 最高的隔离级别，完全服从ACID的隔离级别，确保阻止脏读、不可重复读以及幻读，也是最慢的事务隔离级别，因为它通常是通过完全锁定事务相关的数据库表来实现的
     */
    ISOLATION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private  int value;
    private TransactionIsolation(int isolation){
        value = isolation;
    }
    public int isolationLvl(){return value;}
}
