package com.project.likelion14thbe.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class OrderResDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 내역 응답 DTO")
    public static class OrderHistoryRes {

        @Schema(description = "주문 ID", example = "20260430-001")
        private Long orderId;

        @Schema(description = "주문 날짜", example = "2026-04-30T13:37:10")
        private String orderDate;

        @Schema(description = "총 주문 금액", example = "75000")
        private Integer totalAmount;

        @Schema(description = "주문 상태", example = "COMPLETED")
        private String status;

        @Schema(description = "주문 상품 상세 목록")
        private List<OrderItemRes> orderItems;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 상품 DTO")
    public static class OrderItemRes {
        @Schema(description = "상품명", example = "Minimal Stand")
        private String productName;

        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;

        @Schema(description = "상품 가격", example = "25000")
        private Integer price;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 상세 조회 응답 DTO")
    public static class OrderDetailResDto {

        @Schema(description = "주문 고유 ID", example = "1")
        private Long id;

        @Schema(description = "주문 번호")
        private  Long orderId;

        @Schema(description = "주문 날짜", example = "2026-04-30T13:37:10")
        private String orderDate;

        @Schema(description = "총 주문 금액", example = "75000")
        private Integer totalAmount;

        @Schema(description = "주문 상태", example = "COMPLETED")
        private String status;

        @Schema(description = "수령인 이름", example = "홍길동")
        private String recipientName;

        @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123")
        private String address;

        @Schema(description = "주문 상품 상세 목록")
        private List<OrderResDTO.OrderItemRes> orderItems;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 생성 응답 DTO")
    public static class OrderCreateResDto {

        @Schema(description = "주문 고유 PK ID", example = "1")
        private Long id;

        @Schema(description = "주문 번호", example = "20260430-001")
        private Long orderId;

        @Schema(description = "총 금액")
        private Integer totalAmount;
    }
}