package jipdol2.jdbc.service;

import jipdol2.jdbc.domain.Member;
import jipdol2.jdbc.repository.MemberRepositoryV2;
import jipdol2.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 repository;

    public void accountTransfer(String fromId,String toId,int money) throws SQLException {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            //비지니스 로직 수행
            bizLogic(fromId, toId, money);
            transactionManager.commit(status);//성공시 커밋
        }catch (Exception e){
            transactionManager.rollback(status);//실패시 롤백
            throw new IllegalStateException(e);
        }
        /**
         * 왜 release가 없지?
         * - 트랜잭션 매니저가 알아서 release를 해준다.
         */
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = repository.findById(fromId);
        Member toMember = repository.findById(toId);

        repository.update(fromId,fromMember.getMoney()- money);
        validation(toMember);
        repository.update(toId,toMember.getMoney()+ money);
    }

    private void release(Connection con) {
        if(con !=null){
            try{
                /**
                 * connection을 close 하면 종료시키는 것이 아니라 connection pool 에 반납하게 된다.
                 * 그래서 이 connection을 다시 사용하는 사람은 자동커밋이 되어있다고 생각한다.
                 * 그렇기 때문에 자동커밋으로 설정해주어야 한다.
                 */
                con.setAutoCommit(true);
                con.close();
            }catch (Exception e){
                log.info("error",e);
            }
        }
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
