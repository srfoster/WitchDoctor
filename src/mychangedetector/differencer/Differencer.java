package mychangedetector.differencer;

import java.util.List;

import mychangedetector.builder.SuperResource;
import mychangedetector.differencer.change_distiller.ChangeDistillerDiff;

import org.eclipse.core.resources.IFile;

public interface Differencer {

	public List<Diff> perform(SuperResource left, SuperResource right);

}