package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XtoOne(ManyToOne, OneToOne)에서 성능최적화!!
 * Order
 * Order->Member
 * Order->Delivery
 * Order를 통해 members, delivery 조회 ->
 * 양방향이므로 Member, Delivery에도 orders존재
 * -> 원활한 객체 조회를 위해서는 Member, Delivery쪽의 orders에jsonignore필요
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        /*
        for (Order order : all) {//LAZY강제 초기화->null proxy 대신 값들 response
            order.getMember().getName(); //getmember시 여전히 proxy, getName시 쿼리 날아가서 가져옴=>강제초기화
            order.getDelivery().getAddress();
        }
         */
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        //ORDER 2개
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        //sql문 결과 row가 2(n)일 때, 2번 loop 돈다
        //결론: orders 1번 조회 쿼리 + member 2번 + delivery 2번 = 5번 쿼리 날아감
        //1 + n 문제 발생
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() { //fetch join -> 1번 쿼리 날림
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

    }

    @GetMapping("/api/v4/simple-orders") //entity -> dto 변환 과정 없앰(jpa->dto로 바로)
    public List<OrderSimpleQueryDto> ordersV4() { //fetch join -> 1번 쿼리 날림
        return orderRepository.findOrderDtos(); //재사용 힘들다는 단점. 성능은 좋음
    } //통계 조회 용은 따로 레포지토리 만든다

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY초기화
        }
    }


}
