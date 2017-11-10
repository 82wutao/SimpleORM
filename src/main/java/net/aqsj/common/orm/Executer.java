package net.aqsj.common.orm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.aqsj.common.orm.query.Query;
import net.aqsj.common.orm.query.WhereClause;
import net.aqsj.common.orm.query.meta.FieldMapper;
import net.aqsj.common.orm.query.meta.ObjectRelationMapper;
import net.aqsj.common.orm.transcation.TransactionContext;

@SuppressWarnings("unchecked")
public class Executer {
	
	protected Map<Class<?>, Query> converters = new HashMap<>();
	protected Map<Class<?>, ObjectRelationMapper<?>> mappers = new HashMap<>();
	
	protected DataSource source = null;
	
	public Executer(DataSource dataSource) {
		source = dataSource;
	}
	

	protected <T> Query getSQLGenerator(Class<T> tClass){
		Query query=converters.get(tClass);
		if (query == null) {
			query = new Query();
			converters.put(tClass,query);
		}
		return query;
	}
	protected <T> ObjectRelationMapper<T> getObjectRelationMapper(Class<T> tClass) throws Exception{
		Object object = mappers.get(tClass);
		if (object == null) {
			ObjectRelationMapper<T> mapper= ObjectRelationMapper.newObjectRelationMapper(tClass);
			mappers.put(tClass,mapper);
			return mapper;
		}
		return (ObjectRelationMapper<T>)object;
	}

	/**
	 * 执行查询
	 * @param objectType
	 * @param sql
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public <T> Collection<T> executeQuery(Class<T> objectType,String sql) throws Exception{
		Query q=getSQLGenerator(objectType);
		ObjectRelationMapper<T> mapper = getObjectRelationMapper(objectType);
		
		Connection connection = null;
		Statement statement=null;
		ResultSet resultSet = null;
		
		List<T> ret = new ArrayList<>();
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while(resultSet.next()){
				T t = q.convertRow2Object(resultSet,mapper);
				ret.add(t);
			}
		} catch (SQLException e) {
			throw e;
		}finally{
			closeResource(resultSet, statement, connection);
		}
		return ret;
	}
	public int executeUpdate(String sql) throws SQLException{
		Connection connection = null;
		Statement statement=null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			int affected = statement.executeUpdate(sql);
			return affected;
		} catch (SQLException e) {
			throw e;
		}finally{
			closeResource(null, statement, connection);
		}
	}
	public int[] executeUpdates(Collection<String> sqls) throws SQLException{
		Connection connection = null;
		Statement statement=null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			
			for(String sql:sqls){
				statement.addBatch(sql);
			}
			int[] affected = statement.executeBatch();
			return affected;
		} catch (SQLException e) {
			throw e;
		}finally{
			closeResource(null, statement, connection);
		}
	}
	
	public <T> T executeSelectOne(Class<T> tClass,WhereClause where) throws Exception{
		Query q=getSQLGenerator(tClass);
		ObjectRelationMapper<T> mapper = getObjectRelationMapper(tClass);
		
		String select_sql = q.toSelectSQL(tClass,mapper,where);

		Collection<T> result = this.executeQuery(tClass, select_sql);
		if (result.isEmpty()) {
			return null;
		}
		
		if (result.size() > 1) {
			throw new Exception("sql["+select_sql+"] find multiple result,but not one");
		}
		
		Iterator<T> iterator=result.iterator();
		while(iterator.hasNext()){
			return iterator.next();
		}
		return null;
	}
	public <T> Collection<T> executeSelect(Class<T> tClass,WhereClause where) throws Exception{
		Query q=getSQLGenerator(tClass);
		ObjectRelationMapper<T> mapper = getObjectRelationMapper(tClass);
		
		String select_sql = q.toSelectSQL(tClass,mapper,where);

		Collection<T> result = this.executeQuery(tClass, select_sql);
		
		return result;
	}
	
	public <T> boolean executeUpdate(T t,WhereClause where) throws Exception{
		Query q=getSQLGenerator((Class<T>)t.getClass());
		ObjectRelationMapper<T> mapper = getObjectRelationMapper((Class<T>)t.getClass());
		
		String update_sql = q.toUpdateSQL(t,mapper,where);
		
		int affected = executeUpdate(update_sql);
		return affected == 1;
	}
	public <T> boolean executeInsert(T t) throws Exception{
		Query q=getSQLGenerator((Class<T>)t.getClass());
		ObjectRelationMapper<T> mapper = getObjectRelationMapper((Class<T>)t.getClass());
		
		String update_sql = q.toInsertSQL(t,mapper);
		
		int affected = executeUpdate(update_sql);
		return affected == 1;
	}
	public <T> boolean executeDetele(T t) throws Exception{
		Query q=getSQLGenerator((Class<T>)t.getClass());
		ObjectRelationMapper<T> mapper = getObjectRelationMapper((Class<T>)t.getClass());
		
		String[] pks = mapper.getPrimaryKeys();
		WhereClause[] clauses= new WhereClause[pks.length];
		for(int i=0;i<pks.length;i++){
			String pk = pks[i];
			
			FieldMapper fieldMapper = mapper.getFieldByColumnName(pk);
			String value = fieldMapper.getter(t);
			clauses[i] = WhereClause.newCondition(pk,WhereClause.COMPARATOR.eq,value);
		}
		
		
		WhereClause where = WhereClause.buildMultipleConditions(clauses,WhereClause.RELATION.AND);
		String update_sql = q.toDeleteSQL(t,mapper,where);
		
		int affected = executeUpdate(update_sql);
		return affected == 1;
	}
	

	
	protected void closeResource(ResultSet resultSet,Statement statement,Connection connection){
		try {
			if(resultSet!=null)resultSet.close();
		} catch (Exception e2) {
		}
		try {
			if(statement!=null)statement.close();
		} catch (Exception e2) {
		}
		try {
			TransactionContext context = TransactionContext.getCurrentTransactionContext();
			if (connection == null){
				if(connection!=null)connection.close();
			}else{
				return ;
			}
		} catch (Exception e2) {
		}
	}

	protected Connection getConnection() throws SQLException {
		TransactionContext context = TransactionContext.getCurrentTransactionContext();
		if (context == null){return this.source.getConnection();}
		return context.getCurrentConn();
	}
}
