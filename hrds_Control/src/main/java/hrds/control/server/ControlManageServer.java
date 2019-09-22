package hrds.control.server;

import java.time.LocalDate;

import hrds.control.task.TaskManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * ClassName: ControlManageServer <br/>
 * Function: 用于控制及管理任务，该类决定具体任务以及任务的执行及执行时间间隔。<br/>
 * Date: 2019/7/30 16:58 <br/>
 *
 * Author Tiger.Wang
 * Version 1.0
 * Since JDK 1.8
 **/
public class ControlManageServer {

	private static final Logger logger = LogManager.getLogger();
	private static final CMServerThread cmThread = new CMServerThread();
	private static TaskManager taskManager;

	/**
	 * ControlManageServer类构造器
	 * @author Tiger.Wang
	 * @date 2019/8/30
	 * @param bathDate	跑批批次日期
	 * @param strSystemCode	调度系统代码
	 * @param isResumeRun	是否续跑
	 * @param isAutoShift	是否自动日切
	 */
	public ControlManageServer(String strSystemCode, LocalDate bathDate, boolean isResumeRun, boolean isAutoShift) {

		taskManager = TaskManager.newInstance(strSystemCode, bathDate, isResumeRun, isAutoShift);
		//FIXME 上面方法里面干了很多事情，都拿出来，在这里按顺序这个调用
		taskManager.initEtlSystem();
	}

	/**
	 * 线程方式启动服务
	 * @author Tiger.Wang
	 * @date 2019/8/30
	 */
	public void runCMServer() {

		cmThread.start();
		logger.info("调度服务启动成功");
	}

	/**
	 * 停止服务，最终会停止线程
	 * @author Tiger.Wang
	 * @date 2019/8/30
	 */
	public void stopCMServer() {

		cmThread.stopThread();
		logger.info("调度服务停止成功");
	}

	private static class CMServerThread extends Thread {

		private volatile boolean run = true;

		void stopThread() {

			this.run = false;
		}

		@Override
		public void run() {
			try {
				//用于将作业定义表中的作业，通过一定的判断及检查，登记到内存表中
				//FIXME 这个应该放到下面的循环作为第一句。if里面的不需要了
				boolean hasFrequancy = taskManager.loadReadyJob();

				while(run) {
					//若publishReadyJob方法进行自动日切，则再次加载初始作业
					//FIXME 为什么要传入这个变量
					// loadReadyJob里面构造了3个MAP，
					// 同理，hasFrequancy也应该地位等同于MAP而作为成员变，
					// 让publishReadyJob自己取。
					if(taskManager.publishReadyJob(hasFrequancy)){
						hasFrequancy = taskManager.loadReadyJob();
					}else {
						logger.info("系统无日切信号，系统退出");
						break;
						//FIXME 作业能配置成：每天某个时刻执行，但是不日切吗？或者等待信号文件到达就执行，执行完继续等待，且没有日切的概念
					}
					//FIXME 这个循环不需要sleep吗？
				}

			}catch(Exception ex) {
				logger.error("Exception happened!" + ex);
				logger.error(ex.getStackTrace());//FIXME 不是这么打堆栈。一句话的事情，搞这么多代码是干嘛
				ex.printStackTrace();
				StackTraceElement[] stackElements = ex.getStackTrace();
				if( stackElements != null ) {
					for (StackTraceElement stackElement : stackElements) {
						logger.error(stackElement.getClassName() + stackElement.getFileName() + stackElement.getLineNumber()
								+ stackElement.getMethodName());
					}
				}
			}
		}
	}
}
