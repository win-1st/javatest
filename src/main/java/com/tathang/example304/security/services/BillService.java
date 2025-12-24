package com.tathang.example304.security.services;

import org.springframework.stereotype.Service;

import com.tathang.example304.model.Bill;
import com.tathang.example304.model.Order;
import com.tathang.example304.repository.BillRepository;
import com.tathang.example304.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;

    public BillService(BillRepository billRepository, OrderRepository orderRepository) {
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
    }

    public Bill createBill(Long orderId, Bill.PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Bill bill = new Bill(order, order.getTotalAmount());
        bill.setPaymentMethod(paymentMethod);
        bill.setPaymentStatus(Bill.PaymentStatus.COMPLETED);
        bill.setIssuedAt(LocalDateTime.now());

        // Update order status to PAID
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);

        return billRepository.save(bill);
    }

    public Bill updatePaymentStatus(Long billId, Bill.PaymentStatus paymentStatus) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        bill.setPaymentStatus(paymentStatus);
        return billRepository.save(bill);
    }

    public Bill getBillByOrderId(Long orderId) {
        Optional<Bill> bill = billRepository.findByOrderId(orderId);
        return bill.orElse(null);
    }
}