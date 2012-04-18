package mychangedetector.test;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;

public class TextAdapter {
	StyledText text;
	IDocument document;
	
	public TextAdapter(StyledText text, IDocument document)
	{
		this.text = text;
		this.document = document;
	}
	
	public StyledText getText()
	{
		return text;
	}
	
	public IDocument getDocument(){
		return document;
	}
}
