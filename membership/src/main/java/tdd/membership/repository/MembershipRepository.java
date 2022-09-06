package tdd.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tdd.membership.entity.Membership;
import tdd.membership.enums.MembershipType;

public interface MembershipRepository extends JpaRepository<Membership, Long> { //타입,id
    Membership findByUserIdAndMembershipType(final String userId, final MembershipType membershipType);

}
