import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

// class Beverage stores the details (name and ingredients ) required to prepare the Beverage
class Beverage {
    String name;
    HashMap<String, Long> ingredients = new HashMap<>();

    Beverage(String name, JSONObject ing) {
        this.name = name;
        ing.keySet().forEach( ingredient -> {
            ingredients.put((String)ingredient, (Long)ing.get(ingredient));
        });
    }
}

public class Main {

    static HashMap<String, Beverage> beverages = new HashMap<>();   // To storage the recipes
    static HashMap<String, Long> availableItemList = new HashMap<>();  // global static list of all available items
    static Long queue_size;
    public static void readInput() {
        JSONParser parser = new JSONParser();
        try {
            String configFile = "/Users/apple/IdeaProjects/coffee-machine/src/sample-input.json";
            Object obj = parser.parse(new FileReader(configFile));
            JSONObject jsonObject = (JSONObject)obj;

            JSONObject machine = (JSONObject)jsonObject.get("machine");
            JSONObject outlets = (JSONObject)machine.get("outlets");

            // int does not work here
            queue_size = (Long)outlets.get("count_n");

            JSONObject availableItems = (JSONObject)machine.get("total_items_quantity");
            availableItems.keySet().forEach( (key) -> {
                availableItemList.put((String)key, (Long)availableItems.get(key));
            });

            JSONObject beveragesJSON = (JSONObject)machine.get("beverages");

            beveragesJSON.keySet().forEach((beverageName) -> {
                JSONObject ingredients = (JSONObject)beveragesJSON.get(beverageName);
                Beverage B = new Beverage( (String)beverageName, ingredients);
                beverages.put( (String)beverageName, B);
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void prepareItem(String itemName) {
        Beverage b = beverages.get(itemName);
        int flag = 0;
        String itemNotPresent = "";
        boolean isNotAvailable = false;
        boolean isNotSufficient = false;

        if(b == null) {
            System.out.println("Invalid Item -> " + itemName);
            return;
        }
        HashMap<String, Long> currentlyUsed = new HashMap<>();  // To revert back used up ingredients if preparation not possible

        for(Map.Entry<String, Long> entry: b.ingredients.entrySet()) {
            String reqIng = entry.getKey();
            Long reqAmt = entry.getValue();

            Long availableAmt = availableItemList.get(reqIng);
            if(availableAmt == null) {
                itemNotPresent = reqIng;
                isNotAvailable = true;
                flag=1;
                break;
            }
            if(availableAmt >= reqAmt) {
                currentlyUsed.put(reqIng, reqAmt);
                availableItemList.replace(reqIng, availableAmt - reqAmt);
            }
            else {
                itemNotPresent = reqIng;
                isNotSufficient = true;
                flag=1;
                break;
            }
        }
        if(flag == 0)
            System.out.println(itemName + " is Prepared");
        else {
            // Restore the values if not used
            // In an actual system, this function will just check for availability. If it return true,
            // the machine should start serving the beverage
            for(Map.Entry<String, Long> entry: currentlyUsed.entrySet()) {
                availableItemList.replace(entry.getKey(), entry.getValue() + availableItemList.get(entry.getKey()));
            }
            System.out.println(itemName + " cannot be prepared because " + itemNotPresent +
                    " is not " + (isNotAvailable?"Available":"Sufficienct"));
        }

    }

    // Update this function to change the test case inputs
    public static String[] testcase() {
        return new String[]{"Hot Tea", "green_tea", "hot_tea", "green_tea"};
    }
    public static void main(String[] args) {
        readInput();    // machine configuration input

        String[] prepList = testcase();    // test case
        for (String s : prepList) {
            prepareItem(s);
        }

        // Final list of available items.
        System.out.println("\nAvailable : " + availableItemList);
    }
}
