package com.tathang.example304.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tathang.example304.model.*;
import com.tathang.example304.payload.request.*;
import com.tathang.example304.repository.UserRepository;
import com.tathang.example304.security.services.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeeController {

    private final OrderService orderService;
    private final CoffeeTableService tableService;
    private final BillService billService;
    private final ProductService productService;
    private final UserRepository userRepository; // Thêm dòng này

    public EmployeeController(OrderService orderService, CoffeeTableService tableService,
            BillService billService, ProductService productService,
            UserRepository userRepository) { // Thêm parameter này
        this.orderService = orderService;
        this.tableService = tableService;
        this.billService = billService;
        this.productService = productService;
        this.userRepository = userRepository; // Thêm dòng này
    }

    // ===================== ORDERS =====================

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // ✅ Tạo order (nhận JSON body)
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrder(request.getTableId(), request.getEmployeeId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Thêm món vào order
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<?> addOrderItem(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequest request) {
        try {
            System.out.println("📥 Received add item request:");
            System.out.println("   Order ID: " + orderId);
            System.out.println("   Product ID: " + request.getProductId());
            System.out.println("   Quantity: " + request.getQuantity());

            // Validate input
            if (request.getProductId() == null) {
                return ResponseEntity.badRequest().body("Product ID is required");
            }
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Quantity must be greater than 0");
            }

            Order order = orderService.addItemToOrder(orderId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error adding item: " + e.getMessage());
        }
    }

    // ✅ Cập nhật trạng thái order
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusRequest request) {
        try {
            Order order = orderService.updateOrderStatus(orderId, request.getStatus());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Lấy order theo bàn
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders/table/{tableId}")
    public ResponseEntity<List<Order>> getOrdersByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(orderService.getOrdersByTable(tableId));
    }

    // ✅ Lấy order theo ID
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(orderId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Xóa order
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok().body("{\"message\": \"Đơn hàng đã được xóa thành công\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Lỗi khi xóa đơn hàng: " + e.getMessage() + "\"}");
        }
    }

    /** ✅ Thanh toán đơn hàng */
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId,
            @RequestBody PaymentRequest request) {
        try {
            // Convert String → Enum
            Bill.PaymentMethod method = Bill.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());

            Bill bill = billService.createBill(orderId, method);
            Long tableId = bill.getOrder().getTable().getId();
            tableService.updateTableStatus(tableId, CoffeeTable.TableStatus.FREE);

            return ResponseEntity.ok(bill);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ Sai phương thức thanh toán: " + request.getPaymentMethod());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Lỗi khi thanh toán: " + e.getMessage());
        }
    }

    // ===================== TABLES =====================

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/tables")
    public ResponseEntity<List<CoffeeTable>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    // ✅ Tạo bàn mới
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/tables")
    public ResponseEntity<CoffeeTable> createTable(@RequestBody TableRequest request) {
        try {
            CoffeeTable table = new CoffeeTable();
            table.setTableName(request.getName());
            table.setNumber(request.getNumber() != null ? request.getNumber() : 1);
            table.setCapacity(request.getCapacity() != null ? request.getCapacity() : 4);
            table.setStatus(CoffeeTable.TableStatus.FREE);
            return ResponseEntity.ok(tableService.createTable(table));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Cập nhật trạng thái bàn
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PatchMapping("/tables/{tableId}/status")
    public ResponseEntity<CoffeeTable> updateTableStatus(
            @PathVariable Long tableId,
            @RequestBody TableStatusRequest request) {
        try {
            System.out.println("📥 Received table status update:");
            System.out.println("   Table ID: " + tableId);
            System.out.println("   New Status: " + request.getStatus());

            CoffeeTable table = tableService.updateTableStatus(tableId, request.getStatus());
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ===================== BILLS =====================

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/bills/order/{orderId}")
    public ResponseEntity<Bill> getBillByOrder(@PathVariable Long orderId) {
        Bill bill = billService.getBillByOrderId(orderId);
        return bill != null ? ResponseEntity.ok(bill) : ResponseEntity.notFound().build();
    }

    // ===================== PRODUCTS =====================

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PutMapping("/orders/{orderId}/items/{productId}")
    public ResponseEntity<Order> updateOrderItemQuantity(
            @PathVariable Long orderId,
            @PathVariable Long productId,
            @RequestBody OrderItemRequest request) {
        try {
            Order order = orderService.updateOrderItemQuantity(orderId, productId, request.getQuantity());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Xóa item khỏi order
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @DeleteMapping("/orders/{orderId}/items/{productId}")
    public ResponseEntity<Order> removeOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long productId) {
        try {
            Order order = orderService.removeItemFromOrder(orderId, productId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Xác nhận thanh toán MoMo thành công
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/confirm-momo")
    public ResponseEntity<?> confirmMomoPayment(@PathVariable Long orderId) {
        try {
            System.out.println("🎉 Confirming MoMo payment for order: " + orderId);

            // Sửa lại: dùng getOrderById thay vì findById
            Order order = orderService.getOrderById(orderId);

            // Cập nhật trạng thái order thành PAID
            order.setStatus(Order.OrderStatus.PAID);

            // Sửa lại: dùng updateOrderStatus hoặc tạo order mới
            Order savedOrder = orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);

            System.out.println("✅ Order status updated to PAID for order: " + orderId);

            // Cập nhật trạng thái bàn về FREE
            if (order.getTable() != null) {
                CoffeeTable table = order.getTable();
                tableService.updateTableStatus(table.getId(), CoffeeTable.TableStatus.FREE);
                System.out.println("✅ Table status updated to FREE for table: " + table.getId());
            }

            // Tạo bill cho order này
            Bill bill = billService.createBill(orderId, Bill.PaymentMethod.MOMO);
            System.out.println("✅ Bill created for MoMo payment: " + bill.getId());

            return ResponseEntity.ok(java.util.Map.of( // THÊM java.util. trước Map
                    "success", true,
                    "message", "Xác nhận thanh toán MoMo thành công",
                    "order", savedOrder,
                    "bill", bill));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of( // THÊM java.util. trước Map
                    "success", false,
                    "message", "Lỗi xác nhận thanh toán: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentEmployeeProfile(Authentication authentication) {
        try {
            // Kiểm tra authentication
            if (authentication == null) {
                return ResponseEntity.status(401).body("Unauthorized: No authentication found");
            }

            String username = authentication.getName();
            System.out.println("🔐 Getting profile for: " + username);

            // Tìm user theo username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            // Tạo response object
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));

            System.out.println("✅ Profile retrieved successfully for: " + username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error getting profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error getting profile: " + e.getMessage());
        }
    }

    // ✅ Lấy order items theo order ID
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders/{orderId}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        try {
            List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);
            return ResponseEntity.ok(orderItems);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
