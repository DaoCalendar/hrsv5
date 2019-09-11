package hrds.b.biz.datasource;

import fd.ng.core.utils.CodecUtil;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.resultset.Result;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.annotation.RequestParam;
import fd.ng.web.annotation.UploadFile;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.FileUploadUtil;
import fd.ng.web.util.ResponseUtil;
import hrds.commons.base.BaseAction;
import hrds.commons.entity.*;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.exception.ExceptionEnum;
import hrds.commons.utils.ActionUtil;
import hrds.commons.utils.key.PrimayKeyGener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源增删改，导入、下载类
 *
 * @author mine
 * @date 2019-09-03 16:44:25
 */
public class DataSourceAction extends BaseAction {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * 新增/编辑数据源
	 * <p>
	 * 1.字段合法性检查
	 * 2.判断数据源编号是否为空，为空则为新增，不为空则为编辑
	 * 3.新增前查询数据源编号是否已存在，存在则抛异常，不存在就新增
	 * 4.保存或更新数据源信息
	 * 5.如果是编辑先删除数据源与部门关系
	 * 6.保存或更新数据源与部门关系信息
	 *
	 * @param dataSource 数据源实体
	 * @param depIds     部门ID(数据源与部门关系表source_relation_dep主键ID,前台传值可能会有1或多个值，
	 *                   通过分隔符拼接成的字符串)
	 */
	public void saveDataSource(@RequestBean Data_source dataSource, @RequestParam String depIds) {
		// 1.字段做合法性检查
		// 验证data_source_remark数据源名称合法性
		if (StringUtil.isBlank(dataSource.getDatasource_name())) {
			throw new BusinessException("数据源名称不能为空以及不能为空格，datasource_name=" + dataSource
					.getDatasource_name());
		}
		// 数据源编号长度
		int len = 4;
		// 验证数据源编号datasource_number合法性
		if (StringUtil.isBlank(dataSource.getDatasource_number())
				|| dataSource.getDatasource_number().length() > len) {
			throw new BusinessException("数据源编号不能为空以及不能为空格或数据源编号长度不能超过四位，" +
					"datasource_number=" + dataSource.getDatasource_number());
		}
		// 验证部门depIds合法性
		if (StringUtil.isBlank(depIds)) {
			throw new BusinessException("部门不能为空格，depIds=" + depIds);
		}
		// 2.判断数据源id(数据源data_source表主键ID)是否为空
		if (dataSource.getSource_id() == null) {
			// 新增,初始化一些非页面传值
			// 数据源主键ID
			dataSource.setSource_id(PrimayKeyGener.getNextId());
			// 数据源创建用户ID
			dataSource.setCreate_user_id(getUserId());
			// 数据源创建日期
			dataSource.setCreate_date(DateUtil.getSysDate());
			// 数据源创建时间
			dataSource.setCreate_time(DateUtil.getSysTime());
			// 3.新增前查询数据源编号是否已存在
			Result result = Dbo.queryResult("select datasource_number from " +
							Data_source.TableName + "  where datasource_number=?",
					dataSource.getDatasource_number());
			if (!result.isEmpty()) {
				// 数据源编号重复
				throw new BusinessException("数据源编号重复,datasource_number=" +
						dataSource.getDatasource_number());
			}
			// 4.保存数据源信息
			if (dataSource.add(Dbo.db()) != 1) {
				// 新增保存失败
				throw new BusinessException("新增保存数据源data_source表数据失败,datasource_number=" +
						dataSource.getDatasource_number());
			}
		} else {
			// 编辑
			// 4.更新数据源信息
			if (dataSource.update(Dbo.db()) != 1) {
				// 编辑保存失败
				throw new BusinessException("编辑保存数据源data_source表数据失败,datasource_number=" +
						dataSource.getDatasource_number());
			}
			// 5.先删除数据源与部门关系信息
			int num = Dbo.execute("delete from " + Source_relation_dep.TableName +
					" where source_id=?", dataSource.getSource_id());
			if (num < 1) {
				throw new BusinessException("编辑时会先删除原数据源与部门关系信息，删除错旧关系时错误，" +
						"source_id=" + dataSource.getSource_id());
			}
		}
		// 6.保存或更新数据源与部门关系信息
		saveSourceRelationDep(dataSource.getSource_id(), depIds);
	}

