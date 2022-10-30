import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumm.account.entities.Account;

public class Test {
    public static void main (String args[]) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Account aa = new Account();
        System.out.println(mapper.writeValueAsString(aa));
    }
}
