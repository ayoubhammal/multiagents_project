package project.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import project.inference.InferenceEngine;

public class Seller extends Agent {

    private String storeFileName;
    private HashMap<String, HashMap<String, HashMap<String, String>>> itemsPerCategory;
    private HashMap<String, HashMap<String, String>> promotions;
    private HashMap<String, HashMap<String, String>> bundles;

    private class OffersRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(messageTemplate);
            if (message != null) {
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject request = (JSONObject) parser.parse(message.getContent());
                    ACLMessage replay = message.createReply();

                    String category = (String) request.get("category");
                    JSONArray filters = (JSONArray) request.get("filters");

                    HashMap<String, HashMap<String, String>> items = itemsPerCategory.get(category);
                    JSONArray selectedItems = InferenceEngine.resolve(items, filters, bundles);


                    JSONObject response = new JSONObject();
                    response.put("seller", myAgent.getLocalName());
                    response.put("items", selectedItems);

                    JSONObject date = (JSONObject) request.get("date");
                    if (date != null) {
                        int month = Integer.parseInt((String) date.get("month"));
                        int day = Integer.parseInt((String) date.get("day"));
                        JSONArray promotionsJSON = new JSONArray();
                        for (HashMap<String, String> promotion : promotions.values()) {
                            int startMonth = Integer.parseInt(promotion.get("start month"));
                            int startDay = Integer.parseInt(promotion.get("start day"));
                            int endMonth = Integer.parseInt(promotion.get("end month"));
                            int endDay = Integer.parseInt(promotion.get("end day"));

                            if (checkDate(month, day, startMonth, startDay, endMonth, endDay)) {
                                promotionsJSON.add(new JSONObject(promotion));
                            }
                        }
                        response.put("promotions", promotionsJSON);
                    }

                    replay.setContent(response.toJSONString());
                    send(replay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                block();
            }
        }
    }

    private class PurchaseOrderServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage message = myAgent.receive(messageTemplate);
            if (message != null) {
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject request = (JSONObject) parser.parse(message.getContent());
                    ACLMessage replay = message.createReply();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    protected void setup() {
        System.out.println("[ " + this.getAID().getName() + " ] : Starting");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.storeFileName = (String) args[0];
        }

        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource("classpath:/stores/" + storeFileName);
            Scanner scanner = new Scanner(resource.getInputStream());
            StringBuilder storeString = new StringBuilder();
            while (scanner.hasNextLine()) 
                storeString.append(scanner.nextLine());
            scanner.close();

            JSONParser parser = new JSONParser();
            JSONArray stockJSON = (JSONArray) ((JSONObject) parser.parse(storeString.toString())).get("stock");
            JSONArray promotionsJSON = (JSONArray) ((JSONObject) parser.parse(storeString.toString())).get("promotions");
            JSONArray bundlesJSON = (JSONArray) ((JSONObject) parser.parse(storeString.toString())).get("bundles");

            itemsPerCategory = new HashMap<String, HashMap<String, HashMap<String, String>>>();
            for (Object category : stockJSON) {
                String categoryName = (String) ((JSONObject) category).get("category");
                JSONArray items = (JSONArray) ((JSONObject) category).get("items");

                itemsPerCategory.put(categoryName, new HashMap<String, HashMap<String, String>>());

                for (Object item : items) {
                    HashMap<String, String> itemMap = new HashMap<String, String>();
                    for (Object k : ((JSONObject) item).keySet()) {
                        String feature = (String) k;
                        String value = (String) ((JSONObject) item).get(feature);
                        itemMap.put(feature, value);
                    }
                    itemsPerCategory.get(categoryName).put(itemMap.get("id"), itemMap);
                }
            }

            promotions = new HashMap<String, HashMap<String, String>>();
            if (promotionsJSON != null) {
                for (Object promotionJSON : promotionsJSON) {
                    HashMap<String, String> promotion = new HashMap<String, String>();
                    for (Object k : ((JSONObject) promotionJSON).keySet()) {
                        String key = (String) k;
                        String value = (String) ((JSONObject) promotionJSON).get(key);
                        promotion.put(key, value);
                    }
                    promotions.put(promotion.get("id"), promotion);
                }
            }

            bundles = new HashMap<String, HashMap<String, String>>();
            if (bundlesJSON != null) {
                for (Object bundleJSON : bundlesJSON) {
                    HashMap<String, String> bundle = new HashMap<String, String>();
                    for (Object k : ((JSONObject) bundleJSON).keySet()) {
                        String key = (String) k;
                        String value = (String) ((JSONObject) bundleJSON).get(key);
                        bundle.put(key, value);
                    }
                    bundles.put(bundle.get("id"), bundle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        addBehaviour(new OffersRequestsServer());
        addBehaviour(new PurchaseOrderServer());
    }

    private static boolean checkDate(int month, int day, int startMonth, int startDay, int endMonth, int endDay) {
        if (startMonth <= endMonth) {
            if (month < startMonth || month > endMonth)
                return false;
        } else {
            if (month > endMonth && month < startMonth)
                return false;
        }
        if (month == startMonth && day < startDay)
            return false;
        if (month == endMonth && day > endDay)
            return false;
        return true;
    }
}
