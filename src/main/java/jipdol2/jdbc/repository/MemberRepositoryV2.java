package jipdol2.jdbc.repository;

import jipdol2.jdbc.connection.DBConnectionUtil;
import jipdol2.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException{
        String sql = "insert into member(member_id, money) values (?, ?)";

        /**
         * try-with-resource 를 사용하지 않았을 경우
         */
/*        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,member.getMemberId());
            pstmt.setInt(2,member.getMoney());
            return member;
        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
        }finally{
            close(con,pstmt,null);
        }*/

        /**
         * try-with-resource 사용
         */
        try(Connection con=getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = null
        ){
            pstmt.setString(1,member.getMemberId());
            pstmt.setInt(2,member.getMoney());
            pstmt.executeUpdate();
            return member;
        }catch(SQLException e){
            log.error("db error",e);
            throw e;
        }
    }

    public Member findById(String memberId) throws SQLException{
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs =null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);

            rs = pstmt.executeQuery();

            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found = "+memberId);
            }
        }catch(SQLException e){
            log.error("db error",e);
            throw e;
        }finally {
            close(con,pstmt,rs);
        }
    }

    public Member findById(Connection con,String memberId) throws SQLException{
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs =null;

        try{
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);

            rs = pstmt.executeQuery();

            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found = "+memberId);
            }
        }catch(SQLException e){
            log.error("db error",e);
            throw e;
        }finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
//            여기서 connection을 닫아버리면 안된다. connection이 계속 유지된 상태로 어플리케이션이 돌아가야 된다.
//            서비스 로직이 끝날때 트랜잭션을 종료하고 닫아야 된다.
//            JdbcUtils.closeConnection(con);
        }
    }
    public void update(String memberId, int money) throws SQLException{
        String sql = "update member set money=? where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,money);
            pstmt.setString(2,memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}",resultSize);
        }catch(SQLException e){
            log.error("db error",e);
            throw e;
        }finally {
            close(con,pstmt,null);
        }
    }
    public void update(Connection con,String memberId, int money) throws SQLException{
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;

        try{
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,money);
            pstmt.setString(2,memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}",resultSize);
        }catch(SQLException e){
            log.error("db error",e);
            throw e;
        }finally {
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
    private void close(Connection con, Statement pstmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(pstmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("connection={}, class={}",con,con.getClass());
        return con;
    }
}
