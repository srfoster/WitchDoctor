package mychangedetector.differencer;

import java.util.List;

import mychangedetector.builder.SuperResource;

public interface Differencer {
	public List<Diff> perform(SuperResource left, SuperResource right);
}