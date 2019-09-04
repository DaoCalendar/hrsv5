package hrds.commons.entity;
/**Auto Created by VBScript Do not modify!*/
import fd.ng.db.entity.TableEntity;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import hrds.commons.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * 机器学习梯度增强回归树特征表
 */
@Table(tableName = "ml_gbrtfeat")
public class Ml_gbrtfeat extends TableEntity
{
	private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "ml_gbrtfeat";
	/**
	* 检查给定的名字，是否为主键中的字段
	* @param name String 检验是否为主键的名字
	* @return
	*/
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); } 
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; } 
	/** 机器学习梯度增强回归树特征表 */
	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("feature_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	private Long feature_id; //特征编号
	private String iv_column; //自变量字段
	private Long model_id; //模型编号

	/** 取得：特征编号 */
	public Long getFeature_id(){
		return feature_id;
	}
	/** 设置：特征编号 */
	public void setFeature_id(Long feature_id){
		this.feature_id=feature_id;
	}
	/** 设置：特征编号 */
	public void setFeature_id(String feature_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(feature_id)){
			this.feature_id=new Long(feature_id);
		}
	}
	/** 取得：自变量字段 */
	public String getIv_column(){
		return iv_column;
	}
	/** 设置：自变量字段 */
	public void setIv_column(String iv_column){
		this.iv_column=iv_column;
	}
	/** 取得：模型编号 */
	public Long getModel_id(){
		return model_id;
	}
	/** 设置：模型编号 */
	public void setModel_id(Long model_id){
		this.model_id=model_id;
	}
	/** 设置：模型编号 */
	public void setModel_id(String model_id){
		if(!fd.ng.core.utils.StringUtil.isEmpty(model_id)){
			this.model_id=new Long(model_id);
		}
	}
}