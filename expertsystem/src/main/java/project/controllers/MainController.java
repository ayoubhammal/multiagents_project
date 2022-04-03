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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import project.inference.Clause;
import project.inference.Condition;
import project.inference.InferenceEngine;
import project.inference.Rule;
import project.inference.Variable;

public class MainController {

    @FXML private Node root;
    @FXML private TableView<Rule> knowledgeBaseTableView;
    @FXML private TableColumn<Rule, String> labelColumn;
    @FXML private TableColumn<Rule, String> antecedentsColumn;
    @FXML private TableColumn<Rule, String> consequentColumn;
    @FXML private TableColumn<Rule, Boolean> firedColumn;
    @FXML private VBox variablesVBox;
    @FXML private TextArea logTextArea;
    @FXML private Button forwardButton;
    @FXML private MenuButton basesMenuButton;
    private HashMap<String, Variable> variables;
    private HashMap<String, Rule> knowledgeBase;
    private InferenceEngine inferenceEngine;
    private HashMap<String, TextField> variablesTextFields;


    @FXML
    protected void initialize() {
        variables = new HashMap<String, Variable>();
        knowledgeBase = new HashMap<String, Rule>();
        jsonToExpertSystem("/bases/base.json");
        inferenceEngine = new InferenceEngine(this.variables, this.knowledgeBase);

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
        firedColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Rule,Boolean>,ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Rule, Boolean> p) {
                return new ReadOnlyBooleanWrapper(p.getValue().isFired());
            }
        });

        // initialize variables vbox
        variablesTextFields = new HashMap<String, TextField>();
        for (Map.Entry<String, Variable> e : this.variables.entrySet()) {
            Label label = new Label(e.getKey());
            TextField textField = new TextField();

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

        knowledgeBaseTableView.getItems().addAll(knowledgeBase.values());

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

                // enabling TextFields
                for (TextField tf : variablesTextFields.values()) {
                    tf.setDisable(false);
                }
            }
        });

        System.out.println(this.getClass().getResource("/bases").getFile());
        Set<String> basesFileNames = Stream.of(new File(this.getClass().getResource("/bases").getFile()).listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());
    
        for (String fileName : basesFileNames) {
            MenuItem baseMenuItem = new MenuItem(fileName);
            basesMenuButton.getItems().add(baseMenuItem);
        }

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

