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
		
		
		
		//I'm going to try using the edit script to (re)create the full_tree_after from before.
		//This is so that we can maintain the matching between the before tree nodes and the after tree nodes.
		//I could alternatively use the matching functionality provided (but annoyingly not exported) by the changedistiller package,
		// but I'm going to need to write code for executing a change script at some point anyway.  So this helps kill two birds with one stone.
		
		
		
		//ASTNode full_tree_before_copy = full_tree_before.subtreeCopy(); // Will (hopefully) become identical to full_tree_after once we apply the changes.
		
		ASTNode full_tree_before_copy = ASTNode.copySubtree(full_tree_before.getAST(),full_tree_before);
		
		//We now establish the matching that we hope to maintain as we apply the changes.
	
		
		/*
		ZippingASTVisitor zipper = new ZippingASTVisitor(full_tree_before_copy);
		full_tree_before.accept(zipper);
		
		for(Object pair_obj : zipper)
		{
			 List<ASTNode> pair = (List<ASTNode>) pair_obj;
			 
			 ASTNode first = pair.get(0);
			 ASTNode second = pair.get(1);
			 
			 second.setProperty("forward_link", first);
			 first.setProperty("backward_link", second);
		}
		*/
		

		/***
		 * 
		 * This for-loop is my attempt to create "traceability" between the AST nodes of each
		 * program instance.  That is, if we think of a program as a sequence of instances --
		 * or a sequence of ASTs -- then it would be useful to have a "matching" between each
		 * adjacent tree.  I didn't have luck extracting Change Distiller's implementation of
		 * Chawathe's tree matching algorithm.  And the loop below represents my equally failed
		 * attempt to obtain the matching by executing the edit "script" that Change Distiller
		 * gives us.
		 * 
		 * The final problem appears to be that I can't figure out how to sort the list in such
		 * a way that the changes reliably output the correct final tree when executed.  The 
		 * order of the changes in an edit script is important -- and for reasons that are 
		 * unknown to me, Change Distiller does not maintain this ordering.
		 * 
		 * So I'm going to punt on this problem for now.
		 * 
		 */
		
		
		/**
		for(SourceCodeChange edit : list)
		{
			if(edit instanceof Delete)
			{
				SourceCodeEntity to_modify = ((Delete)edit).getChangedEntity();
				ASTNode to_delete = getASTFromEntityAndTree(to_modify,full_tree_before).getParent();
				
				ASTNode future_self = (ASTNode) to_delete.getProperty("forward_link");
				future_self.delete();
				to_delete.setProperty("forward_link",null);
			} else if (edit instanceof Insert) {
				
				SourceCodeEntity to_modify = ((Insert)edit).getParentEntity();
				ASTNode to_insert_into = (ASTNode) getASTFromEntityAndTree(to_modify,full_tree_before).getProperty("forward_link");
				
				//Problem 1: How to get the position to move the node to?  (Not exposed by API)  OMG, so stupid.
				
				
				SourceCodeEntity node_to_insert = ((Insert)edit).getChangedEntity();
				ASTNode to_insert = getASTFromEntityAndTree(node_to_insert,full_tree_after);
				to_insert = ASTNodeJoiner.parentThatShouldBeRemoved(to_insert);
				
				StructuralPropertyDescriptor location = to_insert.getLocationInParent();
				List<ASTNode> to_insert_siblings = (List<ASTNode>) to_insert.getParent().getStructuralProperty(location);
				int position_to_insert = to_insert_siblings.indexOf(to_insert);
				
				//We'll insert a copy to avoid changing full_tree_after, which we need to use to check our work.
				ASTNode to_insert_copy = ASTNode.copySubtree(to_insert_into.getAST(),to_insert);

				
				//Follow to_insert_into's forward_link, insert copy into to the appropriate position.
				//.... Problem 3: How to insert at a particular position in the AST?
				//                Probably need to use ASTJoiner.  Modify addChild to take an index.

				ASTNodeJoiner joiner = ASTNodeJoiner.joinerFor(to_insert_into);
				joiner.addChildAt(position_to_insert, to_insert_copy);
				
				
			} else if (edit instanceof Move) {

				SourceCodeEntity to_modify = ((Move)edit).getParentEntity();
				ASTNode to_move_into = (ASTNode) getASTFromEntityAndTree(to_modify,full_tree_before).getProperty("forward_link");
				
				SourceCodeEntity node_to_move = ((Move)edit).getChangedEntity();
				ASTNode to_move = (ASTNode) getASTFromEntityAndTree(node_to_move,full_tree_before).getProperty("forward_link");

				//We try to unparent this node, otherwise, we move up the tree until the unparenting works.
				//We return the topmost node in this chain.  Presumably, this is the node we actually want to move (because it's the first node in the chain we actually CAN move).
				//  For example, if to_move was an Assignment, the following will return the unparented ExpressionStatement which is an ancestor of the Assignment
				to_move = ASTNodeJoiner.unparent(to_move); 

				
				//Problem 1: How to get the position to move the node to?  (Not exposed by API)  OMG, so stupid.

				SourceCodeEntity after_move_ent = ((Move)edit).getNewEntity();
				ASTNode after_move = (ASTNode) getASTFromEntityAndTree(after_move_ent,full_tree_after);
				after_move = ASTNodeJoiner.parentThatShouldBeRemoved(after_move);
				
				StructuralPropertyDescriptor location = after_move.getLocationInParent();
				List<ASTNode> after_move_siblings = (List<ASTNode>) after_move.getParent().getStructuralProperty(location);
				int position_to_move_to = after_move_siblings.indexOf(after_move);
								
				ASTNodeJoiner joiner = ASTNodeJoiner.joinerFor(to_move_into);
				joiner.addChildAt(position_to_move_to, to_move);
				
							
			} else if (edit instanceof Update) {
				
				//Use the before doc to find the ASTNode to update (to replace).  Follow forward_link.
				
				SourceCodeEntity to_modify = ((Update)edit).getChangedEntity();
				ASTNode to_update = (ASTNode) getASTFromEntityAndTree(to_modify,full_tree_before).getProperty("forward_link");
				to_update = ASTNodeJoiner.parentThatShouldBeRemoved(to_update);
				
				
				//Find the parent of the node to update (for the later insertion).

				ASTNode parent_of_update = ASTNodeJoiner.parentThisCouldBeRemovedFrom(to_update);
				
				//Use the after doc to find the ASTNode that will replace the other one
				
				SourceCodeEntity node_to_update_to = ((Update)edit).getNewEntity();
				ASTNode replacement_final = getASTFromEntityAndTree(node_to_update_to,full_tree_after);
				replacement_final = ASTNodeJoiner.parentThatShouldBeRemoved(replacement_final);
								
				//Make a copy of the replacement node.
				
				ASTNode replacement = ASTNode.copySubtree(to_update.getAST(),replacement_final);
				
				//Do the replacement.
									
				ASTNodeJoiner joiner = ASTNodeJoiner.joinerFor(parent_of_update);
				joiner.replaceChildWith(to_update,replacement);
								
				//Fix the forward_link and backward_link
				
				replacement.setProperty("backward_link",(ASTNode) to_update.getProperty("backward_link"));
				((ASTNode)replacement.getProperty("backward_link")).setProperty("forward_link",replacement);
				
			} else {
				throw new RuntimeException("How'd we get here???");
			}
			

			
		}
		**/
		
		for(Diff change : list){
			ChangeWrapper wrapper = new ChangeWrapper(change,full_tree_before, full_tree_after);
			
			ASTNode main_node = getASTNode(change);
			wrapper.addASTNode(main_node);
			
			if(change.isUpdate())
			{
				ASTNode updated = getUpdatedASTNode(change);
				wrapper.addASTNode(updated);
			}
			
			ASTNode source_node = getSourceNode(change);
						
			wrappers.add(wrapper);
		}
		return wrappers;
	}


	private ASTNode getASTFromEntityAndTree(DiffEntity entity, ASTNode tree)
	{
		
		DiffRange range = entity.getSourceRange();
		int offset = range.getOffset();
		int length = range.getLength();
		final int start = offset; 
					
		NodeFinder nf = new NodeFinder(tree,start,length);
		
		ASTNode ast_node_to_modify = nf.getCoveringNode();
		return ast_node_to_modify;
	}

	
	public ASTNode getSourceNode(Diff change)
	{
		DiffEntity entity = null;
		if(change.isUpdate())
			entity = change.getChangedEntity();
		else
			entity = change.getParentEntity();
		String file = before.getContents();
		
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
