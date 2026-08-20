package com.example.pricetag.features.order.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.dto.PaginationRequestDto;
import com.example.pricetag.features.order.dto.requestDto.CancelOrderRequestDto;
import com.example.pricetag.features.order.dto.requestDto.CreateOrderRequestDto;
import com.example.pricetag.features.order.dto.responseDto.CreateOrderResponseDto;
import com.example.pricetag.features.order.dto.responseDto.OrderDetailsResponseDto;
import com.example.pricetag.features.order.dto.responseDto.OrderSummaryResponseDto;
import com.example.pricetag.features.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<CommonResponseDto<CreateOrderResponseDto>> createOrder(
            @Valid
            @RequestBody
            CreateOrderRequestDto createOrderRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(createOrderRequestDto));
    }

    @GetMapping
    public ResponseEntity<CommonResponseDto<List<OrderSummaryResponseDto>>> getAllOrdersByUser(
            @RequestParam(defaultValue = "1",
                    required = false)
            int page,
            @RequestParam(defaultValue = "10",
                    required = false)
            int limit,
            @RequestParam(name = "sortBy",
                    required = false)
            String sortBy,
            @RequestParam(name = "order",
                    required = false)
            String order) {
        PaginationRequestDto paginationRequestDto = PaginationRequestDto
                .builder()
                .page(page)
                .limit(limit)
                .sortBy(sortBy)
                .order(order)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.getAllOrdersByUser(paginationRequestDto));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<CommonResponseDto<OrderDetailsResponseDto>> getSingleOrder(
            @PathVariable
            String orderNumber) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.getSingleOrderDetailsByUser(orderNumber));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<CommonResponseDto<OrderDetailsResponseDto>> cancelOrder(
            @PathVariable
            String orderNumber,
            @RequestBody
            CancelOrderRequestDto cancelOrderRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.cancelOrder(orderNumber, cancelOrderRequestDto));
    }
}
