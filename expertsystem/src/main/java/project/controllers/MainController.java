package project.controllers;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
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
    private HashMap<String, Variable> variables = new HashMap<String, Variable>();
    private HashMap<String, Rule> knowledgeBase = new HashMap<String, Rule>();



    @FXML
    protected void initialize() {
        // initializing knowledge base tableview's columns

        jsonToExpertSystem("/bases/base.json");


        
        /*
        System.out.println("Starting the inference engine");
        InferenceEngine inferenceEngine = new InferenceEngine(this.variables, this.knowledgeBase);
        String selectedRule = inferenceEngine.forwardPass();
        while (selectedRule != null) {
            System.out.println("conflict set :");
            for (String rule : inferenceEngine.getConflictSet())
                System.out.println(rule);
            System.out.println("selected rule : " + selectedRule);
            knowledgeBase.get(selectedRule).fire();
            selectedRule = inferenceEngine.forwardPass();
        }
        */
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

