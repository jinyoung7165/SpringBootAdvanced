package tdd.membership;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tdd.membership.entity.Membership;
import tdd.membership.enums.MembershipType;
import tdd.membership.repository.MembershipRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest //JPA Repository에 대한 Bean을 등록(Transactional, Runwith, Config포함)
public class MembershipRepositoryTest {

    @Autowired
    private MembershipRepository membershipRepository;

    /*@Test
    public void MembershipRepository가null이아님() { //빈이 잘 띄워지는지 테스트
        assertThat(membershipRepository).isNotNull();
    }*/

    @Test
    public void 멤버십등록() {
        final Membership membership = Membership.builder()
                .userId("userId")
                .membershipType(MembershipType.NAVER)
                .point(10000)
                .build();

        membershipRepository.save(membership);
        final Membership result = membershipRepository.findByUserIdAndMembershipType("userId", MembershipType.NAVER);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo("userId");
        assertThat(result.getMembershipType()).isEqualTo(MembershipType.NAVER);
        assertThat(result.getPoint()).isEqualTo(10000);
    }
}
