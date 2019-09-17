package hrds.b.biz.agent;

import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;
import fd.ng.web.annotation.RequestParam;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.RequestUtil;
import fd.ng.web.util.ResponseUtil;
import hrds.b.biz.agent.tools.LogReader;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.AgentType;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.codes.IsFlag;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 数据库采集应用管理端数据源Agent列表后台服务类
 * @author: WangZhengcheng
 * @create: 2019-09-03 14:17
 **/
public class AgentListAction extends BaseAction {

	private static final String SFTP_PORT = "22";

	/**
	 * 获取数据源Agent列表信息
	 *
	 * 1、获取用户ID并根据用户ID去数据库中查询数据源信息
	 *
	 * @Param: 无
	 * @return: fd.ng.db.resultset.Result
	 *          含义：数据源信息查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result getAgentInfoList() {
		//1、获取用户ID并根据用户ID去数据库中查询数据源信息
		return Dbo.queryResult("SELECT datas.source_id,datas.datasource_name " +
				"FROM agent_info age JOIN data_source datas ON age.source_id = datas.SOURCE_ID " +
				"WHERE age.user_id = ? GROUP BY datas.source_id,datas.datasource_name",
				getUserId());
	}

	/**
	 * 根据sourceId、agentType、userId获取相应信息
	 *
	 * 1、获取用户ID并根据用户ID去数据库中查询数据源信息
	 *
	 * @Param: sourceId long
	 *         含义：数据源ID,data_source表主键，agent_info表外键
	 *         取值范围：不为空
	 * @Param: agentType String
	 *         含义：agent类型
	 *         取值范围：AgentType代码项的code值(1-5)
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result getAgentInfo(long sourceId, String agentType) {
		//1、根据sourceId和agentType查询数据库获取相应信息
		return Dbo.queryResult("SELECT * FROM agent_info WHERE source_id = ? AND agent_type = ? " +
						"AND user_id = ?", sourceId, agentType, getUserId());
	}

	/**
	 * 根据sourceId和agentId获取某agent下所有任务的信息
	 *
	 * 1、获取用户ID
	 * 2、判断在当前用户，当前数据源下，某一类型的agent是否存在
	 * 3、如果存在，查询结果中应该有且只有一条数据
	 * 4、判断该agent是那种类型，并且根据类型，到对应的数据库表中查询采集任务管理详细信息
	 * 5、返回结果
	 *
	 * @Param: sourceId long
	 *         含义：数据源ID,data_source表主键，agent_info表外键
	 *         取值范围：不为空
	 * @Param: agentId long
	 *         含义：agentID,agent_info表主键
	 *         取值范围：不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	//TODO 采集频率目前暂未拿到
	public Result getTaskInfo(long sourceId, long agentId) {
		//1、获取用户ID
		Long userId = getUserId();
		//2、判断在当前用户，当前数据源下，agent是否存在
		Result result = Dbo.queryResult("select ai.* from data_source ds " +
				" left join agent_info ai on ds.SOURCE_ID = ai.SOURCE_ID " +
				" where ds.source_id=? AND ai.user_id = ? " +
				" AND ai.agent_id = ?", sourceId, userId, agentId);
		//3、如果存在，查询结果中应该有且只有一条数据
		if (result.isEmpty()) {
			throw new BusinessException("未找到Agent");
		}

		//4、判断该agent是那种类型，并且根据类型，到对应的数据库表中查询采集任务管理详细信息
		StringBuilder sqlSB = new StringBuilder();
		//数据库直连采集Agent
		if (AgentType.ShuJuKu == AgentType.ofEnumByCode(result.getString(0, "agent_type"))) {
				sqlSB.append(" SELECT ds.DATABASE_ID ID,ds.task_name task_name,ds.AGENT_ID AGENT_ID, ")
					.append(" gi.source_id source_id ")
					.append(" FROM database_set ds ")
					.append(" LEFT JOIN agent_info gi ON ds.Agent_id = gi.Agent_id ")
					.append(" where ds.Agent_id=? and ds.is_sendok = ? ");
		}
		//数据文件Agent
		else if (AgentType.DBWenJian == AgentType.ofEnumByCode(result.getString(0, "agent_type"))){
			sqlSB.append(" SELECT ds.DATABASE_ID ID,ds.task_name task_name,ds.AGENT_ID AGENT_ID, ")
					.append(" gi.source_id source_id")
					.append(" FROM database_set ds ")
					.append(" LEFT JOIN agent_info gi ON ds.Agent_id = gi.Agent_id ")
					.append(" where ds.Agent_id=? and ds.is_sendok = ? ");
		}
		//半结构化采集Agent
		else if (AgentType.DuiXiang == AgentType.ofEnumByCode(result.getString(0, "agent_type"))) {
			sqlSB.append(" SELECT fs.odc_id id,fs.obj_collect_name task_name,fs.AGENT_ID AGENT_ID,gi.source_id ")
					.append(" FROM object_collect fs ")
					.append(" LEFT JOIN agent_info gi ON gi.Agent_id = fs.Agent_id ")
					.append(" WHERE fs.Agent_id = ? AND fs.is_sendok = ? ");
		}
		//FtpAgent
		else if (AgentType.FTP == AgentType.ofEnumByCode(result.getString(0, "agent_type"))) {
			sqlSB.append(" SELECT fs.ftp_id id,fs.ftp_name task_name,fs.AGENT_ID AGENT_ID,gi.source_id ")
					.append(" FROM ftp_collect fs ")
					.append(" LEFT JOIN agent_info gi ON gi.Agent_id = fs.Agent_id ")
					.append(" WHERE fs.Agent_id = ? and fs.is_sendok = ? ");
		}
		//非结构化Agent
		else {
			sqlSB.append(" SELECT fs.fcs_id id,fs.fcs_name task_name,fs.AGENT_ID AGENT_ID,gi.source_id ")
					.append(" FROM file_collect_set fs ")
					.append(" ON fs.fcs_id = cf.COLLECT_SET_ID ")
					.append(" LEFT JOIN agent_info gi ON gi.Agent_id = fs.Agent_id ")
					.append(" where fs.Agent_id=? and fs.is_sendok = ? ");
		}
		Result agentInfo = Dbo.queryResult(sqlSB.toString(), result.getLong(0, "agent_id"),
				IsFlag.Shi.getCode());
		//5、返回结果
		return agentInfo;
	}

	/**
	 * 查看任务日志
	 *
	 * 1、对显示日志条数做处理，该方法在加载页面时被调用，readNum可以不传，则默认显示100条，
	 *    如果用户在页面上进行了选择并点击查看按钮，则最多给用户显示1000条日志
	 * 2、调用方法读取日志并返回
	 *
	 * @Param: sourceId long
	 *         含义：数据源ID,data_source表主键，agent_info表外键
	 *         取值范围：不为空
	 * @Param: logType String
	 *         含义：日志类型(完整日志、错误日志)
	 *         取值范围：All : 完整日志, Wrong : 错误日志
	 * @Param: readNum int
	 *         含义：查看日志条数
	 *         取值范围：不为空
	 * @return: String
	 *          含义：日志信息
	 *          取值范围：不会为null
	 *
	 * */
	public String viewTaskLog(long agentId, String logType,
	                          @RequestParam(nullable = true, valueIfNull = "100") int readNum) {
		//1、对显示日志条数做处理，该方法在加载页面时被调用，readNum可以不传，则默认显示100条，
		// 如果用户在页面上进行了选择并点击查看按钮，则最多给用户显示1000条日志
		if (readNum > 1000) readNum = 1000;
		//2、调用方法读取日志并返回
		return getTaskLog(agentId, getUserId(), logType, readNum).get("log");
	}

