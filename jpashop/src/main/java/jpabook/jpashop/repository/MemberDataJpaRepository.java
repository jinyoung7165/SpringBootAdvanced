package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberDataJpaRepository extends JpaRepository<Member, Long> {
    //select m from Member m where m.name = ?
    List<Member> findByName(String name); //메소드 이름 findBy~ 규칙 지키면 JPQL 생성됨
}
