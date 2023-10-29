package eu.pm.tools.quartz.controller;

import eu.pm.tools.quartz.QuartzApplicationContext;
import eu.pm.tools.quartz.QuartzUtilityAuthorization;
import eu.pm.tools.quartz.controller.QuartzUtilityController;
import org.junit.Before;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;

import static junit.framework.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * <p>
 * TODO : document me !!
 * </p>
 * created : 14/10/14 23:11
 *
 * @author Silviu Ilie
 */
public class QuartzUtilityControllerTest {

    private QuartzUtilityController testedController = new QuartzUtilityController();
    private Scheduler schedulerMock = mock(Scheduler.class);

    private HttpSession httpSessionMock = mock(HttpSession.class);
    private HttpServletRequest httpRequestMock = mock(HttpServletRequest.class);
    private QuartzUtilityAuthorization authorizationMock = mock(QuartzUtilityAuthorization.class);


    @Before
    public void init() {
        testedController.setApplicationContext(
                new QuartzApplicationContext(authorizationMock)
        );

        testedController.setQuartzScheduler(schedulerMock);

        reset(authorizationMock
                , schedulerMock
                , httpRequestMock
                , httpRequestMock);
    }

    @Test
    public void pauseAll() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        testedController.pauseAll(httpSessionMock);

        verify(schedulerMock).pauseAll();
        verify(authorizationMock).authorize(httpSessionMock);
    }

    @Test
    public void resumeAll() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        testedController.resumeAll(httpSessionMock);

        verify(schedulerMock).resumeAll();
        verify(authorizationMock).authorize(httpSessionMock);
    }

    @Test
    public void interruptJob() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        final String groupName = "groupName";
        final String target = "jobName";
        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(groupName);
        }});

        final JobKey testKey = new JobKey(target, groupName);
        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(groupName))).thenReturn(
                new HashSet<JobKey>() {{
                    add(testKey);
                }}
        );
        JobDetail detailMock = mock(JobDetail.class);
        when(schedulerMock.getJobDetail(testKey)).thenReturn(detailMock);
        when(detailMock.getKey()).thenReturn(testKey);

        testedController.interruptJob(target, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();
        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(groupName));
        verify(schedulerMock).getJobDetail(testKey);
        verify(schedulerMock).interrupt(testKey);

    }
    @Test
    public void interruptJob_err() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        final String groupName = "groupName";
        final String target = "jobName";
        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(groupName);
        }});

        final JobKey testKey = new JobKey(target, groupName);
        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(groupName))).thenReturn(
                new HashSet<JobKey>() {{
                    add(testKey);
                }}
        );
        JobDetail detailMock = mock(JobDetail.class);
        when(schedulerMock.getJobDetail(testKey)).thenReturn(detailMock);
        when(detailMock.getKey()).thenReturn(testKey);
        when(schedulerMock.interrupt(testKey)).thenThrow(new UnableToInterruptJobException("test"));

        testedController.interruptJob(target, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();
        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(groupName));
        verify(schedulerMock).getJobDetail(testKey);

    }

    @Test
    public void pauseJob() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        final String groupName = "groupName";
        final String target = "jobName";
        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(groupName);
        }});

        final JobKey testKey = new JobKey(target, groupName);
        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(groupName))).thenReturn(
                new HashSet<JobKey>() {{
                    add(testKey);
                }}
        );
        JobDetail detailMock = mock(JobDetail.class);
        when(schedulerMock.getJobDetail(testKey)).thenReturn(detailMock);
        when(detailMock.getKey()).thenReturn(testKey);

        testedController.pauseJob(target, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();
        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(groupName));
        verify(schedulerMock).getJobDetail(testKey);
        verify(schedulerMock).pauseJob(testKey);

    }

    @Test
    public void resumeJob() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        final String groupName = "groupName";
        final String target = "jobName";
        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(groupName);
        }});

        final JobKey testKey = new JobKey(target, groupName);
        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(groupName))).thenReturn(
                new HashSet<JobKey>() {{
                    add(testKey);
                }}
        );
        JobDetail detailMock = mock(JobDetail.class);
        when(schedulerMock.getJobDetail(testKey)).thenReturn(detailMock);
        when(detailMock.getKey()).thenReturn(testKey);

        testedController.resumeJob(target, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();
        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(groupName));
        verify(schedulerMock).getJobDetail(testKey);
        verify(schedulerMock).resumeJob(testKey);

    }

    @Test
    public void pauseTrigger() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        final String triggerGroup = "groupName";
        final String triggerName = "triggerName";

        final TriggerKey testKey = new TriggerKey(triggerName, triggerGroup);

        testedController.pauseTrigger(triggerName, triggerGroup, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).pauseTrigger(testKey);

    }

    @Test
    public void resumeTrigger() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        final String triggerGroup = "groupName";
        final String triggerName = "triggerName";

        final TriggerKey testKey = new TriggerKey(triggerName, triggerGroup);

        testedController.resumeTrigger(triggerName, triggerGroup, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).resumeTrigger(testKey);

    }


    @Test
    public void quartzChange() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        testedController.quartzChange(httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

    }

    @Test
    public void quartzChangeNotAuthorized() throws SchedulerException {

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(false);

        try {
            testedController.quartzChange(httpSessionMock);

            fail();

        } catch (Exception e) {
            //expected
        }

        verify(authorizationMock).authorize(httpSessionMock);

    }


    @Test
    public void listJobs() throws SchedulerException {

        final String groupName = "groupName";

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);
        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(groupName);
        }});

        testedController.listJobs(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();
        verify(authorizationMock, times(2)).authorize(httpSessionMock);

    }

    @Test
    public void setPriority() throws SchedulerException {

        final String target = "jobName";
        final String trigger = "trigger";
        final String newExpression = "0 1 1 * * ?";
        final String oldExpression = "0 0/1 7-16 * * ?";

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        testedController.changeTrigger(
                target,
                trigger,
                newExpression,
                oldExpression,
                httpSessionMock
        );

        verify(authorizationMock).authorize(httpSessionMock);

    }

    @Test
    public void revertPriority() throws SchedulerException {

        final String trigger = "trigger";

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        testedController.revertPriority(trigger, httpSessionMock);

        verify(authorizationMock).authorize(httpSessionMock);

    }

}
