package tdd.membership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tdd.membership.entity.Membership;
import tdd.membership.exception.MembershipErrorResult;
import tdd.membership.exception.MembershipException;
import tdd.membership.enums.MembershipType;
import tdd.membership.repository.MembershipRepository;
import tdd.membership.service.MembershipService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class) //Repository계층 모킹
public class MembershipServiceTest {

    private final String userId = "userId";
    private final MembershipType membershipType = MembershipType.NAVER;
    private final Integer point = 10000;

    @InjectMocks //테스트 대상
    private MembershipService target;
    @Mock //DI할 Repository 가짜 생성
    private MembershipRepository membershipRepository;
    

    @Test
    public void 멤버십등록실패_이미존재함() {
        //실제 메소드를 호출하지 않으면서 미리 호출 시 리턴 값 선언 가능. 여기선 Membership객체를 리턴
        doReturn(Membership.builder().build()).when(membershipRepository).findByUserIdAndMembershipType(userId, membershipType);

        //호출 시 위에서 정의한대로 객체가 있음:null이 아님->err발생
        final MembershipException result = assertThrows(MembershipException.class, ()->target.addMembership(userId, membershipType, point));

        assertThat(result.getErrorResult()).isEqualTo(MembershipErrorResult.DUPLICATED_MEMBERSHIP_REGISTER);

    }
}
