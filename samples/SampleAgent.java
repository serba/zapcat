import org.kjkoster.zapcat.Agent;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;

public class SampleAgent {
    public static void main(String[] args) throws Exception {
        Agent agent = null;
        try {
            agent = new ZabbixAgent();

            // simulate lots of important work being done...
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (agent != null) {
                agent.stop();
            }
        }
    }
}
