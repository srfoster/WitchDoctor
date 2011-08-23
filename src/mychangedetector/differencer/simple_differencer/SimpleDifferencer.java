package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffRange;
import mychangedetector.differencer.Differencer;
import mychangedetector.editors.RefactoringEditor;
import mychangedetector.util.EclipseUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class SimpleDifferencer implements Differencer {
	
	String delimiter; 
	
	public SimpleDifferencer(){
		delimiter = "\\n";
	}

	@Override
	public List<Diff> perform(IFile left, IFile right) {
		String left_string = EclipseUtil.fileToString(left);
		String right_string = EclipseUtil.fileToString(right);
		
        List<String> original = stringToLines(left_string);
        List<String> revised  = stringToLines(right_string);
        
        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        Patch patch = DiffUtils.diff(original, revised);

        List<Diff> ret = new ArrayList<Diff>();
        
        for (Delta delta: patch.getDeltas()) {
    		SimpleDiff diff = new SimpleDiff(delta, original, revised, delimiter);
    		diff.setParent(this);    		
        	ret.add(diff);
        }

        return ret;
	}

	private List<String> stringToLines(String string){
		return new ArrayList<String>(Arrays.asList(string.split(delimiter)));
	}
	
}
