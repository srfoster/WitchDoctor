package mychangedetector.sandbox;

import org.evolizer.changedistiller.treedifferencing.ITreeEditOperation;
import org.evolizer.changedistiller.treedifferencing.Node;
import org.evolizer.changedistiller.treedifferencing.operation.InsertOperation;

public class MyInsertOperation implements ITreeEditOperation {

    private static final String LEFT_PARENTHESIS = " (";
    private static final String RIGHT_PARENTHESIS = ")";
    private Node fNodeToInsert;
    private Node fParent;
    private int fPosition = -1;
    private boolean fApplied;

    /**
     * Creates a new insert operation.
     * 
     * @param nodeToInsert
     *            the node to insert
     * @param parent
     *            the parent in which the node is inserted
     * @param position
     *            the position of the node to insert
     */
    public MyInsertOperation(Node nodeToInsert, Node parent, int position) {
        fNodeToInsert = nodeToInsert;
        fParent = parent;
        fPosition = position;
    }

    /**
     * {@inheritDoc}
     */
    public void apply() {
        if (!fApplied) {
            fParent.insert(fNodeToInsert, fPosition);
            fApplied = true;
        }
    }

    /**
     * Returns the {@link Node} to insert.
     * 
     * @return the node to insert
     */
    public Node getNodeToInsert() {
        return fNodeToInsert;
    }

    /**
     * Returns the parent {@link Node} of the {@link Node} to insert.
     * 
     * @return the parent node of the node to insert
     */
    public Node getParentNode() {
        return fParent;
    }

    /**
     * {@inheritDoc}
     */
    public int getOperationType() {
        return ITreeEditOperation.INSERT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--Insert operation--\n");
        sb.append("Node value to insert: ");
        sb.append(fNodeToInsert.toString() + LEFT_PARENTHESIS + fNodeToInsert.getLabel() + RIGHT_PARENTHESIS);
        sb.append("\nas child of: ");
        sb.append(fParent.toString() + LEFT_PARENTHESIS + fParent.getLabel() + RIGHT_PARENTHESIS);
        sb.append("\nat position: ");
        sb.append(fPosition);
        return sb.toString();
    }
}
