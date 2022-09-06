package tdd.membership.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tdd.membership.exception.MembershipErrorResult;

@Getter
@RequiredArgsConstructor
public class MembershipException extends RuntimeException {

    private final MembershipErrorResult errorResult;

}
