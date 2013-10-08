
				/*
 *  Adito
 *
 *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
			
package com.adito.webforwards;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adito.boot.HostService;
import com.adito.core.CoreEvent;
import com.adito.core.CoreServlet;
import com.adito.navigation.FavoriteResourceType;
import com.adito.navigation.WrappedFavoriteItem;
import com.adito.policyframework.DefaultResourceType;
import com.adito.policyframework.PolicyConstants;
import com.adito.policyframework.PolicyDatabaseFactory;
import com.adito.policyframework.Resource;
import com.adito.policyframework.ResourceChangeEvent;
import com.adito.policyframework.ResourceDeleteEvent;
import com.adito.security.LogonControllerFactory;
import com.adito.security.SessionInfo;

/**
 * Implementation of a {@link com.adito.policyframework.ResourceType} for
 * <i>Web Forward</i> resources.
 */
public class WebForwardResourceType extends DefaultResourceType implements FavoriteResourceType {

    final static Log log = LogFactory.getLog(WebForwardResourceType.class);

    /**
     * Constructor
     */
    public WebForwardResourceType() {
        super(WebForwardPlugin.WEBFORWARD_RESOURCE_TYPE_ID, "policyframework", PolicyConstants.DELEGATION_CLASS);
    }

    CoreEvent addWebForwardAttributes(CoreEvent evt, WebForward webForward) {
        evt.addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_TYPE,
            ((WebForwardTypeItem) WebForwardTypes.WEB_FORWARD_TYPES.get(webForward.getType())).getName());
        evt.addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_URL, webForward.getDestinationURL());
        evt.addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_CATEGORY, webForward.getCategory());

        if (webForward instanceof AbstractAuthenticatingWebForward) {
            AbstractAuthenticatingWebForward abstractAuthenticatingWebForward = (AbstractAuthenticatingWebForward) webForward;
            evt.addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_PREFERED_AUTH_SCHEME,
                abstractAuthenticatingWebForward.getPreferredAuthenticationScheme()).addAttribute(
                WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_AUTH_USERNAME,
                abstractAuthenticatingWebForward.getAuthenticationUsername()).addAttribute(
                WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_AUTH_FORM_TYPE, abstractAuthenticatingWebForward.getFormType());
            abstractAuthenticatingWebForward.addFormParametersToEvent(evt,
                WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_AUTH_FORM_PARAMETERS);

            if (abstractAuthenticatingWebForward instanceof ReplacementProxyWebForward) {
                ReplacementProxyWebForward rpwf = (ReplacementProxyWebForward) abstractAuthenticatingWebForward;
                rpwf.addRestrictToHostsToEvent(evt, WebForwardEventConstants.EVENT_ATTR_REPLACEMENT_WEB_FORWARD_RESTRICT_TO_HOSTS);

                evt.addAttribute(WebForwardEventConstants.EVENT_ATTR_REPLACEMENT_WEB_FORWARD_ENCODEING, rpwf.getEncoding());
            } else if (abstractAuthenticatingWebForward instanceof ReverseProxyWebForward) {
                ReverseProxyWebForward rpwf = (ReverseProxyWebForward) abstractAuthenticatingWebForward;
                evt.addAttribute(WebForwardEventConstants.EVENT_ATTR_REVERSE_WEB_FORWARD_ACTIVE_DNS,
                    String.valueOf(rpwf.getActiveDNS())).addAttribute(
                    WebForwardEventConstants.EVENT_ATTR_REVERSE_WEB_FORWARD_HOST_HEADER, rpwf.getHostHeader());

                rpwf.addPathsToEvent(evt, WebForwardEventConstants.EVENT_ATTR_REVERSE_WEB_FORWARD_PATHS);
                rpwf.addCustomHeadersToEvent(evt, WebForwardEventConstants.EVENT_ATTR_REVERSE_WEB_FORWARD_CUSTOM_HEADERS);

            }
        }
        return evt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adito.navigation.FavoriteResourceType#createWrappedFavoriteItem(int,
     *      javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public WrappedFavoriteItem createWrappedFavoriteItem(int resourceId, HttpServletRequest request, String type) throws Exception {
        WebForward wf = WebForwardDatabaseFactory.getInstance().getWebForward(resourceId);
        SessionInfo sessionInfo = LogonControllerFactory.getInstance().getSessionInfo(request);
        WebForwardItem wfi;
        String hostField = request.getHeader("Host");
        HostService aditoHost = hostField == null ? null : new HostService(hostField);
        if (wf != null) {
            wfi = new WebForwardItem(wf, aditoHost, PolicyDatabaseFactory.getInstance().getPoliciesAttachedToResource(wf,
                sessionInfo.getUser().getRealm()), wf.sessionPasswordRequired(sessionInfo));
            return new WrappedFavoriteItem(wfi, type);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adito.navigation.FavoriteResourceType#getResourceById(int)
     */
    public Resource getResourceById(int resourceId) throws Exception {
        return WebForwardDatabaseFactory.getInstance().getWebForward(resourceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adito.policyframework.DefaultResourceType#getResourceByName(java.lang.String,
     *      com.adito.security.SessionInfo)
     */
    public Resource getResourceByName(String resourceName, SessionInfo session) throws Exception {
        return WebForwardDatabaseFactory.getInstance().getWebForward(resourceName, session.getUser().getRealm().getRealmID());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adito.boot.policyframework.ResourceType#removeResource(int,
     *      com.adito.security.SessionInfo)
     */
    public Resource removeResource(int resourceId, SessionInfo session) throws Exception {
        try {
            WebForward resource = WebForwardDatabaseFactory.getInstance().deleteWebForward(resourceId);
            CoreEvent evt = new ResourceDeleteEvent(this, WebForwardEventConstants.DELETE_WEBFORWARD, resource, session,
                            CoreEvent.STATE_SUCCESSFUL).addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_URL,
                resource.getDestinationURL()).addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_TYPE,
                ((WebForwardTypeItem) WebForwardTypes.WEB_FORWARD_TYPES.get(resource.getType())).getName());
            CoreServlet.getServlet().fireCoreEvent(evt);
            return resource;
        } catch (Exception e) {
            CoreServlet.getServlet().fireCoreEvent(
                new ResourceDeleteEvent(this, WebForwardEventConstants.DELETE_WEBFORWARD, null, session,
                                CoreEvent.STATE_UNSUCCESSFUL));
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adito.boot.policyframework.ResourceType#updateResource(com.adito.boot.policyframework.Resource,
     *      com.adito.security.SessionInfo)
     */
    public void updateResource(Resource resource, SessionInfo session) throws Exception {
        try {
            WebForwardDatabaseFactory.getInstance().updateWebForward((WebForward) resource);
            CoreServlet.getServlet().fireCoreEvent(
                addWebForwardAttributes(new ResourceChangeEvent(this, WebForwardEventConstants.UPDATE_WEB_FORWARD, resource,
                                session, CoreEvent.STATE_SUCCESSFUL), ((WebForward) resource)));
        } catch (Exception e) {
            CoreServlet.getServlet().fireCoreEvent(
                new ResourceChangeEvent(this, WebForwardEventConstants.UPDATE_WEB_FORWARD, session, e));
            throw e;
        }
    }

    @Override
    public Resource createResource(Resource resource, SessionInfo session) throws Exception {
        return WebForwardDatabaseFactory.getInstance().createWebForward((WebForward) resource);
    }

}
