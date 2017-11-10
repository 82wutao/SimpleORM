package net.aqsj.common.orm.query.meta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObjectRelationMapper<ObjectType> {
	private String tableName=null; 
	private Constructor<ObjectType> constructor= null;
	
	private String[] primaryKeys;
	
	private List<FieldMapper> fields=new ArrayList<>();
	
	public ObjectRelationMapper(String tableName,Constructor<ObjectType> constructor,
			String[] pks,Collection<FieldMapper> fieldMappers) {
		this.tableName = tableName;
		this.constructor = constructor;
		this.primaryKeys=pks;
		this.fields.addAll(fieldMappers);
	}
	
	public Constructor<ObjectType> getConstructor() {
		return constructor;
	}
	public List<FieldMapper> getFields() {
		return fields;
	}
	
	public FieldMapper getFieldByColumnName(String columnName){
		for(FieldMapper fieldMapper:fields){
			String cn = fieldMapper.getColumnName();
			
			if (cn.equals(columnName)) {
				return fieldMapper;
			}
		}
		return null;
	}
	
	public FieldMapper getFieldByPropertyName(String propertyName){
		for(FieldMapper fieldMapper:fields){
			if (fieldMapper.getFieldName().equals(propertyName)) {
				return fieldMapper;
			}
		}
		return null;
	}
	public String[] getPrimaryKeys() {
		return primaryKeys;
	}
	public String getTableName() {
		return tableName;
	}
	
	public static <T> ObjectRelationMapper<T> newObjectRelationMapper(Class<T> clazz) throws Exception{
		TableMeta orm_annotation = clazz.getAnnotation(TableMeta.class);
		if (orm_annotation == null) {
			throw new Exception(MessageFormat.format("class {0} has not been annotated by {1}",clazz.getSimpleName(),"ORM"));
		}
		String tableName = orm_annotation.table();
		if (tableName.equals("")) {
			tableName = clazz.getSimpleName();
		}
		String[] primaryKeys= orm_annotation.primarykey();
		
		Constructor<T> constructor = clazz.getConstructor();
		
		List<FieldMapper> fieldMappers = new ArrayList<>();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field:fields){
			ColumnMeta columnMeta = field.getAnnotation(ColumnMeta.class);
			if (columnMeta == null) {
				continue;
			}
			
			String fieldName = field.getName();
			String propertyName = null;
			
			if (fieldName.startsWith("t_")
					|| fieldName.startsWith("f_")) {
				propertyName = fieldName.substring(2);
			}
			
			String[] partial = propertyName==null?fieldName.split("_"):propertyName.split("_");
			StringBuilder builder = new StringBuilder();
			for(String part:partial){
				builder.append(part);
			}
			propertyName = builder.toString().toLowerCase();
			
			Method[] methods = getterAndSetter(propertyName, clazz);
			
			fieldMappers.add(new FieldMapper(fieldName, field.getType(),columnMeta, methods[1], methods[0]));
		}
		
		ObjectRelationMapper<T> mapper = new ObjectRelationMapper<>(tableName, constructor, primaryKeys, fieldMappers);
		return mapper;
	}
	
	private static <T> Method[] getterAndSetter(String propertyName,Class<T> classType){
		Method[] array = new Method[]{null,null};
		
		Method[] methods=classType.getDeclaredMethods();
		for(Method method:methods){
			String methodName = method.getName().toLowerCase();
			if (methodName.equals("set"+propertyName)) {
				array[1] = method;
			}else if (methodName.equals("get"+propertyName)
					|| methodName.equals("is"+propertyName)) {
				array[0] = method;
			}
			
			if (array[0] != null
					&& array[1] != null) {
				return array;
			}
		}
		
		return null;
	}
}
