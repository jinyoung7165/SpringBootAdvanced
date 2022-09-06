package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable //내장 타입
@Getter
public class Address { //변경 불가능하게 설계하자
//Setter는 제거하고, 생성자에서 값을 모두 초기화
    private String city;
    private String street;
    private String zipcode;

    //JPA에서 Embeddable은 기본생성자를 public/protected로 설정해야함(리플렉션 사용할수 있게 기본생성자 있어야함)
    protected Address() {} //함부로 생성하지 말자

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