	/**
	 * 保存数据源与部门关系表信息
	 * <p>
	 * 1.循环保存或更新数据源与部门关系信息
	 *
	 * @param source_id 含义：数据源与部门关系表外键ID
	 *                  取值范围，不能为空以及不能为空格
	 * @param depIds    含义：数据源与部门关系表source_relation_dep主键ID
	 *                  取值范围：前台传值可能会有1或多个值，通过分隔符拼接成的字符串，不能为空已经不能为空格
	 */
	private void saveSourceRelationDep(long source_id, String depIds) {
		// 建立数据源与部门关系信息
		Source_relation_dep srd = new Source_relation_dep();
		// 设置数据源与部门关系表外键ID
		srd.setSource_id(source_id);
		// 分隔部门id
		String[] split = depIds.split(",");
		// 循环保存数据源与部门关系表信息
		for (String dep_id : split) {
			// 设置数据源与部门关系表主键ID
			srd.setDep_id(dep_id);
			if (srd.add(Dbo.db()) != 1) {
				throw new BusinessException("新增保存数据源与部门关系Source_relation_dep表信息失败，" +
						"dep_id=" + dep_id + ",source_id=" + source_id);
			}
		}
	}

	/**
	 * 根据数据源编号查询数据源及数据源与部门关系信息
	 *
	 * <p>
	 * 1.判断该数据源下是否有数据，没有抛异常，有则返回查询结果
	 *
	 * @param source_id 数据源编号
	 * @return 返回查询结果集
	 */
	public Result searchDataSource(Long source_id) {
		// 1.判断该数据源下是否有数据，没有抛异常，有则返回查询结果
		Result result = Dbo.queryResult("select ds.*,srd.dep_id from data_source ds " +
				" join source_relation_dep srd on ds.source_id=srd.source_id " +
				"  where ds.source_id = ?", source_id);
		if (result.isEmpty()) {
			// 该数据源下数据为空(此为编辑情况下数据不能为空）
			//FIXME 查询不到数据为什么要抛异常。只有具体使用这个数据的地方才应该根据是否有数据来抛出异常
			throw new BusinessException(ExceptionEnum.DATA_NOT_EXIST);
		}
		// 不为空，返回查询结果
		return result;

	}

	/**
	 * 删除数据源与部门关系表信息
	 * <p>
	 * 1.删除数据源与部门关系表信息，失败就抛异常，否则就正常删除
	 *
	 * @param source_id 数据源编号
	 */
	public void deleteSourceRelationDep(Long source_id) {
		// 1.删除数据源与部门关系表信息，
		int num = Dbo.execute("delete from " + Source_relation_dep.TableName +
				" where source_id=?", source_id);
		if (num != 1) {
			// 删除失败
			throw new BusinessException(ExceptionEnum.DATA_DELETE_ERROR);
		}
	}

	/**
	 * 删除数据源信息
	 * <p>
	 * 1.先查询该datasource下是否还有agent,有不能删除，没有，可以删除
	 * 2.删除data_source表信息，删除失败就抛异常，否则正常删除
	 * 3.判断删除的数据是否不存在，不存在就抛异常
	 * 3.删除source_relation_dep信息
	 *
	 * @param source_id 数据源编号
	 */
	public void deleteDataSource(Long source_id) {

		// 1.先查询该datasource下是否还有agent
		if (Dbo.queryNumber("SELECT * FROM agent_info  WHERE source_id=?", source_id)
				.orElse(-1) > 0) {
			throw new BusinessException("此数据源下还有agent，不能删除,source_id=" + source_id);
		}

		// 2.删除data_source表信息
		int num = Dbo.execute("delete from " + Data_source.TableName +
				" where source_id=?", source_id);
		if (num != 1) {
			// 3.判断库里是否没有这条数据
			if (num == 0) {
				throw new BusinessException("删除数据源信息表data_source失败，数据库里没有此条数据，source_id=" + source_id);
			}
			throw new BusinessException("删除数据源信息表data_source失败，source_id=" + source_id);
		}
		// 4.删除source_relation_dep信息
		deleteSourceRelationDep(source_id);
	}

