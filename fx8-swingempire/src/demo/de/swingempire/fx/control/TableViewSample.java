/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.control;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import com.sun.javafx.css.Stylesheet;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.XTableView;
import de.swingempire.fx.scene.control.cell.FocusableTableCell;
import de.swingempire.fx.scene.control.cell.XTextFieldTableCell;

/**
 * Example from tutorial. 
 * 
 * Changes:
 * - removed hard-coded stage sizing
 * - use enhanced person bean, thus no need for onEditCommitHandler (removed)
 * - tabs represent variations in tableCells
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewSample extends Application {
    private final ObservableList<Person> data =
            FXCollections.observableArrayList(
                    new Person("Jacob", "Smith", "jacob.smith@example.com"),
                    new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Person("Ethan", "Williams", "ethan.williams@example.com"),
                    new Person("Emma", "Jones", "emma.jones@example.com"),
                    new Person("Michael", "Brown", "michael.brown@example.com"));
   
    /**
     * Creates and returns the content of the stage.
     * 
     * Creates one tab for each type of cellFactory.
     * 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Parent createContent() {
        TabPane tabPane = new TabPane();
        // plain TextFieldTableCell
        // Note: TextFieldTableCell either needs a converter
        Callback<TableColumn<Person, String>, TableCell<Person, String>> 
            coreTextFieldCellFactory = p -> new TextFieldTableCell<>(new DefaultStringConverter());
        // or use the convenience factory method
//        Callback<TableColumn<Person, String>, TableCell<Person, String>> 
//            coreTextFieldCellFactory = TextFieldTableCell.forTableColumn();
        addTab(tabPane, "Core", coreTextFieldCellFactory);
        
        // issue: no focus indication
        //        table.getSelectionModel().setCellSelectionEnabled(true);
        // TableCell with focus indicator (not editable)
        Callback focusableCellFactory = p -> new FocusableTableCell<>();
        addTab(tabPane, "Focusable", focusableCellFactory);
        
        // enhanced editing cell from tutorial: commits on focusLost
        Callback<TableColumn<Person, String>, TableCell<Person, String>> editingCellFactory = 
                (TableColumn<Person, String> p) -> new EditingCell();
        addTab(tabPane, "Tutorial editingCell", editingCellFactory);

        // enhanced core textFieldCell with notion of terminate
        // Note: stopped working as of jdk8_u20
        Callback xTextFieldCellFactory = p -> new XTextFieldTableCell<>(new DefaultStringConverter());
        addTab(tabPane, "xTextFieldCell", xTextFieldCellFactory);
        
        // testing binding approach from SO - not really working
        Callback boundEditingCellFactory = p -> new BoundEditingCell();
        addTab(tabPane, "Bounding Editing", boundEditingCellFactory);

        // binding approach (not editable) vs. plain subclassing
        Callback<TableColumn<Person, String>, TableCell<Person, String>> bound = 
                (TableColumn<Person, String> p) -> new BoundTableCell();
        
        // plain TableCell override .. c&p from default in TableColumn        
        Callback<TableColumn<Person, String>, TableCell<Person, String>> plainCellFactory =        
            p-> new PlainTableCell();
            
            return tabPane;
        }

    /**
     * This is original of the tutorial example.
     * 
     * Issues: 
     * - commits on focuslost to external control, not when clicking 
     *   inside table
     * - missing keyHandlers for esc/enter (it's an example, after all)
     */
    public static class EditingCell extends TableCell<Person, String> {
        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {

            // trying to go tricky: force commit instead of cancel
            // no luck? throws NPE on commit
            // probably because the tableView already
            // has a null editingCell
            // cancelEdit(false);
            super.cancelEdit();
            setText((String) getItem());
            setGraphic(null);
        }

        // public void cancelEdit(boolean really) {
        // if(really) {
        // new RuntimeException("who's calling? ").printStackTrace();
        // super.cancelEdit();
        // setText((String) getItem());
        // setGraphic(null);
        // } else {
        //
        // commitEdit();
        // }
        // }
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        public void commitEdit() {
            commitEdit(textField.getText());
        }

        private void createTextField() {
            // re-create the field on each edit, why?
            if (textField == null) {
            }
            textField = new TextField(getString());
            // missing keybindings to esc/enter
            textField.focusedProperty().addListener(
                    (ObservableValue<? extends Boolean> arg0, Boolean arg1,
                            Boolean arg2) -> {
                        if (!arg2) {
                            LOG.info("lost focus, editing? " + isEditing());
//                            commitEdit();
                             commitEdit(textField.getText());
                        }
                    });
            textField.setText(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()
                    * 2);
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }


    /**
     * solution via binding: http://stackoverflow.com/a/25118925/203657
     * 
     * not fully correct - has spurious hysteresis effects
     */
    public static class BoundEditingCell<S, T> extends TableCell<S, T> {
    
        private final TextField mTextField;
    
        public BoundEditingCell() {
    
            super();
    
            mTextField = new TextField();
    
            mTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
    
                @Override
                public void handle(KeyEvent event) {
    
                    if( event.getCode().equals(KeyCode.ENTER) )
                        commitEdit((T)mTextField.getText());
                }
            });
    
            mTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
    
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    
                    if( !newValue )
                        commitEdit((T)mTextField.getText());
                }
    
            });
    
            mTextField.textProperty().bindBidirectional(textProperty());
        }
    
        @Override
        public void startEdit() {
    
            super.startEdit();
    
            setGraphic(mTextField);
        }
    
        @Override
        public void cancelEdit() {
    
            super.cancelEdit();
    
            setGraphic(null);
        }
    
        @Override
        public void updateItem(final T item, final boolean empty) {
    
            super.updateItem(item, empty);
    
            if( empty ) {
                setText(null);
                setGraphic(null);
            }
            else {
                if( item == null ) {
                    setGraphic(null);
                }
                else {
                    if( isEditing() ) {
                        setGraphic(mTextField);
                        setText((String)getItem());
                    }
                    else {
                        setGraphic(null);
                        setText((String)getItem());
                    }
                }
            }
        }
    }


    /**
     * Binding the item to textProperty instead of 
     * subclassing and implementing updateItem.
     * http://www.marshall.edu/genomicjava/2013/12/30/javafx-tableviews-with-contextmenus/
     * 
     */
    public static class BoundTableCell<S> extends TableCell<S, String> {
        
        public BoundTableCell() {
            textProperty().bind(itemProperty());
        }
    }

    /**
     * Though not abstract, TableCell simply shows nothing. Need to
     * subclass and implement updateItem.
     * 
     * C&P of default tableCell in TableColumn.
     */
    public static class PlainTableCell<S, T> extends TableCell<S, T> {
        @Override protected void updateItem(T item, boolean empty) {
            if (item == getItem()) return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node)item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
        }


    }
    protected Tab addTab(TabPane pane, String title, Callback cellFactory) {
        boolean useExtended = false;
        return addTab(pane, title, cellFactory, useExtended);
    }

    protected Tab addTab(TabPane pane, String title, Callback cellFactory,
            boolean useExtended) {
        Tab tab = new Tab(title);
        pane.getTabs().add(tab); 
        tab.setContent(createTabContent(cellFactory, useExtended));
        return tab;
    }

    protected Parent createTabContent(
            Callback<TableColumn<Person, String>, TableCell<Person, String>> coreTextFieldCellFactory) {
        boolean useExtended = false;
        return createTabContent(coreTextFieldCellFactory, useExtended);
    }

    protected Parent createTabContent(
            Callback<TableColumn<Person, String>, TableCell<Person, String>> coreTextFieldCellFactory,
            boolean useExtended) {
        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 20));
        TableView<Person> table = createBaseTable(useExtended);
        setCellFactories(table, coreTextFieldCellFactory);
        final TextField addFirstName = new TextField();
        addFirstName.setPromptText("First Name");
        addFirstName.setMaxWidth(table.getColumns().get(0).getPrefWidth());
        final TextField addLastName = new TextField();
        addLastName.setMaxWidth(table.getColumns().get(1).getPrefWidth());
        addLastName.setPromptText("Last Name");
        final TextField addEmail = new TextField();
        addEmail.setMaxWidth(table.getColumns().get(2).getPrefWidth());
        addEmail.setPromptText("Email");
        final Button addButton = new Button("Add");
        addButton.setOnAction((ActionEvent e) -> {
            // check: change item is reflected in cell using BoundTableCell
   //            Person selected = table.getSelectionModel().getSelectedItem();
   //            if (selected == null) return;
   //            selected.setFirstName(selected.getFirstName() + "x");
            // original
            // except inserting at top to see problem with incorrect focus on
            // inserting
            // http://stackoverflow.com/q/25559022/203657
            // might be fixed by https://javafx-jira.kenai.com/browse/RT-37632
            data.add(0, new Person(addFirstName.getText() + "x", addLastName.getText() + "x",
                    addEmail.getText()+ "x"));
            addFirstName.clear();
            addLastName.clear();
            addEmail.clear();
            
        });
   
        HBox hb = new HBox(3);
        hb.getChildren().addAll(addFirstName, addLastName, addEmail, addButton);
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table, hb);
        return vbox;
    }

    /**
     * Sets the given cellfactory to each column of the table.
     * 
     * @param table2
     * @param factory
     */
    private void setCellFactories(TableView<Person> table, Callback factory) {
        table.getColumns().stream().forEach(c -> c.setCellFactory(factory));
    }

    protected TableView<Person> createBaseTable(boolean useExtended) {
        TableView<Person> table = useExtended ? new XTableView<>() : new TableView<>();
        // quick check for https://javafx-jira.kenai.com/browse/RT-18937
        // requirement: no selection
        // side-effect here: can't start edits
        // table.setSelectionModel(null);
        table.setEditable(true);
        
        // side-testing: focus not updated correctly
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getFocusModel().focusedCellProperty().addListener((p, oldValue, newValue)-> {
            LOG.info("old/new " + oldValue + "\n  " + newValue);
            LOG.info("anchor? " + table.getProperties().get("anchor"));
        });
        TableColumn<Person, String> firstNameCol = new TableColumn<>(
                "First Name");
        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        firstNameCol.setMinWidth(100);
        firstNameCol
                .setCellValueFactory(new PropertyValueFactory<>("firstName"));
        // changed JW: removed editCommitHandlers
        // only need a custom handler if the bean doesn't expose the property
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol);
        table.setItems(data);
        return table;
    }

    @Override
    public void start(Stage stage) {
        // http://stackoverflow.com/q/25354538/203657
        // prevent full-screen
        // still jumping to the upper leading corner of the screen
//        stage.initStyle(StageStyle.UTILITY);
//        stage.setMaxHeight(500);
//        stage.setMaxWidth(600);

        stage.setTitle("Table View Sample");
        final Parent vbox = createContent();
        BorderPane pane = new BorderPane(vbox);
        Scene scene = new Scene(pane);
        Stylesheet s;
        scene.getStylesheets().add(getClass().getResource("focusedtablecell.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    @SuppressWarnings("unused")
    static final Logger LOG = Logger.getLogger(TableViewSample.class
            .getName());
}