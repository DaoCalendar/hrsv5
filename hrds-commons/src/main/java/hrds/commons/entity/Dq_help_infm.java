package hrds.commons.entity;
/**Auto Created by VBScript Do not modify!*/
import hrds.commons.entity.fdentity.ProjectTableEntity;
import fd.ng.db.entity.anno.Table;
import fd.ng.core.annotation.DocBean;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * 系统帮助提示信息表
 */
@Table(tableName = "dq_help_infm")
public class Dq_help_infm extends ProjectTableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "dq_help_infm";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 系统帮助提示信息表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("help_infm_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	@DocBean(name ="help_infm_id",value="帮助提示编号:",dataType = Long.class,required = true)
	private Long help_infm_id;
	@DocBean(name ="help_infm_desc",value="帮助提示描述:",dataType = String.class,required = false)
	private String help_infm_desc;
	@DocBean(name ="help_infm_dtl",value="帮助提示详细信息:",dataType = String.class,required = true)
	private String help_infm_dtl;

	/** 取得：帮助提示编号 */
	public Long getHelp_infm_id(){
		return help_infm_id;
	}
	/** 设置：帮助提示编号 */
	public void setHelp_infm_id(Long help_infm_id){
		this.help_infm_id=help_infm_id;
	}
	/** 设置：帮助提示编号 */
	public void setHelp_infm_id(String help_infm_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(help_infm_id)){
			this.help_infm_id=new Long(help_infm_id);
		}
	}
	/** 取得：帮助提示描述 */
	public String getHelp_infm_desc(){
		return help_infm_desc;
	}
	/** 设置：帮助提示描述 */
	public void setHelp_infm_desc(String help_infm_desc){
		this.help_infm_desc=help_infm_desc;
	}
	/** 取得：帮助提示详细信息 */
	public String getHelp_infm_dtl(){
		return help_infm_dtl;
	}
	/** 设置：帮助提示详细信息 */
	public void setHelp_infm_dtl(String help_infm_dtl){
		this.help_infm_dtl=help_infm_dtl;
	}
}