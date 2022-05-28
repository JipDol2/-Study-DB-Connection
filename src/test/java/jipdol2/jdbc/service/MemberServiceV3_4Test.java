package jipdol2.jdbc.service;

import jipdol2.jdbc.domain.Member;
import jipdol2.jdbc.repository.MemberRepositoryV3;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static jipdol2.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - DataSource, transaction 매니저 자동등록
 */
@Slf4j
@SpringBootTest
/**
 * @SpringBootTest 를 사용하지 않으면 에러가 발생한다. 그 이유는 로직실패시 예외가 터지면 rollback이 되어야 하는데,
 * 현재 MemberServiceV3_3는 @Transactional 어노테이션을 사용하고 있기 때문에 '스프링 컨테이너'에 해당 객체들이 등록이 되어있는 상태여야 된다.
 * 그러나 memberRepository 나 memberService는 스프링 Bean 에 등록하지 않고, 직접 만들어 사용하기 때문에 에러가 발생하는 것이다.
 * @SpringBootTest 를 사용하면 이런 객체들을 스프링Bean으로 등록을 해준다.
 */

class MemberServiceV3_4Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService;

    /**
     * @SpringBootTest로 스프링 컨테이너를 띄우고 난 후에 내가 직접 만든 Bean들을 컨테이너에 넣고 싶을때 사용하는 어노테이션
     */
    @TestConfiguration
    static class TestConfig{

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        @Bean
        MemberRepositoryV3 memberRepositoryV3(){
            return new MemberRepositoryV3(dataSource);
        }
        @Bean
        MemberServiceV3_3 memberServiceV3_3(){
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @Test
    void AopCheck(){
        log.info("memberService class={}",memberService.getClass());
        log.info("memberRepository={}",memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }
    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);
        //when
        memberService.accountTransfer(memberA.getMemberId(),memberB.getMemberId(),2000);
        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEX() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);
        //when
        assertThatThrownBy(()->memberService.accountTransfer(memberA.getMemberId(),memberB.getMemberId(),2000))
                .isInstanceOf(IllegalStateException.class);
        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);    //rollback 이 되었기 때문에 8000이 아닌 10000이다.
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}