package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

    private final Environment env;
    private final OrderService orderService;

    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in Catalog Service on PORT %s",
                env.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,@RequestBody RequestOrder orderDetails){
        ModelMapper mapper= new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        OrderDto createOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createOrder, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> createOrder(@PathVariable("userId") String userId){
        Iterable<OrderEntity> ordersByUserId = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();
        ordersByUserId.forEach(v -> {
            result.add(new ModelMapper().map(v,ResponseOrder.class));
        });


        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
