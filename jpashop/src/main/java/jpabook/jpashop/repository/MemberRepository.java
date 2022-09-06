package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository { //Bean등록

    //@PersistenceContext 안 쓰고-RequiredArgsConstructor로 주입함
    private final EntityManager em; //JPA em주입

    public void save(Member member) {
        em.persist(member);  //Transaction이 commit되는 순간 DB에 쿼리->저장
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id); //type,PK
    }

    public List<Member> findAll() { //JPQL의 쿼리 대상은 테이블이 아닌 엔티티
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
