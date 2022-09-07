package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    //@NotEmpty -> 어떤 request/response에선 이게 null일 수도 있음-> DTO마다 다르게 정의
    private String name;

    @Embedded //내장 타입을 포함했다
    private Address address;

    //한 사람이 많을 걸 주문
    @JsonIgnore //response body로 반환할 때 orders가 빠짐
    @OneToMany(mappedBy = "member") //order의 member 필드에 의해 매핑됨
    private List<Order> orders = new ArrayList<>();

}