package project;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import project.inference.Clause;
import project.inference.Condition;
import project.inference.InferenceEngine;
import project.inference.Rule;
import project.inference.Variable;

@SpringBootApplication
@RestController
public class App {

    private String targetVariable;
    private HashMap<String, ArrayList<String>> variables;
    private HashMap<String, Variable> memory;
    private HashMap<String, Rule> knowledgeBase;
    private InferenceEngine inferenceEngine;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping("api/bases")
    public String baseGet(@RequestParam(name = "base", defaultValue = "base.json") String base) throws Exception {
        /**
         * request structure
         * base : file name, default base.json
         * response structure
         * base : the expert system
         * bases list : list of available bases
         */

        Scanner scanner = new Scanner(new File(this.getClass().getResource("/bases/" + base).getFile()));
        StringBuilder baseJSON = new StringBuilder();
        while (scanner.hasNextLine()) 
            baseJSON.append(scanner.nextLine());
        scanner.close();

        Set<String> basesFileNames = Stream.of(new File(this.getClass().getResource("/bases").getFile()).listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());

        return baseToJson(baseJSON.toString(), new ArrayList<String>(basesFileNames));
    }

    @PostMapping("api/bases")
    public String basePost(@RequestBody String base) throws Exception {
        /**
         * request structure
         * base : file name
         * memory : variables values
         * response structure
         * log : array of outputs
         */

        JSONParser parser = new JSONParser();
        JSONObject request = (JSONObject) parser.parse(base);

        String baseFileName = (String) request.get("base");
        JSONArray memoryJSON = (JSONArray) request.get("memory");

        this.variables = new HashMap<String, ArrayList<String>>();
        this.memory = new HashMap<String, Variable>();
        this.knowledgeBase = new HashMap<String, Rule>();
        this.jsonToExpertSystem("/bases/" + baseFileName, memoryJSON);
        this.inferenceEngine = new InferenceEngine(this.memory, this.knowledgeBase);

        StringBuilder response = new StringBuilder("[");

        String selectedRule = inferenceEngine.forwardPass();
        while (true) {
            if (!memory.get(targetVariable).getValue().equals("")) {
                response.append("\"[ Inference Engine ] : Target variable found.\",");
                break;
            } else if (selectedRule == null) {
                response.append("\"[ Inference Engine ] : No rule can be applied.\",");
                break;
            } else {
                response.append("\"[ Inference Engine ] : Conflict set : " + String.join(", ", inferenceEngine.getConflictSet()) + "\",");
                response.append("\"[ Inference Engine ] : Selected rule : " + selectedRule + "\",");
                knowledgeBase.get(selectedRule).fire();
            }

            selectedRule = inferenceEngine.forwardPass();
        }
        
        response.setCharAt(response.length() - 1, ']');
        return response.toString();

    }

    private String baseToJson(String base, ArrayList<String> basesFileNames) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < basesFileNames.size(); ++i) {
            list.append("\"" + basesFileNames.get(i) + "\"");
            if (i < basesFileNames.size() - 1)
                list.append(",");
        }

        return "{" +
            "\"base\" : " + base + "," + 
            "\"bases list\" : [" + list.toString() + 
            "]" +
        "}";
    }
    private void jsonToExpertSystem(String baseFile, JSONArray memory) {

        JSONParser parser = new JSONParser();
        JSONObject base = null;
        try {
            base = (JSONObject) parser.parse(
                new FileReader(
                    this.getClass().getResource(baseFile).getFile()
                )
            );

            targetVariable = (String) base.get("target");

            JSONArray variables = (JSONArray) base.get("variables");
            for (Object variable : variables) {
                String name = (String) ((JSONObject) variable).get("name");
                JSONArray valuesJSON = (JSONArray) ((JSONObject) variable).get("values");
                ArrayList<String> values  = new ArrayList<String>();
                for (Object value : valuesJSON)
                    values.add((String) value);

                this.memory.put(name, new Variable(name));
                this.variables.put(name, values);
            }
            
            for (Object valuation : memory) {
                String variable = (String) ((JSONObject) valuation).get("variable");
                String value = (String) ((JSONObject) valuation).get("value");

                this.memory.get(variable).setValue(value);
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
            this.memory.get(variable),
            condition,
            value
        );
    }
}
