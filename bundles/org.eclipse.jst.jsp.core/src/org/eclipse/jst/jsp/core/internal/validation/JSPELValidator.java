package org.eclipse.jst.jsp.core.internal.validation;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.jsp.core.internal.JSPCoreMessages;
import org.eclipse.jst.jsp.core.internal.domdocument.DOMModelForJSP;
import org.eclipse.jst.jsp.core.internal.java.jspel.JSPELParser;
import org.eclipse.jst.jsp.core.internal.java.jspel.ParseException;
import org.eclipse.jst.jsp.core.internal.java.jspel.Token;
import org.eclipse.jst.jsp.core.internal.java.jspel.TokenMgrError;
import org.eclipse.jst.jsp.core.internal.regions.DOMJSPRegionContexts;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;
import org.eclipse.wst.validation.internal.core.Message;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;


public class JSPELValidator implements org.eclipse.wst.validation.internal.provisional.core.IValidator {
	
	static boolean shouldValidate(IFile file) {
		IResource resource = file;
		do {
			if (resource.isDerived() || resource.isTeamPrivateMember() || !resource.isAccessible() || resource.getName().charAt(0) == '.') {
				return false;
			}
			resource = resource.getParent();
		}
		while ((resource.getType() & IResource.PROJECT) == 0);
		return true;
	}

	protected void validateRegionContainer(ITextRegionCollection container, IReporter reporter, IFile file) {
		
		ITextRegionCollection containerRegion = container;
		Iterator regions = containerRegion.getRegions().iterator();
		ITextRegion region = null;
		while (regions.hasNext() && !reporter.isCancelled()) {
			region = (ITextRegion) regions.next();
			String type = region.getType();
			if (type != null && region instanceof ITextRegionCollection) {
				ITextRegionCollection parentRegion = ((ITextRegionCollection)region);				
				Iterator childRegions = parentRegion.getRegions().iterator();
				while(childRegions.hasNext() && !reporter.isCancelled()) {
					ITextRegion childRegion = (ITextRegion)childRegions.next();
					if(childRegion.getType() == DOMJSPRegionContexts.JSP_EL_CONTENT)
						validateXMLNode(parentRegion, childRegion, reporter, file);
				}
			}
		}
	}
			
	protected void validateXMLNode(ITextRegionCollection container, ITextRegion region, IReporter reporter, IFile file) {
		String elText = container.getText(region);
		JSPELParser elParser = JSPELParser.createParser(elText);
		int contentStart = container.getStartOffset(region);
		int contentLength = container.getLength();
		try {
			elParser.Expression();
		} catch (ParseException e) {
			Token curTok = e.currentToken;
			int problemStartOffset =  contentStart + curTok.beginColumn;
			Message message = new LocalizedMessage(IMessage.LOW_SEVERITY, JSPCoreMessages.JSPEL_Syntax);
			message.setOffset(problemStartOffset);
			message.setLength(curTok.endColumn - curTok.beginColumn + 1);
			message.setTargetObject(file);
			reporter.addMessage(this, message);
		} catch (TokenMgrError te) {
			Message message = new LocalizedMessage(IMessage.LOW_SEVERITY, JSPCoreMessages.JSPEL_Token);
			message.setOffset(contentStart);
			message.setLength(contentLength);
			message.setTargetObject(file);
			reporter.addMessage(this, message);
		}
	}

	public void cleanup(org.eclipse.wst.validation.internal.provisional.core.IReporter reporter) {
		// TODO Auto-generated method stub
		
	}

	public void validate(IValidationContext helper, IReporter reporter) throws ValidationException {
		String[] uris = helper.getURIs();
		reporter.removeAllMessages(this);
		// IValidationContext may return a null array
		if (uris != null) {
			for (int i = 0; i < uris.length && !reporter.isCancelled(); i++) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uris[i]));
				if(!shouldValidate(file)) {
					continue;
				}
				IStructuredModel model = null;
				try {
					model = StructuredModelManager.getModelManager().getModelForRead(file);
					DOMModelForJSP jspModel = (DOMModelForJSP) model;
					IStructuredDocument structuredDoc = jspModel.getStructuredDocument();
					IStructuredDocumentRegion curNode = structuredDoc.getFirstStructuredDocumentRegion();
					while (null != (curNode = curNode.getNext())  && !reporter.isCancelled()) {
						if (curNode.getType() != DOMRegionContext.XML_COMMENT_TEXT && curNode.getType() != DOMRegionContext.XML_CDATA_TEXT && curNode.getType() != DOMRegionContext.UNDEFINED) {
							validateRegionContainer(curNode, reporter, file);
						}
					}
				}
				catch (Exception e) {
				}
				finally {
					if (null != model)
						model.releaseFromRead();
				}
			}
		}
	}
}
