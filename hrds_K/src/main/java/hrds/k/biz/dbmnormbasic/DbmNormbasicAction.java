package hrds.k.biz.dbmnormbasic;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.Page;
import fd.ng.web.util.Dbo;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Dbm_normbasic;
import hrds.commons.entity.Dbm_sort_info;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.DboExecute;
import hrds.commons.utils.key.PrimayKeyGener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@DocClass(desc = "数据对标元管理标准管理类", author = "BY-HLL", createdate = "2020/2/16 0016 下午 02:09")
public class DbmNormbasicAction extends BaseAction {

    @Method(desc = "添加标准分类",
            logicStep = "1.数据校验" +
                    "1-1.分类名不能为空" +
                    "1-2.分类名称重复" +
                    "1-3.父分类不为空的情况下,检查上级分类是否存在" +
                    "2.设置标准分类信息" +
                    "3.添加标准分类信息")
    @Param(name = "dbm_normbasic", desc = "标准分类的实体对象", range = "标准分类的实体对象", isBean = true)
    public void addDbmNormbasicInfo(Dbm_normbasic dbm_normbasic) {
        //1.数据校验
        if (checkNormCodeIsRepeat(dbm_normbasic.getNorm_code())) {
            throw new BusinessException("分类编码已经存在!" + dbm_normbasic.getNorm_code());
        }
        if (StringUtil.isNotBlank(dbm_normbasic.getSort_id().toString())) {
            if (!checkSortIdIsNotExist(dbm_normbasic.getSort_id())) {
                throw new BusinessException("选择分类不存在!" + dbm_normbasic.getSort_id());
            }
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_cname())) {
            throw new BusinessException("标准中文名称为空!" + dbm_normbasic.getNorm_cname());
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_ename())) {
            throw new BusinessException("标准英文名称为空!" + dbm_normbasic.getNorm_ename());
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_aname())) {
            throw new BusinessException("标准别名为空!" + dbm_normbasic.getNorm_aname());
        }
        if (StringUtil.isBlank(dbm_normbasic.getDbm_domain())) {
            throw new BusinessException("标准值域为空!" + dbm_normbasic.getDbm_domain());
        }
        if (StringUtil.isBlank(dbm_normbasic.getCol_len().toString())) {
            throw new BusinessException("标准字段长度为空!" + dbm_normbasic.getCol_len());
        }
        if (StringUtil.isBlank(dbm_normbasic.getDecimal_point().toString())) {
            throw new BusinessException("标准小数长度为空!" + dbm_normbasic.getDecimal_point());
        }
        if (StringUtil.isBlank(dbm_normbasic.getFormulator())) {
            throw new BusinessException("标准制定人为空!" + dbm_normbasic.getFormulator());
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_status())) {
            throw new BusinessException("标准发布状态为空!" + dbm_normbasic.getNorm_status());
        }
        //2.设置标准分类信息
        dbm_normbasic.setBasic_id(PrimayKeyGener.getNextId());
        dbm_normbasic.setNorm_status(IsFlag.Fou.getCode());
        dbm_normbasic.setCreate_user(getUserName());
        dbm_normbasic.setCreate_date(DateUtil.getSysDate());
        dbm_normbasic.setCreate_time(DateUtil.getSysTime());
        //3.添加标准分类信息
        dbm_normbasic.add(Dbo.db());
    }

    @Method(desc = "删除标准信息",
            logicStep = "1.检查标准id是否存在" +
                    "2.根据标准id删除分类")
    @Param(name = "basic_id", desc = "标准id", range = "Int类型")
    public void deleteDbmNormbasicInfo(long basic_id) {
        //1.检查分类id是否存在
        if (checkBasicIdIsNotExist(basic_id)) {
            throw new BusinessException("删除的标准已经不存在! basic_id" + basic_id);
        }
        //2.根据分类id删除分类
        DboExecute.deletesOrThrow("删除标准失败!" + basic_id, "DELETE FROM " +
                Dbm_normbasic.TableName + " WHERE basic_id = ? ", basic_id);
    }

    @Method(desc = "修改标准信息",
            logicStep = "1.数据校验" +
                    "2.设置标准信息" +
                    "3.修改数据")
    @Param(name = "dbm_normbasic", desc = "标准信息的实体对象", range = "标准信息的实体对象", isBean = true)
    public void updateDbmNormbasicInfo(Dbm_normbasic dbm_normbasic) {
        if (checkBasicIdIsNotExist(dbm_normbasic.getBasic_id())) {
            throw new BusinessException("修改的分类已经不存在! basic_id=" + dbm_normbasic.getBasic_id());
        }
        if (StringUtil.isBlank(dbm_normbasic.getSort_id().toString())) {
            throw new BusinessException("所属分类为空! sort_id=" + dbm_normbasic.getSort_id());
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_cname())) {
            throw new BusinessException("标准中文名字为空!" + dbm_normbasic.getNorm_cname());
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_ename())) {
            throw new BusinessException("标准英文名字为空!" + dbm_normbasic.getNorm_ename());
        }
        if (StringUtil.isBlank(dbm_normbasic.getNorm_aname())) {
            throw new BusinessException("标准别名为空!" + dbm_normbasic.getNorm_aname());
        }
        if (StringUtil.isBlank(dbm_normbasic.getDbm_domain())) {
            throw new BusinessException("标准值域为空!" + dbm_normbasic.getDbm_domain());
        }
        if (StringUtil.isBlank(dbm_normbasic.getCol_len().toString())) {
            throw new BusinessException("字段长度为空!" + dbm_normbasic.getCol_len());
        }
        if (StringUtil.isBlank(dbm_normbasic.getDecimal_point().toString())) {
            throw new BusinessException("小数长度为空!" + dbm_normbasic.getDecimal_point());
        }
        if (StringUtil.isBlank(dbm_normbasic.getFormulator())) {
            throw new BusinessException("制定人为空!" + dbm_normbasic.getFormulator());
        }
        dbm_normbasic.update(Dbo.db());
    }

    @Method(desc = "获取所有标准信息", logicStep = "获取所有标准信息")
    @Param(name = "currPage", desc = "分页当前页", range = "大于0的正整数", valueIfNull = "1")
    @Param(name = "pageSize", desc = "分页查询每页显示条数", range = "大于0的正整数", valueIfNull = "10")
    @Return(desc = "所有分类信息", range = "所有分类信息")
    public Map<String, Object> getDbmNormbasicInfo(int currPage, int pageSize) {
        Map<String, Object> dbmNormbasicInfoMap = new HashMap<>();
        Page page = new DefaultPageImpl(currPage, pageSize);
        List<Dbm_normbasic> dbmNormbasicInfos = Dbo.queryPagedList(Dbm_normbasic.class, page,
                "select * from " + Dbm_normbasic.TableName);
        dbmNormbasicInfoMap.put("dbmNormbasicInfos", dbmNormbasicInfos);
        dbmNormbasicInfoMap.put("totalSize", page.getTotalSize());
        return dbmNormbasicInfoMap;
    }

    @Method(desc = "获取所有标准信息(只获取basic_id,norm_cname)", logicStep = "获取所有标准信息")
    @Return(desc = "所有分类信息(只获取basic_id,norm_cname)", range = "所有分类信息")
    public Map<String, Object> getDbmNormbasicIdAndNameInfo() {
        Map<String, Object> dbmNormbasicInfoMap = new HashMap<>();
        List<Map<String, Object>> dbmNormbasicInfos =
                Dbo.queryList("select basic_id,norm_cname from " + Dbm_normbasic.TableName);
        dbmNormbasicInfoMap.put("dbmNormbasicInfos", dbmNormbasicInfos);
        dbmNormbasicInfoMap.put("totalSize", dbmNormbasicInfos.size());
        return dbmNormbasicInfoMap;
    }

    @Method(desc = "根据Id获取标准信息",
            logicStep = "根据Id获取标准信息")
    @Param(name = "basic_id", desc = "标准Id", range = "long类型")
    @Return(desc = "返回值说明", range = "返回值取值范围")
    public Optional<Dbm_normbasic> getDbmNormbasicInfoById(long basic_id) {
        return Dbo.queryOneObject(Dbm_normbasic.class, "select * from " + Dbm_normbasic.TableName +
                " where basic_id = ?", basic_id);
    }

    @Method(desc = "根据sort_id获取标准信息",
            logicStep = "根据sort_id获取标准信息")
    @Param(name = "sort_id", desc = "分类id", range = "long类型")
    @Return(desc = "返回值说明", range = "返回值取值范围")
    public Optional<Dbm_normbasic> getDbmNormbasicInfoBySortId(long sort_id) {
        return Dbo.queryOneObject(Dbm_normbasic.class, "select * from " + Dbm_normbasic.TableName +
                " where sort_id = ?", sort_id);
    }

    @Method(desc = "检查标准编号是否存在", logicStep = "检查标准编号是否存在")
    @Param(name = "norm_code", desc = "分类名称", range = "String类型，长度为10，该值唯一", example = "国籍")
    @Return(desc = "分类名称是否存在", range = "true：存在，false：不存在")
    private boolean checkNormCodeIsRepeat(String norm_code) {
        //1.根据 sort_name 检查名称是否重复
        return Dbo.queryNumber("select count(norm_code) count from " + Dbm_normbasic.TableName +
                        " WHERE norm_code =?",
                norm_code).orElseThrow(() -> new BusinessException("检查标准编号是否重复的SQL编写错误")) != 0;
    }

    @Method(desc = "检查分类id是否存在", logicStep = "检查分类id是否存在")
    @Param(name = "sort_id", desc = "分类id", range = "long类型")
    @Return(desc = "分类否存在", range = "true：不存在，false：存在")
    private boolean checkSortIdIsNotExist(long sort_id) {
        //1.根据 sort_id 检查分类是否存在(1 : 表示存在, 其他为异常情况,因为根据主键只能查出一条记录信息)
        return Dbo.queryNumber("SELECT COUNT(sort_id) FROM " + Dbm_sort_info.TableName +
                " WHERE sort_id = ?", sort_id).orElseThrow(() ->
                new BusinessException("检查分类id否存在的SQL编写错误")) != 1;
    }

    @Method(desc = "检查标准id是否存在", logicStep = "检查标准id是否存在")
    @Param(name = "basic_id", desc = "标准id", range = "long类型")
    @Return(desc = "标准是否存在", range = "true：不存在，false：存在")
    private boolean checkBasicIdIsNotExist(long basic_id) {
        //1.根据 sort_id 检查分类是否存在(1 : 表示存在, 其他为异常情况,因为根据主键只能查出一条记录信息)
        return Dbo.queryNumber("SELECT COUNT(basic_id) FROM " + Dbm_normbasic.TableName +
                " WHERE basic_id = ?", basic_id).orElseThrow(() ->
                new BusinessException("检查分类id否存在的SQL编写错误")) != 1;
    }
}
