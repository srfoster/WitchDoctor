package mychangedetector.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class ExtractMethodSpecificationAdapter extends SpecificationAdapter {
	
	protected Map<String,List<String>> remappings;
	protected Map<String,List<FreeVarQuery>> named_queries;

	public ExtractMethodSpecificationAdapter(Specification s) {
		super(s);
		
		//Be careful here.  Coding the strings here instead of using the
		//  API provided by the requirements could get messy.
		//  It looks okay now.  But if there are lots of strings floating
		//  around, it will open the door for pesky errors.
		
		
		/**
		 * Initialize remappings here.
		 */
		remappings = new TreeMap<String,List<String>>();
		
		String meth_decl = "method_declaration";
		List<String> meth_decl_mappings = new ArrayList<String>();
		meth_decl_mappings.add("method_declaration");
		meth_decl_mappings.add("added_statement_method_declaration");
		
		remappings.put(meth_decl,meth_decl_mappings);

				
		/**
		 * Initialize named_queries here.
		 */
		named_queries = new TreeMap<String,List<FreeVarQuery>>();
		
		String meth_name = "method_name";

		List<FreeVarQuery> meth_name_queries = new ArrayList<FreeVarQuery>();
		
		meth_name_queries.add(
				new FreeVarQuery()
				{
					public FreeVar execute(SpecificationAdapter spec)
					{
						FreeVar decl_var = spec.getProperty("method_declaration");
						
						if(decl_var == null || decl_var.isNotBound())
							return null;
						
						MethodDeclaration declaration = (MethodDeclaration) decl_var.binding();
						
						FreeVar result = new FreeVar("method_name");
						result.bind(declaration.getName());
						
						return result;
					}
				}
		);
		
		meth_name_queries.add(
				new FreeVarQuery()
				{
					public FreeVar execute(SpecificationAdapter spec)
					{
						FreeVar call_var = spec.getProperty("method_call");
						
						if(call_var == null || call_var.isNotBound())
							return null;
						
						MethodInvocation call = (MethodInvocation) call_var.binding();
						
						FreeVar result = new FreeVar("method_name");
						result.bind(call.getName());
						
						return result;
					}
				}
		);
		
		named_queries.put(meth_name,meth_name_queries);
		
		
		String class_name = "class_name";

		List<FreeVarQuery> class_name_queries = new ArrayList<FreeVarQuery>();
		
		class_name_queries.add(
				new FreeVarQuery()
				{
					public FreeVar execute(SpecificationAdapter spec)
					{
						FreeVar removed_statement_1 = spec.getProperty("removed_statement_1");
						
						FreeVar result = new FreeVar("class_name");
						
						CompilationUnit c = (CompilationUnit) removed_statement_1.binding().getRoot();
						
						SimpleName class_name = ((TypeDeclaration)c.types().get(0)).getName();
						
						result.bind(class_name);
						
						return result;
					}
				}
		);
		
		named_queries.put(class_name,class_name_queries);
	}
	
	
	public FreeVar getProperty(String s)
	{
		List<String> targets = remappings.get(s);
		
		if(targets != null)
		{
			for(String target : targets)
			{
				FreeVar result = super.getProperty(target);
				if(result == null)
					return null;
				
				if(result.isBound())
					return result;
			}
		}
		
		List<FreeVarQuery> queries = named_queries.get(s);
		
		if(queries != null)
		{
			for(FreeVarQuery query : queries)
			{
				FreeVar result = query.execute(this);
				
				if(result != null)
					return result;
			}
		}
		
		return super.getProperty(s);
	}
	
	private interface FreeVarQuery
	{
		public FreeVar execute(SpecificationAdapter spec);
	}

	public String getMethodName() {
		FreeVar meth_name_var = getProperty("method_name");
		if(meth_name_var == null)
			return null;
		return meth_name_var.binding().toString();
	}

}
