package com.supermarket.inventory.config;

import com.supermarket.inventory.dto.req.CreateOrderRequest;
import com.supermarket.inventory.dto.req.OrderItemRequest;
import com.supermarket.inventory.dto.req.OrderStatusUpdateRequest;
import com.supermarket.inventory.entity.*;
import com.supermarket.inventory.repository.CategoryRepository;
import com.supermarket.inventory.repository.ProductRepository;
import com.supermarket.inventory.repository.RoleRepository;
import com.supermarket.inventory.repository.UserRepository;
import com.supermarket.inventory.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, CategoryRepository categoryRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder, OrderService orderService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Roles
        Role adminRole = roleRepository.findByName(UserRole.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ADMIN)));
        Role managerRole = roleRepository.findByName(UserRole.MANAGER)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.MANAGER)));
        Role staffRole = roleRepository.findByName(UserRole.STAFF)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.STAFF)));

        // 2. Seed Users
        if (!userRepository.existsByEmail("admin@supermarket.com")) {
            userRepository.save(new User("admin@supermarket.com", passwordEncoder.encode("Password123!"), "System Admin", adminRole));
        }
        if (!userRepository.existsByEmail("manager@supermarket.com")) {
            userRepository.save(new User("manager@supermarket.com", passwordEncoder.encode("Password123!"), "Store Manager", managerRole));
        }
        if (!userRepository.existsByEmail("staff@supermarket.com")) {
            userRepository.save(new User("staff@supermarket.com", passwordEncoder.encode("Password123!"), "Inventory Staff", staffRole));
        }

        // 3. Seed Categories
        if (categoryRepository.count() == 0) {
            Category dairy = categoryRepository.save(new Category("Dairy & Eggs", "Fresh milk, cheese, butter, and eggs"));
            Category produce = categoryRepository.save(new Category("Fresh Produce", "Organic fruits and fresh vegetables"));
            Category beverages = categoryRepository.save(new Category("Beverages", "Fruit juices, soda, water, and tea"));
            Category bakery = categoryRepository.save(new Category("Bakery", "Freshly baked bread, rolls, and pastries"));
            Category meat = categoryRepository.save(new Category("Meat & Seafood", "Fresh poultry, beef, pork, and seafood"));

            // 4. Seed Products (6 Products per Category = 30 Products total)
            
            // Dairy & Eggs (6 items)
            Product p2 = productRepository.save(new Product("SKU-MILK01", "Organic Whole Milk 1 Gallon", "Pasteurized grade A organic whole milk", new BigDecimal("4.49"), 30, 8, dairy, "Sunshine Dairy", "+1-800-555-0144"));
            productRepository.save(new Product("SKU-EGG01", "Farm Fresh Grade A Eggs 12pk", "Organic pasture-raised brown eggs", new BigDecimal("3.99"), 50, 10, dairy, "Happy Hen Farms", "+1-800-555-0145"));
            productRepository.save(new Product("SKU-CHSE01", "Aged Sharp Cheddar Block 400g", "Natural Wisconsin aged sharp cheddar", new BigDecimal("5.49"), 24, 5, dairy, "Valley Cheese Co", "+1-800-555-0146"));
            productRepository.save(new Product("SKU-YGUR01", "Greek Plain Whole Milk Yogurt 900g", "Authentic strained thick Greek yogurt", new BigDecimal("4.29"), 30, 6, dairy, "Olympus Dairy", "+1-800-555-0147"));
            productRepository.save(new Product("SKU-BUTR01", "Unsalted Organic Irish Butter 250g", "Churned pure cream European butter", new BigDecimal("3.89"), 40, 8, dairy, "Irish Meadow Dairy", "+1-800-555-0148"));
            productRepository.save(new Product("SKU-CREM01", "Heavy Whipping Cream 500ml", "Grade A 36% milkfat whipping cream", new BigDecimal("2.99"), 20, 5, dairy, "Sunshine Dairy", "+1-800-555-0144"));

            // Fresh Produce (6 items)
            Product p1 = productRepository.save(new Product("SKU-RICE01", "Royal Basmati Rice 5kg", "Premium long-grain fragrant basmati rice", new BigDecimal("18.99"), 50, 10, produce, "Global Grain", "+1-800-555-0199"));
            Product p3 = productRepository.save(new Product("SKU-APPL01", "Honeycrisp Fresh Apples 1kg", "Crisp, sweet fresh red Honeycrisp apples", new BigDecimal("3.99"), 4, 10, produce, "Valley Orchards", "+1-800-555-0188"));
            productRepository.save(new Product("SKU-BANA01", "Organic Cavendish Bananas 1kg", "Fresh ripe organic sweet bananas", new BigDecimal("1.89"), 60, 15, produce, "Tropical Farms", "+1-800-555-0189"));
            productRepository.save(new Product("SKU-TOMO01", "Vine Ripened Red Tomatoes 1kg", "Juicy fresh red tomatoes on the vine", new BigDecimal("2.79"), 30, 10, produce, "Greenhouse Produce", "+1-800-555-0190"));
            productRepository.save(new Product("SKU-SPIN01", "Fresh Organic Baby Spinach 300g", "Pre-washed tender baby spinach leaves", new BigDecimal("3.19"), 25, 5, produce, "Organic Green Fields", "+1-800-555-0191"));
            productRepository.save(new Product("SKU-AVOC01", "Hass Fresh Avocados 4pk", "Ripe creamy Mexican Hass avocados", new BigDecimal("4.99"), 35, 8, produce, "Avocado Grove", "+1-800-555-0192"));

            // Beverages (6 items)
            Product p4 = productRepository.save(new Product("SKU-JUIC01", "Florida Orange Juice 1.5L", "100% pure squeezed orange juice with pulp", new BigDecimal("5.29"), 25, 5, beverages, "Citrus Grove", "+1-800-555-0177"));
            productRepository.save(new Product("SKU-COFF01", "Dark Roast Colombian Coffee 340g", "100% Arabica ground dark roast coffee", new BigDecimal("8.99"), 20, 5, beverages, "Andes Coffee Roasters", "+1-800-555-0178"));
            productRepository.save(new Product("SKU-WATR01", "Sparkling Mineral Water 12pk", "Pure carbonated natural spring water", new BigDecimal("6.49"), 40, 10, beverages, "Alpine Springs", "+1-800-555-0179"));
            productRepository.save(new Product("SKU-TEA01", "Organic Earl Grey Tea 50 Bags", "Black tea infused with pure bergamot oil", new BigDecimal("4.19"), 25, 5, beverages, "Royal Tea House", "+1-800-555-0180"));
            productRepository.save(new Product("SKU-SMTH01", "Berry Antioxidant Smoothie 1L", "Mixed strawberry blueberry acai smoothie", new BigDecimal("4.79"), 15, 5, beverages, "Orchard Fresh Drinks", "+1-800-555-0181"));
            productRepository.save(new Product("SKU-LEMN01", "Fresh Squeezed Lemonade 1.5L", "Classic old-fashioned fresh lemonade", new BigDecimal("3.69"), 28, 5, beverages, "Citrus Grove", "+1-800-555-0177"));

            // Bakery (6 items)
            Product p5 = productRepository.save(new Product("SKU-BRD01", "Whole Grain Whole Wheat Bread", "Freshly baked 100% whole grain wheat bread", new BigDecimal("2.99"), 0, 5, bakery, "Golden Crust", "+1-800-555-0166"));
            productRepository.save(new Product("SKU-CRSS01", "French All-Butter Croissants 4pk", "Flaky layered golden butter croissants", new BigDecimal("4.49"), 18, 5, bakery, "Parisian Bakehouse", "+1-800-555-0167"));
            productRepository.save(new Product("SKU-BAGL01", "Everything Toasted Bagels 6pk", "New York style boiled & baked bagels", new BigDecimal("3.79"), 30, 8, bakery, "Golden Crust", "+1-800-555-0166"));
            productRepository.save(new Product("SKU-MUFN01", "Wild Blueberry Muffins 4pk", "Freshly baked muffins with real berries", new BigDecimal("4.99"), 15, 5, bakery, "Sweet Bakery Co", "+1-800-555-0168"));
            productRepository.save(new Product("SKU-TORT01", "Soft Flour Tortillas 10pk", "Authentic soft white flour tortillas", new BigDecimal("2.49"), 40, 10, bakery, "Sun Tortillas", "+1-800-555-0169"));
            productRepository.save(new Product("SKU-DNUT01", "Cinnamon Sugar Donuts 6pk", "Hand-crafted cake donuts coated in cinnamon", new BigDecimal("3.99"), 20, 5, bakery, "Sweet Bakery Co", "+1-800-555-0168"));

            // Meat & Seafood (6 items)
            Product p6 = productRepository.save(new Product("SKU-CHCK01", "Boneless Chicken Breast 1kg", "Fresh skinless boneless chicken breast", new BigDecimal("9.99"), 40, 12, meat, "Farm Fresh Poultry", "+1-800-555-0155"));
            productRepository.save(new Product("SKU-SLMN01", "Wild Atlantic Salmon Fillet 500g", "Fresh skin-on Atlantic salmon fillet", new BigDecimal("12.99"), 15, 4, meat, "Ocean Catch Seafood", "+1-800-555-0156"));
            productRepository.save(new Product("SKU-BEEF01", "Ground Angus Beef 90/10 1kg", "Fresh lean ground Angus beef", new BigDecimal("11.49"), 25, 6, meat, "Prime Ranch Meats", "+1-800-555-0157"));
            productRepository.save(new Product("SKU-SHRM01", "Jumbo Frozen Peeled Shrimp 500g", "Raw deveined tail-on jumbo shrimp", new BigDecimal("10.89"), 20, 5, meat, "Ocean Catch Seafood", "+1-800-555-0156"));
            productRepository.save(new Product("SKU-STEK01", "Ribeye Steak Prime Cut 400g", "USDA Prime bone-in marbling ribeye steak", new BigDecimal("14.99"), 12, 3, meat, "Prime Ranch Meats", "+1-800-555-0157"));
            productRepository.save(new Product("SKU-TURK01", "Organic Ground Turkey 500g", "Lean 93/7 organic ground turkey breast", new BigDecimal("7.49"), 22, 5, meat, "Farm Fresh Poultry", "+1-800-555-0155"));

            // 5. Seed Sample Orders
            CreateOrderRequest orderReq1 = new CreateOrderRequest(
                    "Alice Johnson", "alice@example.com", "+1-555-0101", "Please deliver before 5 PM",
                    List.of(new OrderItemRequest(p1.getId(), 2), new OrderItemRequest(p2.getId(), 3))
            );
            var order1 = orderService.createOrder(orderReq1);
            orderService.updateOrderStatus(order1.getId(), new OrderStatusUpdateRequest(OrderStatus.CONFIRMED));

            CreateOrderRequest orderReq2 = new CreateOrderRequest(
                    "Bob Smith", "bob@example.com", "+1-555-0102", "Standard delivery",
                    List.of(new OrderItemRequest(p4.getId(), 1), new OrderItemRequest(p6.getId(), 2))
            );
            orderService.createOrder(orderReq2);
        }
    }
}
