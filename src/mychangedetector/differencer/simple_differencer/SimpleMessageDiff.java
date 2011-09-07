package mychangedetector.differencer.simple_differencer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;

import difflib.Delta;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.Differencer;

public class SimpleMessageDiff extends SimpleDiff {
	
	public SimpleMessageDiff(Delta delta, List<String> original,
			List<String> revised, String delimiter) {
		super(delta, original, revised, delimiter);
	}

	public DiffEntity originalToEntity() {
		DiffEntity entity = new SimpleMessageDiffEntity(delta.getOriginal());
		entity.setParent(parent);
		
		return entity;
	}

	public DiffEntity revisedToEntity() {
		DiffEntity entity = new SimpleMessageDiffEntity(delta.getRevised());
		entity.setParent(parent);
		
		return entity;
	}

}
