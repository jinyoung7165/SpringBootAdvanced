package jpabook.jpashop.domain.Item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //정규화 덜한 SINGLE TABLE전략으로 상속
@DiscriminatorColumn(name = "dtype") //구현체마다 다른 dtype 값을 가짐
@Getter @Setter
public abstract class Item { //구현체 가짐

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;
    
    private String name;
    private int price;
    private int stockQuantity; //재고

    @ManyToMany(mappedBy = "items") //category의 items 필드에 의해 매핑됨
    private List<Category> categories = new ArrayList<>();
    //item-orderItem 단방향이기 때문에 item에서는 orderItem에 접근 불가 - 매핑 부분 필요없음

    //==비즈니스 로직==//
    //setter 외부(레포지토리)에서 호출하기보다는 엔티티 단에서 처리할 수 있는 로직은 엔티티 안에 만들기

    /**
     * stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity; //남은 수량
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
