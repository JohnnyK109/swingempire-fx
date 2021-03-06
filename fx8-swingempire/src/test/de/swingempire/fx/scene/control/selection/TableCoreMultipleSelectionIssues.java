/*
 * Created on 02.06.2013
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(Parameterized.class)
public class TableCoreMultipleSelectionIssues 
    extends AbstractTableMultipleSelectionIssues<TableView> {



    public TableCoreMultipleSelectionIssues(boolean multiple, boolean cellSelection) {
        super(multiple, cellSelection);
    }

    @Override
    protected TableView createEmptyView() {
        TableView table = new TableView();
        TableColumn column = new TableColumn("numberedItems");
        table.getColumns().add(column);
        return table;
    }
    
}
