/*
 * Created on 03.09.2014
 *
 */
package de.swingempire.fx.control.selection;

import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * Technical interface (think of it as an extension of MultipleSelectionModel
 * which we can't do as its a class) 
 * to support the notion of anchor in MultipleSelectionModel. Methods with
 * the same signature must comply to their contract plus update the anchor
 * as appropriate. We doc here only the anchor update.
 * 
 * 
 * Has-a read-only anchorProperty. 
 * All selection methods must define how they handle the anchor property.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface AnchoredSelectionModel {

    /**
     * Returns the anchor index.
     * @return
     */
    int getAnchorIndex();
    
    /**
     * 
     * @return
     */
    ReadOnlyIntegerProperty anchorIndexProperty();
    
    /**
     * Makes the current focused index the anchor.
     * 
     * Experimental: trying to get away with minimal api - all use-cases I have seen
     * so far require anchoring the current focus, allowing arbitrary indices to
     * become the anchor seems not needed. Can re-visit.
     */
    void anchor();
    
    /**
     * Updates anchor depending on selection state and selectionMode.
     * In singleSelectionMode, sets the anchor to the selected index. In 
     * mulitpleSelectionMode, the update depends on whether or not the selection
     * is empty: if so, sets the anchor to the selected index, if not 
     * keeps the anchor where it was.
     * 
     * @param index
     * 
     * @see javafx.scene.control.SelectionModel#select(index)
     */
    void select(int index);
    
    /**
     * Updates the anchor to the selected index.
     * @param index
     * @see javafx.scene.control.SelectionModel#clearAndSelect(index)
     */
    void clearAndSelect(int index);
    
    /**
     * Keeps the anchor unchanged.
     * 
     * PENDING JW: what if index == anchor? Try: "Keeps the anchor unchanged"
     * Not so easy: then we have inconsistent behaviour between focus and anchor
     * 
     * clearAt calls clearSelection if the index is the only selected index, which
     * in turn clears the focus. Anchor should behave the same. Then
     * client code can treat that very last clearing just the same way, with
     * respect to both anchor and focus. Maybe the question is if
     * clearing the last selected _should_ clear anchor/focus? If not, we would
     * have inconsistent state of an empty selection: if it were reached via
     * clearSelection(), anchor and focus are cleared. If reached (via multiple)
     * clearAt(int), anchor/focus would not be cleared ... 
     * 
     * 
     * @param index
     * @see javafx.scene.control.SelectionModel#clearSelection(index)
     */
    void clearSelection(int index);
    
    /**
     * Clears anchor.
     * @see javafx.scene.control.SelectionModel#clearSelection()
     */
    void clearSelection();
    
    /**
     * Updates anchor depending on selection state and selectionMode.
     * In singleSelectionMode, sets the anchor to the selected index. In 
     * mulitpleSelectionMode, the update depends on whether or not the selection
     * is empty: if so, sets the anchor to the start index, if not 
     * keeps the anchor where it was.<p>
     * 
     * Note that start may be < end as well as > end. Mapped to sequential first ... last
     * 
     * start < end:
     * first == start
     * last == end - 1
     * 
     * start > end
     * first == end + 1
     * last == start
     * 
     * 
     * PENDING JW: start == end? do nothing, there is no range
     * 
     * @param start the start of the range, inclusive
     * @param end the end of the range, exclusive
     * @see javafx.scene.control.MultipleSelectionModel#selectRange(int, int)
     */
    void selectRange(int start, int end);
    
    /**
     * Updates anchor depending on selection state and selectionMode.
     * In singleSelectionMode, sets the anchor to the selected index. In 
     * mulitpleSelectionMode, the update depends on whether or not the selection
     * is empty: if so, sets the anchor to the first valid index, if not 
     * keeps the anchor where it was.<p>
     * 
     * @param index
     * @param indices
     * 
     * @see javafx.scene.control.MultipleSelectionModel#selectIndices(int, int...)
     * 
     */
    void selectIndices(int index, int... indices);
}