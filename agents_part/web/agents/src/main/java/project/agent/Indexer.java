package project.agent;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.boot.SpringApplication;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import project.App;

public class Indexer extends Agent {

    private static long conversationID = 0;
    private ArrayList<AID> sellers;

    private class FetchItemsServer extends OneShotBehaviour {
        private String request;
        private long id;
        private ArrayList<AID> sellers;

        public FetchItemsServer(ArrayList<AID> sellers, String request, long id) {
            this.request = request;
            this.id = id;
            this.sellers = sellers;
        }

        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            for (AID seller : sellers) {
                message.addReceiver(seller);
            }
            message.setConversationId(Long.toString(id));
            message.setContent(request);
            send(message);
        }
    }

    protected void setup() {
        System.out.println("[ " + this.getAID().getName() + " ] : Starting");

        this.sellers = new ArrayList<AID>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i)
                sellers.add(new AID((String) args[i], AID.ISLOCALNAME));
        }

        App.indexer = this;
        SpringApplication.run(App.class);
    }
    
    public String getItems(String request) {
        long id = conversationID++;
        addBehaviour(new FetchItemsServer(sellers, request, id));
        
        JSONArray responseJSON = new JSONArray();
        JSONParser parser = new JSONParser();
        MessageTemplate messageTemplate = MessageTemplate.MatchConversationId(Long.toString(id));
        for (int i = 0; i < sellers.size(); ++i) {
            ACLMessage response = blockingReceive(messageTemplate);
            try {
                responseJSON.add((JSONObject) parser.parse(response.getContent()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseJSON.toJSONString();
    }

    public String purchaseItem(String request) {
        long id = conversationID++;
        try {
            JSONParser parser = new JSONParser();
            JSONObject requestJSON = (JSONObject) parser.parse(request);
            //addBehaviour(new PurchaseItemServe(
            //    new AID((String) requestJSON.get("seller"), AID.ISLOCALNAME),
            //    (JSONObject) requestJSON.get("details"),
            //    id
            //));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
