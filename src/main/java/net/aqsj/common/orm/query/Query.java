package net.aqsj.common.orm.query;

import java.sql.ResultSet;

import net.aqsj.common.orm.query.meta.FieldMapper;
import net.aqsj.common.orm.query.meta.ObjectRelationMapper;


public class Query {

	
	protected String selectTemplate = "select * from {0} where {1}";
	protected String insertTemplate = "insert into {0} ({1}) value ({2})";
	protected String updateTemplate = "update {0} set {1} where {2}";
	protected String deleteTemplate = "delete from {0} where {1}";
	
	public Query() {}
	
	public <T> String toInsertSQL(T t,ObjectRelationMapper<T> mapper) throws Exception{
		//insert into {0} ({1}) value ({2})
		String tableName = t.getClass().getSimpleName();
		
		StringBuilder fieldSB = new StringBuilder();
		StringBuilder valueSB = new StringBuilder();
		for(FieldMapper field:mapper.getFields()){
			String fn=field.getColumnName();
			
			fieldSB.append(',').append(fn);
			
			String value = field.getter(t);
			valueSB.append(',').append(value);
		}
		
		String sql = java.text.MessageFormat.format(insertTemplate, tableName,fieldSB.substring(1),valueSB.substring(1));
		return sql;
	}

	public <T> String toSelectSQL(Class<T> tClass,ObjectRelationMapper<T> mapper,WhereClause where){
		//select * from {0} where {1}
		String sql = java.text.MessageFormat.format(selectTemplate, mapper.getTableName(),where==null?"1=1":where.toString());
		return sql;
	}
	
	public <T>  String toUpdateSQL(T t,ObjectRelationMapper<T> mapper,WhereClause where) throws Exception {
		//update {0} set {1} where {2}
		String tableName = mapper.getTableName();
		
		StringBuilder fieldSB = new StringBuilder();
		for(FieldMapper field:mapper.getFields()){
			String fn=field.getColumnName();
			
			String value = field.getter(t);
			fieldSB.append(',').append(fn).append('=').append(value);
		}
		
		String sql = java.text.MessageFormat.format(updateTemplate, tableName,fieldSB.substring(1),where==null?"1=1":where.toString());
		return sql;
	}
	public <T> String toDeleteSQL(T t,ObjectRelationMapper<T> mapper,WhereClause where) throws Exception{
		//delete from {0} where {1}
		String sql = java.text.MessageFormat.format(deleteTemplate, mapper.getTableName(),where==null?"1=1":where.toString());
		return sql;
	}
	
	public <T> T convertRow2Object(ResultSet row,ObjectRelationMapper<T> mapper)throws Exception{
		T t = mapper.getConstructor().newInstance(new Object[0]);
		for(FieldMapper field : mapper.getFields()){
			Object value = row.getObject(field.getColumnName());
			field.setter(t, value);
		}
		return t;
	}
	
	
}