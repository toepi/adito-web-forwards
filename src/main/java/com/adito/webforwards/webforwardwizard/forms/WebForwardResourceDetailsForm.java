
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
			
package com.adito.webforwards.webforwardwizard.forms;

import com.adito.core.forms.AbstractFavoriteResourceDetailsWizardForm;
import com.adito.navigation.FavoriteResourceType;
import com.adito.webforwards.WebForwardPlugin;

/**
 * The form for the default resource attributes.
 */
public class WebForwardResourceDetailsForm extends AbstractFavoriteResourceDetailsWizardForm {
    /**
     * Construtor
     */
    public WebForwardResourceDetailsForm() {
        super(true, true, "/WEB-INF/jsp/content/webforward/webforwardwizard/webForwardResourceDetails.jspf", "resourceName", true, false,
                        "webForwardResourceDetails", "webForwards", "webForwardWizard.webForwardResourceDetails", 2,
                        (FavoriteResourceType)WebForwardPlugin.WEBFORWARD_RESOURCE_TYPE);
    }
}