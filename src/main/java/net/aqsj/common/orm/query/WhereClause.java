package net.aqsj.common.orm.query;

import java.util.Collection;

public class WhereClause {
	public enum COMPARATOR{
		eq(" = "),ne(" <> "),gt(" > "),ge(" >= "),lt(" < "),le(" <= "),
		in(" in "),is(" is "),not(" is not "),like("like");
		
		private String symbol;
		private COMPARATOR(String symbol){this.symbol=symbol;}
	}
	
	public enum RELATION{
		OR(" or "),AND(" and ");
		
		private String symbol;
		private RELATION(String symbol){this.symbol=symbol;}
	}
	
	private String clmnName;
	private COMPARATOR comparator;
	private String[] values;
	
	private WhereClause condition1;
	private RELATION relation;
	private WhereClause condition2;
	
	private WhereClause(String columnName,COMPARATOR comparator,String[] values){
		this.clmnName = columnName;
		this.comparator = comparator;
		this.values = values;
	}
	private WhereClause(WhereClause condition1,RELATION relation,WhereClause condition2){
		this.condition1 = condition1;
		this.relation = relation;
		this.condition2 = condition2;
	}
	
	public static WhereClause newCondition(String columnName,COMPARATOR comparator,String... values){
		return new WhereClause(columnName, comparator, values);
	}
	public static WhereClause buildMultipleConditions(WhereClause condition1,RELATION relation,WhereClause condition2){
		return new WhereClause(condition1, relation, condition2);
	}
	
	public static WhereClause buildMultipleConditions(Collection<WhereClause> conditions,RELATION relation){
		WhereClause condition1 = null;
		for(WhereClause condition:conditions){
			if (condition1 == null) {
				condition1 = condition;
				continue;
			}
			
			condition1 = buildMultipleConditions(condition1, relation, condition);
		}
		return condition1;
	}
	public static WhereClause buildMultipleConditions(WhereClause[] conditions,RELATION relation){
		WhereClause condition1 = null;
		for(WhereClause condition:conditions){
			if (condition1 == null) {
				condition1 = condition;
				continue;
			}
			
			condition1 = buildMultipleConditions(condition1, relation, condition);
		}
		return condition1;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if (condition1 != null ) {
			builder.append('(').append(condition1.toString()).append(')');
		}
		if (relation != null) {
			builder.append(relation.symbol);
		}
		if (condition2 != null ) {
			builder.append('(').append(condition2.toString()).append(')');
		}
		if (builder.length() != 0) {
			return builder.toString();
		}
		
		builder.append(clmnName).append(comparator.symbol);
		for (String value:values) {
			builder.append(value).append(',');
		}
		builder.setLength(builder.length()-1);
		return builder.toString();
	}
}
