/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.html.core.internal.format;



import org.eclipse.wst.css.core.internal.format.CSSSourceFormatter;
import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleSheetAdapter;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSNode;
import org.eclipse.wst.html.core.internal.provisional.HTMLFormatContraints;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;

// nakamori_TODO: check and remove

public class EmbeddedCSSFormatter extends HTMLFormatter {

	//private IAdapterFactory factory = new CSSSourceFormatterFactory(CSSSourceFormatter.class, true);
	/**
	 */
	protected EmbeddedCSSFormatter() {
		super();
	}

	/**
	 */
	protected void formatNode(IDOMNode node, HTMLFormatContraints contraints) {
		if (node == null)
			return;
		IDOMText text = (IDOMText) node;

		String source = getCSSContent(node);
		if (source == null) { // fallback
			source = text.getSource();
		}

		int offset = text.getStartOffset();
		int length = text.getEndOffset() - offset;
		replaceSource(text.getModel(), offset, length, source);
		setWidth(contraints, source);
	}

	/**
	 */
	private String getCSSContent(IDOMNode text) {
		ICSSModel model = getCSSModel(text);
		if (model == null)
			return null;
		ICSSNode document = model.getDocument();
		if (document == null)
			return null;
		INodeNotifier notifier = (INodeNotifier) document;
		INodeAdapter adapter = notifier.getAdapterFor(CSSSourceFormatter.class);
		if (adapter == null)
			return null;
		CSSSourceFormatter formatter = (CSSSourceFormatter) adapter;
		StringBuffer buffer = formatter.format(document);
		if (buffer == null)
			return null;
		return buffer.toString();
	}

	/**
	 */
	private ICSSModel getCSSModel(IDOMNode text) {
		if (text == null)
			return null;
		INodeNotifier notifier = (INodeNotifier) text.getParentNode();
		if (notifier == null)
			return null;
		INodeAdapter adapter = notifier.getAdapterFor(IStyleSheetAdapter.class);
		if (adapter == null)
			return null;
		if (!(adapter instanceof IStyleSheetAdapter))
			return null;
		IStyleSheetAdapter styleAdapter = (IStyleSheetAdapter) adapter;
		return styleAdapter.getModel();
	}
}