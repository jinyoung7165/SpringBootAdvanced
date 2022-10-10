package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderQueryDto;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders") //Entity직접 노출(비추)
    public List<Order> ordersV1() { //order:orderitem 1:M
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) { //proxy 강제 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //각 item의 이름 가져옴
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() { //DTO로 변환
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() { //distint +fetch join(쿼리 1번) -> DTO로 변환
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }//collections를 fetch join으로 가져오면 paging불가!!(join으로 DB에서 order row가 늘어나 1:n에서 n이 기준이 됨)

    @GetMapping("/api/v3.1/orders") //paging+collections 조회하기
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); //Order에서 XtoOne관계 모두 fetch join
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))  //loop돌며 LAZY 초기화(쿼리 수 증가)
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v4/orders") //JPA에서 바로 collections를 DTO로 반환 (1+n)
    public List<OrderQueryDto> ordersV4() {
        return orderRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders") //JPA에서 바로 collections를 DTO로 반환1+1 (in연산)
    public List<OrderQueryDto> ordersV5() {
        return orderRepository.findAllByDto_optimization();
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //private List<OrderItem> orderItems; //Dto안에서 Entity를 반환해버림
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            //order.getOrderItems().stream().forEach(o -> o.getItem().getName()); //proxy 초기화
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {


        private String itemName; //상품명
        private int orderPrice; //가격
        private int count; //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
