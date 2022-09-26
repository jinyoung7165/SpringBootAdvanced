package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY) //order의 delivery field에 의해 매핑됨
    private Order order;

    @Embedded
    private Address address;

    //기본은 숫자인데, 중간에 뭐가 추가되면 잘못될 수 있기 때문에 반드시 이 부분 적어주자
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; //READY, COMP

}
