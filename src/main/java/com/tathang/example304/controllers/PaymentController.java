package com.tathang.example304.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tathang.example304.model.*;
import com.tathang.example304.payload.request.*;
import com.tathang.example304.security.services.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final OrderService orderService;
    private final BillService billService;
    private final CoffeeTableService tableService;
    private final MomoService momoService;

    public PaymentController(OrderService orderService, BillService billService,
            CoffeeTableService tableService, MomoService momoService) {
        this.orderService = orderService;
        this.billService = billService;
        this.tableService = tableService;
        this.momoService = momoService;
    }

    // ===================== MOMO PAYMENT =====================

    /**
     * Tạo thanh toán MoMo
     */
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomoPayment(@RequestBody MomoPaymentRequest request) {
        try {
            System.out.println("💰 Creating MoMo payment for order: " + request.getOrderId());

            // Kiểm tra order tồn tại
            Order order = orderService.getOrderById(request.getOrderId());
            if (order == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Order not found\"}");
            }

            // Tạo thanh toán MoMo
            Map<String, Object> paymentResult = momoService.createPayment(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getOrderInfo(),
                    request.getExtraData());

            return ResponseEntity.ok(paymentResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Failed to create MoMo payment: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán MoMo
     */
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/momo/status/{orderId}")
    public ResponseEntity<?> checkMomoPaymentStatus(@PathVariable Long orderId) {
        try {
            System.out.println("🔍 Checking MoMo payment status for order: " + orderId);

            Map<String, String> statusResult = momoService.checkPaymentStatus(orderId);
            return ResponseEntity.ok(statusResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Failed to check payment status: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Webhook nhận kết quả thanh toán từ MoMo (IPN)
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<?> momoIPN(@RequestBody Map<String, Object> momoResponse) {
        try {
            System.out.println("📱 Received MoMo IPN: " + momoResponse);

            // Xử lý kết quả thanh toán từ MoMo
            Map<String, String> result = momoService.processIPN(momoResponse);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"error\": \"IPN processing failed\"}");
        }
    }

    /**
     * Xác nhận thanh toán MoMo thành công
     */
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/momo/confirm/{orderId}")
    public ResponseEntity<?> confirmMomoPayment(@PathVariable Long orderId) {
        try {
            System.out.println("✅ Confirming MoMo payment for order: " + orderId);

            // Lấy order
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Order not found\"}");
            }

            // Tạo bill với phương thức MOMO
            Bill bill = billService.createBill(orderId, Bill.PaymentMethod.MOMO);

            // Cập nhật trạng thái bàn về trống
            Long tableId = order.getTable().getId();
            tableService.updateTableStatus(tableId, CoffeeTable.TableStatus.FREE);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment confirmed successfully");
            response.put("billId", bill.getId());
            response.put("orderId", orderId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Failed to confirm payment: " + e.getMessage() + "\"}");
        }
    }

   
    /**
     * API cập nhật trạng thái thanh toán thủ công (cho testing)
     */
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/momo/test-status/{orderId}")
    public ResponseEntity<?> testUpdateStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            momoService.updatePaymentStatus(orderId, status);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Updated order " + orderId + " to status: " + status);
            response.put("orderId", orderId.toString());
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Failed to update status\"}");
        }
    }
}