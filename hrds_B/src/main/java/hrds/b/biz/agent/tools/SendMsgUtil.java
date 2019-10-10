package hrds.b.biz.agent.tools;

import fd.ng.core.utils.JsonUtil;
import fd.ng.core.utils.StringUtil;
import fd.ng.netclient.http.HttpClient;
import fd.ng.web.action.ActionResult;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.AgentActionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @Description: 海云应用管理端向Agent端发送消息的工具类
 * @Author: wangz
 * @CreateTime: 2019-09-26-11:36
 * @BelongsProject: hrsv5
 * @BelongsPackage: hrds.b.biz.agent.tools
 **/
public class SendMsgUtil {

	private static final Log logger = LogFactory.getLog(SendMsgUtil.class);

	/**
	 * 海云应用管理端根据agentIp和agentPort向Agent端发送jsonData
	 *
	 * 1、对参数合法性进行校验
	 * 2、对请求的参数调用工具类做压缩加密操作，得到处理后的数据并封装
	 * 3、httpClient发送请求并接收响应
	 * 4、根据响应状态码判断响应是否成功
	 * 5、若响应成功，调用方法解析响应报文，并返回响应数据
	 * 6、若响应不成功，记录日志，并返回操作失败
	 *
	 * @Param: agentId Long
	 *         含义：agentId，agent_info表主键，agent_down_info表外键
	 *         取值范围：不为空
	 * @Param: userId Long
	 *         含义：当前登录用户Id，sys_user表主键，agent_down_info表外键
	 *         取值范围：不为空
	 * @Param: jsonData String
	 *         含义：海云应用管理端到Agent端的请求参数
	 *         取值范围 : json格式的字符串
	 * @Param: methodName String
	 *         含义：Agent端的提供服务的方法的方法名
	 *         取值范围 : AgentActionUtil类中的静态常量
	 *
	 * @return: String
	 *          含义 : Agent端通过本地http交互得到的响应数据的msg部分，内容是按照表名模糊查询或者查询所有表得到的表信息
	 *          取值范围 : json格式的字符串
	 * */
	public static String sendMsg(Long agentId, Long userId, String jsonData, String methodName){
		//1、对参数合法性进行校验
		if(agentId == null){
			throw new BusinessException("向Agent发送信息时agentId不能为空");
		}
		if(userId == null){
			throw new BusinessException("向Agent发送信息时userId不能为空");
		}
		if(StringUtil.isBlank(methodName)){
			throw new BusinessException("向Agent发送信息时methodName不能为空");
		}

		String url = AgentActionUtil.getUrl(agentId, userId, methodName);
		logger.debug("准备建立连接，请求的URL为" + url);

		//FIXME 为什么要用这个HTTPCLIENT包？已修复

		//2、对请求的参数调用工具类做压缩加密操作，得到处理后的数据并封装
		String sendStr = PackUtil.packMsg(jsonData);
		//3、httpClient发送请求并接收响应
		HttpClient.ResponseValue resVal = new HttpClient().addData("msgjson", sendStr).post(url);
		ActionResult ar = JsonUtil.toObjectSafety(resVal.getBodyString(), ActionResult.class)
				.orElseThrow(() -> new BusinessException("调用" + url + "服务异常"));
		//4、根据响应状态码判断响应是否成功
		if (ar.isSuccess()) {
			//5、若响应成功，调用方法解析响应报文，并返回响应数据
			String msg = PackUtil.unpackMsg((String) ar.getData()).get("msg");
			logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>返回消息为：" + msg);
			return msg;
		}
		//6、若响应不成功，记录日志，并返回操作失败
		logger.error(">>>>>>>>>>>>>>>>>>>>>>>>错误信息为：" + ar.getMessage());
		//TODO 后面可以进行国际化操作
		return  "操作失败，错误信息请查看日志";
	}
}