package hrds.entity;

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import hrds.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体类中所有属性都应定义为对象，不要使用int等主类型，方便对null值的操作
 */
@Table(tableName = "source_relation_dep")
public class SourceRelationDep extends TableEntity {
    private static final long serialVersionUID = 321566460595860L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "source_relation_dep";

	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("dep_id");
		__tmpPKS.add("source_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	/**
	 * 检查给定的名字，是否为主键中的字段
	 * @param name String 检验是否为主键的名字
	 * @return
	 */
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	private BigDecimal dep_id;
	private BigDecimal source_id;

	public BigDecimal getDep_id() { return dep_id; }
	public void setDep_id(BigDecimal dep_id) {
		if (dep_id==null) {
			throw new BusinessException("Entity : SourceRelationDep.dep_id must not null!");
		}
		this.dep_id = dep_id;
	}

	public BigDecimal getSource_id() { return source_id; }
	public void setSource_id(BigDecimal source_id) {
		if (source_id==null) {
			throw new BusinessException("Entity : SourceRelationDep.source_id must not null!");
		}
		this.source_id = source_id;
	}

}