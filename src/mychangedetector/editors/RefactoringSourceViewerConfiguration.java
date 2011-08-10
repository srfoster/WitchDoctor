package mychangedetector.editors;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.texteditor.ITextEditor;

public class RefactoringSourceViewerConfiguration extends JavaSourceViewerConfiguration {

	RuleBasedScanner codeScanner;
	
	public RefactoringSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {

		super(colorManager, preferenceStore, editor, partitioning);
	
		codeScanner = new MyJavaCodeScanner(getColorManager(), fPreferenceStore);
	}


	
	@Override
	protected RuleBasedScanner getCodeScanner(){
		return codeScanner;
	}

}