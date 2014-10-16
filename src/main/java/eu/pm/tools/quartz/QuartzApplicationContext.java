package eu.pm.tools.quartz;

/**
 * Created by silviu
 * Date: 15/02/14 / 16:55944
 * <p>
 * describes the application that uses the utility (base package name and authorization).
 * </p>
 * <br/>
 *
 * @author Silviu Ilie
 */
public class QuartzApplicationContext {

    /**
     * default authorization.
     */
    public static DefaultQuartzUtilityAuthorization DEFAULT_AUTHORIZATION = new DefaultQuartzUtilityAuthorization();


    /**
     * describes aut
     */
    public QuartzUtilityAuthorization utilityAuthorization;

    /**
     * sets mandatory fields.
     *
     * @param utilityAuthorization authorization.
     */
    public QuartzApplicationContext(QuartzUtilityAuthorization utilityAuthorization) {
        this.utilityAuthorization = utilityAuthorization;
    }

    /**
     * default.
     */
    public QuartzApplicationContext() {
        this.utilityAuthorization = DEFAULT_AUTHORIZATION;
    }

    public QuartzUtilityAuthorization getUtilityAuthorization() {
        return utilityAuthorization;
    }
}
