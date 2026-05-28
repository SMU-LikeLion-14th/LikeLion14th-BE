package com.project.likelion14thbe.domain.order.controller;

import com.project.likelion14thbe.domain.order.dto.request.OrderReqDTO;
import com.project.likelion14thbe.domain.order.dto.response.OrderResDTO;
import com.project.likelion14thbe.domain.order.service.command.OrderCommandService;
import com.project.likelion14thbe.domain.order.service.query.OrderQueryService;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Order", description = "주문 관련 API")
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @PostMapping("")
    @Operation(summary = "주문 생성")
    public CustomResponse<OrderResDTO.OrderCreateResDto> createOrder(
            @RequestBody OrderReqDTO.CreateOrderReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResDTO.OrderCreateResDto response = orderCommandService.createOrder(request, userDetails.getMemberId());
        return CustomResponse.onSuccess(HttpStatus.CREATED,response);
    }

    @GetMapping("/me")
    @Operation(summary = "내 주문 목록 조회")
    public CustomResponse<List<OrderResDTO.OrderHistoryRes>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.onSuccess(orderQueryService.getMyOrders(userDetails.getMemberId()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "내 주문 상세 조회")
    public CustomResponse<OrderResDTO.OrderDetailResDto> getMyOrderDetail(@PathVariable Long orderId) {
        return CustomResponse.onSuccess(orderQueryService.getOrder(orderId));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 변경")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomResponse<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderReqDTO.UpdateOrderStatusReq request
    ) {
        orderCommandService.updateOrderStatus(orderId, request.getStatus());
        return CustomResponse.onSuccess("주문상태가 변경되었습니다.");
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소")
    public CustomResponse<String> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        orderCommandService.cancelOrder(orderId, userDetails.getMemberId());
        return CustomResponse.onSuccess("주문이 취소되었습니다.");
    }

    @GetMapping("")
    @Operation(summary = "전체 주문 목록 조회")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomResponse<List<OrderResDTO.OrderHistoryRes>> getOrders() {
        return CustomResponse.onSuccess(orderQueryService.getOrderList());
    }
}