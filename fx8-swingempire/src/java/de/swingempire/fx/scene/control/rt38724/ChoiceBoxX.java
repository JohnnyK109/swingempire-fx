/*
 * Created on 24.09.2014
 *
 */
package de.swingempire.fx.scene.control.rt38724;

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */


import java.util.logging.Logger;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.control.Control;
import javafx.scene.control.Separator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
//import javafx.scene.accessibility.Action;
//import javafx.scene.accessibility.Attribute;
//import javafx.scene.accessibility.Role;
import javafx.util.StringConverter;
import de.swingempire.fx.property.PathAdapter;

/**
 * C&P'ed core to experiment with cleanup ideas.
 * 
 * Experimentations
 * - add and make use of ListProperty for items
 * - cleanup selection model
 * - cleanup selection related code (let the model be in full control)
 * 
 * Changes
 * - used custom skin
 * - replaced manual wiring of selectedItemListener by PathAdapter
 * - TODO add itemsListProperty 
 * - TODO replace all internal access to itemsProperty by itemsListProperty
 * - enhanced ChoiceSelectionModel to take over all items updates related to items
 * - removed item-related selection updates from choicebox (rely on model)
 * - TODO similar for skin
 * 
 * Particular pitfalls in core
 * 
 * Duplication of logic: update selection state on changes to itemsProperty and itemsListContent
 * 
 * - there are 4 code blocks that deal with it (2 Listeners on box/selectionModel)
 * - all 4 code blocks are different!
 * - all 4 are active all the time, thus competing and interfering with each other - the
 *   combined outcome can't be predictable 
 * 
 * 
 * 
 * 
 * -------------------- below: unchanged core api doc
 * 
 * The ChoiceBox is used for presenting the user with a relatively small set of
 * predefined choices from which they may choose. The ChoiceBox, when "showing",
 * will display to the user these choices and allow them to pick exactly one
 * choice. When not showing, the current choice is displayed.
 * <p>
 * By default, the ChoiceBox has no item selected unless otherwise specified. 
 * Although the ChoiceBox will only allow a user to select from the predefined
 * list, it is possible for the developer to specify the selected item to be
 * something other than what is available in the predefined list. This is
 * required for several important use cases.
 * <p>
 * It means configuration of the ChoiceBox is order independent. You
 * may either specify the items and then the selected item, or you may
 * specify the selected item and then the items. Either way will function
 * correctly.
 * <p>
 * ChoiceBox item selection is handled by 
 * {@link javafx.scene.control.SelectionModel SelectionModel}
 * As with ListView and ComboBox, it is possible to modify the 
 * {@link javafx.scene.control.SelectionModel SelectionModel} that is used, 
 * although this is likely to be rarely changed. ChoiceBox supports only a 
 * single selection model, hence the default used is a {@link SingleSelectionModel}.
 *
 * <pre>
 * import javafx.scene.control.ChoiceBox;
 *
 * ChoiceBox cb = new ChoiceBox();
 * cb.getItems().addAll("item1", "item2", "item3");
 * </pre>
 * @since JavaFX 2.0
 */
@DefaultProperty("items")
public class ChoiceBoxX<T> extends Control {
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Create a new ChoiceBox which has an empty list of items.
     */
    public ChoiceBoxX() {
        this(FXCollections.<T>observableArrayList());
    }