	/**
	 * 上传文件
	 * <p>
	 * 1.通过页面传值循环遍历获取文件以及文件名
	 * 2.创建一个缓冲区,循环将输入流读入到缓冲区
	 * 3.使用base64对数据进行编码
	 * 4.导入数据
	 *
	 * @param agent_ip   agent地址
	 * @param agent_port agent端口
	 * @param user_id    页面传递用户编号
	 * @param files      所有文件
	 * @throws IOException
	 */
	@UploadFile
	public void uploadFile(@RequestParam String agent_ip, @RequestParam String agent_port,
	                       @RequestParam Long user_id, String[] files) throws IOException {
		String strTemp = null;
		// 1.循环遍历获取文件以及文件名
		try {
			for (String file : files) {
				//获取文件
				File uploadedFile = FileUploadUtil.getUploadedFile(file);
				// 获得文件名
				String fileName = FileUploadUtil.getOriginalFileName(file);
				if (fileName == null || fileName.trim().equals("")) {
					//FIXME 说明在什么情况下，会进入这里
					continue;
				}
				/*注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，
				如： c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt*/
				//处理获取到的上传文件的文件名的路径部分，只保留文件名部分
				fileName = fileName.substring(fileName.lastIndexOf(File.separator + File.separator) + 1);
				InputStream in = new FileInputStream(uploadedFile);

				// 2.创建一个缓冲区
				//FIXME 缓冲区为什么这么小
				byte[] buffer = new byte[1];
				//循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
				StringBuffer temp = new StringBuffer();
				while ((in.read(buffer)) > 0) {
					temp.append((new String(buffer, CodecUtil.UTF8_CHARSET)));
				}
				//关闭输入流
				in.close();
				// 3.使用base64编码
				strTemp = new String(Base64.getDecoder().decode(temp.toString()), CodecUtil.UTF8_CHARSET);
				//FIXME 这程序测试过吗？上面的 temp 没有做 clear 也可以？
			}
			// 4.导入贴源层元数据
			//FIXME 为什么只用循环最后一次形成的变量？
			importDclData(strTemp, agent_ip, agent_port, user_id,
					ActionUtil.getUser().getUserId());
		} catch (Exception e) {
			throw new AppSystemException(e);
		}
	}

