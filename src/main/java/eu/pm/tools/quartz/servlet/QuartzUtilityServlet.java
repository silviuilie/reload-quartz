package eu.pm.tools.quartz.servlet;


import eu.pm.tools.quartz.QuartzApplicationContext;
import eu.pm.tools.quartz.QuartzUtility;
import eu.pm.tools.quartz.QuartzUtilityAuthorization;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Created by silviu
 * Date: 13/02/14 / 22:14279
 * <p>
 * services {@link eu.pm.tools.quartz.QuartzUtility}.
 * </p>
 * <br/>
 *
 * @author Silviu Ilie
 */
public class QuartzUtilityServlet extends HttpServlet {

    private static final String DEFAULT_JSP_LOCATION = "/WEB-INF/jsp/";

    /**
     * default jsp location.
     */
    String jspLocation = DEFAULT_JSP_LOCATION;
    String viewName = "quartzReload.jsp";

    /**
     * quartz tools.
     */
    private QuartzUtility quartzUtility = QuartzUtility.getInstance();

    QuartzUtility getQuartzUtility() {
        return quartzUtility;
    }

    /**
     * handles all GET requests, if  URI is not expected redirect to home.
     *
     * @param req  request
     * @param resp response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestName = req.getRequestURI();

        // when expected URI requested and authorized, go home.
        if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_HOME) && quartzUtility.isAuthorized(req.getSession())) {

            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getRequestDispatcher(jspLocation + viewName);
            dispatcher.forward(req, resp);
            return;

        } // list jobs and quartz details.
        else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_LIST)) {
            resp.getWriter().write(
                    quartzUtility.schedulerDetails(req.getSession())
            );
        }
        resp.sendRedirect(req.getContextPath());
    }


    /**
     * handles all POST requests, if  URI is not expected redirect to home.
     *
     * @param req  request
     * @param resp response
     * @throws ServletException
     * @throws IOException
     */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestName = req.getRequestURI();

        if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_LIST_CHANGES)) {
            resp.getWriter().write(
                    quartzUtility.listChanges(req.getSession())
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_PAUSE_ALL)) {
            resp.getWriter().write(
                    quartzUtility.pauseAll(req.getSession())
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_RESUME_ALL)) {
            resp.getWriter().write(
                    quartzUtility.resumeAll(req.getSession())
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_PAUSE_JOB)) {
            resp.getWriter().write(
                    quartzUtility.pauseJob(req.getParameter("target"), req.getSession())
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_RESUME_JOB)) {
            resp.getWriter().write(
                    quartzUtility.resumeJob(req.getParameter("target"), req.getSession())
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_INTERRUPT_JOB)) {
            resp.getWriter().write(
                    quartzUtility.interruptJob(req.getParameter("target"), req.getSession())
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_CHANGE_TRIGGER)) {
            resp.getWriter().write(
                    quartzUtility.setNewTrigger(
                            req.getParameter("target")
                            , req.getParameter("trigger")
                            , req.getParameter("newExpression")
                            , req.getParameter("oldExpression")
                            , req.getSession()
                    )
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_REVERT_TRIGGER_CHANGES)) {
            resp.getWriter().write(
                    quartzUtility.resetCronExpression(
                            req.getParameter("trigger")
                            , req.getSession()
                    )
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_PAUSE_TRIGGER)) {
            resp.getWriter().write(
                    quartzUtility.pauseTrigger(
                            req.getParameter("triggerName")
                            , req.getParameter("triggerGroup")
                            , req.getSession()
                    )
            );
        } else if (requestName.contains(QuartzUtility.QUARTZ_UTILITY_RESUME_TRIGGER)) {
            resp.getWriter().write(
                    quartzUtility.resumeTrigger(
                            req.getParameter("triggerName")
                            , req.getParameter("triggerGroup")
                            , req.getSession()
                    )
            );
        }

    }


    /**
     * handle configuration.
     *
     * @param config container provided.
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final String jspLocation = config.getServletContext().getInitParameter("quartz-jsp-location");
        if (jspLocation == null) {
            log.warn("quartz-jsp-location parameter not found, using default " + DEFAULT_JSP_LOCATION);
        } else {
            this.jspLocation = jspLocation;
        }

        final String authorizationClassName = config.getServletContext().getInitParameter("quartz-authorization-class");
        if (authorizationClassName == null) {
            log.warn("quartz-authorization-class parameter not found, using default");
        }

        QuartzUtilityAuthorization quartzUtilityAuthorization = null;

        if (isNotEmpty(authorizationClassName)) {
            try {
                Class<QuartzUtilityAuthorization> authorizationClass =
                        (Class<QuartzUtilityAuthorization>) Class.forName(authorizationClassName);

                Constructor<QuartzUtilityAuthorization> authClassConstructor = authorizationClass.getDeclaredConstructor();
                quartzUtilityAuthorization = authClassConstructor.newInstance();

            } catch (Throwable e) {
                log.error(authorizationClassName + " not found", e);
                log.warn(authorizationClassName + " not found.using default : "
                        + QuartzApplicationContext.DEFAULT_AUTHORIZATION.getClass());
                quartzUtilityAuthorization = QuartzApplicationContext.DEFAULT_AUTHORIZATION;
            }

        } else {
            log.warn("using default authorization " + QuartzApplicationContext.DEFAULT_AUTHORIZATION.getClass());
            quartzUtilityAuthorization = QuartzApplicationContext.DEFAULT_AUTHORIZATION;
        }

        quartzUtility.setApplicationContext(new QuartzApplicationContext(quartzUtilityAuthorization));

        if (log.isDebugEnabled()) {
            log.debug("=====================================");
            log.debug("quartz reloader available as /" + QuartzUtility.QUARTZ_UTILITY_HOME);
            log.debug("=====================================");
        }
    }


    /**
     * private.
     */
    private Log log = LogFactory.getLog(this.getClass());
}
