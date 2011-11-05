/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.AWTEvent;
import java.io.File;

/**
 * The event that passes a file reception to HttpFileEventListeners.
 */
public class HttpConnectionEvent extends AWTEvent {

	public static final int CONNECTION_EVENT = AWTEvent.RESERVED_ID_MAX + 4271;
	public static final int STARTUP = 1;
	public static final int SHUTDOWN = 1;
	public static final int RECEIVED = 0;
	public static final int ERROR = -1;

	public int status;
	public String message;

	/**
	 * Class constructor capturing a general HttpFileEvent.
	 * @param object the source of the event.
	 * @param status the type of file event.
	 * @param message a text message describing the event.
	 */
	public HttpConnectionEvent(Object object, int status, String message) {
		super(object, CONNECTION_EVENT);
		this.status = status;
		this.message = message;
	}

}
