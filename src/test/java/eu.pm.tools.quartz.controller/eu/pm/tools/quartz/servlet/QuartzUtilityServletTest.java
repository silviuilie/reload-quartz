package eu.pm.tools.quartz.servlet;

import eu.pm.tools.quartz.QuartzApplicationContext;
import eu.pm.tools.quartz.QuartzUtility;
import eu.pm.tools.quartz.QuartzUtilityAuthorization;
import org.junit.Before;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.*;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * <p>
 * QuartzUtilityServlet test
 * </p>
 * created : 15/10/14 20:40
 *
 * @author Silviu Ilie
 */
public class QuartzUtilityServletTest {

    QuartzUtilityServlet tested = new QuartzUtilityServlet();


    private HttpServletRequest httpRequestMock = mock(HttpServletRequest.class);
    private HttpServletResponse httpResponseMock = mock(HttpServletResponse.class);

    private HttpSession httpSessionMock = mock(HttpSession.class);

    private QuartzUtilityAuthorization authorizationMock = mock(QuartzUtilityAuthorization.class);

    private ServletConfig servletConfigMock = mock(ServletConfig.class);
    private ServletContext servletContextMock = mock(ServletContext.class);
    private RequestDispatcher requestDispatcherMock = mock(RequestDispatcher.class);
    private PrintWriter writerMock = mock(PrintWriter.class);

    private Scheduler schedulerMock = mock(Scheduler.class);

    public class TestJob extends JobDetailImpl {

    }

    @Before
    public void init() throws ServletException, IOException {

        reset(httpRequestMock
                , httpResponseMock
                , httpSessionMock
                , authorizationMock
                , servletConfigMock
                , servletContextMock
                , schedulerMock
                , requestDispatcherMock);

        when(servletConfigMock.getServletContext()).thenReturn(servletContextMock);


        when(httpSessionMock.getServletContext()).thenReturn(servletContextMock);

        when(servletContextMock.getRequestDispatcher(tested.jspLocation + tested.viewName)).thenReturn(requestDispatcherMock);

        tested.init(servletConfigMock);

        tested.getQuartzUtility().setApplicationContext(
                new QuartzApplicationContext(authorizationMock) {{

                }}
        );
        when(httpResponseMock.getWriter()).thenReturn(writerMock);

        tested.getQuartzUtility().setQuartzScheduler(schedulerMock);

    }


