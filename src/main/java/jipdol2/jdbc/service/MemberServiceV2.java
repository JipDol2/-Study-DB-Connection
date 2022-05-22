package jipdol2.jdbc.service;

import jipdol2.jdbc.domain.Member;
import jipdol2.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동,풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 repository;

    public void accountTransfer(String fromId,String toId,int money) throws SQLException {
        Connection con = dataSource.getConnection();

        try{
            con.setAutoCommit(false);   //트랜잭션 시작
            //비지니스 로직 수행
            bizLogic(con, fromId, toId, money);

            con.commit();   //성공시 커밋
        }catch (Exception e){
            con.rollback(); //실패시 롤백
            throw new IllegalStateException(e);
        }finally {
            release(con);
        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = repository.findById(con, fromId);
        Member toMember = repository.findById(con, toId);

        repository.update(con, fromId,fromMember.getMoney()- money);
        validation(toMember);
        repository.update(con, toId,toMember.getMoney()+ money);
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
