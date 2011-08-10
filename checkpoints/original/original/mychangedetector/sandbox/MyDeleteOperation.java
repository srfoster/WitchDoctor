package mychangedetector.sandbox;

import org.evolizer.changedistiller.treedifferencing.ITreeEditOperation;
import org.evolizer.changedistiller.treedifferencing.Node;


public class MyDeleteOperation implements ITreeEditOperation
{

    private Node fNodeToDelete;
    private Node fParent;
    private boolean fApplied;

    /**
     * Creates a new delete operation.
     * 
     * @param nodeToDelete
     *            the node to delete
     */
    public MyDeleteOperation(Node nodeToDelete) {
        fNodeToDelete = nodeToDelete;
        fParent = (Node) fNodeToDelete.getParent();
    }

    /**
     * {@inheritDoc}
     */
    public void apply() {
        if (!fApplied) {
            fNodeToDelete.removeFromParent();
            fApplied = true;
        }
    }

    /**
     * Returns the {@link Node} to delete.
     * 
     * @return the node to delete
     */
    public Node getNodeToDelete() {
        return fNodeToDelete;
    }

    /**
     * Returns the parent {@link Node} of the {@link Node} to delete.
     * 
     * @return the parent node of the node to delete
     */
    public Node getParentNode() {
        return fParent;
    }

    /**
     * {@inheritDoc}
     */
    public int getOperationType() {
        return ITreeEditOperation.DELETE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--Delete operation--\n");
        sb.append("Node to delete: ");
        sb.append(fNodeToDelete.toString() + " (" + fNodeToDelete.getLabel() + ")");
        return sb.toString();
    }

}
