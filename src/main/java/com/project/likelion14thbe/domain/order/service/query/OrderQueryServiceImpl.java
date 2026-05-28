package com.project.likelion14thbe.domain.order.service.query;

import com.project.likelion14thbe.domain.order.converter.OrderConverter;
import com.project.likelion14thbe.domain.order.dto.response.OrderResDTO;
import com.project.likelion14thbe.domain.order.entity.Order;
import com.project.likelion14thbe.domain.order.exception.OrderErrorCode;
import com.project.likelion14thbe.domain.order.exception.OrderException;
import com.project.likelion14thbe.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public OrderResDTO.OrderDetailResDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderConverter.toOrderDetailResDto(order);
    }

    @Override
    public List<OrderResDTO.OrderHistoryRes> getOrderList() {
        return orderRepository.findAll().stream()
                .map(OrderConverter::toOrderHistoryRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResDTO.OrderHistoryRes> getMyOrders(Long memberId) {
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderConverter::toOrderHistoryRes)
                .collect(Collectors.toList());
    }
}