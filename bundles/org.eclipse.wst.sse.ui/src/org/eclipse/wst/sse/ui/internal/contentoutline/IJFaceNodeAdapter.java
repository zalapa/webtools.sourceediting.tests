/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.sse.ui.internal.contentoutline;



import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;


public interface IJFaceNodeAdapter extends INodeAdapter {

	public Object[] getChildren(Object node);

	/**
	 * Returns an enumeration with the elements belonging to the passed
	 * element. These are the top level items in a list, tree, table, etc...
	 */
	public Object[] getElements(Object node);

	/**
	 * Fetches the label image specific to this object instance.
	 */
	public Image getLabelImage(Object node);

	/**
	 * Fetches the label text specific to this object instance.
	 */
	public String getLabelText(Object node);

	public Object getParent(Object node);

	public boolean hasChildren(Object node);
}
