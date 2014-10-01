/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.SingleSelectionModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.sun.javafx.scene.control.skin.ComboBoxBaseSkin;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import static org.junit.Assert.*;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ComboSelectionIssues 
    extends AbstractChoiceInterfaceSelectionIssues<ComboBox> {

    
    /**
     * Trying to reproduce RT_26079 with builder: 
     * blowing if set equal but not same list
     * 
     * 
     */
    @Test
    public void testSelectFirstMemoryWithBuilderSameList() {
        view = 
                ComboBoxBuilder.<String>create()
                .items(FXCollections.observableArrayList("E1", "E2", "E3"))
                // no difference
//                .editable(false)
                .build();
//        initSkin();
        view.getSelectionModel().selectFirst();
        view.getItems().setAll(FXCollections.observableArrayList("E1", "E2", "E3"));
        view.getSelectionModel().clearSelection();
        assertEquals(-1, view.getSelectionModel().getSelectedIndex());
        assertEquals(null, view.getSelectionModel().getSelectedItem());
        assertEquals(null, view.getValue());
        assertEquals("", getDisplayText());

    }
    @Override
    protected String getDisplayText() {
        Node node = ((ComboBoxBaseSkin) getView().getSkin()).getDisplayNode();
        if (node instanceof ListCell) {
            return ((ListCell) node).getText();
        } 
        return "";
    }

    @Override
    protected ComboBox createView(ObservableList items) {
        return new ComboCoreControl(items);
    }

    @Override
    protected SimpleComboSelectionModel createSimpleSelectionModel() {
        return new SimpleComboSelectionModel(getView());
    }
    
    @Override
    protected ChoiceInterface getChoiceView() {
        return (ChoiceInterface) getView();
    }

    
    @Override
    protected boolean supportsSeparators() {
        return false;
    }

    @Override
    protected boolean hasPopup() {
        return false;
    }
    

    /**
     * Very simplistic model, just for testing setSelectionModel. Can't 
     * handle changes in the underlying items nor separators!
     */
    public static class SimpleComboSelectionModel extends SingleSelectionModel {

        private ComboBox choiceBox;

        /**
         * 
         */
        public SimpleComboSelectionModel(ComboBox box) {
            this.choiceBox = Objects.requireNonNull(box, "ChoiceBox must not be null");
        }

        @Override
        protected Object getModelItem(int index) {
            if (index < 0 || index >= getItemCount()) return null;
            return choiceBox.getItems().get(index);
        }

        @Override
        protected int getItemCount() {
            return choiceBox.getItems() != null ? choiceBox.getItems().size() : 0;
        }
        
    }
    
    
    public static class ComboCoreControl<T> extends ComboBox<T> implements ChoiceInterface<T> {

        public ComboCoreControl() {
            super();
        }

        public ComboCoreControl(ObservableList<T> items) {
            super(items);
        }
        
        
    }


    @Override
    protected boolean isClearSelectionOnSetItem() {
        // TODO Auto-generated method stub
        return false;
    }


}
