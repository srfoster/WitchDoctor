package mychangedetector.test.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mychangedetector.ast_helpers.ASTNodeDescriptor;
import mychangedetector.matching.MatchingASTVisitor;
import mychangedetector.matching.MyASTMatcher;
import mychangedetector.util.EclipseUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class FindExtractMethodPoints {
	
	Connection conn;
	
	int batch_size = 1;
	int total_batches = 1932;
	
	int data_set_id = 3;
	//String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/org.eclipse.compare/plugins/org.eclipse.compare/";
	//String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/org.eclipse.jface/";
	String prefix = "/Users/stephenfoster/Desktop/Research/javaProjects/current/struts2/";

	//String project_name = "org.eclipse.compare";
	//String project_name = "org.eclipse.jface";
	String project_name = "struts";
	
	int current_file_id = -1;
	int refactoring_id = 1;
	
	java.sql.Statement stat = null;
	
	List<PendingUpdate> updates;

	public void execute()
	{
		
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try{
		    conn = DriverManager.getConnection("jdbc:sqlite:/Users/stephenfoster/Documents/workspace/MyChangeDetectorNew/data/data.db");
		    stat = conn.createStatement();
		    
		    for(int i = 0; i < total_batches; i++)
		    {
		    	updates = new ArrayList<PendingUpdate>();

				System.out.println("\nBatch #" + (i + 1));

				ResultSet results = stat.executeQuery("select * from files where data_set_id = "+ data_set_id + " limit " + batch_size + " offset " + (batch_size * i));
	
				while(results.next())
				{
					String full_name = results.getString("name");
					String short_name = full_name.replace(prefix,"");
					System.out.println(short_name);
					
					if(!short_name.endsWith(".java"))
						continue;
					
					
					current_file_id = results.getInt("id");
					
					//System.out.println(results.getString("contents"));
					findBlocksInFile(short_name);
	
				}
				
				for(PendingUpdate p : updates)
				{
					
					try {
						String query = "insert into ranges (file_id, refactoring_id, offset, length, snippet) values ("+p.file_id+","+refactoring_id+","+p.offset+","+p.length+", ?)";
						
						PreparedStatement prep = conn.prepareStatement(query);
						prep.setString(1, p.snippet);
						prep.addBatch();
						prep.executeBatch();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		    }
			
		}catch(SQLException e) {
			
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
	private void findBlocksInFile(String file_name)
	{
		IFile file = getFile(file_name);
		
		ICompilationUnit element = JavaCore.createCompilationUnitFrom(file);
	
		
		String full_text = null;
		try {
			full_text = EclipseUtil.convertStreamToString(file.getContents());
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<Block> bodies = getMethodBodies(full_text);
		
		if(bodies.size() == 0)
		{
			return;
		}
		
		List<ProgramRange> ranges = getProgramRanges(bodies);
		
		
		for(ProgramRange range : ranges)
		{
			if(!range.isEmpty())
				range.checkRange(full_text, element);
		}
	}

	
	private IFile getFile(String name)
	{
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject(project_name);
		
		IPath location = new Path(name);
		IFile file = project.getFile(location);
		
		return file;
	}
	
	
	private List<Block> getMethodBodies(String text){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(text.toCharArray());
		
		ASTNode tree = parser.createAST(null);
		
		List types = ((CompilationUnit)tree).types();
		
		if(types.size() == 0 || !(types.get(0) instanceof TypeDeclaration))
			return new ArrayList<Block>();
		
		TypeDeclaration type = (TypeDeclaration) types.get(0);
		
		MethodDeclaration[] methods = type.getMethods();
		
		List<Block> bodies = new ArrayList<Block>();
		
		for(MethodDeclaration m : methods)
		{
			bodies.add(m.getBody());
		}
		
		return bodies;
	}
	
	private List<ProgramRange> getProgramRanges(List<Block> blocks)
	{
		
		List<ProgramRange> ret = new ArrayList<ProgramRange>();
		
		for(Block block : blocks)
		{
			if(block != null)
			{
				ProgramRange p = new ProgramRange();
				
				
				p.setStatements(block.statements());
				
				ret.add(p);
			}
		}
		
		return ret;
	}
	
	private class ProgramRange  // Represents a block
	{
		private List<Statement> statements;
		
		private List<ProgramRange> child_ranges = new ArrayList<ProgramRange>();
		
		public void setStatements(List<Statement> statements)
		{
			this.statements = statements;
			
			
			for(Statement s : statements)
			{
				final MyASTMatcher matcher = new MyASTMatcher();
				
				final List<Block> child_blocks = new ArrayList<Block>();

				final ASTNodeDescriptor descriptor = new ASTNodeDescriptor(){
					public void onMatch(String key, ASTNode node)
					{
						matcher.setProperty("block",null); // So we keep searching if we find one.
						child_blocks.add((Block)node);
					}
				};
				descriptor.setClassName("org.eclipse.jdt.core.dom.Block");
				descriptor.setBindingName("block");

				
				s.accept(new MatchingASTVisitor(matcher, descriptor));
				
				
				for(Block b : child_blocks)
				{
					ProgramRange new_range = new ProgramRange();
					new_range.setStatements(b.statements());
					child_ranges.add(new_range);
				}
			}
			
		}
		
		public int getOffset()
		{
			return statements.get(0).getStartPosition();
		}
		
		public int getLength()
		{
			Statement last = statements.get(statements.size() - 1);
			int end_position = last.getStartPosition() + last.getLength();
			
			return end_position - getOffset();
		}
		
		public void checkRange(String text, ICompilationUnit element)
		{
			if(statements.size() == 0)
				return;
			
			int offset = getOffset();
			int length = getLength();
			int end    = getOffset() + length;
			String to_extract = text.substring(getOffset(), getOffset() + getLength());

			//System.out.println(to_extract);
			
			ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(element, getOffset(), getLength());
			refactoring.setMethodName("new_method");
			
			RefactoringStatus status = null;
			try {
				status = refactoring.checkInitialConditions(new NullProgressMonitor());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			
			if(status.hasError())
			{
				//System.out.println(status.toString());

				return;
			}
			
			
			//System.out.println(status.toString());
			
			System.out.print(".");
						
			PendingUpdate p = new PendingUpdate();
			p.file_id = current_file_id;
			p.offset  = offset;
			p.length  = length;
			p.snippet = text.substring(offset,offset+length);
			
			updates.add(p);

			
			Change change = null;
			try {
				change = refactoring.createChange(new NullProgressMonitor());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			change.initializeValidationData(new NullProgressMonitor());
			
			
			
			for(ProgramRange child : child_ranges)
			{
				child.checkRange(text, element);
			}
		}
		
		public boolean isEmpty()
		{
			return statements.size() == 0;
		}
		
	}
	
	private class PendingUpdate
	{
		public int file_id;
		public int offset;
		public int length;
		public String snippet;
	}
}
