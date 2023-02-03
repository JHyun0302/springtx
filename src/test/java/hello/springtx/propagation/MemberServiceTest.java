package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService @Transactional: OFF
     * memberRepository @Transactional: ON
     * logRepository @Transactional: ON
     */
    @Test
    void outerTxOff_success() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transactional: OFF
     * memberRepository @Transactional: ON
     * logRepository @Transactional: ON Exception
     */
    @Test
    void outerTxOff_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);


        //then: 완전히 롤백되지 않고, member 데이터가 남아서 저장된다.
        assertTrue(memberRepository.find(username).isPresent()); //member는 커밋되서 저장되었고
        assertTrue(logRepository.find(username).isEmpty()); //log는 롤백되서 비어있다!
    }


    /**
     * memberService @Transactional: ON
     * memberRepository @Transactional: OFF
     * logRepository @Transactional: OFF
     */
    @Test
    void singleTx() {
        //given
        String username = "singleTx";

        //when
        memberService.joinV1(username);

        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository @Transactional: ON
     */
    @Test
    void outerTxOn_success() {
        //given
        String username = "outerTxOn_success";

        //when
        memberService.joinV1(username);

        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository @Transactional: ON Exception
     */
    @Test
    void outerTxOn_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class); //(logRepository: rollback-only)


        //then: 모든 데이터가 롤백된다.(log와 member와 memberservice 모두 같은 커넥션 사용)
        assertTrue(memberRepository.find(username).isEmpty()); //member는 커밋하지만 신규 트랙잭션이 아니므로 실제 커밋 호출하지 않음!
        assertTrue(logRepository.find(username).isEmpty()); //log는 롤백되서 비어있다!
    }

    /**
     * memberService @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository @Transactional: ON Exception
     */
    @Test
    void recoverException_fail() {
        //given
        String username = "로그예외_recoverException_fail";

        //when
        Assertions.assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class); //logRepository에서 rollback-only 마크해서 예외 터지고 클라이언트까지 넘어옴!


        //then: 모든 데이터가 롤백된다.(log와 member와 memberservice 모두 같은 커넥션 사용)
        assertTrue(memberRepository.find(username).isEmpty()); //member는 커밋하지만 신규 트랙잭션이 아니므로 실제 커밋 호출하지 않음!
        assertTrue(logRepository.find(username).isEmpty()); //log는 런타임 예외터지고 롤백됨
    }

    /**
     * memberService @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository @Transactional: ON(REQUIRES_NEW) Exception
     */
    @Test
    void recoverException_success() {
        //given
        String username = "로그예외_recoverException_success";

        //when
        memberService.joinV2(username);

        //then: 회원가입을 시도한 로그를 남기는데 실패하더라도 회원 가입은 유지됨!
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }
}