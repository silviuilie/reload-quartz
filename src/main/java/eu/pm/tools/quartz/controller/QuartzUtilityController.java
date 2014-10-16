package eu.pm.tools.quartz.controller;

import eu.pm.tools.quartz.QuartzUtility;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by silviu
 * Date: 13/02/14 / 22:14279
 * <p>
 * controller {@code Controller} for {@link eu.pm.tools.quartz.QuartzUtility}.
 * </p>
 * <br/>
 *
 * @author Silviu Ilie
 */
@Controller
public class QuartzUtilityController extends QuartzUtility {

    /**
     * quartz priority reload 'home'.
     *
     * @return view name.
     */
    @RequestMapping(value = QUARTZ_UTILITY_HOME)
    public final String quartzChange(HttpSession session) {
        if (isAuthorized(session)) {
            return "quartzReload";
        }
        throw new IllegalStateException();
    }

    /**
     * lists scheduler jobs.
     * @param session {@code HttpSession} used for authorization.
     * @return JSON containing list of {@link eu.pm.tools.quartz.ui.JobDescription}s and general scheduler
     * data.
     */
    @RequestMapping(value = QUARTZ_UTILITY_LIST)
    @ResponseBody
    public final String listJobs(HttpSession session) {
        String result = "{}";

        if (isAuthorized(session)) result = schedulerDetails(session);

        return result;
    }

    /**
     * sets new cron expression on trigger.
     *
     * @param target        job name
     * @param trigger       trigger name
     * @param newExpression old cron expression
     * @param oldExpression new cron expression
     *
     * @return new cron expression or failure message if change is not executed.
     */
    @RequestMapping(value = QUARTZ_UTILITY_CHANGE_TRIGGER, method = RequestMethod.POST)
    @ResponseBody
    public final String changeTrigger(@RequestParam(value = "target") final String target,
                                      @RequestParam(value = "trigger") final String trigger,
                                      @RequestParam(value = "newExpression") final String newExpression,
                                      @RequestParam(value = "oldExpression") final String oldExpression,
                                      HttpSession session) {

        return setNewTrigger(target, trigger, oldExpression, newExpression, session);
    }

    /**
     * revert to the original trigger cron expression.
     *
     * @param trigger job name
     * @param session session
     * @return
     */
    @RequestMapping(value = QUARTZ_UTILITY_REVERT_TRIGGER_CHANGES, method = RequestMethod.POST)
    @ResponseBody
    public final String revertPriority(@RequestParam(value = "trigger") final String trigger, HttpSession session) {
        return resetCronExpression(trigger, session);
    }

    /**
     * provides all changes as json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_LIST_CHANGES, method = RequestMethod.GET)
    @ResponseBody
    public final String listChangeLog(final HttpSession session) {
        return listChanges(session);
    }

    /**
     * interrupts Job identified by {@code target}.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_INTERRUPT_JOB, method = RequestMethod.POST)
    @ResponseBody
    public final String interruptJob(String target, HttpSession session) {
        return super.interruptJob(target, session);
    }

    /**
     * pauses job identified by {@code target}.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_PAUSE_JOB, method = RequestMethod.POST)
    @ResponseBody
    public final String pauseJob(String target, HttpSession session) {
        return super.pauseJob(target, session);
    }

    /**
     * resumes job identified by {@code target}.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_RESUME_JOB, method = RequestMethod.POST)
    @ResponseBody
    public final String resumeJob(String target, HttpSession session) {
        return super.resumeJob(target, session);
    }

    /**
     * pauses trigger identified by {@code triggerName} from group identified by {@code triggerGroup}.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_PAUSE_TRIGGER, method = RequestMethod.POST)
    @ResponseBody
    public final String pauseTrigger(String triggerName, String triggerGroup, HttpSession session) {
        return super.pauseTrigger(triggerName, triggerGroup, session);
    }

    /**
     * resumes trigger identified by {@code triggerName} from group identified by {@code triggerGroup}.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_RESUME_TRIGGER, method = RequestMethod.POST)
    @ResponseBody
    public final String resumeTrigger(String triggerName, String triggerGroup, HttpSession session) {
        return super.resumeTrigger(triggerName, triggerGroup, session);
    }

    /**
     * resumes all scheduled jobs.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_RESUME_ALL, method = RequestMethod.POST)
    @ResponseBody
    public final String resumeAll(HttpSession session) {
        return super.resumeAll(session);
    }

    /**
     * pauses all scheduled jobs.
     *
     * @return json array of {@link eu.pm.tools.quartz.QuartzConfigResetResponse}
     */
    @RequestMapping(value = QUARTZ_UTILITY_PAUSE_ALL, method = RequestMethod.POST)
    @ResponseBody
    public final String pauseAll(HttpSession session) {
        return super.pauseAll(session);
    }
}