    /**
     * Create a new ChoiceBox with the given set of items. Since it is observable,
     * the content of this list may change over time and the ChoiceBox will
     * be updated accordingly.
     * @param items
     */
    public ChoiceBoxX(ObservableList<T> items) {
        getStyleClass().setAll("choice-box");
        // CHANGED JW: replaced manual wiring by PathAdapter
        selectedItemPath = new PathAdapter<>(selectionModelProperty(), p -> p.selectedItemProperty());
        selectedItemPath.addListener(selectedItemListener);
        itemsProperty().bindBidirectional(itemsList);
        
        setItems(items);
        setSelectionModel(new ChoiceBoxSelectionModel<T>(this));
        
        // listen to the value property, if the value is
        // set to something that exists in the items list, update the
        // selection model to indicate that this is the selected item
        // PENDING JW: looks fishy - duplication? valueProperty selects item in invalidated
        valueProperty().addListener((ov, t, t1) -> {
            if (getItems() == null) return;
            int index = getItems().indexOf(t1);
            if (index > -1) {
                getSelectionModel().select(index);
            }
        });
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * The selection model for the ChoiceBox. Only a single choice can be made,
     * hence, the ChoiceBox supports only a SingleSelectionModel. Generally, the
     * main interaction with the selection model is to explicitly set which item
     * in the items list should be selected, or to listen to changes in the
     * selection to know which item has been chosen.
     * 
     * CHANGED JW: just a simple property, re-wiring listeners is done in PathAdapter.
     */
    private ObjectProperty<SingleSelectionModel<T>> selectionModel = 
            new SimpleObjectProperty<SingleSelectionModel<T>>(this, "selectionModel");
    
    private PathAdapter<SingleSelectionModel<T>, T> selectedItemPath;
    
    // CHANGED JW: listener registered on path
    private ChangeListener<T> selectedItemListener = (ov, t, t1) -> {
        if (! valueProperty().isBound()) {
            setValue(t1);
        }
    };

    
    public final void setSelectionModel(SingleSelectionModel<T> value) { selectionModel.set(value); }
    public final SingleSelectionModel<T> getSelectionModel() { return selectionModel.get(); }
    public final ObjectProperty<SingleSelectionModel<T>> selectionModelProperty() { return selectionModel; }


    /**
     * Indicates whether the drop down is displaying the list of choices to the
     * user. This is a readonly property which should be manipulated by means of
     * the #show and #hide methods.
     */
    private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper() {
        @Override protected void invalidated() {
            pseudoClassStateChanged(SHOWING_PSEUDOCLASS_STATE, get());
//            accSendNotification(Attribute.EXPANDED);
        }

        @Override
        public Object getBean() {
            return ChoiceBoxX.this;
        }

        @Override
        public String getName() {
            return "showing";
        }
    };
    public final boolean isShowing() { return showing.get(); }
    public final ReadOnlyBooleanProperty showingProperty() { return showing.getReadOnlyProperty(); }

    /**
     * The items to display in the choice box. The selected item (as indicated in the
     * selection model) must always be one of these items.
     */
    private ObjectProperty<ObservableList<T>> items = new SimpleObjectProperty<ObservableList<T>>(this, "items");
    public final void setItems(ObservableList<T> value) { items.set(value); }
    public final ObservableList<T> getItems() { return items.get(); }
    public final ObjectProperty<ObservableList<T>> itemsProperty() { return items; }

    private ListProperty<T> itemsList = new SimpleListProperty<>(this, "itemsList");
    public final ListProperty<T> itemsListProperty() {return itemsList;}; 
    
    /**
     * Allows a way to specify how to represent objects in the items list. When
     * a StringConverter is set, the object toString method is not called and 
     * instead its toString(object T) is called, passing the objects in the items list. 
     * This is useful when using domain objects in a ChoiceBox as this property 
     * allows for customization of the representation. Also, any of the pre-built
     * Converters available in the {@link javafx.util.converter} package can be set. 
     * @since JavaFX 2.1
     */
    public ObjectProperty<StringConverter<T>> converterProperty() { return converter; }
    private ObjectProperty<StringConverter<T>> converter = 
            new SimpleObjectProperty<StringConverter<T>>(this, "converter", null);
    public final void setConverter(StringConverter<T> value) { converterProperty().set(value); }
    public final StringConverter<T> getConverter() {return converterProperty().get(); }
    
    /**
     * The value of this ChoiceBox is defined as the selected item in the ChoiceBox
     * selection model. The valueProperty is synchronized with the selectedItem. 
     * This property allows for bi-directional binding of external properties to the 
     * ChoiceBox and updates the selection model accordingly. 
     * @since JavaFX 2.1
     */
    public ObjectProperty<T> valueProperty() { return value; }
    private ObjectProperty<T> value = new SimpleObjectProperty<T>(this, "value") {
        @Override protected void invalidated() {
            super.invalidated();
            fireEvent(new ActionEvent());
            // Update selection
            final SingleSelectionModel<T> sm = getSelectionModel();
            if (sm != null) {
                sm.select(super.getValue());
            }
//            accSendNotification(Attribute.TITLE);
        }
    };
    public final void setValue(T value) { valueProperty().set(value); }
    public final T getValue() { return valueProperty().get(); }

    /***************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * Opens the list of choices.
     */
    public void show() {
        if (!isDisabled()) showing.set(true);
    }

    /**
     * Closes the list of choices.
     */
    public void hide() {
        showing.set(false);
    }

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new ChoiceBoxXSkin<T>(this);
    }

    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final PseudoClass SHOWING_PSEUDOCLASS_STATE =
            PseudoClass.getPseudoClass("showing");

