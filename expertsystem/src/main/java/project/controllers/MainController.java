package project.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import project.inference.Clause;
import project.inference.Condition;
import project.inference.InferenceEngine;
import project.inference.Rule;
import project.inference.Variable;

public class MainController {

    @FXML private BorderPane root;
    @FXML private TableView<Rule> knowledgeBaseTableView;
    @FXML private TableColumn<Rule, String> labelColumn;
    @FXML private TableColumn<Rule, String> antecedentsColumn;
    @FXML private TableColumn<Rule, String> consequentColumn;
    @FXML private VBox variablesVBox;
    @FXML private TextArea logTextArea;
    @FXML private Button forwardButton;
    @FXML private MenuButton basesMenuButton;
    @FXML private Label title;
    @FXML private HBox hboxTitle;
    @FXML private ScrollPane sc;
    @FXML private VBox vboxright;
    @FXML private VBox vbtable;
    @FXML private HBox hb_btn;
    private HashMap<String, Variable> variables;
    private HashMap<String, Rule> knowledgeBase;
    private InferenceEngine inferenceEngine;
    private HashMap<String, TextField> variablesTextFields;


    @FXML
    protected void initialize() {

        vboxright.setSpacing(50);
        vboxright.setMinWidth(500);
        vboxright.setMaxWidth(500);
        vboxright.setMinHeight(500);
        vboxright.setMaxHeight(500);
        vboxright.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; ");

        hb_btn.setSpacing(20);
        hb_btn.setAlignment(Pos.TOP_RIGHT);

        variablesVBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; ");

        vbtable.setAlignment(Pos.TOP_LEFT);
        vbtable.setSpacing(20);
        vbtable.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; ");

        knowledgeBaseTableView.setMinWidth(600);
        knowledgeBaseTableView.setMaxWidth(700);
        knowledgeBaseTableView.setMinHeight(400);
        knowledgeBaseTableView.setMaxHeight(400);

        title = new Label("Bus Stations Management");

        root.setMargin(vboxright, new Insets(3));
        root.setMargin(vbtable, new Insets(3));
        root.setMargin(hboxTitle, new Insets(3));

        logTextArea.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; ");

        hboxTitle.setAlignment(Pos.CENTER);
        hboxTitle.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; ");
        hboxTitle.setMinHeight(40);
        hboxTitle.setPadding(new Insets(7, 5, 7, 5));
        // initializing knowledge base tableview's columns
        labelColumn.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Rule,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Rule, String> p) {
                return new ReadOnlyStringWrapper(p.getValue().getLabel());
            }
        });
        antecedentsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Rule,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Rule, String> p) {
                return new ReadOnlyStringWrapper(p.getValue().getAntecedentsString());
            }
        });
        consequentColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Rule,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Rule, String> p) {
                return new ReadOnlyStringWrapper(p.getValue().getConsequentString());
            }
        });

        forwardButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // disabling TextFields
                for (TextField tf : variablesTextFields.values()) {
                    tf.setDisable(true);
                }

                String selectedRule = inferenceEngine.forwardPass();
                while (true) {
                    if (selectedRule == null) {
                        logTextArea.appendText("[ Inference Engine ] : No rule can be applied.\n");
                        break;
                    } else {
                        logTextArea.appendText("[ Inference Engine ] : Conflict set : " + String.join(", ", inferenceEngine.getConflictSet()) + "\n");
                        logTextArea.appendText("[ Inference Engine ] : Selected rule : " + selectedRule + "\n");
                        knowledgeBase.get(selectedRule).fire();

                        // updating TextFields
                        for (Map.Entry<String, TextField> e : variablesTextFields.entrySet()) {
                            e.getValue().setText(variables.get(e.getKey()).getValue());
                        }
                    }

                    selectedRule = inferenceEngine.forwardPass();
                }
            }
        });

        Set<String> basesFileNames = Stream.of(new File(this.getClass().getResource("/bases").getFile()).listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());
    
        for (String fileName : basesFileNames) {
            MenuItem baseMenuItem = new MenuItem(fileName);

            baseMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setBase(((MenuItem)event.getSource()).getText());
                }
            });

            basesMenuButton.getItems().add(baseMenuItem);
        }

        setBase("base.json");
    }

    private void setBase(String fileName) {
        variables = new HashMap<String, Variable>();
        knowledgeBase = new HashMap<String, Rule>();
        jsonToExpertSystem("/bases/" + fileName);
        inferenceEngine = new InferenceEngine(this.variables, this.knowledgeBase);
    
        knowledgeBaseTableView.getItems().clear();
        knowledgeBaseTableView.getItems().addAll(knowledgeBase.values());

        // initialize variables vbox
        variablesVBox.getChildren().clear();
        variablesTextFields = new HashMap<String, TextField>();
        for (Map.Entry<String, Variable> e : this.variables.entrySet()) {
            Label label = new Label(e.getKey());
            label.setStyle(" -fx-font-size:16px; ");
            TextField textField = new TextField(e.getValue().getValue());
            textField.setStyle("-fx-background-color:#F0E5D8;  ");

            variablesTextFields.put(e.getKey(), textField);

            textField.setOnInputMethodTextChanged(new EventHandler<InputMethodEvent>() {
                @Override
                public void handle(InputMethodEvent event) {
                    e.getValue().setValue(label.getText());
                }
            });

            variablesVBox.getChildren().add(label);
            variablesVBox.getChildren().add(textField);
        }

        logTextArea.setText("");
    }

    private void jsonToExpertSystem(String baseFile) {
        JSONParser parser = new JSONParser();
        JSONObject base = null;
        try {
            base = (JSONObject) parser.parse(
                new FileReader(
                    this.getClass().getResource(baseFile).getFile()
                )
            );

            JSONArray variables = (JSONArray) base.get("variables");
            for (Object variable : variables) {
                this.variables.put((String) variable, new Variable((String) variable));
            }
            
            JSONArray memory = (JSONArray) base.get("memory");
            for (Object valuation : memory) {
                String variable = (String) ((JSONObject) valuation).get("variable");
                String value = (String) ((JSONObject) valuation).get("value");

                this.variables.get(variable).setValue(value);
            }

            JSONArray knowledgeBase = (JSONArray) base.get("knowledge base");
            for (Object rule : knowledgeBase) {
                String label = (String) ((JSONObject) rule).get("label");
                JSONArray antecedentsJSON = (JSONArray) ((JSONObject) rule).get("antecedents");
                JSONObject consequentJSON = (JSONObject) ((JSONObject) rule).get("consequent");

                ArrayList<Clause> antecedents = new ArrayList<Clause>();            
                for (Object antecedentJSON : antecedentsJSON) {
                    antecedents.add(objectToClause((JSONObject) antecedentJSON));
                }

                Clause consequent = objectToClause(consequentJSON);

                this.knowledgeBase.put(
                    label,
                    new Rule(label, antecedents, consequent)
                );
            }
        } catch (ParseException e) {
            System.out.println("Parser exception");
        } catch (IOException e) {
            System.out.println(this.getClass().getResource(
                "/bases/base.json").getFile() + " not found"
            );
        }
    }

    private Clause objectToClause(JSONObject object) {
        String variable = (String) object.get("variable");
        Condition condition = Condition.fromString(
            (String) object.get("condition")
        );
        String value = (String) object.get("value");
        return new Clause(
            this.variables.get(variable),
            condition,
            value
        );
    }
}

