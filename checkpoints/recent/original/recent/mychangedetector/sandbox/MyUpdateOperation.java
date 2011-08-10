package mychangedetector.sandbox;

import org.evolizer.changedistiller.treedifferencing.ITreeEditOperation;
import org.evolizer.changedistiller.treedifferencing.Node;

public class MyUpdateOperation implements ITreeEditOperation {

    private Node fNodeToUpdate;
    private String fValue;
    private String fOldValue;
    private boolean fApplied;
    private Node fNewNode;

    /**
     * Creates a new update operation.
     * 
     * @param nodeToUpdate
     *            the node to update
     * @param newNode
     *            the node the updated node becomes
     * @param value
     *            the new value of the node to be updated
     */
    public MyUpdateOperation(Node nodeToUpdate, Node newNode, String value) {
        fNodeToUpdate = nodeToUpdate;
        fNewNode = newNode;
        fOldValue = fNodeToUpdate.getValue();
       // fNodeToUpdate.getEntity().setUniqueName(value);
        fValue = value;
    }

    /**
     * {@inheritDoc}
     */
    public void apply() {
        if (!fApplied) {
            fNodeToUpdate.setValue(fValue);
            fApplied = true;
        }
    }

    /**
     * Returns the old value of the {@link Node} before it was updated.
     * 
     * @return the old value of the node
     */
    public String getOldValue() {
        return fOldValue;
    }

    /**
     * Returns the {@link Node} to update.
     * 
     * @return the node to update
     */
    public Node getNodeToUpdate() {
        return fNodeToUpdate;
    }

    /**
     * {@inheritDoc}
     */
    public int getOperationType() {
        return ITreeEditOperation.UPDATE;
    }

    /**
     * Returns the updated {@link Node}.
     * 
     * @return the updated node
     */
    public Node getNewNode() {
        return fNewNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--Update operation--\n");
        sb.append("Node value to update: ");
        sb.append(fOldValue);
        sb.append("\nwith value: ");
        sb.append(fValue);
        return sb.toString();
    }
}
