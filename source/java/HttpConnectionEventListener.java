/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.util.EventListener;

/**
 * The interface for listeners to HttpFileEvents.
 */
public interface HttpConnectionEventListener extends EventListener {

	/**

	 * Notify listeners that a connection event has occurred.
	 * @param event the event describing the file that was affected.
	 */

	public void httpConnectionEventOccurred(HttpConnectionEvent event);

}
