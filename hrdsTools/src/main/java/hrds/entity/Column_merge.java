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
 * 列合并信息表
 */
@Table(tableName = "column_merge")
public class Column_merge extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "column_merge";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 列合并信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("col_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long col_id; //字段编号
	private String col_name; //合并后字段名称
	private String old_name; //要合并的字段
	private String col_zhname; //中文名称
	private String col_type; //字段类型
	private String remark; //备注
	private Long table_id; //表名ID
	private String valid_s_date; //有效开始日期
	private String valid_e_date; //有效结束日期

	/** 取得：字段编号 */
	public Long getCol_id(){
		return col_id;
	}
	/** 设置：字段编号 */
	public void setCol_id(Long col_id){
		this.col_id=col_id;
	}
	/** 设置：字段编号 */
	public void setCol_id(String col_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(col_id)){
			this.col_id=new Long(col_id);
		}
	}
	/** 取得：合并后字段名称 */
	public String getCol_name(){
		return col_name;
	}
	/** 设置：合并后字段名称 */
	public void setCol_name(String col_name){
		this.col_name=col_name;
	}
	/** 取得：要合并的字段 */
	public String getOld_name(){
		return old_name;
	}
	/** 设置：要合并的字段 */
	public void setOld_name(String old_name){
		this.old_name=old_name;
	}
	/** 取得：中文名称 */
	public String getCol_zhname(){
		return col_zhname;
	}
	/** 设置：中文名称 */
	public void setCol_zhname(String col_zhname){
		this.col_zhname=col_zhname;
	}
	/** 取得：字段类型 */
	public String getCol_type(){
		return col_type;
	}
	/** 设置：字段类型 */
	public void setCol_type(String col_type){
		this.col_type=col_type;
	}
	/** 取得：备注 */
	public String getRemark(){
		return remark;
	}
	/** 设置：备注 */
	public void setRemark(String remark){
		this.remark=remark;
	}
	/** 取得：表名ID */
	public Long getTable_id(){
		return table_id;
	}
	/** 设置：表名ID */
	public void setTable_id(Long table_id){
		this.table_id=table_id;
	}
	/** 设置：表名ID */
	public void setTable_id(String table_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(table_id)){
			this.table_id=new Long(table_id);
		}
	}
	/** 取得：有效开始日期 */
	public String getValid_s_date(){
		return valid_s_date;
	}
	/** 设置：有效开始日期 */
	public void setValid_s_date(String valid_s_date){
		this.valid_s_date=valid_s_date;
	}
	/** 取得：有效结束日期 */
	public String getValid_e_date(){
		return valid_e_date;
	}
	/** 设置：有效结束日期 */
	public void setValid_e_date(String valid_e_date){
		this.valid_e_date=valid_e_date;
	}
}