/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import org.rsna.util.*;

/**
 * A JPanel for managing Authorization parameters. This class provides
 * a user interface allowing for enabling and disabling the Authorization
 * header in HTTP client connections.
 */
public class Authorization extends JPanel {

	private static JCheckBox enabled;
	private static JTextField user;
	private static JTextField password;

	/**
	 * Class constructor; creates an instance of the Authorization class
	 * and initializes it to disable the Authorization header.
	 */
	public Authorization() {
		super();
		Border border = BorderFactory.createEmptyBorder(10,10,10,10);
		this.setBorder(border);
		this.setLayout(new SpecialLayout(10,10));

		enabled = new JCheckBox("Include Authorization header in client connections", false);
		JLabel userLabel = new JLabel("Username:");
		user = new JTextField(10);
		JLabel passwordLabel = new JLabel("Password:");
		password = new JTextField(10);

		this.add(enabled);
		this.add(userLabel);
		this.add(user);
		this.add(passwordLabel);
		this.add(password);
	}

	/**
	 * Get the status of the Authorization checkbox.
	 * @return true if the Authorization checkbox is selected; otherwise false.
	 */
	public static boolean getEnabled() {
		return enabled.isSelected();
	}

	/**
	 * Get the contents of the User text field.
	 * @return the contents of the User text field.
	 */
	public static String getUser() {
		return user.getText().trim();
	}

	/**
	 * Get the contents of the Password text field.
	 * @return the contents of the Password text field.
	 */
	public static String getPassword() {
		return password.getText().trim();
	}

	/**
	 * Determine whether the current values in the user interface indicate
	 * that Authorization is to be used.
	 * @return true if both User and Proxy Password are not blank; false otherwise.
	 */
	public static boolean authenticate() {
		if (!getUser().equals("") && !getPassword().equals("")) return true;
		return false;
	}

	/**
	 * Get the base-64 encoded value of the credentials
	 * in the form required for an HTTP Authorization header:
	 * <ul><li>
	 * encode((getUser() + ":" + getPassword()).getBytes())
	 * </li></ul>
	 * @return the user authentication credentials from the user interface.
	 */
	public static String getEncodedCredentials() {
		return Base64.encodeToString((getUser() + ":" + getPassword()).getBytes());
	}

	//Special layout manager for the Authorization panel
	class SpecialLayout implements LayoutManager {

		private int horizontalGap;
		private int verticalGap;

		public SpecialLayout(int horizontalGap, int verticalGap) {
			this.horizontalGap = horizontalGap;
			this.verticalGap = verticalGap;
		}

		public void addLayoutComponent(String name,Component component) {
			//not necessary for this layout manager
		}

		public void removeLayoutComponent(Component component) {
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getLayoutSize(parent,horizontalGap,verticalGap,false);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return getLayoutSize(parent,horizontalGap,verticalGap,false);
		}

		public void layoutContainer(Container parent) {
			getLayoutSize(parent,horizontalGap,verticalGap,true);
		}

		private Dimension getLayoutSize(Container parent, int hGap, int vGap, boolean layout) {
			int width = 0;
			int height = 0;
			Component p = parent.getParent();
			if (p instanceof JScrollPane)
				width = ((JScrollPane)p).getViewport().getExtentSize().width;
			else
				width = parent.getSize().width;
			Component[] components = parent.getComponents();
			Insets insets = parent.getInsets();
			int currentY = insets.top;
			Dimension d0, d1;

			if (components.length == 0)
				return new Dimension(
					insets.left + insets.right, insets.top + insets.bottom);

			//Okay, there is something in the container.
			//Lay out the first component on its own line
			d0 = components[0].getPreferredSize();
			components[0].setBounds(insets.left,currentY,d0.width,d0.height);
			currentY += d0.height + vGap;

			//Now lay out the rows of labels + textfields.
			//First make the labels all the same width.
			//Find the maximum width of the labels.
			int w;
			int labelWidth = 0;
			for (int i=1; i<components.length-1; i+=2) {
				w = components[i].getPreferredSize().width;
				labelWidth = Math.max(w, labelWidth);
			}

			for (int i=1; i<components.length-1; i+=2) {
				int currentX = insets.left;
				d0 = components[i].getPreferredSize();
				d1 = components[i+1].getPreferredSize();
				//put the components on the line
				if (layout) {
					components[i].setBounds(currentX, currentY, labelWidth, d0.height);
					int x1 = currentX + labelWidth + hGap;
					int w1 = width - x1 - insets.right;
					components[i+1].setBounds(x1, currentY, w1, d1.height);
				}
				currentY += vGap + Math.max(d0.height, d1.height);
			}
			return new Dimension(width, currentY+insets.bottom);
		}
	}

}
