package mychangedetector.differencer;

import java.util.List;

import mychangedetector.differencer.change_distiller.ChangeDistillerDiff;

import org.eclipse.core.resources.IFile;

public interface Differencer {

	public List<Diff> perform(IFile left, IFile right);

}