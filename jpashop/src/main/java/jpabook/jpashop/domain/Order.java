package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") //order_by 메소드때문에 이름을 orders로 바꿔준다
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //생성메서드 제한
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) //주문을 위해선 Member필요하다(주입). Member가 주문하는 거라고 생각하지 말자
    @JoinColumn(name = "member_id") //주문한 회원과 매핑
    private Member member; //연관 관계의 주인

    //orderItem의 order field에 의해 매핑됨
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //cascade: orderItem 각각을 persist후에 따로 order persist 안 하고 orderItems에 담아 order를 persist하면 orderItem에게 persist전파
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) //order persist저장하면 delivery도 persist전파
    @JoinColumn(name = "delivery_id") //delivery와 매핑
    private Delivery delivery; //1대1의 경우, 더 많이 조회하는 쪽에 foregin key두자->연관관계의 주인

    private LocalDateTime orderDate; //주문시간
    //기본은 숫자인데, 중간에 뭐가 추가되면 잘못될 수 있기 때문에 반드시 이 부분 적어주자
    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER,CANCEL]

    //연관관계 -> 영속성 context랑 db에 영향 o, 객체에는 영향 x -> 연관관계 메소드를 작성해 따로 서로의 객체에 넣어줘야함
    //DB에 저장할 때는 한쪽에 CASCADE가 있으면 된다
    //==연관관계 메서드==양방향일 때 작성해줘야 함!!!//
    /*
    원래라면 바깥에서 새로 Member, Order생성할 때
    Member member = new Member();
    Order order = new Order();
    member.getOrders().add(order);
    order.setMember(member);
    다 적어줘야 하지만, 연관관계 메서드 작성해놓으면
    Member member = new Member();
    Order order = new Order();
    order.setMember(member);
     */
    public void setMember(Member member) { //주문을 위해선 Member필요하다(주입). Member가 주체라고 생각하지 말자
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==엔티티 생성 메서드==//
    //주문 생성 시 멤버, 배달, 주문 상품 등 필드 세팅
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) { //각각을 추가
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 메서드==//
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 성품은 취소가 불가능합니다");
        }

        this.setStatus(OrderStatus.CANCEL); //그렇지 않으면 취소
        for (OrderItem orderItem : orderItems) { //this.orderItems의 각 아이템마다 취소 -> 재고 수량 늘어남
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        /*
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) { //this.orderItems
            totalPrice += orderItem.getTotalPrice(); //각각의 가격을 가져옴
        }
        return totalPrice;
         */
        return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}
