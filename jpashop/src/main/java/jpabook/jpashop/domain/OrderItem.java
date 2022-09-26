package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.Item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "order_item") //아마 이름 정하는 거라 생략가능
@Getter @Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id") //item과 맵핑
    private Item item; //연관관계의 주인

    @JsonIgnore //양방향 관계 -> 한쪽에 jsonignore하지않으면 무한으로 객체를 찾아나감
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") //order과 맵핑
    private Order order; //연관관계의 주인

    private int orderPrice; //주문 가격
    private int count; //주문 수량

    protected OrderItem() { //@NoArgsConstructor(access = AccessLevel.PROTECTED)와 같음
        //->다른 곳에서 createOrderItem말고 new OrderItem();으로 생성하는 것을 막아줌
    }
    //==생성 메서드==//
    //주문->주문상품->상품(재고) 관리
    //주문 상품 생성 시 아이템 등 필드 세팅, 주문 상품은 아이템 관리 -> 재고 감소
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        
        item.removeStock(count); //주문 시 재고 감소
        return orderItem;
    }

    //==비즈니스 로직==//

    /**
     * 주문 취소
     */
    public void cancel() { //주문 취소 시 재고 수량 증가
        getItem().addStock(count); //Item 재고 연산
    }

    //==조회 로직==//

    /**
     * 주문 상품 전체 가격 조회
     */
    public int getTotalPrice() { //주문 수량 * 가격(할인될 수도)
        return getOrderPrice() * getCount();
    }
}
