import com.wq.pojo.Person;
import org.junit.Test;

public class test {
    // Junit包
    @Test
    public void test(){
        Person p = new Person("强", "王");
        System.out.println(p.getFirstName() + " " + p.getLastName());
    }
}
