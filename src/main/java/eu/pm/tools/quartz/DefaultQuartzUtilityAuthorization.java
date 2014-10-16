package eu.pm.tools.quartz;

import javax.servlet.http.HttpSession;

/**
 * default authorization.
 * <p>
 * <b>
 * WARNING: this allows all requests!.
 * </b>
 * </p>
 * created : 12/10/14 20:10
 *
 * @author Silviu Ilie
 */
public class DefaultQuartzUtilityAuthorization implements QuartzUtilityAuthorization {

    @Override
    public boolean authorize(HttpSession session) {
        return true;
    }

}
