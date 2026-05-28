package com.project.likelion14thbe.domain.order.service.command;

import com.project.likelion14thbe.domain.order.dto.request.OrderReqDTO;
import com.project.likelion14thbe.domain.order.dto.response.OrderResDTO;

public interface OrderCommandService {
    OrderResDTO.OrderCreateResDto createOrder(OrderReqDTO.CreateOrderReq request, Long memberId);
    void updateOrderStatus(Long orderId, String status);
    void cancelOrder(Long orderId, Long memberId);
}