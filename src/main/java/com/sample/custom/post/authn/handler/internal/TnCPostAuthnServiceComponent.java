package com.sample.custom.post.authn.handler.internal;

import com.sample.custom.post.authn.handler.TnCPostAuthnHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.user.core.service.RealmService;


/**
 * @scr.component name="com.sample.custom.post.authn.handler.component" immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class TnCPostAuthnServiceComponent {

    private static Log log = LogFactory.getLog(TnCPostAuthnServiceComponent.class);

    private static RealmService realmService;

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        TnCPostAuthnServiceComponent.realmService = realmService;
    }

    protected void activate(ComponentContext ctxt) {
        try {
            TnCPostAuthnHandler tnCPostAuthnHandler = new TnCPostAuthnHandler();
            ctxt.getBundleContext().registerService(PostAuthenticationHandler.class.getName(), tnCPostAuthnHandler, null);
            if (log.isDebugEnabled()) {
                log.info("TnC Post Authentication Handler bundle is activated");
            }
        } catch (Throwable e) {
            log.error("TnC Post Authentication Handler bundle activation Failed", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.info("TnC Post Authentication Handler bundle is deactivated");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service");
        }
        TnCPostAuthnServiceComponent.realmService = null;
    }

}
