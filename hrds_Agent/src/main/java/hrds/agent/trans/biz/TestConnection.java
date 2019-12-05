package hrds.agent.trans.biz;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.agent.job.biz.bean.DBConfigBean;
import hrds.commons.base.AgentBaseAction;
import hrds.commons.entity.Database_set;
import hrds.commons.exception.AppSystemException;

import java.sql.ResultSet;
import java.sql.SQLException;

@DocClass(desc = "测试连接相关接口", author = "xchao", createdate = "2019-09-05 11:18")
public class TestConnection extends AgentBaseAction {

	@Method(desc = "测试连接的方法",
			logicStep = "1、使用dbinfo将需要测试连接的内容填充" +
					"2、测试连接")
	@Param(name = "dbSet", desc = "数据库连接设置表对象，此对象不能为空的字段必须有值",
			range = "不能为空", isBean = true)
	@Return(desc = "是否连接成功的判断", range = "不会为空")
	public boolean testConn(Database_set dbSet) {
		//1、使用dbinfo将需要测试连接的内容填充
		DBConfigBean dbInfo = new DBConfigBean();
		dbInfo.setDatabase_drive(dbSet.getDatabase_drive());
		dbInfo.setJdbc_url(dbSet.getJdbc_url());
		dbInfo.setUser_name(dbSet.getUser_name());
		dbInfo.setDatabase_pad(dbSet.getDatabase_pad());
		dbInfo.setDatabase_type(dbSet.getDatabase_type());
		//2、测试连接
		try (DatabaseWrapper db = ConnetionTool.getDBWrapper(dbInfo)) {
			return db.isConnected();
		}
	}

	@Method(desc = "测试并行抽取SQL", logicStep = "" +
			"1、使用dbinfo将需要测试并行抽取的数据库连接内容填充" +
			"2、创建DatabaseWrapper，并执行SQL语句" +
			"3、如果根据SQL获取到了数据，返回true，否则返回false")
	@Param(name = "dbSet", desc = "数据库连接设置表对象，此对象不能为空的字段必须有值", range = "不能为空", isBean = true)
	@Param(name = "pageSql", desc = "并行抽取使用的分页SQL", range = "不为空")
	@Return(desc = "根据并行抽取是否成功获取数据的判断", range = "不会为空")
	public boolean testParallelSQL(Database_set dbSet, String pageSql){
		//1、使用dbinfo将需要测试并行抽取的数据库连接内容填充
		DBConfigBean dbInfo = new DBConfigBean();
		dbInfo.setDatabase_drive(dbSet.getDatabase_drive());
		dbInfo.setJdbc_url(dbSet.getJdbc_url());
		dbInfo.setUser_name(dbSet.getUser_name());
		dbInfo.setDatabase_pad(dbSet.getDatabase_pad());
		dbInfo.setDatabase_type(dbSet.getDatabase_type());
		//2、创建DatabaseWrapper，并执行SQL语句
		try (DatabaseWrapper db = ConnetionTool.getDBWrapper(dbInfo)) {
			//3、如果根据SQL获取到了数据，返回true,否则返回false
			String countSQL = "select count(1) as count from ( " + pageSql + " ) tmp";
			ResultSet resultSet = db.queryGetResultSet(countSQL);
			int rowCount = 0;
			while (resultSet.next()) {
				rowCount = resultSet.getInt("count");
			}
			return rowCount != 0;
		}catch (SQLException e) {
			throw new AppSystemException(e);
		}
	}
}
