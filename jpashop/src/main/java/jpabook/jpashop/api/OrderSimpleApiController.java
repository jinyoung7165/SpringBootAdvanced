package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
