package eu.pm.tools.quartz.dummy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * Created by silviu
 * Date: 02/03/14 / 14:16142
 * <p/>
 * <p>
 * TODO :comment me!
 * </p>
 * <p/>
 * <br/>
 * log4j-level-reloader|eu.pm.tools.quartz.dummy
 *
 * @author Silviu Ilie
 */
public class NOPJobDetail implements Job {

    @Override
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


    public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = new org.quartz.impl.StdSchedulerFactory().getScheduler();

        final String groupName = "dummyGroup";
        final String jobName = "NOPJob";
        final String triggerName = "sampleTrigger";

        scheduler.scheduleJob(
                newJob(NOPJobDetail.class)
                        .withIdentity(jobName, groupName)
                        .build(),
                newTrigger()
                        .withIdentity(triggerName, groupName)
                        .startNow()
                        .withSchedule(cronSchedule("0 0/2 8-17 * * ?"))
                        .build()
        );

        scheduler.start();
/*

        for (String group : scheduler.getJobGroupNames()) {
            System.out.println("group = " + group);
        }

*/

        for(String group: scheduler.getJobGroupNames()) {
            // enumerate each job in group
            for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                System.out.println("Found job identified by: " + jobKey);
                System.out.println("=>" + scheduler.getJobDetail(jobKey));
                JobDetail jd = scheduler.getJobDetail(jobKey);
                System.out.println("=>" + jd.getJobDataMap());
                for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                    if (trigger instanceof CronTrigger) {
                        System.out.println("=>" + ((CronTrigger)trigger).getCronExpression());
                    }
                }
                System.out.println("===================================");
            }
        }

    }

    /**
     * this class logger.
     */
    private static final Log log = LogFactory.getLog(NOPJobDetail.class);
}

