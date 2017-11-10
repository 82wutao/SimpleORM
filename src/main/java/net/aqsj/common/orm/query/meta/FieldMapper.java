package net.aqsj.common.orm.query.meta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FieldMapper {
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String name;
	private ColumnMeta columnMeta;
	
	private Class<?> type;
	
	private Method setter;
	private Method getter;
	
	public FieldMapper(String fieldName,Class<?> fieldType,ColumnMeta columnMeta,Method setter,Method getter) {
		name = fieldName;
		type = fieldType;
		this.columnMeta = columnMeta;
		this.setter = setter;
		this.getter = getter;
	}
	
	public String getFieldName(){return name;}
	public String getColumnName(){
		String cn=columnMeta.column();
		if (!cn.equals("")) {
			return cn;
		}
		return name;
	}
	public Class<?> getFieldType(){return type;}
	public String getter(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Object value = getter.invoke(instance);
		return javaValue2SqlValue(value);
	}
	public void setter(Object instance,Object arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		setter.invoke(instance, arg);
	}
//	public ColumnMeta getColumnMeta() {
//		return columnMeta;
//	}
	
	protected String javaValue2SqlValue(Object javaValue){
		if (javaValue == null) {
			return "null";
		}
		if (type.equals(String.class)) {
			return "'"+javaValue.toString()+"'";
		}else if (type.equals(Long.class)
				|| type.equals(Integer.class)
				|| type.equals(Short.class)
				|| type.equals(Byte.class)
				|| type.equals(BigInteger.class)) {
			return javaValue.toString();
		}else if (type.equals(Float.class)
				|| type.equals(Double.class)
				|| type.equals(BigDecimal.class)) {
			return javaValue.toString();
		}else if (type.equals(Boolean.class)) {
			return ((Boolean)javaValue).booleanValue()?"1":"0";
		}else if (type.equals(java.util.Date.class)
				|| type.equals(java.sql.Date.class)
				|| type.equals(java.sql.Time.class)
				|| type.equals(java.sql.Timestamp.class)) {
			return dateFormat.format(javaValue);
		}
		return javaValue.toString();
	}
}
