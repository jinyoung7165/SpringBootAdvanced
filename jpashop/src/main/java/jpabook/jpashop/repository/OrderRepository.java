package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jpabook.jpashop.domain.QMember.*;
import static jpabook.jpashop.domain.QOrder.*;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query; //queryDSL

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {//order를 검색하고 member랑 join
        /*return em.createQuery("select o from Order o join o.member m" +
                        "where o.status =:status" +
                        "and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000)//최대 1000건
                .getResultList();
         */
        String jpql = "select o from Order o join o.member m";
        //동적쿼리를 문자열로 생성하는 것은 번거롭다
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }
    /*
    JPA Criteria -> jpa가 동적쿼리를 build해주는 표준 ->이것도 실무에서 안 씀
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000 건
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() { //fetch(jpa문법) join
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() { //order 1->M orderItems->item
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class
        ).getResultList();
        //order:2 row, orderitems:4 row. 결과적으로 같은 order(2배) 중복돼버린 4row 반환
        //"1대 다 JOIN"=> fetch join + select distinct 키워드 사용 =>
        //JPA에선 order객체(id같음) 2개 반환(orderId같은 order Entity객체마다 orderItems담아줌)
        //but, 여전히 DB에선 각 row(order+orderItems)가 모두 다르기 때문에 4row 출력
        //암튼 1쿼리 날아감, but 1:M fetch join 시 페이징 불가 단점!!!!!
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) { //XToOne을 fetch join해서 paging
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDtos() {//jpa select는 entity, embeddable(value type)만 가능 -> Dto반환하려면 new!
        return em.createQuery(
                        "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }


    private List<OrderQueryDto> findOrders() { //toOne관계 한번에 조회
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class
        ).getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) { //toN관계 orderItems조회
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId",
                        OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    //루트 1번, 컬렉션 N번(별도)
    //단건 조회에서 많이 사용
    public List<OrderQueryDto> findOrderQueryDtos() {
        //루트 조회(toOne 한번에 조회)
        List<OrderQueryDto> result = findOrders();

        //루프 돌며 OrderItemQueryDto를 생성해 컬렉션 추가
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }


    private List<Long> toOrderIds(List<OrderQueryDto> result) { //IN 연산을 위한 orderIds
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) { //!!in연산 -> orderItems찾는 쿼리 1번
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }

    //최적화 -> 루트 1번, 컬렉션 1번
    //데이터 한꺼번에 처리할 때
    public List<OrderQueryDto> findAllByDto_optimization() {
        //루트 조회(toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();

        //orderItem 컬렉션을 MAP 한번에 조회
        Map<Long, List<OrderItemQueryDto>> orderItemMap =
                findOrderItemMap(toOrderIds(result));

        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행X)
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }




    /*
    JPA QueryDSL
     */

    public List<Order> findAll(OrderSearch orderSearch) {//회원이름, 주문상태

        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();

    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null){ //null이면 where절에서 안 씀
            return null;
        }
        return order.status.eq(statusCond);
    }

    private BooleanExpression nameLike(String nameCond) {
        if (!StringUtils.hasText(nameCond)) {
            return null;
        }
        return member.name.like(nameCond);
    }

}
