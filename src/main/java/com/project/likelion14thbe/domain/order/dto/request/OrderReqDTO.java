package com.project.likelion14thbe.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class OrderReqDTO {

    @Getter
    @Setter
    @Schema(description = "주문 생성 요청 DTO")
    public static class CreateOrderReq {

        @Schema(description = "주문 상품 목록")
        private List<OrderItemReq> items;

        @Schema(description = "배송지 주소", example = "서울시 성북구 삼선동 3가 162-1")
        private String address;

        @Schema(description = "결제 수단 (CARD, TRANSFER)", example = "CARD")
        private String paymentMethod;

        @Schema(description = "총 금액")
        private Integer totalAmount;

    }

    @Getter
    @Setter
    public static class OrderItemReq {
        @Schema(description = "상품 ID", example = "101")
        private Integer productId;

        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;
    }
    @Getter
    @Setter
    @Schema(description = "주문 상태 변경 요청 DTO")
    public static class UpdateOrderStatusReq {

        @Schema(description = "변경할 주문 상태", example = "SHIPPING")
        private String status;
    }

}