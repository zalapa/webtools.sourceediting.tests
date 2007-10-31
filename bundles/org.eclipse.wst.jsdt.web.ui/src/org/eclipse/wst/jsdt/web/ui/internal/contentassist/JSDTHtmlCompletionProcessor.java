package org.eclipse.wst.jsdt.web.ui.internal.contentassist;
import java.util.ArrayList;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.web.core.internal.java.IJsTranslation;
import org.eclipse.wst.jsdt.web.core.internal.java.JsTranslation;
import org.eclipse.wst.jsdt.web.core.internal.java.JsTranslationAdapter;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

/**
 * 
 */
/**
 * @author childsb
 *
 */
public class JSDTHtmlCompletionProcessor {
	
	public JSDTHtmlCompletionProcessor() {}
	
	
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		/* add </script if necisary */
		ArrayList allProposals = new ArrayList();
		JsTranslation tran = getJSPTranslation(viewer);
		int missingAtOffset = tran.getMissingTagStart();
		
		return (ICompletionProposal[])allProposals.toArray(new ICompletionProposal[allProposals.size()]);
	}
	
	public ICompletionProposal getEndScriptProposal(ITextViewer viewer, int offset) {
		/* add </script if necisary */
	
		JsTranslation tran = getJSPTranslation(viewer);
		int missingAtOffset = tran.getMissingTagStart();
		
		if(offset>=missingAtOffset&& missingAtOffset>-1) {
			
			String allText = viewer.getDocument().get();
			String text = "</script>";
			
			int startInTag = -1;
			int replaceLength =0;
			
			for(int i=0;i<text.length() && allText.length()>offset-1;i++) {
				if(allText.charAt(offset-1)==text.charAt(i)) {
					startInTag = i;
					break;
				}
			}
			
			if(startInTag==-1 ) {
				String displayText = "<> end with </script>";
				return new CustomCompletionProposal("\n" + text + "\n" ,offset,0,offset,null,displayText,null,"Close the script tag.",100);
			}
			
			String text1 = allText.substring(offset - startInTag - 1, offset).toLowerCase();
			String text2 = text.substring(0, startInTag+1).toLowerCase();
			if(startInTag>-1  && text2.compareTo(text1)==0 ) {
				String displayText = "<> end with </script>";
				return new CustomCompletionProposal(text  ,offset-startInTag-1,0,text.length(),null,displayText,null,"Close the script tag.",100);
			}
			
		}
		
		return null;
	}
	private JsTranslation getJSPTranslation(ITextViewer viewer) {
		IDOMModel xmlModel = null;
		try {
			xmlModel = (IDOMModel) StructuredModelManager.getModelManager().getExistingModelForRead(viewer.getDocument());
			IDOMDocument xmlDoc = xmlModel.getDocument();
			
			JsTranslationAdapter fTranslationAdapter = (JsTranslationAdapter) xmlDoc.getAdapterFor(IJsTranslation.class);
			
			if (fTranslationAdapter != null) {
				return fTranslationAdapter.getJSPTranslation(true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (xmlModel != null) {
				xmlModel.releaseFromRead();
			}
		}
		return null;
	}
	
}