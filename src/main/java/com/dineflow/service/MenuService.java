package com.dineflow.service;

import com.dineflow.model.MenuItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final List<MenuItem> menu = new ArrayList<>();

    public MenuService() {
        initMenu();
    }

    private void initMenu() {
        // Starters
        menu.add(new MenuItem(1L, "Paneer Tikka Croquettes", "Starters", 280.0, "4.8 ★ (190+)",
                "Chef's Special", "popular",
                "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800&auto=format&fit=crop&q=80",
                "Golden crusted croquettes stuffed with marinated cottage cheese, spiced herbs, and mint chutney emulsion.",
                Arrays.asList("Vegetarian", "Crispy", "Signature")));

        menu.add(new MenuItem(2L, "Crispy Calamari & Herb Aioli", "Starters", 340.0, "4.7 ★ (140+)",
                "Popular", "popular",
                "https://images.unsplash.com/photo-1541544741938-0af808871cc0?w=800&auto=format&fit=crop&q=80",
                "Flash-fried calamari dusted with smoked paprika and sea salt, served with garlic herb aioli.",
                Arrays.asList("Seafood", "Crispy")));

        menu.add(new MenuItem(3L, "Truffle Parmesan Hand-Cut Fries", "Starters", 220.0, "4.9 ★ (210+)",
                "Top Rated", "",
                "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=800&auto=format&fit=crop&q=80",
                "Double-fried hand-cut Idaho potatoes tossed in white truffle oil, grated parmesan, and rosemary.",
                Arrays.asList("Vegetarian", "Snack")));

        // Main Course
        menu.add(new MenuItem(4L, "Saffron Truffle Risotto", "Main Course", 520.0, "4.9 ★ (320+)",
                "Signature", "popular",
                "https://images.unsplash.com/photo-1633964913295-ceb43826e7c9?w=800&auto=format&fit=crop&q=80",
                "Carnaroli rice slow-cooked with Kashmiri saffron, wild mushrooms, aged parmesan, and black truffle oil.",
                Arrays.asList("Vegetarian", "Gluten-Free Option")));

        menu.add(new MenuItem(5L, "Slow-Smoked Butter Chicken & Naan", "Main Course", 440.0, "4.9 ★ (410+)",
                "Bestseller", "popular",
                "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=800&auto=format&fit=crop&q=80",
                "Tender tandoor-roasted chicken simmered in a velvety tomato-cashew makhani gravy with butter garlic naan.",
                Arrays.asList("Traditional", "Rich & Creamy")));

        menu.add(new MenuItem(6L, "Pan-Seared Atlantic Salmon", "Main Course", 680.0, "4.8 ★ (180+)",
                "Wild Caught", "popular",
                "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=800&auto=format&fit=crop&q=80",
                "Crispy skin fresh salmon fillet accompanied by braised beluga lentils, asparagus, and lemon butter sauce.",
                Arrays.asList("High Protein", "Gluten-Free")));

        // Pizza
        menu.add(new MenuItem(7L, "Wood-Fired Burrata Margherita", "Pizza", 460.0, "4.8 ★ (290+)",
                "Organic", "vegan",
                "https://images.unsplash.com/photo-1604382355076-af4b0eb60143?w=800&auto=format&fit=crop&q=80",
                "San Marzano tomato sauce, fresh creamy burrata, Genovese basil, and extra virgin olive oil on sourdough.",
                Arrays.asList("Vegetarian", "Sourdough", "Wood-Fired")));

        menu.add(new MenuItem(8L, "Spicy Pepperoni & Hot Honey Pizza", "Pizza", 540.0, "4.9 ★ (340+)",
                "Spicy Favorite", "popular",
                "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80",
                "Cured artisanal pepperoni, mozzarella, crushed red chilies, and a drizzle of organic wildflower hot honey.",
                Arrays.asList("Crispy Crust", "Sweet & Spicy")));

        menu.add(new MenuItem(9L, "Truffled Wild Mushroom Pizza", "Pizza", 490.0, "4.7 ★ (160+)",
                "Chef's Choice", "",
                "https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?w=800&auto=format&fit=crop&q=80",
                "Fontina and smoked mozzarella, roasted portobello, caramelized shallots, thyme, and truffle essence.",
                Arrays.asList("Vegetarian", "Earthy")));

        // Burgers
        menu.add(new MenuItem(10L, "Classic Gourmet Angus Cheeseburger", "Burgers", 390.0, "4.8 ★ (275+)",
                "Bestseller", "popular",
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
                "Prime smashed Angus beef patty, melted aged cheddar, house pickles, crisp lettuce, and smoky relish on brioche.",
                Arrays.asList("Juicy", "Signature")));

        menu.add(new MenuItem(11L, "Crispy Chipotle Fried Chicken Burger", "Burgers", 360.0, "4.9 ★ (310+)",
                "Popular", "popular",
                "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=800&auto=format&fit=crop&q=80",
                "Buttermilk-brined crispy fried chicken thigh, spicy chipotle crema, purple slaw, and pickled jalapeños.",
                Arrays.asList("Crispy", "Spicy")));

        menu.add(new MenuItem(12L, "Smoked Portobello Mushroom Burger", "Burgers", 340.0, "4.7 ★ (130+)",
                "Plant-Based", "vegan",
                "https://images.unsplash.com/photo-1520072959219-c595dc870360?w=800&auto=format&fit=crop&q=80",
                "Marinated and grilled jumbo portobello mushroom cap, vegan garlic aioli, caramelized onions, and arugula.",
                Arrays.asList("Vegetarian", "Healthy")));

        // Desserts
        menu.add(new MenuItem(13L, "Belgian Dark Chocolate Lava Cake", "Desserts", 290.0, "5.0 ★ (450+)",
                "Bestseller", "popular",
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=800&auto=format&fit=crop&q=80",
                "Warm molten cake with 70% Belgian chocolate center, served with Madagascar vanilla bean ice cream.",
                Arrays.asList("Warm", "Indulgent")));

        menu.add(new MenuItem(14L, "Rose Pistachio Panna Cotta", "Desserts", 240.0, "4.8 ★ (160+)",
                "Delicate", "vegan",
                "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800&auto=format&fit=crop&q=80",
                "Silky cardamom-infused Italian cream topped with fragrant Damascus rose jelly and crushed roasted pistachios.",
                Arrays.asList("Floral", "Chilled")));

        menu.add(new MenuItem(15L, "Classic Venetian Tiramisu", "Desserts", 260.0, "4.9 ★ (220+)",
                "Classic", "",
                "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=800&auto=format&fit=crop&q=80",
                "Espresso-soaked savoiardi ladyfingers layered with airy mascarpone cream and dusted with Dutch cocoa.",
                Arrays.asList("Coffee", "Traditional")));

        // Drinks
        menu.add(new MenuItem(16L, "Botanical Citrus & Mint Spritz", "Drinks", 190.0, "4.8 ★ (180+)",
                "Refreshing", "vegan",
                "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=800&auto=format&fit=crop&q=80",
                "Cold-pressed blood orange, bruised organic mint, elderflower botanical cordial, and sparkling spring water.",
                Arrays.asList("Mocktail", "Zero-Sugar Option")));

        menu.add(new MenuItem(17L, "Passionfruit Mango Iced Quencher", "Drinks", 180.0, "4.9 ★ (210+)",
                "Fruity", "vegan",
                "https://images.unsplash.com/photo-1536935338788-846bb9981813?w=800&auto=format&fit=crop&q=80",
                "Tropical Alphonso mango purée shaken with vibrant passionfruit reduction, crushed ice, and lime wheels.",
                Arrays.asList("Chilled", "Tropical")));

        menu.add(new MenuItem(18L, "Cold Brew Tonic & Orange Zest", "Drinks", 210.0, "4.7 ★ (115+)",
                "Artisan Coffee", "",
                "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=800&auto=format&fit=crop&q=80",
                "18-hour steeped single-origin Ethiopian cold brew served over botanical tonic water and flamed orange peel.",
                Arrays.asList("Craft Coffee", "Energizing")));
    }

    public List<MenuItem> getAllItems() {
        return new ArrayList<>(menu);
    }

    public List<MenuItem> getItemsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            return getAllItems();
        }
        return menu.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category.trim()))
                .collect(Collectors.toList());
    }

    public List<MenuItem> searchItems(String query, String category) {
        return menu.stream()
                .filter(item -> {
                    boolean matchesCategory = (category == null || category.equalsIgnoreCase("All")
                            || item.getCategory().equalsIgnoreCase(category.trim()));
                    if (!matchesCategory) return false;

                    if (query == null || query.trim().isEmpty()) return true;

                    String q = query.toLowerCase().trim();
                    return item.getName().toLowerCase().contains(q)
                            || item.getDescription().toLowerCase().contains(q)
                            || item.getCategory().toLowerCase().contains(q)
                            || item.getTags().stream().anyMatch(t -> t.toLowerCase().contains(q));
                })
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return Arrays.asList("Starters", "Main Course", "Pizza", "Burgers", "Desserts", "Drinks");
    }
}
