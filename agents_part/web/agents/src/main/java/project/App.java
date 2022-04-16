package project;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import project.agent.Indexer;

@SpringBootApplication
@RestController
public class App {

    public static Indexer indexer;

    public static void main( String[] args ) {
        Properties prop = new ExtendedProperties();
        prop.setProperty(Profile.AGENTS, "seller1:project.agent.Seller(store1.json);seller2:project.agent.Seller(store2.json);indexer:project.agent.Indexer(seller1,seller2)");
        ProfileImpl profMain = new ProfileImpl(prop);
        Runtime runtime = Runtime.instance();
        runtime.createMainContainer(profMain);
    }

    @PostMapping("/api/items")
    public String fetchItems(@RequestBody String body) throws Exception {
        /**
         * request structure
         * category : string
         * filters : array of filter including (quantity (optional))
         *     filter : feature, condition, value
         * date : month and day (optional)
         * response structure
         * array of :
         *     seller : seller id
         *     items : array of items
         *         item : informations about the item + total price + bundle if any
         *     promotions : if any 
         */

        return indexer.getItems(body);
    }

    @PostMapping("/api/item/purchase")
    public String purchaseItem(@RequestBody String body) throws Exception {
        /**
         * request structure
         * seller : seller name
         * details : category, id, quantity, bundle, date (optional)
         * response structure
         * status : failed or completed 
         */

        return indexer.purchaseItem(body);
    }
}
