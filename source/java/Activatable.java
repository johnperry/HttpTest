/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

/**
 * The interface for components that can be activated or deactivated.
 */
public interface Activatable {
	public void activate();
	public void deactivate();
}
