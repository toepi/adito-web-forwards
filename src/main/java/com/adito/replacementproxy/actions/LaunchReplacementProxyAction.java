
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
			
package com.adito.replacementproxy.actions;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.adito.boot.SystemProperties;
import com.adito.boot.Util;
import com.adito.core.CoreEvent;
import com.adito.core.CoreServlet;
import com.adito.core.actions.AuthenticatedAction;
import com.adito.core.stringreplacement.VariableReplacement;
import com.adito.policyframework.LaunchSession;
import com.adito.policyframework.LaunchSessionFactory;
import com.adito.policyframework.ResourceAccessEvent;
import com.adito.security.SessionInfo;
import com.adito.webforwards.ReplacementProxyWebForward;
import com.adito.webforwards.WebForwardEventConstants;
import com.adito.webforwards.WebForwardTypeItem;
import com.adito.webforwards.WebForwardTypes;

/**
 * Implementation of {@link com.adito.core.actions.AuthenticatedAction}
 * that is the entry point for <i>Replacement Proxy Web Forward</i>.
 */
public class LaunchReplacementProxyAction extends AuthenticatedAction {

	static boolean debug = "true".equalsIgnoreCase(SystemProperties.get("adito.debugReplacementProxyTraffic"));
	static HashMap caches = new HashMap();
	static Log log = LogFactory.getLog(LaunchReplacementProxyAction.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adito.core.actions.AuthenticatedAction#isIgnoreSessionLock()
	 */
	protected boolean isIgnoreSessionLock() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adito.core.actions.AuthenticatedAction#onExecute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward onExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
		// Setup the Replacement proxy
		LaunchSession launchSession = LaunchSessionFactory.getInstance()
						.getLaunchSession(request.getParameter(LaunchSession.LAUNCH_ID));
		launchSession.checkAccessRights(null, getSessionInfo(request));
		ReplacementProxyWebForward f = (ReplacementProxyWebForward) launchSession.getResource();

		VariableReplacement r = new VariableReplacement();
		r.setServletRequest(request);
		r.setLaunchSession(launchSession);
		String destinationURL = r.replace(f.getDestinationURL());

		CoreEvent evt = new ResourceAccessEvent(this,
                        WebForwardEventConstants.WEB_FORWARD_STARTED,
						f,
						launchSession.getPolicy(),
						this.getSessionInfo(request),
						CoreEvent.STATE_SUCCESSFUL).addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_TYPE,
			((WebForwardTypeItem) WebForwardTypes.WEB_FORWARD_TYPES.get(f.getType())).getName())
						.addAttribute(WebForwardEventConstants.EVENT_ATTR_WEB_FORWARD_URL, destinationURL);

		CoreServlet.getServlet().fireCoreEvent(evt);

		response.sendRedirect("/replacementProxyEngine?" + LaunchSession.LONG_LAUNCH_ID
			+ "="
			+ launchSession.getId()
			+ "&sslex_url="
			+ Util.urlEncode(destinationURL));
		return null;
	}

	public int getNavigationContext(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		return SessionInfo.MANAGEMENT_CONSOLE_CONTEXT | SessionInfo.USER_CONSOLE_CONTEXT;
	}

}
