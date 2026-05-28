package com.project.likelion14thbe.domain.order.service.query;

import com.project.likelion14thbe.domain.order.dto.response.OrderResDTO;
import java.util.List;

public interface OrderQueryService {
    OrderResDTO.OrderDetailResDto getOrder(Long id);
    List<OrderResDTO.OrderHistoryRes> getOrderList();
    List<OrderResDTO.OrderHistoryRes> getMyOrders(Long memberId);
}