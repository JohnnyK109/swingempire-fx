/*
 * Created on 16.06.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.concurrent.Callable;

import de.swingempire.fx.scene.control.selection.SimpleTreeSelectionModel;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Filtering Tree: all related classes copied from
 * http://www.kware.net/?p=204
 * 
 * Here we use FilteredTreeItemX and SimpleTreeSelectionModel ... throwing 
 * illegalState, need to dig!
 */
public class FilterableTreeItemXSample extends Application {

        public static void main(String[] args) {
                launch(args);
        }
        
        private TextField filterField;
        private FilterableTreeItemX<Actor> folder1;

        @Override
        public void start(Stage stage) throws Exception {
                try {
            Parent root = createContents();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
        
        private Parent createContents() {
        VBox vbox = new VBox(6);
        vbox.getChildren().add(createFilterPane());
        vbox.getChildren().add(createAddItemPane());
        Node demoPane = createDemoPane();
        VBox.setVgrow(demoPane, Priority.ALWAYS);
        vbox.getChildren().add(demoPane);
        return new BorderPane(vbox);
    }

        private Node createFilterPane() {
        filterField = new TextField();
        filterField.setPromptText("Enter filter text ...");

        TitledPane pane = new TitledPane("Filter", filterField);
        pane.setCollapsible(false);
        return pane;
    }
        
        private Node createAddItemPane() {
                HBox box = new HBox(6);
                TextField firstname = new TextField();
                firstname.setPromptText("Enter first name ...");
                TextField lastname = new TextField();
                lastname.setPromptText("Enter last name ...");
                
                Button addBtn = new Button("Add new actor to \"Folder 1\"");
                addBtn.setOnAction(event -> {
                        FilterableTreeItemX<Actor> treeItem = new FilterableTreeItemX<>(new Actor(firstname.getText(), lastname.getText()));
                        folder1.getBackingChildren().add(treeItem);
                });
                addBtn.disableProperty().bind(Bindings.isEmpty(lastname.textProperty()));
                
                box.getChildren().addAll(firstname, lastname, addBtn);
                TitledPane pane = new TitledPane("Add new element", box);
                pane.setCollapsible(false);
        return pane;
        }

    private Node createDemoPane() {
        HBox hbox = new HBox(6);
        Node filteredTree = createFilteredTree();
        HBox.setHgrow(filteredTree, Priority.ALWAYS);
        hbox.getChildren().add(filteredTree);
        return hbox;
    }

    private Node createFilteredTree() {
        FilterableTreeItemX<Actor> root = getTreeModel();
        Callable<TreeItemPredicate<Actor>> call = () -> {
            if (filterField.getText() == null || filterField.getText().isEmpty())
                return null;
            return TreeItemPredicate.create(actor -> actor.toString().contains(filterField.getText()));
            
        };
        root.predicateProperty().bind(Bindings.createObjectBinding(
                call,
//                () -> {
//            if (filterField.getText() == null || filterField.getText().isEmpty())
//                return null;
//            return TreeItemPredicate.create(actor -> actor.toString().contains(filterField.getText()));
//        }, 
        filterField.textProperty()));
        
        TreeView<Actor> treeView = new TreeView<>();
        treeView.setShowRoot(false);
        treeView.setSelectionModel(new SimpleTreeSelectionModel<>(treeView));
        
        treeView.setRoot(root);
        TitledPane pane = new TitledPane("Filtered TreeView", treeView);
        pane.setCollapsible(false);
        pane.setMaxHeight(Double.MAX_VALUE);
        return pane;
    }

    private FilterableTreeItemX<Actor> getTreeModel() {
        FilterableTreeItemX<Actor> root = new FilterableTreeItemX<>(new Actor("Root"));
        folder1 = createFolder("Folder 1");
        folder1.setExpanded(true);
        root.getBackingChildren().add(folder1);
        root.getBackingChildren().add(createFolder("Folder 2"));
        root.getBackingChildren().add(createFolder("Folder 3"));
        return root;
    }

    private FilterableTreeItemX<Actor> createFolder(String name) {
        FilterableTreeItemX<Actor> folder = new FilterableTreeItemX<>(new Actor(name));
        getActorList().forEach(actor -> folder.getBackingChildren().add(new FilterableTreeItemX<>(actor)));
        return folder;
    }

        private Iterable<Actor> getActorList() {
                ObservableList<Actor> actorList = FXCollections.observableArrayList(
                                new Actor("Jack", "Nicholson"), 
                                new Actor("Marlon", "Brando"), 
                                new Actor("Robert", "De Niro"), 
                                new Actor("Al", "Pacino"), 
                                new Actor("Daniel","Day-Lewis"), 
                                new Actor("Dustin", "Hoffman"), 
                                new Actor("Tom", "Hanks"),
                                new Actor("Anthony", "Hopkins"), 
                                new Actor("Paul", "Newman"), 
                                new Actor("Denzel", "Washington"),
                                new Actor("Spencer", "Tracy"), 
                                new Actor("Laurence", "Olivier"), 
                                new Actor("Jack", "Lemmon"));
                return actorList;
        }
        
        private static class Actor {
                public String firstname;
        public String lastname;
        
        public Actor(String string) {
                this.lastname = string;
        }
        
        public Actor(String firstname, String lastname) {
                this.firstname = firstname;
                this.lastname = lastname;
        }

                @Override
        public String toString() {
            return firstname == null ? lastname : firstname + " " + lastname;
        }
    }

}
