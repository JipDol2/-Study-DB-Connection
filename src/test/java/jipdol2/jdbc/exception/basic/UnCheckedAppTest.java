package jipdol2.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void unchecked(){
        Controller controller = new Controller();

        assertThatThrownBy(()->controller.request())
                .isInstanceOf(Exception.class);
    }
    @Test
    void printEx(){
        Service service = new Service();
        try{
            service.logic();
        }catch (Exception e){
            log.info("ex",e);
        }
    }
    static class Controller{
        Service service = new Service();

        public void request(){
            service.logic();
        }
    }
    static class Service{
        NetworkClient networkClient = new NetworkClient();
        Repository repository = new Repository();

        public void logic(){
            repository.call();
            networkClient.call();
        }
    }
    static class NetworkClient{
        public void call() {
            throw new RuntimeConnectionException("연결실패");
        }

    }
    static class Repository{
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }
        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectionException extends RuntimeException{
        public RuntimeConnectionException(String message) {
            super(message);
        }
    }
    static class RuntimeSQLException extends RuntimeException{
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
