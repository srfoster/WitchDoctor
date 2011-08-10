package mychangedetector.specifications;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class SpecificationAdapter implements Cloneable {
	protected Specification specification;


	public FreeVar getProperty(String s) {
		return specification.getProperty(s);
	}

	public List<FreeVar> getPropertiesByRegex(String s) {
		return specification.getPropertiesByRegex(s);
	}

	public SpecificationAdapter(Specification s)
	{
		specification = s;
	}
	
	public void setSpecification(Specification s)
	{
		specification = s;
	}
	
	public SpecificationAdapter copy(Specification spec_copy)
	{
		SpecificationAdapter copy = null;
		try {
			copy = (SpecificationAdapter) this.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copy.setSpecification(spec_copy);
		return copy;
	}

	//This will get a bit dicey when it comes to multi-file refactorings.
	//  For now we'll just return the top-level type of the first non-null property binding.
	public String getCheckpointName() {
		for(FreeVar current : specification.getBindings())
		{
			if(current.isBound())
			{
				return getTopLevelType(current.binding());
			}
		}
		return null;
	}

	private String getTopLevelType(ASTNode node)
	{
		CompilationUnit root = (CompilationUnit) node.getRoot();
		
		List types = root.types();
		
		SimpleName type_name = (SimpleName) ((TypeDeclaration)types.get(0)).getStructuralProperty(TypeDeclaration.NAME_PROPERTY);
		PackageDeclaration package_declaration = (PackageDeclaration) root.getStructuralProperty(CompilationUnit.PACKAGE_PROPERTY);
		
		QualifiedName package_name = (QualifiedName) package_declaration.getStructuralProperty(PackageDeclaration.NAME_PROPERTY);
		String class_name_prefix = package_name.toString().replace(".","/");
		
		
		
		return class_name_prefix + "/" + type_name.toString();
	}
}