    // package for testing
    static class ChoiceBoxSelectionModel<T> extends SingleSelectionModel<T> {
        private final ChoiceBoxX<T> choiceBox;

        public ChoiceBoxSelectionModel(final ChoiceBoxX<T> cb) {
            if (cb == null) {
                throw new NullPointerException("ChoiceBox can not be null");
            }
            this.choiceBox = cb;
       
            /*
             * The following two listeners are used in conjunction with
             * SelectionModel.select(T obj) to allow for a developer to select
             * an item that is not actually in the data model. When this occurs,
             * we actively try to find an index that matches this object, going
             * so far as to actually watch for all changes to the items list,
             * rechecking each time.
             */

            // watching for changes to the items list content
            final ListChangeListener<T> itemsContentObserver = c -> {
                updateSelectionStateOnItemsContentChanged(c);
            };
            choiceBox.itemsListProperty().addListener(itemsContentObserver);
//            if (this.choiceBox.getItems() != null) {
//                this.choiceBox.getItems().addListener(itemsContentObserver);
//            }

            // watching for changes to the items list
            ChangeListener<ObservableList<T>> itemsObserver = (valueModel, oldList, newList) -> {
//                if (oldList != null) {
//                    oldList.removeListener(itemsContentObserver);
//                }
//                if (newList != null) {
//                    newList.addListener(itemsContentObserver);
//                }
                updateSelectionStateOnItemsPropertyChanged();
            };
            choiceBox.itemsListProperty().addListener(itemsObserver);
        }

        protected void updateSelectionStateOnItemsContentChanged(Change<? extends T> c) {
            if (isEmptyItems()) {
//                setSelectedIndex(-1);
                // clear if empty items - was handled by box' listener
                clearSelection();
            } 
            else if (wasRemoved(c, getSelectedItem())) {
                clearSelection();
            } else { // selected item either still in list or wasn't before the change
                // update index
                int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
                setSelectedIndex(newIndex);
            }
            
//-------------- original block makes testRemoveItemSelectedItemUncontained fail 
// ------------- with expected null, actual oldSelected            
//            else {
//                // taken from box's contentListener
//                int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
//                setSelectedIndex(newIndex);
//
//            }
//            // original comment:
//            // Look for the selected item as having been removed. If it has been,
//            // then we need to clear the selection in the selection model.
//            //   JW: seems to indicate the intention of _really_ removing the 
//            //   a selected item that has been removed from the model
//            // PENDING JW: plain copy from box' contentlistener - really special
//            // casing a remove?
//            // JW to PENDING: current answer seems to be yes - until here the 
//            // removed selected item is still the selectedItem 
//            final T selectedItem = getSelectedItem();
//            while (c.next()) {
//                if (selectedItem != null && c.getRemoved().contains(selectedItem)) {
//                    // JW: doesn't remove the selectedItem, though
//                    // SingleSelectionModel doesn't clear a selectedItem that's not in the list
//                    clearSelection();
//                    break;
//                    }
//            }
//----------------- end block            
            
//----------------- alternative block makes test fail with expected -1, actual 2            
//            else if (!isEmpty()) { // have a selected index might be different
//                int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
//                if (newIndex != -1) {
//                    setSelectedIndex(newIndex);
//                }
//                
//            } else if (getSelectedIndex() == -1 && getSelectedItem() != null) {
//                int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
//                if (newIndex != -1) {
//                    setSelectedIndex(newIndex);
//                }
//            }
//-------------- end alternative block            

        }

