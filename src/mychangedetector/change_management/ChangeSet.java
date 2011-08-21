package mychangedetector.change_management;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mychangedetector.ast_helpers.MyASTVisitor;
import mychangedetector.builder.FileVersion;
import mychangedetector.differencer.Diff;
import mychangedetector.differencer.DiffEntity;
import mychangedetector.differencer.DiffRange;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.NodeFinder;


public class ChangeSet implements Set {
	
	List<Diff> list = new ArrayList<Diff>();

	FileVersion before;
	FileVersion after;
	
	
	public void setBeforeFile(FileVersion before)
	{
		this.before = before;
	}
	
	public void setAfterFile(FileVersion after)
	{
		this.after = after;
	}

	public List<ChangeWrapper> getChanges()
	{
	 	ArrayList<ChangeWrapper> wrappers = new ArrayList<ChangeWrapper>();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(after.getContents().toCharArray());
		
		ASTNode full_tree_after = parser.createAST(null);
		
		ASTParser parser2 = ASTParser.newParser(AST.JLS3);
		parser2.setSource(before.getContents().toCharArray());
		ASTNode full_tree_before = parser2.createAST(null);
		
		

		
		for(Diff change : list){
			ChangeWrapper wrapper = new ChangeWrapper(change,full_tree_before, full_tree_after);
			
			ASTNode main_node = getASTNode(change);
			wrapper.addASTNode(main_node);
			
			if(change.isUpdate())
			{
				ASTNode updated = getUpdatedASTNode(change);
				wrapper.addASTNode(updated);
			}
									
			wrappers.add(wrapper);
		}
		return wrappers;
	}
	
	public ASTNode getUpdatedASTNode(Diff change)
	{
		DiffEntity entity = change.getChangedEntity();
		String file = after.getContents();
		
		DiffRange range = entity.getSourceRange();
		int offset = range.getOffset();
		int length = range.getLength();
		int start = offset; 
		int end   = length + offset;
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(file.toCharArray());
		
		
		ASTNode node = parser.createAST(null);
		
		MyASTVisitor visitor = new MyASTVisitor(start,end,0);
        node.accept(visitor);
		
        return visitor.getTopLevelNode();
	}
	
	//Wowzers.  TODO: Lots of duplicated code between getASTNode() and print().  Clean up.
	
	public ASTNode getASTNode(Diff change)
	{
		DiffEntity entity = null;
    	
    	if(change.isUpdate())
    		entity = change.getNewEntity();
    	else
    		entity = change.getChangedEntity();
    	
    	String file = null;
    	if(change.isInsert())
    	{
    		file = after.getContents();
    	} else {
    		file = before.getContents();
    	}
		
		DiffRange range = entity.getSourceRange();
		int offset = range.getOffset();
		int length = range.getLength();
		int start = offset; 
		int end   = length + offset;
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(file.toCharArray());
		
		
		ASTNode node = parser.createAST(null);
		
		MyASTVisitor visitor = new MyASTVisitor(start,end,0);
        node.accept(visitor);
		
        return visitor.getTopLevelNode();
	}
	
	public ASTNode getNodeFromRange(DiffRange range, ASTNode root)
	{
		int offset = range.getOffset();
		int length = range.getLength();
		int start = offset; 
		int end   = length + offset;
		
		
		MyASTVisitor visitor = new MyASTVisitor(start,end,0);
        root.accept(visitor);
		
        return visitor.getTopLevelNode();
	}
	
	public void print()
	{
		
		for(Diff change : list)
		{
			String change_type = change.getChangeType();
        	
        	if(change != null)
        	{
	        	DiffEntity entity = null;
	        	
	        	if(change.isUpdate())
	        		entity = change.getNewEntity();
	        	else
	        		entity = change.getChangedEntity();
	        	
	        	String file = null;
	        	if(change.isDelete())
	        	{
	        		file = before.getContents();
	        	} else {
	        		file = after.getContents();
	        	}
        		
        		DiffRange range = entity.getSourceRange();
        		int offset = range.getOffset();
        		int length = range.getLength();
        		int start = offset; 
        		int end   = length + offset;
        		
        		String value = file.substring(start, end);
        		
        		ASTParser parser = ASTParser.newParser(AST.JLS3);
        		parser.setSource(file.toCharArray());
        		
        		
        		ASTNode node = parser.createAST(null);
        		
        		
        		
        		String[] class_name_array = change.getClass().getName().split("\\.");
        		String short_name = class_name_array[class_name_array.length - 1];
        		
        		System.out.print("(" + start + ", " + end + ") ");
        		System.out.print(short_name + " ");
	            System.out.println(change_type);
	            
	            System.out.println("   " + entity.getLabel());
	            System.out.println("   " + value);

	            
	            MyASTVisitor visitor = new MyASTVisitor(start,end,0);
	            node.accept(visitor);
	            
	            System.out.println("   AST:");
	            System.out.println(visitor.getMessage());
        	}
		}
	}
	
	public String fileSubstring(IFile file, int start, int end)
	{
		InputStream contents = null;
		try {
			contents = file.getContents();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String contents_string = "";
		try {
			contents_string = convertStreamToString(contents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return contents_string.substring(start,end);
		 
	}
	
	public String convertStreamToString(InputStream is) throws IOException {
		/*
		* To convert the InputStream to String we use the
		* Reader.read(char[] buffer) method. We iterate until the
		* Reader return -1 which means there's no more data to
		* read. We use the StringWriter class to produce the string.
		*/
		if (is != null) {
			Writer writer = new StringWriter();
			 
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {        
			return "";
		}
	}


	

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(Object e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection c) {
		return list.addAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return list.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection c) {
		return list.removeAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

}
