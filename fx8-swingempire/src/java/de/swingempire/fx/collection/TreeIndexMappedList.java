/*
 * Created on 18.11.2014
 *
 */
package de.swingempire.fx.collection;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.transformation.TransformationList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;

import com.sun.javafx.collections.SortHelper;

import de.swingempire.fx.scene.control.tree.TreeItemX;
import de.swingempire.fx.scene.control.tree.TreeModificationEventX;

/**
 * Helper for selectedItems in tree-based controls. 
 * Source contains the selectedIndices, "backingList" is the tree with its visible
 * treeItems.
 * <p>
 * 
 * Similar to IndexMappedList, we are _not_ listening directly to tree modifications,
 * but to changes in the sourceChanged property of the treeIndicesList. Get them
 * when the treeIndicesList has updated itself to the modification but not yet 
 * broadcasted. 
 * 
 * @see TreeIndicesList
 * @see IndexMappedList
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeIndexMappedList<T> extends TransformationList<TreeItem<T>, Integer> {

//    private List<? extends T> backingList;
    private TreeView<T> backingTree;
    private ChangeListener<TreeModificationEvent<T>> sourceChangeListener;
    private WeakChangeListener<TreeModificationEvent<T>> weakSourceChangeListener;
    
    /**
     * @param source
     */
    public TreeIndexMappedList(TreeIndicesList<T> source) {
        super(source);
        this.backingTree = source.getSource();
        sourceChangeListener = (p, old, value) -> backingTreeModified(value);
        weakSourceChangeListener = new WeakChangeListener<>(sourceChangeListener);
        source.sourceChangeProperty().addListener(weakSourceChangeListener);
    }

    /**
     * Called when a treeModificationevent from the indicesList is passed on to
     * this.
     * 
     * @param c
     */
    protected void backingTreeModified(TreeModificationEvent<T> event) {
        // nothing to do, event was reset by indicesList
        if (event == null)
            return;
        if (!(event.getTreeItem() instanceof TreeItemX)) {
            throw new IllegalStateException(
                    "all treeItems must be of type TreeItemX but was "
                            + event.getTreeItem());
        }
        TreeModificationEventX<T> ex = event instanceof TreeModificationEventX ? 
                (TreeModificationEventX<T>) event : null;
        beginChange();
        if (ex != null && ex.getChange() != null) {
            childrenChanged((TreeItemX<T>) event.getTreeItem(), ex.getChange());
        } else {
            treeItemModified(event);
        }
        endChange();

    }
    
    
    
    /**
     * Handles TreeModificationEvents that are not list changes.
     * @param event
     */
    protected void treeItemModified(TreeModificationEvent<T> event) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Handles TreeModificationEvents that are list changes. 
     * @param treeItem
     * @param change
     */
    protected void childrenChanged(TreeItemX<T> source,
            Change<? extends TreeItem<T>> c) {
        
        c.reset();
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                throw new UnsupportedOperationException("TBD - permutation " + c);
//               permutatedItems(c);
            } else if (c.wasUpdated()) {
                throw new UnsupportedOperationException("TBD - updated " + c);
//                updatedItems(c);
            } else if (c.wasReplaced()) {
                throw new UnsupportedOperationException("TBD - replaced " + c);
//                replacedItems(c);
            } else {
//                throw new UnsupportedOperationException("TBD - addedOrRemoved " + c);
                addedOrRemovedItems(source, c);
            }
        }
        endChange();
    }

    /**
     * @param c
     */
    private void addedOrRemovedItems(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        if (c.wasAdded() && c.wasRemoved()) 
            throw new IllegalStateException("expected real add/remove but was: " + c);
        if (c.wasAdded()) {
            addedItems(c);
        } else if (c.wasRemoved()){
            removedItems(source, c);
        } else {
            throw new IllegalStateException("shouldn't be here: " + c);
        }
    }


    /**
     * Called on real additions to the backing list.
     * 
     * Implemented to do nothing: real addition to the backinglist don't change the
     * state of this.
     * 
     * @param c
     */
    private void addedItems(Change<? extends TreeItem<T>> c) {
        // no-op
    }

    /**
     * This is called for a removed or replaced change in the backingList.
     * 
     * @param c
     */
    private void removedItems(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        int treeFrom = backingTree.getRow(source) + 1 + c.getFrom();

        // fromIndex is startIndex of our own change - that doesn't change
        // as a subChange is about a single interval!
        int fromIndex = findIndex(treeFrom, c.getRemovedSize());
        if (fromIndex < 0) return;
        for (int i = treeFrom; i < treeFrom + c.getRemovedSize(); i++) {
            // have to fire if the item had been selected before and
            // no longer is now - how to detect the "before" part? we 
            // have no real state, only indirectly accessed
            int oldIndex = getIndicesList().oldIndices.indexOf(i);
//            int oldIndex = -1;
            if(oldIndex > -1) {
                // was selected, check if still is
                int index = getIndicesList().indexOf(i);
                if (index < 0) {
                    TreeItem<T> oldItem = c.getRemoved().get(i - treeFrom);
                    nextRemove(fromIndex, oldItem);
                }
            }
        }
    }

    /**
     * Returns the first index in indicesList _old_ state. The result is
     * in our own coordinate system, start is in backingList's coordinates.
     *  
     * @param start the backinglist's index to start looking for a match
     * @param size the end of the interval, in backinglists counting.
     * 
     * @return a the first index of old indices coordinates that matches 
     *    a coordinate in the range, or -1 for no match.
     */
    protected Integer findIndex(int start, int size) {
        for (int i = start; i < start + size; i++) {
            int index = getIndicesList().oldIndices.indexOf(i);
            if (index > - 1) return index;
        }
        return -1;
    }

    /**
     * @param c
     */
    private void replacedItems(TreeItemX<T> source, Change<? extends TreeItem<T>> c) {
        // need to replace even if unchanged, listeners to selectedItems
        // depend on it
        // handle special case of "real" replaced, often size == 1
        if (c.getAddedSize() == 1 && c.getAddedSize() == c.getRemovedSize()) {
            for (int i = c.getFrom(); i < c.getTo(); i++) {
                int index = getIndicesList().indexOf(i);
                if (index > -1) {
                    nextSet(index, c.getRemoved().get(i - c.getFrom()));
                }
            } 
            return;
        }
        // here we most (?) likely received a setAll/setItems
        // that results in a removed on the indicesList, independent on the 
        // actual relative sizes of removed/added: all (?) old indices are invalid
        // PENDING JW: when can we get a replaced on a subrange?
        if (size() == 0) {
            removedItems(source, c);
        } else {
            throw new IllegalStateException("unexpected replaced: " + c);
        }
    }


    /**
     * Called when list change in backing list is of type update.
     * @param c
     */
    private void updatedItems(Change<? extends TreeItem<T>> c) {
        for (int i = c.getFrom(); i < c.getTo(); i++) {
            int index = getIndicesList().indexOf(i);
            if (index > -1) {
                nextUpdate(index);
            }
        } 
    }

    protected SortHelper sortHelper = new SortHelper();

    /**
     * @param c
     */
    private void permutatedItems(Change<? extends T> c) {
        throw new IllegalStateException("not yet implemented");
//        List<Integer> perm = new ArrayList<>(getIndicesList().oldIndices);
//        int[] permA = new int[size()];
//        for (int i = 0; i < getIndicesList().oldIndices.size(); i++) {
//            perm.set(i, c.getPermutation(getIndicesList().oldIndices.get(i)));
//        }
//        int[] permutation = sortHelper.sort(perm);
//        nextPermutation(0, perm.size(), permutation);
    }


    /**
     * Implemented to react to changes in indicesList only if they originated
     * from directly modifying its indices. Indirect changes (by backingList)
     * are handled on receiving a sourceChanged from indicesList.
     */
    @Override
    protected void sourceChanged(Change<? extends Integer> c) {
        if (getIndicesList().getSourceChange() != null) return;
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                permutated(c);
            } else if (c.wasUpdated()) {
                updated(c);
            } else if (c.wasReplaced()) {
                replaced(c);
            } else {
                addedOrRemoved(c);
            }
        }
        endChange();
    }

    /**
     * Called on real added/removed changes to the indices.
     * @param c
     */
    private void addedOrRemoved(Change<? extends Integer> c) {
        if (c.wasAdded() && c.wasRemoved()) 
            throw new IllegalStateException("expected real add/remove but was: " + c);
        if (c.wasAdded()) {
            added(c);
        } else if (c.wasRemoved()) {
            removed(c);
        } else {
            throw new IllegalStateException("what have we got here? " + c);
        }
    }

    /**
     * Called on real adds to the indices.
     * @param c
     */
    private void added(Change<? extends Integer> c) {
        nextAdd(c.getFrom(), c.getTo());
    }
    
    /**
     * Called on real removes on indices.
     * <p>
     * @param c
     */
    private void removed(Change<? extends Integer> c) {
        List<? extends Integer> indices = c.getRemoved();
        List<TreeItem<T>> items = new ArrayList<>();
        if (getIndicesList().getSourceChange() == null) {
            // change resulted from direct modification of indices
            // no change in backingList, so we can access its items 
            // directly
            for (Integer index : indices) {
                items.add(backingTree.getTreeItem(index));
            }
        } else { 
//            if (true)
//                throw new IllegalStateException("shouldn't be here - separated out indirect changes");
//            for (int i = 0 ; i < c.getRemovedSize(); i++) {
//                int removedSourceIndex = c.getRemoved().get(i);
//                // find change in source that covers removedSource
//                Change<? extends T> sourceChange = getIndicesList().getSourceChange();
//                sourceChange.reset();
//                int accumulatedRemovedSize = 0;
//                while (sourceChange.next()) {
//                    if (!sourceChange.wasRemoved()) continue;
//                    int fromSource = sourceChange.getFrom() + accumulatedRemovedSize;
//                    if (removedSourceIndex >= fromSource && removedSourceIndex < fromSource + sourceChange.getRemovedSize()) {
//                        // hit - PENDING JW: need to accumulate if we have multiple removes!
//                        int indexInRemoved = removedSourceIndex - fromSource;
//                        items.add(sourceChange.getRemoved().get(indexInRemoved));
//                        break;
//                    }
//                    accumulatedRemovedSize += sourceChange.getRemovedSize();
//                }
//            }
        }
        nextRemove(c.getFrom(), items);
    }

    
    /**
     * @return
     */
    private TreeIndicesList<T> getIndicesList() {
        return (TreeIndicesList<T>) getSource();
    }

    /**
     * PENDING JW: all removes are carying incorrect removes - they access the 
     * backingList which has them already removed!
     * 
     * @param c
     */
    private void replaced(Change<? extends Integer> c) {
        List<? extends Integer> indices = c.getRemoved();
        List<TreeItem<T>> items = new ArrayList<>();
        for (Integer index : indices) {
            items.add(backingTree.getTreeItem(index));
        }
        nextReplace(c.getFrom(), c.getTo(), items);
    }

    /**
     * @param c
     */
    private void updated(Change<? extends Integer> c) {
        nextUpdate(c.getFrom());
    }

    /**
     * @param c
     */
    private void permutated(Change<? extends Integer> c) {
        throw new IllegalStateException("don't expect permutation changes from indices " + c);
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public TreeItem<T> get(int index) {
        int realIndex = getSource().get(index);
        return backingTree.getTreeItem(realIndex);
    }

    @Override
    public int size() {
        return getSource().size();
    }


}