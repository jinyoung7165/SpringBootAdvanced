package jpabook.jpashop.api;

import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController //REST API를 위함. responseBody 반환
@RequiredArgsConstructor //final 의존성 주입
public class MemberApiController {

    private final MemberService memberService;

    /**
    * 멤버 등록
     **/
    //외부에 노출하면 안됨-entity를 parameter로 받으면 위험->DTO쓰자
    @PostMapping("/api/v1/members") //회원등록 @RequestBody:JSON들어온거->Member객체로 바꿔줌
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 멤버 수정
     */
    @PutMapping("/api/v2/members/{id}") //pathvariable -
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName()); //수정

        Member findMember = memberService.findOne(id); //수정된 멤버 찾음(update와 Select쿼리 분리)
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    /**
     * 멤버 조회
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() { //arr Entity그대로 반환하면 추가 정보 넣어주기 어려움
        return memberService.findMembers(); //[{}, {}, {}] 그대로 반환
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))//각멤버의 이름마다 dto 생성
                .collect(Collectors.toList()); //MemberDto가 담긴 리스트
        return new Result(collect.size(), collect); //{count:?, data: [{}, {}]} 반환하기 위함
    }

    @Data
    @AllArgsConstructor
    static class Result<T> { //data를 Object로 감쌈 {data: 객체}
        private int count;
        private T data; //data란 key에 객체를 담기 위함
    }
    @Data
    @AllArgsConstructor
    static class MemberDto { //조회하면 이름만 반환할 것
        private String name;
    }

    @Data //getter, setter, final의존성 주입
    static class CreateMemberRequest {
        @NotEmpty //DTO애서 valid 제한 걸어줌 Entity말고
        private String name;
    }
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }
    @Data
    @AllArgsConstructor //생성자
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
}