        /**
         * @param c
         * @param selectedItem
         * @return
         */
        private boolean wasRemoved(Change<? extends T> c, T selectedItem) {
            while (c.next()) {
                if (selectedItem != null && c.getRemoved().contains(selectedItem)) {
                    return true;
                    }
            }
            return false;
        }

        protected void updateSelectionStateOnItemsPropertyChanged() {
            // setSelectedIndex(-1);
            // CHANGED JW: clear selection on empty items RT-29433
            if (isEmptyItems() || !isExternalSelectedItem()) {
                clearSelection();
            } else { // items not empty, had uncontained selectedItem (implies
                     // != null, doesn't hurt, though)
                int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
                setSelectedIndex(newIndex);
//                if (newIndex != -1) {
//                }
            }
        }
        
        /**
         * @return a boolean indicating whether the selectedItem is considered
         *   part of the list
         */
        private boolean isExternalSelectedItem() {
            return getSelectedItem() != null && getSelectedIndex() == - 1;
        }

        /**
         * @return
         */
        private boolean isEmptyItems() {
            return choiceBox.getItems() == null || choiceBox.getItems().isEmpty();
        }

        // API Implementation
        @Override protected T getModelItem(int index) {
            final ObservableList<T> items = choiceBox.getItems();
            if (items == null) return null;
            if (index < 0 || index >= items.size()) return null;
            return items.get(index);
        }

        @Override protected int getItemCount() {
            final ObservableList<T> items = choiceBox.getItems();
            return items == null ? 0 : items.size();
        }

        /**
         * Selects the given row. Since the SingleSelectionModel can only support having
         * a single row selected at a time, this also causes any previously selected
         * row to be unselected.
         * This method is overridden here so that we can move past a Separator
         * in a ChoiceBox and select the next valid menuitem.
         * 
         * PENDING JW: 
         * <li> remove navigation here, outside contract of method
         * simply do nothing if not selectable
         * <li> shouldn't change box' state (here it is hiding)
         */
        @Override public void select(int index) {
            // this does not sound right, we should let the superclass handle it.
            final T value = getModelItem(index);
            if (value instanceof Separator) {
                select(++index);
            } else {
                super.select(index);
            }
            
            if (choiceBox.isShowing()) {
                choiceBox.hide();
            }
        }

        /**
         * Overridden to clear selectedIndex if selectedItem not contained 
         * in items.
         */
        @Override
        public void select(T obj) {
            super.select(obj);
            if (isExternalSelectedItem(obj)) {
                setSelectedIndex(-1);
            }
        }

        /**
         * Checks and returns whether item is an external selectedItem
         * @param item
         * @return
         */
        private boolean isExternalSelectedItem(T item) {
            if (item == null) return false;
            if (getItemCount() == 0) {
                return true;
            }
            return !choiceBox.getItems().contains(item);
        }
        
        
    }

    /***************************************************************************
     *                                                                         *
     * Accessibility handling                                                  *
     *                                                                         *
     **************************************************************************/

//    /** @treatAsPrivate */
//    @Override
//    public Object accGetAttribute(Attribute attribute, Object... parameters) {
//        switch(attribute) {
//            case ROLE: return Role.COMBOBOX;
//            case TITLE:
//                //let the skin first.
//                Object title = super.accGetAttribute(attribute, parameters);
//                if (title != null) return title;
//                StringConverter<T> converter = getConverter();
//                if (converter == null) {
//                    return getValue() != null ? getValue().toString() : "";
//                }
//                return converter.toString(getValue());
//            case EXPANDED: return isShowing();
//            default: return super.accGetAttribute(attribute, parameters);
//        }
//    }
//
//    @Override
//    public void accExecuteAction(Action action, Object... parameters) {
//        switch (action) {
//            case COLLAPSE: hide(); break;
//            case EXPAND: show(); break;
//            default: super.accExecuteAction(action); break;
//        }
//    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ChoiceBoxX.class
            .getName());
}
