package tester.dummyJobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;


/**
 * Created by silviu
 * Date: 02/03/14 / 14:16142
 * <p/>
 * <p>
 * TODO :comment me!
 * </p>
 * <p/>
 * <br/>
 *
 * @author Silviu Ilie
 */
public class NOPJobDetail implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug(this.getClass().getName() + " executed.");

        if (jobExecutionContext != null) {
            try {
                for (String key : jobExecutionContext.getScheduler().getContext().keySet()) {
                    System.out.println("key = " + key);
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } else {
            log.debug(jobExecutionContext + " is null");
        }
    }



    /**
     * this class logger.
     */
    private static final Log log = LogFactory.getLog(NOPJobDetail.class);
}

