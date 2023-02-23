package eu.pm.tools.quartz;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.pm.tools.quartz.ui.JobDescription;
import eu.pm.tools.quartz.ui.SimpleCronExpression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by silviu
 * Date: 13/02/14 / 22:14279
 * <p>
 * handles all quartz job change requests.
 * </p>
 * <br/>
 *
 * @author Silviu Ilie
 */
public class QuartzUtility {


    @Autowired
    private QuartzApplicationContext applicationContext;

    @Autowired(required = false)
    private Scheduler quartzScheduler;

    /**
     * initial trigger settings.
     */
    private Map<String, OriginalCronSetting> triggerOriginalSetting = new HashMap<String, OriginalCronSetting>();

    /**
     * there is no way to find paused jobs/triggers from quartz job store.
     */
    private Set<String> pausedJobs = new HashSet<String>();
    private Set<String> pausedTriggers = new HashSet<String>();
    private boolean allPaused = false;


    /**
     * URI for the 'home' of the utility
     */
    public final static String QUARTZ_UTILITY_HOME = "quartz.quartz";

    /**
     * URI for setting the priority
     */
    public final static String QUARTZ_UTILITY_CHANGE_TRIGGER = "quartzChange.quartz";

    /**
     * URI for reverting the trigger to the default value.
     */
    public final static String QUARTZ_UTILITY_REVERT_TRIGGER_CHANGES = "revertChange.quartz";

    /**
     * URI for loading the changes list.
     */
    public final static String QUARTZ_UTILITY_LIST_CHANGES = "listChanges.quartz";

    /**
     * URI for interrupting the job.
     */
    public final static String QUARTZ_UTILITY_INTERRUPT_JOB = "interruptJob.quartz";

    /**
     * URI for pausing the job.
     */
    public final static String QUARTZ_UTILITY_PAUSE_JOB = "pauseJob.quartz";

    /**
     * URI for resuming a job.
     */
    public final static String QUARTZ_UTILITY_RESUME_JOB = "resumeJob.quartz";

    /**
     * URI for pausing a trigger.
     */
    public final static String QUARTZ_UTILITY_PAUSE_TRIGGER = "pauseTrigger.quartz";

    /**
     * URI for resuming a trigger.
     */
    public final static String QUARTZ_UTILITY_RESUME_TRIGGER = "resumeTrigger.quartz";

    /**
     * pauses all scheduled jobs.
     */
    public final static String QUARTZ_UTILITY_RESUME_ALL = "resumeAll.quartz";

    /**
     * resumes all scheduled jobs.
     */
    public final static String QUARTZ_UTILITY_PAUSE_ALL = "pauseAll.quartz";

    /**
     * URI for loading the jobs list.
     */
    public final static String QUARTZ_UTILITY_LIST = "listJobs.quartz";


    /**
     * all jobs list.
     */
    private HashMap jobs = new HashMap<String, JobDescription>();