	/**
	 * 任务日志下载
	 *
	 * 1、对显示日志条数做处理，该方法在加载页面时被调用，readNum可以不传，则默认显示100条，
	 *    如果用户在页面上进行了选择并点击查看按钮，如果用户输入的条目多于1000，则给用户显示3000条
	 * 2、调用方法读取日志，获得日志信息和日志文件路径
	 * 3、将日志信息由字符串转为byte[]
	 * 4、得到本次http交互的request和response
	 * 5、设置响应头信息
	 * 6、使用response获得输出流，完成文件下载
	 *
	 * @Param: sourceId long
	 *         含义：数据源ID,data_source表主键，agent_info表外键
	 *         取值范围：不为空
	 * @Param: logType String
	 *         含义：日志类型(完整日志、错误日志)
	 *         取值范围：All : 完整日志, Wrong : 错误日志
	 * @Param: readNum int
	 *         含义：查看日志条数
	 *         取值范围：不为空
	 * @return: 无
	 *
	 * */
	public void downloadTaskLog(long agentId, String logType,
	                            @RequestParam(nullable = true, valueIfNull = "100") int readNum) {
		try {
			//1、对显示日志条数做处理，该方法在加载页面时被调用，readNum可以不传，则默认显示100条，
			// 如果用户在页面上进行了选择并点击查看按钮，如果用户输入的条目多于1000，则给用户显示3000条
			if (readNum > 1000) readNum = 3000;
			//2、调用方法读取日志，获得日志信息和日志文件路径
			Map<String, String> taskLog = getTaskLog(agentId, getUserId(), logType, readNum);

			//3、将日志信息由字符串转为byte[]
			byte[] bytes = taskLog.get("log").getBytes();

			//4、得到本次http交互的request和response
			HttpServletResponse response = ResponseUtil.getResponse();
			HttpServletRequest request = RequestUtil.getRequest();

			//5、设置响应头信息
			response.reset();
			if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				// 对firefox浏览器做特殊处理
				response.setHeader("content-disposition", "attachment;filename=" +
						new String(taskLog.get("filePath").getBytes(DataBaseCode.UTF_8.getValue()),
								DataBaseCode.ISO_8859_1.getValue()));
			} else {
				response.setHeader("content-disposition", "attachment;filename=" +
						URLEncoder.encode(taskLog.get("filePath"), DataBaseCode.UTF_8.getValue()));
			}
			response.setContentType("APPLICATION/OCTET-STREAM");
			OutputStream out = response.getOutputStream();
			//6、使用response获得输出流，完成文件下载
			out.write(bytes);
			out.flush();
		} catch (IOException e) {
			throw new AppSystemException(e);
		}
	}

	/**
	 * 根据ID删除半结构化采集任务数据
	 *
	 * 1、根据collectSetId在源文件属性表(source_file_attribute)中获得采集的原始表名(table_name)，可能有多条
	 * 2、调用IOnWayCtrl.checkExistsTask()方法对将要删除的信息进行检查
	 * 3、在对象采集设置表(object_collect)中删除该条数据
	 *
	 * @Param: collectSetId long
	 *         含义：源文件属性表ID,object_collect表ID
	 *         取值范围：不为空
	 * @return: 无
	 *
	 * */
	public void deletehalfStructTask(long collectSetId) {
		//1、根据collectSetId在源文件属性表(source_file_attribute)中获得采集的原始表名(table_name)，可能有多条
		List<Object> tableNames = Dbo.queryOneColumnList(
				"select table_name from source_file_attribute where collect_set_id = ?",
				collectSetId);
		if (tableNames.isEmpty()) {
			throw new BusinessException("源文件属性表中未找到采集的原始表名");
		}
		//2、调用IOnWayCtrl.checkExistsTask()方法对将要删除的信息进行检查
		//IOnWayCtrl.checkExistsTask(tableNames, DataSourceType.DML.toString(), db);
		//3、在对象采集设置表(object_collect)中删除该条数据，有且只有一条
		int firNum = Dbo.execute("delete from object_collect where odc_id = ?", collectSetId);
		if (firNum != 1) {
			if (firNum == 0) throw new BusinessException("object_collect表中没有数据被删除!");
			else throw new BusinessException("object_collect表删除数据异常!");
		}
	}

	/**
	 * 根据ID删除FTP采集任务数据
	 *
	 * 1、根据collectSetId在源文件属性表(source_file_attribute)中获得采集的原始表名(table_name)，可能有多条
	 * 2、调用IOnWayCtrl.checkExistsTask()方法对将要删除的信息进行检查
	 * 3、在FTP采集设置表(ftp_collect)中删除该条数据
	 *
	 * @Param: collectSetId long
	 *         含义：源文件属性表ID,object_collect表ID
	 *         取值范围：不为空
	 * @return: 无
	 *
	 * */
	public void deleteFTPTask(long collectSetId) {
		//1、根据collectSetId在源文件属性表(source_file_attribute)中获得采集的原始表名(table_name)，可能有多条
		List<Object> tableNames = Dbo.queryOneColumnList(
				"select table_name from source_file_attribute where collect_set_id = ?",
				collectSetId);
		if (tableNames.isEmpty()) {
			throw new BusinessException("源文件属性表中未找到采集的原始表名");
		}
		//2、调用IOnWayCtrl.checkExistsTask()方法对将要删除的信息进行检查
		//IOnWayCtrl.checkExistsTask(tableNames, DataSourceType.DML.toString(), db);
		//3、在FTP采集设置表(ftp_collect)中删除该条数据，有且只有一条
		int firNum = Dbo.execute("delete from ftp_collect where odc_id = ?", collectSetId);
		if (firNum != 1) {
			if (firNum == 0) throw new BusinessException("ftp_collect表中没有数据被删除!");
			else throw new BusinessException("ftp_collect表删除数据异常!");
		}
	}

	/**
	 * 根据ID和Agent类型删除数据库直连，非结构化文件，DB文件采集任务数据
	 *
	 * 1、判断Agent类型
	 * 2、如果是数据库直连采集任务
	 *      2-1、在数据库设置删除对应的记录
	 *      2-2、在表对应字段表中找到对应的记录并删除
	 *      2-3、在数据库对应表删除对应的记录
	 * 3、如果是结构化文件、DB文件
	 *      3-1、在文件系统设置表删除对应的记录
	 *      3-2、在文件源设置表删除对应的记录
	 * 4、对其他类型的任务进行统一处理
	 *      4-1、在压缩作业参数表删除对应的记录
	 *      4-2、在传送作业参数表删除对应的记录
	 *      4-3、在清洗作业参数表删除对应的记录
	 *      4-4、在hdfs存储作业参数表删除对应的记录
	 *
	 * @Param: collectSetId long
	 *         含义：源文件属性表ID,object_collect表ID
	 *         取值范围：不为空
	 * @Param: agentType String
	 *         含义：agent类型
	 *         取值范围：AgentType代码项的code值(1-5)
	 * @return: 无
	 *
	 * */
	public void deleteOtherTask(long collectSetId, String agentType) {
		//1、判断Agent类型
		if (AgentType.ShuJuKu == AgentType.ofEnumByCode(agentType)) {
			//2、如果是数据库直连采集任务
			List<Object> hbaseNames = Dbo.queryOneColumnList(
					"select hbase_name from source_file_attribute where collect_set_id = ?",
					collectSetId);
			if (hbaseNames.isEmpty()) {
				throw new BusinessException("源文件属性表中未找到系统内对应表名");
			}
			//IOnWayCtrl.checkExistsTask(hbaseNames, DataSourceType.DML.toString(), db);
			//2-1、在数据库设置表删除对应的记录，有且只有一条
			int firNum = Dbo.execute("delete from database_set where database_id =?"
					, collectSetId);
			if (firNum != 1) {
				if (firNum == 0) throw new BusinessException("database_set表中没有数据被删除!");
				else throw new BusinessException("database_set表删除数据异常!");
			}

			//2-2、在表对应字段表中找到对应的记录并删除，可能会有多条
			int secNum = Dbo.execute("delete from table_column where EXISTS" +
							"(select 1 from table_info ti where database_id = ? " +
							"and table_column.table_id=ti.table_id)", collectSetId);
			if (secNum == 0) {
				throw new BusinessException("table_column表中没有数据被删除!");
			}

			//2-3、在数据库对应表删除对应的记录,可能会有多条
			int thiExecute = Dbo.execute("delete from table_info where database_id = ?",
					collectSetId);
			if (thiExecute == 0) {
				throw new BusinessException("table_info表中没有数据被删除!");
			}
		}
		//3、如果是结构化文件、DB文件
		else {
			//3-1、在文件系统设置表删除对应的记录，有且只有一条
			int fouNum = Dbo.execute("delete  from file_collect_set where fcs_id =?"
					, collectSetId);
			if (fouNum != 1) {
				if (fouNum == 0) throw new BusinessException("file_collect_set表中没有数据被删除!");
				else throw new BusinessException("file_collect_set表删除数据异常!");
			}
			//3-2、在文件源设置表删除对应的记录，有且只有一条
			int fifNum = Dbo.execute("delete  from file_source where fcs_id =?", collectSetId);
			if (fifNum != 1) {
				if (fifNum == 0) throw new BusinessException("file_source表中没有数据被删除!");
				else throw new BusinessException("file_source表删除数据异常!");
			}
		}
		//4、对其他类型的任务进行统一处理
		//4-1、在压缩作业参数表删除对应的记录,有且只有一条
		int sevNum = Dbo.execute("delete  from collect_reduce where collect_set_id =?",
				collectSetId);
		if (sevNum != 1) {
			if (sevNum == 0) throw new BusinessException("collect_reduce表中没有数据被删除!");
			else throw new BusinessException("collect_reduce表删除数据异常!");
		}
		//4-2、在传送作业参数表删除对应的记录,有且只有一条
		int eigNum = Dbo.execute("delete  from collect_transfer where collect_set_id =?",
				collectSetId);
		if (eigNum != 1) {
			if (eigNum == 0) throw new BusinessException("collect_transfer表中没有数据被删除!");
			else throw new BusinessException("collect_transfer表删除数据异常!");
		}
		//4-3、在清洗作业参数表删除对应的记录,有且只有一条
		int ninNum = Dbo.execute("delete  from collect_clean where collect_set_id =?",
				collectSetId);
		if (ninNum != 1) {
			if (ninNum == 0) throw new BusinessException("collect_clean表中没有数据被删除!");
			else throw new BusinessException("collect_clean表删除数据异常!");
		}
		//4-4、在hdfs存储作业参数表删除对应的记录,有且只有一条
		int tenNum = Dbo.execute("delete  from collect_hdfs where collect_set_id =?",
				collectSetId);
		if (tenNum != 1) {
			if (tenNum == 0) throw new BusinessException("collect_hdfs表中没有数据被删除!");
			else throw new BusinessException("collect_hdfs表删除数据异常!");
		}
	}

	/**
	 * 查询工程信息
	 *
	 * 1、获取用户ID
	 * 2、根据用户ID在工程登记表(etl_sys)中查询工程代码(etl_sys_cd)和工程名称(etl_sys_name)并返回
	 *
	 * @Param: 无
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result buildJob() {
		//1、获取用户ID
		//2、根据用户ID在工程登记表(etl_sys)中查询工程代码(etl_sys_cd)和工程名称(etl_sys_name)并返回
		return Dbo.queryResult("select etl_sys_cd,etl_sys_name from etl_sys where user_id = ?"
				, getUserId());
	}

	/**
	 * 根据taskId获得某个工程下的任务信息
	 *
	 * 1、根据工程代码在子系统定义表(etl_sub_sys_list)中查询子系统代码(sub_sys_cd)和子系统描述(sub_sys_desc)并返回
	 *
	 * @Param: taskId String
	 *         含义 : 任务ID, etl_sub_sys_list表主键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result selectProject(String taskId) {
		return Dbo.queryResult("select sub_sys_cd,sub_sys_desc from etl_sub_sys_list " +
				"where etl_sys_cd = ? ", taskId);
	}


	/**
	 * 保存FTP采集工程信息
	 *
	 * 1、
	 *
	 * @Param: ftpId String
	 *         含义 : ftp_collect表主键
	 *         取值范围 : 不为空
	 * @Param: projectCode String
	 *         含义 : 工程编码，etl_sub_sys_list表主键
	 *         取值范围 : 不为空
	 * @Param: subSysCode String
	 *         含义 : 子系统代码，etl_sub_sys_list表主键，etl_job_def表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public void saveFTPProjectInfo(String ftpId, String projectCode, String subSysCode) {
		StringBuilder sql = new StringBuilder();
		sql.append(" select datasource_number,datasource_name, ")
				.append(" agent_name,ftp_number,ftp_name,c.ftp_id ")
				.append(" from data_source a join agent_info b on a.source_id = b.source_id ")
				.append(" join ftp_collect c on b.agent_id = c.agent_id ")
				.append(" where is_sendok = ? and ftp_id = ?");
		Map<String, Object> result = Dbo.queryOneObject(sql.toString(), IsFlag.Shi.getCode()
				, ftpId);
		if(result.isEmpty()){
			throw new BusinessException("根据ID未能找到对应的FTP采集信息");
		}
		String dsName = (String)result.get("datasource_name");
		String dsNumber = (String)result.get("datasource_number");
		String ftpNumber = (String)result.get("ftp_number");
		String ftpName = (String)result.get("ftp_name");

	}

	/**
	 * 保存半结构化(对象)采集工程信息
	 *
	 * 1、
	 *
	 * @Param: objCollId String
	 *         含义 : object_collect表主键
	 *         取值范围 : 不为空
	 * @Param: projectCode String
	 *         含义 : 工程编码，etl_sub_sys_list表主键
	 *         取值范围 : 不为空
	 * @Param: subSysCode String
	 *         含义 : 子系统代码，etl_sub_sys_list表主键，etl_job_def表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public void saveHalfStructProjectInfo(String objCollId, String projectCode, String subSysCode) {

	}

	/**
	 * 保存非结构化(文件系统)采集工程信息
	 *
	 * 1、
	 *
	 * @Param: fileCollId String
	 *         含义 : file_collect_set表主键
	 *         取值范围 : 不为空
	 * @Param: projectCode String
	 *         含义 : 工程编码，etl_sub_sys_list表主键
	 *         取值范围 : 不为空
	 * @Param: subSysCode String
	 *         含义 : 子系统代码，etl_sub_sys_list表主键，etl_job_def表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public void saveNonStructProjectInfo(String fileCollId, String projectCode, String subSysCode) {

	}

	/**
	 * 保存数据文件采集和数据库采集采集工程信息
	 *
	 * 1、
	 *
	 * @Param: dataSourceId String
	 *         含义 : datasource_set表主键
	 *         取值范围 : 不为空
	 * @Param: projectCode String
	 *         含义 : 工程编码，etl_sub_sys_list表主键
	 *         取值范围 : 不为空
	 * @Param: subSysCode String
	 *         含义 : 子系统代码，etl_sub_sys_list表主键，etl_job_def表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public void saveDBAndDFProjectInfo(String dataSourceId, String projectCode, String subSysCode) {

	}

	/**
	 * 根据任务ID发送半结构化文件采集任务
	 *
	 * 1、
	 *
	 * @Param: taskId String
	 *         含义 : 任务ID
	 *         取值范围 : 不为空
	 * @return: 无
	 *
	 * */
	public void sendHalfStructTask(String taskId) {
		//SendMsg.sendObjectCollect2Agent(taskId);
	}

	/**
	 * 根据任务ID发送FTP采集任务
	 *
	 * 1、
	 *
	 * @Param: taskId String
	 *         含义 : 任务ID
	 *         取值范围 : 不为空
	 * @return: 无
	 *
	 * */
	public void sendFTPTask(String taskId) {
		//SendMsg.sendFTP2Agent(taskId);
	}

	/**
	 * 根据任务ID发送数据库直连、DB文件、非结构化文件采集任务
	 *
	 * 1、
	 *
	 * @Param: taskId String
	 *         含义 : 任务ID
	 *         取值范围 : 不为空
	 * @return: 无
	 *
	 * */
	public void sendOtherTask(String taskId) {
		//SendMsg.sendMsg2Agent(taskId);
	}

	/**
	 * 根据sourceId查询出设置完成的数据库采集任务和DB文件采集任务的任务ID
	 *
	 * 1、根据数据源ID和用户ID查询出设置完成的数据库采集任务和DB文件采集任务的任务ID并返回
	 *
	 * @Param: sourceId long
	 *         含义 : data_source表主键, agent_info表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result getDBAndDFTaskBySourceId(long sourceId) {
		//1、根据数据源ID和用户ID查询出设置完成的数据库采集任务和DB文件采集任务的任务ID并返回
		return Dbo.queryResult("SELECT das.database_id " +
				"FROM data_source ds " +
				"JOIN agent_info ai ON ds.source_id = ai.source_id " +
				"JOIN database_set das ON ai.agent_id = das.agent_id " +
				"WHERE ds.source_id = ? AND das.is_sendok = ? AND ds.user_id = ?"
				, sourceId, IsFlag.Shi.getCode(), getUserId());
	}

	/**
	 * 根据sourceId查询出设置完成的非结构化文件采集任务的任务ID
	 *
	 * 1、根据数据源ID和用户ID查询出设置完成的非结构化文件采集任务的任务ID并返回
	 *
	 * @Param: sourceId long
	 *         含义 : data_source表主键, agent_info表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result getNonStructTaskBySourceId(long sourceId) {
		//1、根据数据源ID和用户ID查询出设置完成的非结构化文件采集任务的任务ID并返回
		return Dbo.queryResult("SELECT fcs.fcs_id " +
				"FROM data_source ds " +
				"JOIN agent_info ai ON ds.source_id = ai.source_id " +
				"JOIN file_collect_set fcs ON ai.agent_id = fcs.agent_id " +
				"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.user_id = ?"
				, sourceId, IsFlag.Shi.getCode() ,getUserId());
	}

	/**
	 * 根据sourceId查询出设置完成的半结构化文件采集任务的任务ID
	 *
	 * 1、根据数据源ID和用户ID查询出设置完成的半结构化文件采集任务的任务ID并返回
	 *
	 * @Param: sourceId long
	 *         含义 : data_source表主键, agent_info表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result getHalfStructTaskBySourceId(long sourceId) {
		//1、根据数据源ID和用户ID查询出设置完成的半结构化文件采集任务的任务ID并返回
		return Dbo.queryResult("SELECT fcs.odc_id " +
				"FROM data_source ds " +
				"JOIN agent_info ai ON ds.source_id = ai.source_id " +
				"JOIN object_collect fcs ON ai.agent_id = fcs.agent_id " +
				"WHERE ds.source_id = ? AND fcs.is_sendok = ? AND ds.user_id = ?"
				, sourceId, IsFlag.Shi.getCode(), getUserId());
	}

	/**
	 * 根据sourceId查询出FTP采集任务的任务ID
	 *
	 * 1、根据数据源ID和用户ID查询出FTP采集任务的任务ID并返回
	 *
	 * @Param: sourceId long
	 *         含义 : data_source表主键, agent_info表外键
	 *         取值范围 : 不为空
	 * @return: fd.ng.db.resultset.Result
	 *          含义：查询结果集
	 *          取值范围：不会为null
	 *
	 * */
	public Result getFTPTaskBySourceId(long sourceId) {
		//1、根据数据源ID和用户ID查询出FTP采集任务的任务ID并返回
		return Dbo.queryResult("SELECT fcs.ftp_id " +
				"FROM data_source ds " +
				"JOIN agent_info ai ON ds.source_id = ai.source_id " +
				"JOIN ftp_collect fcs ON ai.agent_id = fcs.agent_id " +
				"WHERE ds.source_id = ? AND ds.user_id = ?", sourceId, IsFlag.Shi.getCode(), getUserId());
	}

	/**
	 * 根据参数获得任务日志信息
	 *
	 * 1、根据agent_id和user_id获取agent信息
	 * 2、在agent信息中获取日志目录
	 * 3、调用方法获取日志,目前工具类不存在
	 * 4、将日志信息和日志文件的路径封装成map
	 * 5、返回map
	 *
	 * @Param: agentId long
	 *         含义 : agent_info表主键, ftp_collect, object_collect, file_collect_set, database_set表外键
	 *         取值范围 : 不为空
	 * @Param: userId long
	 *         含义 : 用户ID，sys_user表主键, agent_down_info表外键
	 *         取值范围 : 不为空
	 * @Param: logType String
	 *         含义：日志类型(完整日志、错误日志)
	 *         取值范围：All : 完整日志, Wrong : 错误日志
	 * @Param: readNum int
	 *         含义：查看日志条数
	 *         取值范围：不为空
	 * @return: Map<String, String></>
	 *          含义：存放文件内容和日志文件路径的map集合
	 *          取值范围：存放文件内容的Entry,key为log，存放文件路径的Entry,key为filePath
	 *
	 * */
	private Map<String, String> getTaskLog(long agentId, long userId, String logType, int readNum) {
		//1、根据agent_id和user_id获取agent信息
		Map<String, Object> result = Dbo.queryOneObject(
				"select * from agent_down_info where agent_id = ? and user_id = ?", agentId,
				userId);
		String agentIP = (String) result.get("agent_ip");
		//2、在agent信息中获取日志目录
		String logDir = (String) result.get("log_dir");
		String userName = (String) result.get("user_name");
		String passWord = (String) result.get("passwd");
		if (StringUtil.isNotBlank(logDir)) {
			throw new BusinessException("日志文件不存在" + logDir);
		}

		if (StringUtil.isNotBlank(agentIP)) {
			throw new BusinessException("AgentIP错误" + agentIP);
		}

		//用户选择查看错误日志
		if (logType.equals("Wrong")) {
			logDir = logDir.substring(0, logDir.lastIndexOf(File.separator) + 1) + "error.log";
		}

		//3、调用方法获取日志,目前工具类不存在
		String taskLog = LogReader.readAgentLog(logDir, agentIP, SFTP_PORT, userName, passWord, readNum);
		if (StringUtil.isBlank(taskLog)) {
			taskLog = "日志信息";
		}
		//4、将日志信息和日志文件的路径封装成map
		Map<String, String> map = new HashMap<>();
		map.put("log", taskLog);
		map.put("filePath", logDir);
		//5、返回map
		return map;
	}

}
