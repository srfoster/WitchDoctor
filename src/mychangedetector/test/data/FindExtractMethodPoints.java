package mychangedetector.test.data;

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

	public void execute()
	{
		IFile file = getFile("compare/org/eclipse/compare/internal/ExceptionHandler.java");
		
		ICompilationUnit element = JavaCore.createCompilationUnitFrom(file);
	
		
		String full_text = null;
		try {
			full_text = EclipseUtil.convertStreamToString(file.getContents());
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<Block> bodies = getMethodBodies(full_text);
		
		List<ProgramRange> ranges = getProgramRanges(bodies);
		
		
		for(ProgramRange range : ranges)
		{
			range.checkRange(full_text, element);
		}
	}
	
	
	
	

	
	private IFile getFile(String name)
	{
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("org.eclipse.compare");
		
		IPath location = new Path(name);
		IFile file = project.getFile(location);
		
		return file;
	}
	
	
	private List<Block> getMethodBodies(String text){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(text.toCharArray());
		
		ASTNode tree = parser.createAST(null);
		
		TypeDeclaration type = (TypeDeclaration) ((CompilationUnit)tree).types().get(0);
		
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
			ProgramRange p = new ProgramRange();
			
			p.setStatements(block.statements());
			
			ret.add(p);
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
			String to_extract = text.substring(getOffset(), getOffset() + getLength());

			System.out.println(to_extract);
			
			ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(element, getOffset(), getLength());
			refactoring.setMethodName("new_method");
			
			RefactoringStatus status = null;
			try {
				status = refactoring.checkInitialConditions(new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(status.toString());
			
			Change change = null;
			try {
				change = refactoring.createChange(new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			change.initializeValidationData(new NullProgressMonitor());
			
			for(ProgramRange child : child_ranges)
			{
				child.checkRange(text, element);
			}
		}
		
	}
}
