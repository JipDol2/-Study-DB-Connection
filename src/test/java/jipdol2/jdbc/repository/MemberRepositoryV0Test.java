package jipdol2.jdbc.repository;

import jipdol2.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(member).isEqualTo(findMember);

        //update: money:10000->20000
        memberRepositoryV0.update(member.getMemberId(), 20000);
        Member updateMember = memberRepositoryV0.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        //delete
        memberRepositoryV0.delete(member.getMemberId());
        assertThatThrownBy(()-> memberRepositoryV0.findById(member.getMemberId())).isInstanceOf(NoSuchElementException.class);
    }

}