    /**
     * default constructor.
     */
    protected QuartzUtility() {
        mapper.setVisibility(mapper.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    private static class UtilityContainer {
        private final static QuartzUtility unique = new QuartzUtility();
    }

    public static QuartzUtility getInstance() {
        return UtilityContainer.unique;
    }


    /**
     * context setter.
     *
     * @param context app. context
     */
    public void setApplicationContext(QuartzApplicationContext context) {
        this.applicationContext = context;
    }

    /**
     * @param quartzScheduler
     */
    public void setQuartzScheduler(Scheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler;
    }

    /**
     * if authorized, lists all changes performed.
     *
     * @param session {@link HttpSession}.
     * @return {@code ArrayList<QuartzConfigResetResponse>}
     */
    public String listChanges(HttpSession session) {
        if (isAuthorized(session)) {
            return toJSON(QuartzConfigResetResponse.list());
        }

        throw new AuthorizationException();
    }

    /**
     * if authorized, interrupts job identified by {@code jobName}.
     *
     * @param jobName job name.
     * @param session session to authorize.
     * @return
     */
    public synchronized String interruptJob(String jobName, HttpSession session) {

        if (isAuthorized(session)) {
            JobDetail jobDetail = quartzFindJob(jobName);
            boolean interrupted = false;
            try {
                interrupted = scheduler().interrupt(jobDetail.getKey());
            } catch (UnableToInterruptJobException e) {
                log.error("unable to interrupt " + jobName + ". failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }
            return toJSON(
                    new QuartzConfigResetResponse(jobName + " job interrupted ( result : " + interrupted + ").",
                            QuartzConfigResetResponse.Type.SUCCESS)
            );
        }

        throw new AuthorizationException();
    }

    /**
     * if authorized, resumes job identified by {@code jobName}.
     *
     * @param jobName job name.
     * @param session session to authorize.
     * @return
     */
    public synchronized String resumeJob(String jobName, HttpSession session) {
        if (isAuthorized(session)) {
            JobDetail jobDetail = quartzFindJob(jobName);
            try {
                scheduler().resumeJob(jobDetail.getKey());
                pausedJobs.remove(jobDetail.getKey().getName());
            } catch (SchedulerException e) {
                log.error("unable to resume " + jobName + ". failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }
            return toJSON(
                    new QuartzConfigResetResponse(jobName + " job resumed.", QuartzConfigResetResponse.Type.SUCCESS)
            );

        }

        throw new AuthorizationException();
    }

    /**
     * if authorized, pauses job identified by {@code jobName}.
     *
     * @param jobName job name.
     * @param session session to authorize.
     * @return
     */
    public synchronized String pauseJob(String jobName, HttpSession session) {
        if (isAuthorized(session)) {
            JobDetail jobDetail = quartzFindJob(jobName);
            try {
                scheduler().pauseJob(jobDetail.getKey());
                pausedJobs.add(jobDetail.getKey().getName());
            } catch (SchedulerException e) {
                log.error("unable to pause " + jobName + ". failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }
            return toJSON(
                    new QuartzConfigResetResponse(jobName + " job paused.", QuartzConfigResetResponse.Type.SUCCESS)
            );
        }

        throw new AuthorizationException();
    }

    /**
     * if authorized, pauses trigger identified by {@code triggerName} and {@code triggerGroup}.
     *
     * @param triggerName  trigger name.
     * @param triggerGroup trigger group.
     * @param session      session to authorize.
     * @return
     */
    public synchronized String pauseTrigger(String triggerName, String triggerGroup, HttpSession session) {
        if (isAuthorized(session)) {
            try {
                scheduler().pauseTrigger(new TriggerKey(triggerName, triggerGroup));
                pausedTriggers.add(triggerName);
            } catch (SchedulerException e) {
                log.error("unable to pause " + triggerName + "(" + triggerGroup
                        + "). failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }
            return toJSON(
                    new QuartzConfigResetResponse(
                            triggerName + "(" + triggerGroup + ") trigger(group) paused.",
                            QuartzConfigResetResponse.Type.SUCCESS
                    )
            );
        }

        throw new AuthorizationException();
    }

    /**
     * if authorized, resumes trigger identified by {@code triggerName} and {@code triggerGroup}.
     *
     * @param triggerName  trigger name.
     * @param triggerGroup trigger group.
     * @param session      session to authorize.
     * @return
     */
    public synchronized String resumeTrigger(String triggerName, String triggerGroup, HttpSession session) {
        if (isAuthorized(session)) {
            try {
                scheduler().resumeTrigger(new TriggerKey(triggerName, triggerGroup));
                pausedTriggers.remove(triggerName);
            } catch (SchedulerException e) {
                log.error("unable to resume " + triggerName + "(" + triggerGroup
                        + "). failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }
            return toJSON(
                    new QuartzConfigResetResponse(
                            triggerName + "(" + triggerGroup + ") trigger(group) resumed.",
                            QuartzConfigResetResponse.Type.SUCCESS
                    )
            );
        }

        throw new AuthorizationException();
    }

    /**
     * if authorized, resumes all jobs.
     *
     * @param session session to authorize.
     * @return
     */
    public synchronized String resumeAll(HttpSession session) {
        if (isAuthorized(session)) {
            try {
                scheduler().resumeAll();

                pausedJobs.clear();
                pausedTriggers.clear();
                allPaused = false;

            } catch (SchedulerException e) {
                log.error("unable to resume all jobs. failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }
            return toJSON(
                    new QuartzConfigResetResponse("all jobs resumed", QuartzConfigResetResponse.Type.SUCCESS)
            );
        }

        throw new AuthorizationException();
    }


    /**
     * if authorized, pause all scheduled jobs.
     *
     * @param session session to authorize.
     * @return
     */
    public synchronized String pauseAll(HttpSession session) {
        if (isAuthorized(session)) {
            try {
                scheduler().pauseAll();
                allPaused = true;
            } catch (SchedulerException e) {
                log.error("unable to pause all jobs. failed with :" + e.getMessage(), e);
                toJSON(new QuartzConfigResetResponse(
                        e.getMessage(),
                        QuartzConfigResetResponse.Type.ERROR
                ));
            }

            return toJSON(
                    new QuartzConfigResetResponse("all jobs paused", QuartzConfigResetResponse.Type.SUCCESS)
            );
        }

        throw new AuthorizationException();
    }


    /**
     * if authorized, sets new trigger with new cron expression {@code newExpression} for the specified job.
     *
     * @param jobName       job name
     * @param triggerKey    trigger name
     * @param oldExpression old cron expression.
     * @param newExpression new cron expression.
     * @return QuartzConfigResetResponse
     */
    public synchronized String setNewTrigger(final String jobName, final String triggerKey,
                                             final String oldExpression, final String newExpression,
                                             HttpSession session) {

        if (isAuthorized(session)) {
            if (jobs.containsKey(jobName.toLowerCase())) {
                return toJSON(
                        changeJobCronExpression(jobName, triggerKey, newExpression, oldExpression)
                );
            } else {
                return toJSON(
                        new QuartzConfigResetResponse(
                                "job " + jobName + " not found",
                                QuartzConfigResetResponse.Type.ERROR)
                );
            }
        }

        throw new AuthorizationException();
    }


    /**
     * authorization check.
     *
     * @param session http session.
     * @return true if authorized
     */
    public boolean isAuthorized(HttpSession session) {
        return applicationContext.getUtilityAuthorization().authorize(session);
    }

    /**
     * if authorized, resets priority for class to original value.
     *
     * @param triggerKey trigger to reset.
     * @return {@link QuartzConfigResetResponse}
     */
    public synchronized final String resetCronExpression(final String triggerKey, HttpSession session) {

        if (isAuthorized(session)) {
            OriginalCronSetting originalCronSetting = triggerOriginalSetting.get(triggerKey);
            if (originalCronSetting == null) {
                return toJSON(
                        new QuartzConfigResetResponse("trigger not changed", QuartzConfigResetResponse.Type.ERROR)
                );
            } else {
                return toJSON(
                        changeJobCronExpression(
                                originalCronSetting.getJobName(),
                                originalCronSetting.getTriggerKey(),
                                originalCronSetting.getNewExpression(),
                                originalCronSetting.getOldExpression()
                        )
                );
            }
        }

        throw new AuthorizationException();
    }

    /**
     * Reflections mapper
     */
    final ObjectMapper mapper = new ObjectMapper();


    /**
     * changes quartz logging priority/level for class name with the name {@code target}.
     *
     * @param jobName       job name.
     * @param oldExpression new chron expression.
     * @param newExpression new chron expression.
     * @return {@link QuartzConfigResetResponse}.
     */
    private synchronized QuartzConfigResetResponse changeJobCronExpression(String jobName, String triggerKey, String newExpression, String oldExpression) {

        log.debug(format("job %s with cron %s will change to %s", jobName, newExpression, oldExpression));

        if (!triggerOriginalSetting.containsKey(triggerKey)) {
            triggerOriginalSetting.put(triggerKey, new OriginalCronSetting(jobName, triggerKey, newExpression, oldExpression));
        }

        JobDetail jobDetail = quartzFindJob(jobName);

        Trigger newTrigger = newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(newExpression))
                .build();
        try {
            scheduler().rescheduleJob(
                    new TriggerKey(
                            triggerKey,
                            ((JobDetailImpl) jobDetail).getGroup()
                    ),
                    newTrigger
            );
        } catch (SchedulerException e) {
            log.error("failed to reschedule job : " + e.getMessage(), e);
            return new QuartzConfigResetResponse(e.getMessage(), QuartzConfigResetResponse.Type.ERROR);
        }

        return new QuartzConfigResetResponse(triggerKey + " rescheduled to " + newExpression, QuartzConfigResetResponse.Type.SUCCESS);
    }


    /**
     * finds job by {@code name}.
     *
     * @param name name of the job.
     * @return {@link JobDetail}.
     */
    public JobDetail quartzFindJob(String name) {
        try {
            for (String jobGroup : scheduler().getJobGroupNames()) {
                for (JobKey jobKey : scheduler().getJobKeys(GroupMatcher.jobGroupEquals(jobGroup))) {
                    if (jobKey.getName().endsWith(name)) {
                        return scheduler().getJobDetail(jobKey);
                    }
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    /**
     * provides all scheduled jobs/triggers and scheduler data.
     *
     * @param session http session.
     * @return all scheduled jobs/triggers and scheduler data represented as JSON.
     */
    public String schedulerDetails(HttpSession session) {

        if (isAuthorized(session)) {

            try {
                return toJSON(new HashMap<String, Object>() {{
                    put("jobs", listJobs());
                    put("details", new HashMap<String, Object>() {{
                        put("version", scheduler().getMetaData().getVersion());
                        put("jobsExecuted", scheduler().getMetaData().getNumberOfJobsExecuted());
                        put("runningSince", scheduler().getMetaData().getRunningSince().toString());
                        put("summary", scheduler().getMetaData().getSummary());
                        put("pausedTriggers", scheduler().getPausedTriggerGroups());
                        put("pausedJobs", pausedJobs);
                    }});
                }});
            } catch (Throwable e) {
                log.error("failed to get quartz details : " + e.getMessage(),e);
                return toJSON(
                        new QuartzConfigResetResponse("render detaisl failed " + e,
                                QuartzConfigResetResponse.Type.ERROR)
                );
            }

        }

        throw new AuthorizationException();
    }

    /**
     * lists scheduled jobs.s
     *
     * @return
     */
    protected List<JobDescription> listJobs() {

        final List<JobDescription> descriptions = new ArrayList<JobDescription>();

        try {
            for (String group : quartzScheduler.getJobGroupNames()) {

                // enumerate each job in group
                for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {

                    JobDescription jobDescription = new JobDescription();
                    JobDetail jd = quartzScheduler.getJobDetail(jobKey);

                    final String jname = ((JobDetailImpl) jd).getName();
                    jobDescription.setName(jname);
                    jobDescription.setDescription(((JobDetailImpl) jd).getFullName() + nvl(jd.getDescription()));
                    jobDescription.setGroupName(((JobDetailImpl) jd).getGroup());
                    jobDescription.setClassName(jd.getJobClass().getName());
                    jobDescription.setPaused(allPaused || pausedJobs.contains(jd.getKey().getName()));

                    for (Trigger trigger : quartzScheduler.getTriggersOfJob(jobKey)) {
                        if (trigger instanceof CronTrigger) {
                            jobDescription.addCronExpression(
                                    new SimpleCronExpression(
                                            trigger.getKey().getName(),
                                            ((CronTrigger) trigger).getCronExpression(),
                                            allPaused
                                                    || pausedJobs.contains(jd.getKey().getName())
                                                    || pausedTriggers.contains(trigger.getKey().getName())
                                    ));
                        }
                    }

                    jobs.put(jname.toLowerCase(), descriptions);
                    descriptions.add(jobDescription);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return descriptions;
    }

    /**
     * handles null value.
     *
     * @param value
     * @return handled value.
     */
    private String nvl(String value) {
        return defaultIfEmpty(value, "");
    }

    /**
     * wraps mapper.writeValueAsString to handle checked exception IOException.
     *
     * @param object object to JSONize.
     * @return JSON representation for {@code object}.
     */
    private String toJSON(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets the scheduler.
     *
     * @return {@code Scheduler}.
     */

    private Scheduler scheduler() {
        if (quartzScheduler == null) {
            try {
                quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
                return quartzScheduler;
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        } else {
            return quartzScheduler;
        }
    }

    /**
     * this class logger.
     */
    private static final Log log = LogFactory.getLog(QuartzUtility.class);

    /**
     * describes a trigger cron change.
     */
    private class OriginalCronSetting {
        private String jobName;
        private String triggerKey;
        private String newExpression;
        private String oldExpression;

        private OriginalCronSetting(String jobName, String triggerKey, String newExpression, String oldExpression) {
            this.jobName = jobName;
            this.triggerKey = triggerKey;
            this.newExpression = newExpression;
            this.oldExpression = oldExpression;
        }

        private String getJobName() {
            return jobName;
        }

        private String getTriggerKey() {
            return triggerKey;
        }

        private String getNewExpression() {
            return newExpression;
        }

        private String getOldExpression() {
            return oldExpression;
        }
    }
}
