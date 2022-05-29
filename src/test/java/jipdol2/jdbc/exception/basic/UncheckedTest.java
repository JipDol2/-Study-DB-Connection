package jipdol2.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch(){
        Service service = new Service();
        service.callCatch();
    }
    @Test
    void unchecked_throw(){
        Service service = new Service();
        assertThatThrownBy(()->service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * RuntimeException을 상속받으면 Unchecked Exception이 된다.
     */
    static class MyUncheckedException extends RuntimeException{
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * Unchecked Exception은 예외를 잡거나, 잡지 않아도 된다.
     * 잡지 않으면 자동으로 던져진다.
     */
    static class Service{
        Repository repository = new Repository();

        public void callCatch(){
            try{
                repository.call();
            }catch (MyUncheckedException e){
                log.info("예외처,message={}",e.getMessage(),e);
            }
        }
        public void callThrow(){
            repository.call();
        }
    }
    static class Repository{
        public void call(){
            throw new MyUncheckedException("ex");
        }
    }
}