	/**
	 * 导入数据源数据
	 * FIXME 为什么是 “贴源层元数据”
	 * <p>
	 * 1.解析文件获取文件所有信息
	 * 2.遍历并解析拿到每张表的信息
	 * 3.将对应表信息插入库（数据源信息还需要判断数据源名称是否重复，重复抛异常，否则正常入库）
	 * FIXME 为什么是从文件中得到了表的信息？这个文件是什么
	 *
	 * @param strTemp         文件信息
	 * @param agent_port      agent端口
	 * @param agent_ip        agent地址
	 * @param user_id         页面传递用户编号
	 * @param user_collect_id 登录用户id     FIXME 解释这个ID和 user_id 有什么区别，为什么需要
	 */
	private void importDclData(String strTemp, String agent_ip, String agent_port, Long
			user_id, Long user_collect_id) {
		// 1.获取文件所有信息
		//FIXME 徐超确认：fastJson在转换对象的时候，不需要使用诸如 TypeReference 一类的明确类型吗？
		Map<String, Object> map = JsonUtil.toObject(strTemp, Map.class);
		// 2.遍历并解析拿到每张表的信息
		//FIXME 对这个MAP的数据进行详细说明。
		//FIXME 导入的主键需要重新生成。
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			// 数据源信息
			if ("data_source".equals(entry.getKey())) {
				// 获取数据源信息
				Data_source data_source = JsonUtil.toObject(entry.getValue().toString(), Data_source.class);
				// 判断上传文件的数据源名称和已有的名称是否重复
				Result result = Dbo.queryResult("select * from data_source where datasource_name = ?",
						data_source.getDatasource_name());
				if (!result.isEmpty()) {
					//FIXME 不把重复的名称返回，用户怎么知道
					throw new BusinessException("数据源名称重复");
				}
				//数据源data_source
				data_source.setCreate_user_id(user_collect_id);
				// 3.入库
				if (data_source.add(Dbo.db()) != 1) {
					throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
				}
				//数据源和部门关系表source_relation_dep
				Result diResult = Dbo.queryResult("select dep_id from department_info where dep_name" +
						" = '第一部门'");
				//FIXME 为什么写死了？
				//FIXME 为什么不做查询结果存在性判断
				//FIXME 需要对数据所属部门进行判断，并正确插入
				String dep_id = diResult.getString(0, "dep_id");
				Source_relation_dep source_relation_dep = new Source_relation_dep();
				source_relation_dep.setDep_id(dep_id);
				source_relation_dep.setSource_id(data_source.getSource_id());
				// 3.入库
				if (1 != source_relation_dep.add(Dbo.db())) {
					throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
				}
			}
			//Agent信息表agent_info
			if ("agent_info".equals(entry.getKey())) {
				// 获取agent信息表信息
				//FIXME idea的提示要解决
				List<Agent_info> agent_info = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				// 3.循环入库agent_info
				for (Agent_info agent : agent_info) {
					agent.setUser_id(user_id);
					agent.setAgent_ip(agent_ip);
					agent.setAgent_port(agent_port);
					if (1 != agent.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//Agent下载信息表Agent_down_info
			if ("agent_down_info".equals(entry.getKey())) {
				// 获取Agent下载信息表信息
				List<Agent_down_info> agent_down_info = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				// 3.循环入库agent_down_info
				for (Agent_down_info down_info : agent_down_info) {
					down_info.setUser_id(user_id);
					down_info.setAgent_ip(agent_ip);
					down_info.setAgent_port(agent_port);
					if (down_info.add(Dbo.db()) != 1) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			if ("collect_job_classify".equals(entry.getKey())) {
				// 获取采集任务分类表collect_job_classify信息
				List<Collect_job_classify> collect_job_classify = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库collect_job_classify
				for (Collect_job_classify classify : collect_job_classify) {
					classify.setUser_id(user_id);
					if (classify.add(Dbo.db()) != 1) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//ftp采集设置ftp_collect
			if ("ftp_collect".equals(entry.getKey())) {
				// 获取tp采集设置ftp_collect信息
				List<Ftp_collect> ftp_collect = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库ftp_collect
				for (Ftp_collect collect : ftp_collect) {
					if (collect.add(Dbo.db()) != 1) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//ftp已传输表ftp_transfered
			if ("ftp_transfered".equals(entry.getKey())) {
				// 获取ftp已传输表ftp_transfered信息
				List<Ftp_transfered> ftp_transfered = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库ftp_transfered
				for (Ftp_transfered transfered : ftp_transfered) {
					if (transfered.add(Dbo.db()) != 1) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//ftp目录表ftp_folder
			if ("ftp_folder".equals(entry.getKey())) {
				// 获取ftp目录表ftp_folder信息
				List<Ftp_folder> ftp_folder = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库ftp_folder
				for (Ftp_folder folder : ftp_folder) {
					if (folder.add(Dbo.db()) != 1) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//对象采集设置object_collect
			if ("object_collect".equals(entry.getKey())) {
				// 获取对象采集设置object_collect信息
				List<Object_collect> object_collect = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库object_collect
				for (Object_collect collect : object_collect) {
					if (1 != collect.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//对象采集对应信息object_collect_task
			if ("object_collect_task".equals(entry.getKey())) {
				// 获取对象采集对应信息object_collect_task信息
				List<Object_collect_task> object_collect_task = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库object_collect_task
				for (Object_collect_task collect_task : object_collect_task) {
					if (1 != collect_task.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//对象采集存储设置object_storage
			if ("object_storage".equals(entry.getKey())) {
				//获取对象采集存储设置object_storage信息
				List<Object_storage> object_storage = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库object_storage
				for (Object_storage storage : object_storage) {
					if (1 != storage.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//对象采集结构信息object_collect_struct
			if ("object_collect_struct".equals(entry.getKey())) {
				//获取对象采集结构信息object_collect_struct信息
				List<Object_collect_struct> object_collect_struct = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库object_collect_struct
				for (Object_collect_struct collect_struct : object_collect_struct) {
					if (1 != collect_struct.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//数据库设置database_set
			if ("database_set".equals(entry.getKey())) {
				//获取数据库设置database_set信息
				List<Database_set> database_set = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库database_set
				for (Database_set set : database_set) {
					if (1 != set.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//文件系统设置file_collect_set
			if ("file_collect_set".equals(entry.getKey())) {
				//获取文件系统设置file_collect_set信息
				List<File_collect_set> file_collect_set = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库file_collect_set
				for (File_collect_set collect_set : file_collect_set) {
					if (1 != collect_set.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//文件源设置file_source
			if ("file_source".equals(entry.getKey())) {
				// 获取文件源设置file_source信息
				List<File_source> file_source = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库file_source
				for (File_source source : file_source) {
					if (1 != source.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			// 信号文件入库信息signal_file
			if ("signal_file".equals(entry.getKey())) {
				//获取信号文件入库信息signal_file信息
				List<Signal_file> signal_file = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库signal_file
				for (Signal_file file : signal_file) {
					if (1 != file.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//数据库对应的表table_info
			if ("table_info".equals(entry.getKey())) {
				//获取数据库对应的表table_info信息
				List<Table_info> table_info = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库table_info
				for (Table_info info : table_info) {
					if (1 != info.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//列合并信息column_merge
			if ("column_merge".equals(entry.getKey())) {
				//获取列合并信息column_merge信息
				List<Column_merge> column_merge = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库column_merge
				for (Column_merge merge : column_merge) {
					if (1 != merge.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//表存储信息table_storage_info
			if ("table_storage_info".equals(entry.getKey())) {
				//获取表存储信息table_storage_info信息
				List<Table_storage_info> table_storage_info = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库table_storage_info
				for (Table_storage_info storage_info : table_storage_info) {
					if (1 != storage_info.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//表清洗参数信息table_clean
			if ("table_clean".equals(entry.getKey())) {
				// 获取表清洗参数信息table_clean信息
				List<Table_clean> table_clean = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库table_clean
				for (Table_clean clean : table_clean) {
					if (1 != clean.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			//表对应的字段table_column
			if ("table_column".equals(entry.getKey())) {
				// 获取表对应的字段table_column信息
				List<Table_column> table_column = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				//3.循环入库table_column
				for (Table_column column : table_column) {
					if (1 != column.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			// 列清洗参数信息column_clean
			if ("column_clean".equals(entry.getKey())) {
				// 获取列清洗参数信息column_clean信息
				List<Column_clean> column_clean = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				// 3.循环入库column_clean
				for (Column_clean clean : column_clean) {
					if (1 != clean.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
			// 列拆分信息表column_split
			if ("column_split".equals(entry.getKey())) {
				// 获取列拆分信息表column_split信息
				List<Column_split> column_split = JsonUtil.toObject(entry.getValue().toString(),
						List.class);
				// 3.循环入库column_split
				for (Column_split split : column_split) {
					if (1 != split.add(Dbo.db())) {
						throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
					}
				}
			}
		}
	}

	/**
	 * 下载文件
	 * FIXME 这是给什么功能用的？为什么要写文件？
	 * <p>
	 * 1.从数据库取出相应数据封装到map中
	 * 2.通过base64将map转string进行编码
	 * 3.通过流的方式写文件
	 *
	 * @param source_id
	 * @throws IOException
	 */
	public void downloadFile(Long source_id) throws IOException {
		// 1.封装数据库数据入map
		//FIXME 被封装进去的数据，不需要使用Result，而且，这个Map应该指定明确类型而不是用Object
		// 写100字的邮件，说明什么是泛型
		Map<String, Object> collection_object = new HashMap<String, Object>();
		//数据源data_source
		Result dsResult = Dbo.queryResult("select * from data_source where source_id = ?", source_id);
		//FIXME 查询结果存在性检查不需要？
		collection_object.put("data_source", dsResult);
		//Agent信息表agent_info
		Result aiResult = Dbo.queryResult("select * from agent_info where source_id = ?",
				source_id);
		collection_object.put("agent_info", aiResult);
		//Agent下载信息Agent_down_info
		Result agent_down_infoResult = new Result();
		for (int i = 0; i < aiResult.getRowCount(); i++) {
			Result adiResult = Dbo.queryResult("select * from agent_down_info where agent_id = ?",
					aiResult.getLong(i, "agent_id"));
			agent_down_infoResult.add(adiResult);
		}
		collection_object.put("agent_down_info", agent_down_infoResult);
		//采集任务分类表collect_job_classify
		Result classifyResult = new Result();
		for (int i = 0; i < aiResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_job_classify where agent_id = ?",
					aiResult.getLong(i, "agent_id"));
			classifyResult.add(result);
		}
		collection_object.put("collect_job_classify", classifyResult);
		//ftp采集设置ftp_collect
		Result ftp_collectResult = new Result();
		for (int i = 0; i < aiResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from ftp_collect where agent_id = ?",
					aiResult.getLong(i, "agent_id"));
			ftp_collectResult.add(result);
		}
		collection_object.put("ftp_collect", ftp_collectResult);
		//ftp已传输表ftp_transfered
		Result ftp_transferedResult = new Result();
		for (int i = 0; i < ftp_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from ftp_transfered where ftp_id = ?",
					ftp_collectResult.getLong(i, "ftp_id"));
			ftp_transferedResult.add(result);
		}
		collection_object.put("ftp_transfered", ftp_transferedResult);
		//ftp目录表ftp_folder
		Result ftp_folderResult = new Result();
		for (int i = 0; i < ftp_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from ftp_folder where ftp_id = ?",
					ftp_collectResult.getLong(i, "ftp_id"));
			ftp_folderResult.add(result);
		}
		collection_object.put("ftp_folder", ftp_folderResult);
		//对象采集设置object_collect
		Result object_collectResult = new Result();
		for (int i = 0; i < aiResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from object_collect where agent_id = ?",
					aiResult.getLong(i, "agent_id"));
			object_collectResult.add(result);
		}
		collection_object.put("object_collect", object_collectResult);
		//对象采集对应信息object_collect_task
		Result object_collect_taskResult = new Result();
		for (int i = 0; i < object_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from object_collect_task where odc_id = ?",
					object_collectResult.getLong(i, "odc_id"));
			object_collect_taskResult.add(result);
		}
		collection_object.put("object_collect_task", object_collect_taskResult);
		//对象采集存储设置object_storage
		Result object_storageResult = new Result();
		for (int i = 0; i < object_collect_taskResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from object_storage where ocs_id = ?",
					object_collect_taskResult.getString(i, "ocs_id"));
			object_storageResult.add(result);
		}
		collection_object.put("object_storage", object_storageResult);
		//对象采集结构信息object_collect_struct
		Result object_collect_structResult = new Result();
		for (int i = 0; i < object_collect_taskResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from object_collect_struct where ocs_id = ?",
					object_collect_taskResult.getLong(i, "ocs_id"));
			object_collect_structResult.add(result);
		}
		collection_object.put("object_collect_struct", object_collect_structResult);
		//数据库设置database_set
		Result database_setResult = new Result();
		for (int i = 0; i < aiResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from database_set where agent_id = ?",
					aiResult.getLong(i, "agent_id"));
			database_setResult.add(result);
		}
		collection_object.put("database_set", database_setResult);
		//文件系统设置file_collect_set
		Result file_collect_setResult = new Result();
		for (int i = 0; i < aiResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from file_collect_set where agent_id = ?",
					aiResult.getLong(i, "agent_id"));
			file_collect_setResult.add(result);
		}
		collection_object.put("file_collect_set", file_collect_setResult);
		//文件源设置file_source
		Result file_sourceResult = new Result();
		for (int i = 0; i < file_collect_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from file_source where fcs_id = ?",
					file_collect_setResult.getLong(i, "fcs_id"));
			file_sourceResult.add(result);
		}
		collection_object.put("file_source", file_sourceResult);
		//卸数作业参数表collect_frequency
		Result collect_frequencyResult = new Result();
		for (int i = 0; i < database_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_frequency where collect_set_id = ?",
					database_setResult.getLong(i, "database_id"));
			collect_frequencyResult.add(result);
		}
		for (int i = 0; i < file_collect_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_frequency where collect_set_id = ?",
					file_collect_setResult.getLong(i, "fcs_id"));
			collect_frequencyResult.add(result);
		}
		for (int i = 0; i < object_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_frequency where collect_set_id = ?",
					object_collectResult.getLong(i, "odc_id"));
			collect_frequencyResult.add(result);
		}
		for (int i = 0; i < ftp_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_frequency where collect_set_id = ?",
					ftp_collectResult.getLong(i, "ftp_id"));
			collect_frequencyResult.add(result);
		}
		collection_object.put("collect_frequency", collect_frequencyResult);

		//压缩作业参数表collect_reduce
		Result collect_reduceResult = new Result();
		for (int i = 0; i < database_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_reduce where collect_set_id = ?",
					database_setResult.getLong(i, "database_id"));
			collect_reduceResult.add(result);
		}
		for (int i = 0; i < file_collect_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_reduce where collect_set_id = ?",
					file_collect_setResult.getLong(i, "fcs_id"));
			collect_reduceResult.add(result);
		}
		for (int i = 0; i < object_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_reduce where collect_set_id = ?",
					object_collectResult.getLong(i, "odc_id"));
			collect_reduceResult.add(result);
		}
		for (int i = 0; i < ftp_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_reduce where collect_set_id = ?",
					ftp_collectResult.getLong(i, "ftp_id"));
			collect_reduceResult.add(result);
		}
		collection_object.put("collect_reduce", collect_reduceResult);

		//传递作业参数表collect_transfer
		Result collect_transferResult = new Result();
		for (int i = 0; i < database_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_transfer where collect_set_id = ?",
					database_setResult.getLong(i, "database_id"));
			collect_transferResult.add(result);
		}
		for (int i = 0; i < file_collect_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_transfer where collect_set_id = ?",
					file_collect_setResult.getLong(i, "fcs_id"));
			collect_transferResult.add(result);
		}
		for (int i = 0; i < object_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_transfer where collect_set_id = ?",
					object_collectResult.getLong(i, "odc_id"));
			collect_transferResult.add(result);
		}
		for (int i = 0; i < ftp_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_transfer where collect_set_id = ?",
					ftp_collectResult.getLong(i, "ftp_id"));
			collect_transferResult.add(result);
		}
		collection_object.put("collect_transfer", collect_transferResult);

		//清洗作业参数表collect_clean
		Result collect_cleanResult = new Result();
		for (int i = 0; i < database_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_clean where collect_set_id = ?",
					database_setResult.getLong(i, "database_id"));
			collect_cleanResult.add(result);
		}
		for (int i = 0; i < file_collect_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_clean where collect_set_id = ?",
					file_collect_setResult.getLong(i, "fcs_id"));
			collect_cleanResult.add(result);
		}
		for (int i = 0; i < object_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_clean where  collect_set_id = ?",
					object_collectResult.getLong(i, "odc_id"));
			collect_cleanResult.add(result);
		}
		for (int i = 0; i < ftp_collectResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from collect_clean where collect_set_id = ?",
					ftp_collectResult.getLong(i, "ftp_id"));
			collect_cleanResult.add(result);
		}
		collection_object.put("collect_clean", collect_cleanResult);

		//信号文件入库信息signal_file
		Result signal_fileResult = new Result();
		for (int i = 0; i < database_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from signal_file where database_id = ?",
					database_setResult.getLong(i, "database_id"));
			signal_fileResult.add(result);
		}
		collection_object.put("signal_file", signal_fileResult);

		//数据库对应的表table_info
		Result table_infoResult = new Result();
		for (int i = 0; i < database_setResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from table_info where database_id = ?",
					database_setResult.getLong(i, "database_id"));
			table_infoResult.add(result);
		}
		collection_object.put("table_info", table_infoResult);

		//列合并信息表column_merge
		Result column_mergeResult = new Result();
		for (int i = 0; i < table_infoResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from column_merge where table_id = ?",
					table_infoResult.getLong(i, "table_id"));
			column_mergeResult.add(result);
		}
		collection_object.put("column_merge", column_mergeResult);

		//表存储信息table_storage_info
		Result table_storage_infoResult = new Result();
		for (int i = 0; i < table_infoResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from table_storage_info where table_id = ?",
					table_infoResult.getLong(i, "table_id"));
			table_storage_infoResult.add(result);
		}
		collection_object.put("table_storage_info", table_storage_infoResult);

		//表清洗参数信息table_clean
		Result table_cleanResult = new Result();
		for (int i = 0; i < table_infoResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from table_clean where table_id = ?",
					table_infoResult.getLong(i, "table_id"));
			table_cleanResult.add(result);
		}
		collection_object.put("table_clean", table_cleanResult);

		//表对应的字段table_column
		Result table_columnResult = new Result();
		for (int i = 0; i < table_infoResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from table_column where table_id = ?",
					table_infoResult.getLong(i, "table_id"));
			table_columnResult.add(result);
		}
		collection_object.put("table_column", table_columnResult);

		//列清洗参数信息 column_clean
		Result column_cleanResult = new Result();
		for (int i = 0; i < table_columnResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from column_clean where column_id = ?",
					table_columnResult.getLong(i, "column_id"));
			column_cleanResult.add(result);
		}
		collection_object.put("column_clean", column_cleanResult);

		//列拆分信息表column_split
		Result column_splitResult = new Result();
		for (int i = 0; i < table_columnResult.getRowCount(); i++) {
			Result result = Dbo.queryResult("select * from column_split where column_id = ?",
					table_columnResult.getLong(i, "column_id"));
			column_splitResult.add(result);
		}
		collection_object.put("column_split", column_splitResult);

		// 2.使用base64编码
		byte[] bytes = Base64.getEncoder().encode(JsonUtil.toJson(collection_object).getBytes(CodecUtil.UTF8_CHARSET));
		// 判断文件是否存在
		if (bytes == null) {
			throw new BusinessException("此文件不存在");
		}
		// 通过流的方式写入文件
		HttpServletResponse response = ResponseUtil.getResponse();
		// 3.清空response
		response.reset();

		// 设置响应编码格式
		response.setCharacterEncoding(CodecUtil.UTF8_STRING);

		// 设置响应头，控制浏览器下载该文件
		response.setContentType("APPLICATION/OCTET-STREAM");

		// 创建输出流
		OutputStream out = response.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();
	}

}