    @Test
    public void doGetHome() throws ServletException, IOException {
        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_HOME);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);


        tested.doGet(httpRequestMock, httpResponseMock);


        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);
    }

    @Test
    public void doGetLogs() throws ServletException, IOException {
        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_LIST);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);


        tested.doGet(httpRequestMock, httpResponseMock);


        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);
    }

    @Test
    public void doPost_pauseAll() throws ServletException, IOException, SchedulerException {

        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_PAUSE_ALL);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);


        tested.doPost(httpRequestMock, httpResponseMock);


        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).pauseAll();
    }

    @Test
    public void doPost_resumeAll() throws ServletException, IOException, SchedulerException {

        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_RESUME_ALL);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);


        tested.doPost(httpRequestMock, httpResponseMock);


        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).resumeAll();
    }

    @Test
    public void doPost_resumeJob() throws ServletException, IOException, SchedulerException {

        final String target = "target";
        final String group = "group";

        final JobKey jobKey = new JobKey(target, group);

        final JobDetail mockJobDetail = mock(JobDetail.class);


        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_RESUME_JOB);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(httpRequestMock.getParameter(target)).thenReturn(target);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(target);
        }});

        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(target))).thenReturn(
            new HashSet<JobKey>() {{
                add(jobKey);
            }}
        );

        when(schedulerMock.getJobDetail(jobKey)).thenReturn(mockJobDetail);

        when(mockJobDetail.getKey()).thenReturn(jobKey);



        tested.doPost(httpRequestMock, httpResponseMock);



        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();

        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(target));

        verify(mockJobDetail, times(2)).getKey();

        verify(schedulerMock).getJobDetail(jobKey);

        verify(schedulerMock).resumeJob(mockJobDetail.getKey());
    }

    @Test
    public void doPost_pauseJob() throws ServletException, IOException, SchedulerException {

        final String target = "target";
        final String group = "group";

        final JobKey jobKey = new JobKey(target, group);

        final JobDetail mockJobDetail = mock(JobDetail.class);


        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_PAUSE_JOB);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(httpRequestMock.getParameter(target)).thenReturn(target);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(target);
        }});

        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(target))).thenReturn(
            new HashSet<JobKey>() {{
                add(jobKey);
            }}
        );

        when(schedulerMock.getJobDetail(jobKey)).thenReturn(mockJobDetail);

        when(mockJobDetail.getKey()).thenReturn(jobKey);



        tested.doPost(httpRequestMock, httpResponseMock);



        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();

        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(target));

        verify(mockJobDetail, times(2)).getKey();

        verify(schedulerMock).getJobDetail(jobKey);

        verify(schedulerMock).pauseJob(mockJobDetail.getKey());
    }

    @Test
    public void doPost_interruptJob() throws ServletException, IOException, SchedulerException {

        final String target = "target";
        final String group = "group";

        final JobKey jobKey = new JobKey(target, group);

        final JobDetail mockJobDetail = mock(JobDetail.class);


        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_INTERRUPT_JOB);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(httpRequestMock.getParameter(target)).thenReturn(target);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(target);
        }});

        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(target))).thenReturn(
            new HashSet<JobKey>() {{
                add(jobKey);
            }}
        );

        when(schedulerMock.getJobDetail(jobKey)).thenReturn(mockJobDetail);

        when(mockJobDetail.getKey()).thenReturn(jobKey);



        tested.doPost(httpRequestMock, httpResponseMock);



        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock).getJobGroupNames();

        verify(schedulerMock).getJobKeys(GroupMatcher.jobGroupEquals(target));

        verify(mockJobDetail).getKey();

        verify(schedulerMock).getJobDetail(jobKey);

        verify(schedulerMock).interrupt(mockJobDetail.getKey());
    }

    @Test
    public void doPost_setNewTrigger() throws ServletException, IOException, SchedulerException {

        final String target = "target";
        final String trigger = "trigger";
        final String group = "DEFAULT";
        final String newExpression = "0 1 1 * * ?";
        final String oldExpression = "0 0/2 8-17 * * ?";

        final JobKey jobKey = new JobKey(target, group);

        final JobDetailImpl mockJobDetail = mock(JobDetailImpl.class);


        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_CHANGE_TRIGGER);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(httpRequestMock.getParameter(target)).thenReturn(target);
        when(httpRequestMock.getParameter(trigger)).thenReturn(trigger);
        when(httpRequestMock.getParameter("newExpression")).thenReturn(newExpression);
        when(httpRequestMock.getParameter("oldExpression")).thenReturn(oldExpression);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(target);
        }});

        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(target))).thenReturn(
            new HashSet<JobKey>() {{
                add(jobKey);
            }}
        );

        when(schedulerMock.getJobDetail(jobKey)).thenReturn(mockJobDetail);

        when(mockJobDetail.getKey()).thenReturn(jobKey);
        when(mockJobDetail.getName()).thenReturn(target);
        TestJob testJob = new TestJob();
        Class jobClass = testJob.getClass();
        when(mockJobDetail.getJobClass()).thenReturn(jobClass);


        /**
         * call list first
         */
        HttpServletRequest mockListRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockListResponse = mock(HttpServletResponse.class);
        HttpSession httpListSessionMock = mock(HttpSession.class);
        when(mockListRequest.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_LIST);
        when(mockListRequest.getSession()).thenReturn(httpListSessionMock);
        when(authorizationMock.authorize(httpListSessionMock)).thenReturn(true);
        when(mockListResponse.getWriter()).thenReturn(writerMock);
        tested.doGet(mockListRequest, mockListResponse);


        /**
         * call setNewTrigger
         */
        tested.doPost(httpRequestMock, httpResponseMock);

        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock, times(2)).getJobGroupNames();

        verify(schedulerMock, times(2)).getJobKeys(GroupMatcher.jobGroupEquals(target));

        verify(mockJobDetail, times(1)).getKey();

        verify(schedulerMock, times(2)).getJobDetail(jobKey);

        verify(schedulerMock).rescheduleJob(
                new TriggerKey(trigger, group),
                newTrigger().withIdentity(trigger)
                .forJob(mockJobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(newExpression))
                .build()
        );
    }

    @Test
    public void doPost_resetCronExpression() throws ServletException, IOException, SchedulerException {

        final String target = "target";
        final String trigger = "trigger";
        final String group = "DEFAULT";
        final String newExpression = "0 1 1 * * ?";
        final String oldExpression = "0 0/2 8-17 * * ?";

        final JobKey jobKey = new JobKey(target, group);

        final JobDetailImpl mockJobDetail = mock(JobDetailImpl.class);


        when(httpRequestMock.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_REVERT_TRIGGER_CHANGES);

        when(httpRequestMock.getSession()).thenReturn(httpSessionMock);

        when(httpRequestMock.getParameter(target)).thenReturn(target);
        when(httpRequestMock.getParameter(trigger)).thenReturn(trigger);
        when(httpRequestMock.getParameter("newExpression")).thenReturn(newExpression);
        when(httpRequestMock.getParameter("oldExpression")).thenReturn(oldExpression);

        when(authorizationMock.authorize(httpSessionMock)).thenReturn(true);

        when(schedulerMock.getJobGroupNames()).thenReturn(new ArrayList<String>() {{
            add(target);
        }});

        when(schedulerMock.getJobKeys(GroupMatcher.jobGroupEquals(target))).thenReturn(
            new HashSet<JobKey>() {{
                add(jobKey);
            }}
        );

        when(schedulerMock.getJobDetail(jobKey)).thenReturn(mockJobDetail);

        when(mockJobDetail.getKey()).thenReturn(jobKey);
        when(mockJobDetail.getName()).thenReturn(target);
        TestJob testJob = new TestJob();
        Class jobClass = testJob.getClass();
        when(mockJobDetail.getJobClass()).thenReturn(jobClass);


        /**
         * call list first
         */
        HttpServletRequest mockListRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockListResponse = mock(HttpServletResponse.class);
        HttpSession httpListSessionMock = mock(HttpSession.class);
        when(mockListRequest.getRequestURI()).thenReturn(QuartzUtility.QUARTZ_UTILITY_LIST);
        when(mockListRequest.getSession()).thenReturn(httpListSessionMock);
        when(authorizationMock.authorize(httpListSessionMock)).thenReturn(true);
        when(mockListResponse.getWriter()).thenReturn(writerMock);
        tested.doGet(mockListRequest, mockListResponse);


        /**
         * call setNewTrigger
         */
        tested.doPost(httpRequestMock, httpResponseMock);

        verify(httpRequestMock).getRequestURI();

        verify(httpRequestMock).getSession();

        verify(authorizationMock).authorize(httpSessionMock);

        verify(schedulerMock, times(1)).getJobGroupNames();

        verify(schedulerMock, times(1)).getJobKeys(GroupMatcher.jobGroupEquals(target));

        verify(schedulerMock, times(1)).getJobDetail(jobKey);

    }

}
