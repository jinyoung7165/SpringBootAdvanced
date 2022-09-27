package jpabook.jpashop.Service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class) //junit이랑 스프링이랑 같이 실행
@SpringBootTest //스프링 컨테이너 안의 빈 모두 가져옴
@Transactional //ROllback-> 결과적으로 db에 쿼리 안날림
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @PersistenceContext EntityManager em;

    @Test
    //@Rollback(false) //commit날리고 롤백 안 하고 싶으면 추가
    public void 회원가입() throws Exception{

            Member member = new Member();
            member.setName("KIM");

            Long saveId = memberService.join(member);

            em.flush(); /*
            영속성 컨텍스트의 변경 내용을 DB 에 반영하는 것을 말한다.
            commit이 일어날 때 flush가 동작하는데, 이때 쓰기 지연 저장소에 쌓아 놨던 SQL들이 DB에 날라간다.
            */
            assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test(expected = IllegalStateException.class) //IllegalState예외가 발생하면 성공
    //@Rollback(false) //commit날리고 롤백 안 하고 싶으면 추가
    public void 중복회원예외() throws Exception{

        Member member1 = new Member();
        member1.setName("KIM1");

        Member member2 = new Member();
        member2.setName("KIM1");

        memberService.join(member1);
        memberService.join(member2); //예외가 발생한다
        
        fail("예외가 발생해야 한다"); //위에서 예상한대로 예외 안 생기면 fail실행


    }
}