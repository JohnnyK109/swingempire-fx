/*
 * Created on 20.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import java.lang.ref.Reference;

import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.control.Cell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.control.skin.TableRowSkin;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowETSkin<T> extends TableRowSkin<T> implements EventTarget {

    EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        TableView<T> tableView = getSkinnable().getTableView();
        TablePosition<T, ?> focused = tableView.getFocusModel().getFocusedCell();
        if (focused != null && focused.getTableColumn() != null) {
            TableColumn<T, ?> column = focused.getTableColumn();
            Reference<TableCell<T, ?>> cellReference = cellsMap.get(column);
            Cell<?> cell = cellReference != null ? cellReference.get() : null;
            if (cell != null) {
                cell.buildEventDispatchChain(tail);
            }
        }
//        return tail.prepend(eventHandlerManager);
        return tail;
    }

//-------------------- boiler-plate constructor
    
    public TableRowETSkin(TableRow<T> tableRow) {
        super(tableRow);
    }

}
