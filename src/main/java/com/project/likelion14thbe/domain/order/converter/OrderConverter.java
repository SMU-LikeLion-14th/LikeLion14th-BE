package com.project.likelion14thbe.domain.order.converter;

import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.order.dto.request.OrderReqDTO;
import com.project.likelion14thbe.domain.order.dto.response.OrderResDTO;
import com.project.likelion14thbe.domain.order.entity.Order;
import com.project.likelion14thbe.domain.order.entity.OrderItem;
import com.project.likelion14thbe.domain.order.entity.OrderStatus;
import com.project.likelion14thbe.domain.product.entity.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderConverter {
    /**
     * [주문 생성 요청 DTO -> 주문 엔티티] 변환
     */
    public static Order toOrder(OrderReqDTO.CreateOrderReq request, List<OrderItem> orderItems, Member member) {
        Order order = Order.builder()
                .totalAmount(request.getTotalAmount())
                .date(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .member(member)
                .orderItems(orderItems)
                .address(request.getAddress())
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        return order;
    }

    /**
     * [주문 엔티티 -> 주문 완료 결과 응답 DTO] 변환
     */
    public static OrderResDTO.OrderCreateResDto toOrderCreateResDto(Order order) {
        return OrderResDTO.OrderCreateResDto.builder()
                .orderId(order.getOrderId())
                .build();
    }
    /**
     * [주문 엔티티 -> 주문 상세 조회 응답 DTO] 변환
     */
    public static OrderResDTO.OrderDetailResDto toOrderDetailResDto(Order order) {
        return OrderResDTO.OrderDetailResDto.builder()
                .id(order.getOrderId())
                .orderId(order.getOrderId())
                .orderDate(order.getDate() != null ? order.getDate().toString() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .recipientName(order.getMember() != null ? order.getMember().getName() : "수령인 없음")
                .address(order.getAddress() != null ? order.getAddress() : "배송지 없음")
                .orderItems(order.getOrderItems() != null ?
                        order.getOrderItems().stream()
                                .map(OrderConverter::toOrderItemRes)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     ** [주문 엔티티 -> 주문 이력 리스트 응답 DTO] 변환
     */
    public static OrderResDTO.OrderHistoryRes toOrderHistoryRes(Order order) {
        return OrderResDTO.OrderHistoryRes.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getDate() != null ? order.getDate().toString() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .orderItems(order.getOrderItems() != null ?
                        order.getOrderItems().stream()
                                .map(OrderConverter::toOrderItemRes)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * * [주문 요청 상품 개별 DTO -> 주문 상품 엔티티] 변환
     */
    public static OrderItem toOrderItem(OrderReqDTO.OrderItemReq itemReq, Product product) {
        return OrderItem.builder()
                .product(product)
                .quantity(itemReq.getQuantity())
                .orderPrice((long) product.getPrice())
                .build();
    }

    /**
     * * [주문 상품 엔티티 -> 화면 표시용 주문 상품 응답 DTO] 변환
     */
    public static OrderResDTO.OrderItemRes toOrderItemRes(OrderItem orderItem) {
        return OrderResDTO.OrderItemRes.builder()
                .productName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : "상품 정보 없음")
                .quantity(orderItem.getQuantity())
                .price(orderItem.getOrderPrice() != null ? orderItem.getOrderPrice().intValue() : 0)
                .build();
    }



}