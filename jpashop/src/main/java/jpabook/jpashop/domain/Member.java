package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded //내장 타입을 포함했다
    private Address address;

    //한 사람이 많을 걸 주문
    @OneToMany(mappedBy = "member") //order의 member 필드에 의해 매핑됨
    private List<Order> orders = new ArrayList<>();

}