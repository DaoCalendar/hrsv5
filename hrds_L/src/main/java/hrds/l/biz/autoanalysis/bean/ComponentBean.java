package hrds.l.biz.autoanalysis.bean;

import fd.ng.core.annotation.DocBean;
import fd.ng.core.annotation.DocClass;
import fd.ng.db.entity.anno.Table;
import hrds.commons.entity.fdentity.ProjectTableEntity;

@DocClass(desc = "可视化组件参数实体bean", author = "dhw", createdate = "2020/9/2 10:03")
@Table(tableName = "component_bean")
public class ComponentBean extends ProjectTableEntity {

	@DocBean(name = "fetch_sum_id", value = "取数汇总ID:", dataType = Long.class, required = false)
	private Long fetch_sum_id;
	@DocBean(name = "showNum", value = "显示条数:", dataType = Long.class)
	private Long showNum;
	@DocBean(name = "condition_sql", value = "条件sql:", dataType = String.class)
	private String condition_sql;
	@DocBean(name = "fetch_name", value = "取数名称:", dataType = String.class, required = false)
	private String fetch_name;
	@DocBean(name = "data_source", value = "数据来源:", dataType = String.class, required = false)
	private String data_source;

	public Long getFetch_sum_id() {
		return fetch_sum_id;
	}

	public void setFetch_sum_id(Long fetch_sum_id) {
		this.fetch_sum_id = fetch_sum_id;
	}

	public Long getShowNum() {
		return showNum;
	}

	public void setShowNum(Long showNum) {
		this.showNum = showNum;
	}

	public String getCondition_sql() {
		return condition_sql;
	}

	public void setCondition_sql(String condition_sql) {
		this.condition_sql = condition_sql;
	}

	public String getFetch_name() {
		return fetch_name;
	}

	public void setFetch_name(String fetch_name) {
		this.fetch_name = fetch_name;
	}

	public String getData_source() {
		return data_source;
	}

	public void setData_source(String data_source) {
		this.data_source = data_source;
	}
}
