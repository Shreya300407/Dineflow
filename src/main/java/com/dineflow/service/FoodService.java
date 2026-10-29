package com.dineflow.service;

import com.dineflow.model.FoodItem;
import com.dineflow.repository.FoodItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class FoodService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FoodService.class);
    private final FoodItemRepository foodItemRepository;

    @Autowired
    public FoodService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    /**
     * Requirement 7: Populate sample food items ONLY if the database table is empty.
     * Guarantees no duplicate records are inserted on application restarts.
     */
    @Override
    public void run(String... args) {
        long count = foodItemRepository.count();
        if (count == 0) {
            log.info("Database table 'food_items' is empty. Seeding initial artisanal menu items...");
            seedSampleFoods();
            log.info("Successfully seeded {} food items into database.", foodItemRepository.count());
        } else {
            log.info("Database already contains {} food items. Skipping initial seeding.", count);
        }
    }

    public List<FoodItem> getAllFoods() {
        return foodItemRepository.findAll();
    }

    public Optional<FoodItem> getFoodById(Long id) {
        if (id == null) return Optional.empty();
        return foodItemRepository.findById(id);
    }

    public List<FoodItem> getFoodsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            return foodItemRepository.findAll();
        }
        return foodItemRepository.findByCategoryIgnoreCase(category.trim());
    }

    public List<FoodItem> searchFoods(String query) {
        if (query == null || query.trim().isEmpty()) {
            return foodItemRepository.findAll();
        }
        String q = query.trim();
        return foodItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q);
    }

    private void seedSampleFoods() {
        List<FoodItem> sampleItems = Arrays.asList(
                // 1. Starters
                new FoodItem("Paneer Tikka Croquettes",
                        "Golden crusted croquettes stuffed with marinated cottage cheese, spiced herbs, and mint chutney emulsion.",
                        "Starters", 280.0,
                        "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800&auto=format&fit=crop&q=80",
                        "4.8 ★ (190+)", "Chef's Special", "popular",
                        Arrays.asList("Vegetarian", "Crispy", "Signature")),

                new FoodItem("Crispy Calamari & Herb Aioli",
                        "Flash-fried calamari dusted with smoked paprika and sea salt, served with garlic herb aioli.",
                        "Starters", 340.0,
                        "https://images.unsplash.com/photo-1541544741938-0af808871cc0?w=800&auto=format&fit=crop&q=80",
                        "4.7 ★ (140+)", "Popular", "popular",
                        Arrays.asList("Seafood", "Crispy")),

                new FoodItem("Truffle Parmesan Hand-Cut Fries",
                        "Double-fried hand-cut Idaho potatoes tossed in white truffle oil, grated parmesan, and rosemary.",
                        "Starters", 220.0,
                        "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (210+)", "Top Rated", "",
                        Arrays.asList("Vegetarian", "Snack")),

                // 2. Main Course
                new FoodItem("Saffron Truffle Risotto",
                        "Carnaroli rice slow-cooked with Kashmiri saffron, wild mushrooms, aged parmesan, and black truffle oil.",
                        "Main Course", 520.0,
                        "https://images.unsplash.com/photo-1633964913295-ceb43826e7c9?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (320+)", "Signature", "popular",
                        Arrays.asList("Vegetarian", "Gluten-Free Option")),

                new FoodItem("Slow-Smoked Butter Chicken & Naan",
                        "Tender tandoor-roasted chicken simmered in a velvety tomato-cashew makhani gravy with butter garlic naan.",
                        "Main Course", 440.0,
                        "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (410+)", "Bestseller", "popular",
                        Arrays.asList("Traditional", "Rich & Creamy")),

                new FoodItem("Pan-Seared Atlantic Salmon",
                        "Crispy skin fresh salmon fillet accompanied by braised beluga lentils, asparagus, and lemon butter sauce.",
                        "Main Course", 680.0,
                        "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=800&auto=format&fit=crop&q=80",
                        "4.8 ★ (180+)", "Wild Caught", "popular",
                        Arrays.asList("High Protein", "Gluten-Free")),

                // 3. Pizza
                new FoodItem("Wood-Fired Burrata Margherita",
                        "San Marzano tomato sauce, fresh creamy burrata, Genovese basil, and extra virgin olive oil on sourdough.",
                        "Pizza", 460.0,
                        "https://images.unsplash.com/photo-1604382355076-af4b0eb60143?w=800&auto=format&fit=crop&q=80",
                        "4.8 ★ (290+)", "Organic", "vegan",
                        Arrays.asList("Vegetarian", "Sourdough", "Wood-Fired")),

                new FoodItem("Spicy Pepperoni & Hot Honey Pizza",
                        "Cured artisanal pepperoni, mozzarella, crushed red chilies, and a drizzle of organic wildflower hot honey.",
                        "Pizza", 540.0,
                        "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (340+)", "Spicy Favorite", "popular",
                        Arrays.asList("Crispy Crust", "Sweet & Spicy")),

                new FoodItem("Truffled Wild Mushroom Pizza",
                        "Fontina and smoked mozzarella, roasted portobello, caramelized shallots, thyme, and truffle essence.",
                        "Pizza", 490.0,
                        "https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?w=800&auto=format&fit=crop&q=80",
                        "4.7 ★ (160+)", "Chef's Choice", "",
                        Arrays.asList("Vegetarian", "Earthy")),

                // 4. Burgers
                new FoodItem("Classic Gourmet Angus Cheeseburger",
                        "Prime smashed Angus beef patty, melted aged cheddar, house pickles, crisp lettuce, and smoky relish on brioche.",
                        "Burgers", 390.0,
                        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
                        "4.8 ★ (275+)", "Bestseller", "popular",
                        Arrays.asList("Juicy", "Signature")),

                new FoodItem("Crispy Chipotle Fried Chicken Burger",
                        "Buttermilk-brined crispy fried chicken thigh, spicy chipotle crema, purple slaw, and pickled jalapeños.",
                        "Burgers", 360.0,
                        "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (310+)", "Popular", "popular",
                        Arrays.asList("Crispy", "Spicy")),

                new FoodItem("Smoked Portobello Mushroom Burger",
                        "Marinated and grilled jumbo portobello mushroom cap, vegan garlic aioli, caramelized onions, and arugula.",
                        "Burgers", 340.0,
                        "https://images.unsplash.com/photo-1520072959219-c595dc870360?w=800&auto=format&fit=crop&q=80",
                        "4.7 ★ (130+)", "Plant-Based", "vegan",
                        Arrays.asList("Vegetarian", "Healthy")),

                // 5. Desserts
                new FoodItem("Belgian Dark Chocolate Lava Cake",
                        "Warm molten cake with 70% Belgian chocolate center, served with Madagascar vanilla bean ice cream.",
                        "Desserts", 290.0,
                        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=800&auto=format&fit=crop&q=80",
                        "5.0 ★ (450+)", "Bestseller", "popular",
                        Arrays.asList("Warm", "Indulgent")),

                new FoodItem("Rose Pistachio Panna Cotta",
                        "Silky cardamom-infused Italian cream topped with fragrant Damascus rose jelly and crushed roasted pistachios.",
                        "Desserts", 240.0,
                        "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800&auto=format&fit=crop&q=80",
                        "4.8 ★ (160+)", "Delicate", "vegan",
                        Arrays.asList("Floral", "Chilled")),

                new FoodItem("Classic Venetian Tiramisu",
                        "Espresso-soaked savoiardi ladyfingers layered with airy mascarpone cream and dusted with Dutch cocoa.",
                        "Desserts", 260.0,
                        "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (220+)", "Classic", "",
                        Arrays.asList("Coffee", "Traditional")),

                // 6. Drinks
                new FoodItem("Botanical Citrus & Mint Spritz",
                        "Cold-pressed blood orange, bruised organic mint, elderflower botanical cordial, and sparkling spring water.",
                        "Drinks", 190.0,
                        "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=800&auto=format&fit=crop&q=80",
                        "4.8 ★ (180+)", "Refreshing", "vegan",
                        Arrays.asList("Mocktail", "Zero-Sugar Option")),

                new FoodItem("Passionfruit Mango Iced Quencher",
                        "Tropical Alphonso mango purée shaken with vibrant passionfruit reduction, crushed ice, and lime wheels.",
                        "Drinks", 180.0,
                        "https://images.unsplash.com/photo-1536935338788-846bb9981813?w=800&auto=format&fit=crop&q=80",
                        "4.9 ★ (210+)", "Fruity", "vegan",
                        Arrays.asList("Chilled", "Tropical")),

                new FoodItem("Cold Brew Tonic & Orange Zest",
                        "18-hour steeped single-origin Ethiopian cold brew served over botanical tonic water and flamed orange peel.",
                        "Drinks", 210.0,
                        "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=800&auto=format&fit=crop&q=80",
                        "4.7 ★ (115+)", "Artisan Coffee", "",
                        Arrays.asList("Craft Coffee", "Energizing"))
        );

        foodItemRepository.saveAll(sampleItems);
    }
}
