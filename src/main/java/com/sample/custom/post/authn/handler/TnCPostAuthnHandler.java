package com.sample.custom.post.authn.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class TnCPostAuthnHandler extends AbstractPostAuthnHandler {

    private static final String TNC_PROMPTED = "tncPrompted";
    private static final Log log = LogFactory.getLog(TnCPostAuthnHandler.class);

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        if (authenticatedUser == null) {
            if (isDebugEnabled()) {
                String message = "User not available in AuthenticationContext. Returning";
                logDebug(message);
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isConsentPrompted(context)) {
            return handlePostConsent(request, response, context);
        } else {
            return handlePreConsent(request, response, context);
        }
    }

    private boolean isDebugEnabled() {

        return log.isDebugEnabled();
    }

    private void logDebug(String message) {

        log.debug(message);
    }

    protected PostAuthnHandlerFlowStatus handlePreConsent(HttpServletRequest request, HttpServletResponse response,
                                                          AuthenticationContext context)
            throws PostAuthenticationFailedException {

        redirectToConsentPage(response, context);
        setConsentPoppedUpState(context);
        return PostAuthnHandlerFlowStatus.INCOMPLETE;
    }

    protected PostAuthnHandlerFlowStatus handlePostConsent(HttpServletRequest request, HttpServletResponse response,
                                                           AuthenticationContext context)
            throws PostAuthenticationFailedException {

        final String TNC_INPUT = "tcInput";
        final String TNC_ON_INPUT = "on";

       /*
        In here we can process the request submitted from terms and condition page.
         */
        String input = request.getParameter(TNC_INPUT);
        if (TNC_ON_INPUT.equals(input)) {
            /*
            logic when user accept terms and condition
             */
            log.info("User accepted terms and conditions.");
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        } else {
            /*
            logic when user rejects terms and condition
             */
            log.info("User rejected terms and conditions.");
            throw new PostAuthenticationFailedException("User rejected terms and conditions.", "TNC-10002");
        }
    }

    private void redirectToConsentPage(HttpServletResponse response, AuthenticationContext context) throws
            PostAuthenticationFailedException {

        URIBuilder uriBuilder;
        try {
            uriBuilder = getUriBuilder(context);
            response.sendRedirect(uriBuilder.build().toString());
        } catch (IOException e) {
            throw new PostAuthenticationFailedException("Authentication failed. Error while processing Terms and " +
                    "Conditions.", "Error while redirecting to terms and conditions page.", e);
        } catch (URISyntaxException e) {
            throw new PostAuthenticationFailedException("Authentication failed. Error while process Terms and " +
                    "Conditions", "Error while building redirect URI.", e);
        }
    }

    private URIBuilder getUriBuilder(AuthenticationContext context) throws URISyntaxException {

        final String LOGIN_ENDPOINT = "login.do";
        final String TNC_ENDPOINT = "termsAndConditionForm.jsp";

        String TNC_ENDPOINT_URL = ConfigurationFacade.getInstance()
                .getAuthenticationEndpointURL().replace(LOGIN_ENDPOINT, TNC_ENDPOINT);
        URIBuilder uriBuilder;
        uriBuilder = new URIBuilder(TNC_ENDPOINT_URL);
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY,
                context.getContextIdentifier());
        uriBuilder.addParameter(FrameworkConstants.REQUEST_PARAM_SP,
                context.getSequenceConfig().getApplicationConfig().getApplicationName());
        return uriBuilder;
    }

    private AuthenticatedUser getAuthenticatedUser(AuthenticationContext authenticationContext) {

        return authenticationContext.getSequenceConfig().getAuthenticatedUser();
    }

    private void setConsentPoppedUpState(AuthenticationContext authenticationContext) {

        authenticationContext.addParameter(TNC_PROMPTED, true);
    }

    private boolean isConsentPrompted(AuthenticationContext authenticationContext) {

        return authenticationContext.getParameter(TNC_PROMPTED) != null;
    }

    @Override
    public String getName() {

        return "TnCPostAuthenticationHandler";
    }
}
