package com.tathang.example304.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tathang.example304.model.*;
import com.tathang.example304.security.services.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final PromotionService promotionService;
    private final ReportService reportService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final RoleService roleService;

    public AdminController(ProductService productService, CategoryService categoryService,
            PromotionService promotionService, ReportService reportService,
            FileStorageService fileStorageService, UserService userService, RoleService roleService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.promotionService = promotionService;
        this.reportService = reportService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.roleService = roleService;
    }

    // === PRODUCT MANAGEMENT ===

    // === GET PRODUCTS ===
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Long categoryId,
            @RequestParam Integer stockQuantity,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            Product product = productService.createProduct(
                    name, description, BigDecimal.valueOf(price),
                    categoryId, stockQuantity, imageUrl);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Long categoryId,
            @RequestParam Integer stockQuantity,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            Product product = productService.updateProduct(
                    id, name, description, BigDecimal.valueOf(price),
                    categoryId, stockQuantity, imageUrl);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // === CATEGORY MANAGEMENT ===

    // === GET CATEGORIES ===
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        try {
            Optional<Category> category = categoryService.getCategoryById(id);
            return category.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            Category category = categoryService.createCategory(name, description, imageUrl);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            Category category = categoryService.updateCategory(id, name, description, imageUrl);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === DELETE CATEGORY ===
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa danh mục: " + e.getMessage());
        }
    }

    // === PROMOTION MANAGEMENT ===

    // === GET PROMOTIONS ===
    @GetMapping("/promotions")
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === GET PROMOTION BY ID ===
    @GetMapping("/promotions/{id}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable Long id) {
        try {
            Promotion promotion = promotionService.getPromotionById(id);
            return promotion != null ? ResponseEntity.ok(promotion) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === GET PROMOTION PRODUCT DETAILS ===
    @GetMapping("/promotions/{id}/product-details")
    public ResponseEntity<List<PromotionProduct>> getPromotionProductDetails(@PathVariable Long id) {
        try {
            List<PromotionProduct> promotionProducts = promotionService.getPromotionProducts(id);
            return ResponseEntity.ok(promotionProducts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === GET PRODUCTS IN PROMOTION ===
    @GetMapping("/promotions/{id}/products")
    public ResponseEntity<List<Product>> getProductsInPromotion(@PathVariable Long id) {
        try {
            List<Product> products = promotionService.getProductsInPromotion(id);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === GET PRODUCTS NOT IN PROMOTION ===
    @GetMapping("/promotions/{id}/products/not-in-promotion")
    public ResponseEntity<List<Product>> getProductsNotInPromotion(@PathVariable Long id) {
        try {
            List<Product> products = promotionService.getProductsNotInPromotion(id);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === TOGGLE PROMOTION STATUS ===
    @PatchMapping("/promotions/{id}/status")
    public ResponseEntity<?> togglePromotionStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            Promotion promotion = promotionService.togglePromotionStatus(id, isActive);
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể cập nhật trạng thái khuyến mãi: " + e.getMessage());
        }
    }

    // === ADD MULTIPLE PRODUCTS TO PROMOTION ===
    @PostMapping("/promotions/{promotionId}/products")
    public ResponseEntity<?> addMultipleProductsToPromotion(
            @PathVariable Long promotionId,
            @RequestBody List<Long> productIds) {
        try {
            boolean added = promotionService.addMultipleProductsToPromotion(promotionId, productIds);
            return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể thêm sản phẩm vào khuyến mãi: " + e.getMessage());
        }
    }

    // === CREATE PROMOTION ===
    @PostMapping("/promotions")
    public ResponseEntity<?> createPromotion(@RequestBody Promotion promotion) {
        try {
            Promotion created = promotionService.createPromotion(promotion);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể tạo khuyến mãi: " + e.getMessage());
        }
    }

    // === UPDATE PROMOTION ===
    @PutMapping("/promotions/{id}")
    public ResponseEntity<?> updatePromotion(
            @PathVariable Long id,
            @RequestBody Promotion promotion) {
        try {
            Promotion updated = promotionService.updatePromotion(id, promotion);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể cập nhật khuyến mãi: " + e.getMessage());
        }
    }

    // === REMOVE MULTIPLE PRODUCTS FROM PROMOTION ===
    @DeleteMapping("/promotions/{promotionId}/products")
    public ResponseEntity<?> removeMultipleProductsFromPromotion(
            @PathVariable Long promotionId,
            @RequestBody List<Long> productIds) {
        try {
            boolean removed = promotionService.removeMultipleProductsFromPromotion(promotionId, productIds);
            return removed ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa sản phẩm khỏi khuyến mãi: " + e.getMessage());
        }
    }

    // === DELETE PROMOTION ===
    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id) {
        try {
            boolean deleted = promotionService.deletePromotion(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa khuyến mãi: " + e.getMessage());
        }
    }

    // === ADD PRODUCT TO PROMOTION ===
    @PostMapping("/promotions/{promotionId}/products/{productId}")
    public ResponseEntity<?> addProductToPromotion(
            @PathVariable Long promotionId,
            @PathVariable Long productId) {
        try {
            boolean added = promotionService.addProductToPromotion(promotionId, productId);
            return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể thêm sản phẩm vào khuyến mãi: " + e.getMessage());
        }
    }

    // === REMOVE PRODUCT FROM PROMOTION ===
    @DeleteMapping("/promotions/{promotionId}/products/{productId}")
    public ResponseEntity<?> removeProductFromPromotion(
            @PathVariable Long promotionId,
            @PathVariable Long productId) {
        try {
            boolean removed = promotionService.removeProductFromPromotion(promotionId, productId);
            return removed ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa sản phẩm khỏi khuyến mãi: " + e.getMessage());
        }
    }

    // === GET ACTIVE PROMOTIONS ===
    @GetMapping("/promotions/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        try {
            List<Promotion> promotions = promotionService.getActivePromotions();
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === GET CURRENT PROMOTIONS ===
    @GetMapping("/promotions/current")
    public ResponseEntity<List<Promotion>> getCurrentPromotions() {
        try {
            List<Promotion> promotions = promotionService.getCurrentPromotions();
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === CLEAR ALL PRODUCTS FROM PROMOTION ===
    @DeleteMapping("/promotions/{promotionId}/products/clear")
    public ResponseEntity<?> clearAllProductsFromPromotion(@PathVariable Long promotionId) {
        try {
            promotionService.clearAllProductsFromPromotion(promotionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa tất cả sản phẩm khỏi khuyến mãi: " + e.getMessage());
        }
    }

    // === REPORTS ===
    @GetMapping("/reports/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardReport() {
        Map<String, Object> report = reportService.getDashboardReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // Parse dates and get report
        Map<String, Object> report = reportService.getRevenueReport(
                LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
        return ResponseEntity.ok(report);
    }

    // === STAFF MANAGEMENT ===

    // === GET ALL STAFF ===
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/staffs")
    public ResponseEntity<List<User>> getAllStaffs() {
        try {
            List<User> staffs = userService.getAllStaffs();
            return ResponseEntity.ok(staffs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === GET STAFF BY ID ===
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/staffs/{id}")
    public ResponseEntity<User> getStaffById(@PathVariable Long id) {
        try {
            User staff = userService.getUserById(id);
            return staff != null ? ResponseEntity.ok(staff) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === CREATE STAFF ===
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staffs")
    public ResponseEntity<?> createStaff(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam List<String> roles) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            User staff = userService.createStaff(
                    username, email, password, fullName, phone, address, imageUrl, roles);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể tạo nhân viên: " + e.getMessage());
        }
    }

    // === UPDATE STAFF ===
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/staffs/{id}")
    public ResponseEntity<?> updateStaff(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam List<String> roles) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }

            User staff = userService.updateStaff(
                    id, username, email, password, fullName, phone, address, imageUrl, roles);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể cập nhật nhân viên: " + e.getMessage());
        }
    }

    // === DELETE STAFF ===
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/staffs/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            boolean deleted = userService.deleteUser(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa nhân viên: " + e.getMessage());
        }
    }

    // === TOGGLE STAFF STATUS ===
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/staffs/{id}/status")
    public ResponseEntity<?> toggleStaffStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            User staff = userService.toggleUserStatus(id, isActive);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể cập nhật trạng thái: " + e.getMessage());
        }
    }

    // === GET ALL ROLES ===
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}