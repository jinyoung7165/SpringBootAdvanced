package jpabook.jpashop.Service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@Transactional(readOnly = true) //JPA 데이터조작은 트랜잭션 안에서 수행돼야함
@RequiredArgsConstructor
public class MemberService {

    //@Autowired field injection -> repository 변경 어려움
    private final MemberRepository memberRepository; //생성자 주입 적었는지 체크할 수 있기 때문에 final로 권장

    /*@Autowired //생성자 하나라 @Autowired생략 가능
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }*/
    //@RequiredArgsConstructor 적어주면 Class내의 final변수들을 생성자 주입해줌

    //회원 가입
    @Transactional //우선권을 가짐.읽기쓰기 모두 가능
    public Long join(Member member) {
        validateDuplicateMember(member);//이름이 같은 멤버 검증
        memberRepository.save(member);
        return member.getId(); //자동생성된 PK
    }

    private void validateDuplicateMember(Member member) { //이걸로 검증해도 멀티스레드->동시에 요청 날리면 둘 다 생성 가능해서 DB에 unique name제한 걸어주는 게 좋음
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long id) {
        return memberRepository.findOne(id);
    }

    @Transactional //있어야 쓰기 가능
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id); //영속 콘텍스트 갖고옴
        member.setName(name); //Transaction끝나는 시점에 영속 콘텍스트 변경 감지
    }
}
