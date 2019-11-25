package hrds.b.biz.collectmonitor;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import hrds.commons.entity.Collect_case;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@DocClass(desc = "处理采集任务表的信息", author = "Mr.Lee", createdate = "2019-11-01")
public class JobTableDetails {

  @Method(
      desc = "处理采集作业的信息",
      logicStep = "1: 将集合中,每个表的N中采集状态剥离出来．因为每个表的采集步骤有很多布，如：(卸数,上传,数据加载,计算增量,数据登记) ")
  @Param(name = "collectJobList", desc = "任务采集表信息的集合", range = "可以为空,如果为空表示当前做任务下没有采集表信息存在")
  @Return(desc = "返回处理后的数据信息", range = "可以为空,如果为空表示没有采集表信息存在")
  public static List<Map<String, String>> getTableDetails(List<Collect_case> collectJobList) {

    // 存放处理后的数据信息
    Map<String, Map<String, String>> detailsMap = new LinkedHashMap<String, Map<String, String>>();

    //      1: 将集合中,每个表的N中采集状态剥离出来．因为每个表的采集步骤有很多布，如：(卸数,上传,数据加载,计算增量,数据登记) ")
    collectJobList.forEach(
        collect_case -> {

          // 表采集步骤的开始时间
          String collect_s_date =
              DateUtil.parseStr2DateWith8Char(collect_case.getCollect_s_date()).toString()
                  + ' '
                  + DateUtil.parseStr2TimeWith6Char(collect_case.getCollect_s_time()).toString();

          // 表采集步骤的结束时间
          String collect_e_date =
              DateUtil.parseStr2DateWith8Char(collect_case.getCollect_e_date()).toString()
                  + ' '
                  + DateUtil.parseStr2TimeWith6Char(collect_case.getCollect_e_time()).toString();
          // 采集原表名称
          String table_name = collect_case.getTask_classify();
          /*
           * 作业步骤的类型
           *  1 : 卸数
           *  2 : 上传
           *  3 : 数据加载
           *  4 : 计算增量
           *  5 : 数据登记
           */
          String job_type = collect_case.getJob_type();
          // 如果前表已经存在了
          if (detailsMap.containsKey(table_name)) {
            Map<String, String> map = detailsMap.get(table_name);
            map.put(job_type + "_S_TITLE", collect_s_date);
            map.put(job_type + "_E_TITLE", collect_e_date);
            map.put(collect_case.getJob_type(), collect_case.getExecute_state());
          } else {
            Map<String, String> map = new HashMap<>();
            map.put("table_name", table_name);
            map.put(job_type + "_S_TITLE", collect_s_date);
            map.put(job_type + "_E_TITLE", collect_e_date);
            map.put(collect_case.getJob_type(), collect_case.getExecute_state());
            detailsMap.put(table_name, map);
          }
        });

    // 存放处理后的数据结果集信息
    List<Map<String, String>> collectTableResult = new ArrayList<Map<String, String>>();
    detailsMap.forEach(
        ($KeyFactory, v) -> {
          collectTableResult.add(v);
        });

    return collectTableResult;
  }
}
