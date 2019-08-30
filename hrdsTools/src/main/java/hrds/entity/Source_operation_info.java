package hrds.entity;
/**Auto Created by VBScript Do not modify!*/
import fd.ng.db.entity.TableEntity;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import hrds.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * 数据操作信息表
 */
@Table(tableName = "source_operation_info")
public class Source_operation_info extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "source_operation_info";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 数据操作信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long id; //信息表id
	private String execute_sql; //执行的sql语句
	private String search_name; //join类型
	private String remark; //备注
	private Long datatable_id; //数据表id

	/** 取得：信息表id */
	public Long getId(){
		return id;
	}
	/** 设置：信息表id */
	public void setId(Long id){
		this.id=id;
	}
	/** 设置：信息表id */
	public void setId(String id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(id)){
			this.id=new Long(id);
		}
	}
	/** 取得：执行的sql语句 */
	public String getExecute_sql(){
		return execute_sql;
	}
	/** 设置：执行的sql语句 */
	public void setExecute_sql(String execute_sql){
		this.execute_sql=execute_sql;
	}
	/** 取得：join类型 */
	public String getSearch_name(){
		return search_name;
	}
	/** 设置：join类型 */
	public void setSearch_name(String search_name){
		this.search_name=search_name;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：数据表id */
	public Long getDatatable_id(){
		return datatable_id;
	}
	/** 设置：数据表id */
	public void setDatatable_id(Long datatable_id){
		this.datatable_id=datatable_id;
	}
	/** 设置：数据表id */
	public void setDatatable_id(String datatable_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(datatable_id)){
			this.datatable_id=new Long(datatable_id);
		}
	}
}