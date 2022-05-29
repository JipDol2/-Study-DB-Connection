package jipdol2.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CheckedAppTest {

    @Test
    void checked(){
        Controller controller = new Controller();

        assertThatThrownBy(()->controller.request())
                .isInstanceOf(Exception.class);
    }
    static class Controller{
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }
    static class Service{
        NetworkClient networkClient = new NetworkClient();
        Repository repository = new Repository();

        public void logic() throws ConnectException, SQLException {
            networkClient.call();
            repository.call();
        }
    }
    static class NetworkClient{
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }

    }
    static class Repository{
        public void call() throws SQLException {
            throw new SQLException("ex");
        }
    }
}
