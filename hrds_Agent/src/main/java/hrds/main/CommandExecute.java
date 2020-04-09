package hrds.main;

import com.alibaba.fastjson.JSONObject;
import fd.ng.core.annotation.DocClass;
import hrds.agent.job.biz.bean.CollectTableBean;
import hrds.agent.job.biz.bean.JobStatusInfo;
import hrds.agent.job.biz.bean.SourceDataConfBean;
import hrds.agent.job.biz.constant.JobConstant;
import hrds.agent.job.biz.core.DataBaseJobImpl;
import hrds.agent.job.biz.core.DataFileJobImpl;
import hrds.agent.job.biz.core.dbstage.writer.JdbcToCsvFileWriter;
import hrds.agent.job.biz.utils.FileUtil;
import hrds.agent.job.biz.utils.JobStatusInfoUtil;
import hrds.commons.codes.CollectType;
import hrds.commons.exception.AppSystemException;
import hrds.commons.utils.Constant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

@DocClass(desc = "作业调度数据库采集、数据库抽取、db文件采集程序入口", author = "zxz", createdate = "2020/1/3 10:38")
public class CommandExecute {
	//打印日志
	private static final Log log = LogFactory.getLog(JdbcToCsvFileWriter.class);

	/**
	 * @param args 主程序入口，获取参数调用采集后台
	 *             参数1：任务ID
	 *             参数2：表名
	 *             参数3：采集类型
	 *             参数4：跑批日期
	 *             参数5-N：sql占位符参数
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 4) {
			log.info("请按照规定的格式传入参数，必须参数不能为空");
			log.info("必须参数：参数1：任务ID；参数2：表名；参数3：采集类型；参数4：跑批日期；");
			log.info("非必须参数：参数5-N：sql占位符参数");
			System.exit(-1);
		}
		String taskId = args[0];
		String tableName = args[1];
		String collectType = args[2];
		String etlDate = args[3];
		StringBuilder sqlParam = new StringBuilder();
		//获取sql占位符的参数
		if (args.length > 4) {
			for (int i = 4; i < args.length; i++) {
				sqlParam.append(args[i]).append(JobConstant.SQLDELIMITER);
			}
			sqlParam.delete(sqlParam.length() - 1, sqlParam.length());
		}
		try {
			String taskInfo = FileUtil.readFile2String(new File(Constant.MESSAGEFILE
					+ taskId));
			//对配置信息解压缩并反序列化为SourceDataConfBean对象
			SourceDataConfBean sourceDataConfBean = JSONObject.parseObject(taskInfo, SourceDataConfBean.class);
			//1.获取json数组转成File_source的集合
			List<CollectTableBean> collectTableBeanList = sourceDataConfBean.getCollectTableBeanArray();
			//获取需要采集的表对象
			CollectTableBean collectTableBean = getCollectTableBean(collectTableBeanList, tableName);
			//设置跑批日期
			collectTableBean.setEtlDate(etlDate);
			//设置sql占位符参数
			collectTableBean.setSqlParam(sqlParam.toString());
			//判断采集类型，根据采集类型调用对应的方法
			if (CollectType.ShuJuKuCaiJi.getCode().equals(collectType)) {
				//XXX 数据库采集，目前是做的数据库抽取的逻辑，这个代码项要改
				startJdbcToFile(sourceDataConfBean, collectTableBean);
			} else if (CollectType.DBWenJianCaiJi.getCode().equals(collectType)) {
				startDbFileCollect(sourceDataConfBean, collectTableBean);
			} else {
				throw new AppSystemException("不支持的采集类型");
			}
		} catch (Exception e) {
			log.error("执行采集失败!", e);
			System.exit(-1);
		}
	}


	private static void startDbFileCollect(SourceDataConfBean sourceDataConfBean, CollectTableBean collectTableBean) {
		ExecutorService executor = null;
		try {
			//初始化当前任务需要保存的文件的根目录
			String[] paths = {Constant.JOBINFOPATH, Constant.DBFILEUNLOADFOLDER};
			FileUtil.initPath(sourceDataConfBean.getDatabase_id(), paths);
			//此处不会有海量的任务需要执行，不会出现队列中等待的任务对象过多的OOM事件。
			//TODO Runtime.getRuntime().availableProcessors()此处不能用这个,因为可能同时又多个数据库采集同时进行
			executor = Executors.newFixedThreadPool(1);
			List<Future<JobStatusInfo>> list = new ArrayList<>();
			//2.校验对象的值是否正确
			//为了确保多个线程之间的值不互相干涉，复制对象的值。
			SourceDataConfBean sourceDataConfBean1 = JSONObject.parseObject(
					JSONObject.toJSONString(sourceDataConfBean), SourceDataConfBean.class);
			DataFileJobImpl fileCollectJob = new DataFileJobImpl(sourceDataConfBean1, collectTableBean);
			Future<JobStatusInfo> submit = executor.submit(fileCollectJob);
			list.add(submit);
			//3.打印每个线程执行情况
			JobStatusInfoUtil.printJobStatusInfo(list);
		} catch (RejectedExecutionException e) {
			throw new AppSystemException("数据库抽取表" + collectTableBean.getTable_name() + "失败", e);
		} finally {
			if (executor != null)
				executor.shutdown();
		}
	}

	private static void startJdbcToFile(SourceDataConfBean sourceDataConfBean, CollectTableBean collectTableBean) {
		ExecutorService executor = null;
		try {
			//初始化当前任务需要保存的文件的根目录
			String[] paths = {Constant.JOBINFOPATH, Constant.DICTIONARY};
			FileUtil.initPath(sourceDataConfBean.getDatabase_id(), paths);
			//此处不会有海量的任务需要执行，不会出现队列中等待的任务对象过多的OOM事件。
			//TODO Runtime.getRuntime().availableProcessors()此处不能用这个,因为可能同时又多个数据库采集同时进行
			executor = Executors.newFixedThreadPool(1);
			List<Future<JobStatusInfo>> list = new ArrayList<>();
			//2.校验对象的值是否正确
			//为了确保多个线程之间的值不互相干涉，复制对象的值。
			SourceDataConfBean sourceDataConfBean1 = JSONObject.parseObject(
					JSONObject.toJSONString(sourceDataConfBean), SourceDataConfBean.class);
			DataBaseJobImpl fileCollectJob = new DataBaseJobImpl(sourceDataConfBean1, collectTableBean);
			Future<JobStatusInfo> submit = executor.submit(fileCollectJob);
			list.add(submit);
			//3.打印每个线程执行情况
			JobStatusInfoUtil.printJobStatusInfo(list);
		} catch (RejectedExecutionException e) {
			throw new AppSystemException("数据库抽取表" + collectTableBean.getTable_name() + "失败", e);
		} finally {
			if (executor != null)
				executor.shutdown();
		}
	}

	private static CollectTableBean getCollectTableBean(List<CollectTableBean> collectTableBeanList, String tableName) {
		for (CollectTableBean collectTableBean : collectTableBeanList) {
			if (tableName.equals(collectTableBean.getTable_name())) {
				return collectTableBean;
			}
		}
		throw new AppSystemException("根据作业参数传递的表名在任务中查询不到对应的表");
	}
}
