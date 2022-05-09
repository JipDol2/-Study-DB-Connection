package jipdol2.jdbc.repository;

import jipdol2.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Slf4j
@Transactional
class MemberRepositoryV0Test {

    MemberRepositoryV0 memberRepositoryV0 = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("V0", 10000);
        memberRepositoryV0.save(member);

        //findById
        Member findMember = memberRepositoryV0.findById(member.getMemberId());
        log.info("findMember={}",findMember);

        Assertions.assertThat(member).isEqualTo(findMember);
    }

}