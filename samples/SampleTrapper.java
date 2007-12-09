import java.util.concurrent.TimeUnit;

import org.kjkoster.zapcat.Trapper;
import org.kjkoster.zapcat.zabbix.ZabbixTrapper;

public class SampleTrapper {
    public static void main(String[] args) throws Exception {
        Trapper trapper = null;
        try {
            trapper = new ZabbixTrapper("192.168.0.150", "mac.kjkoster.org");

            trapper.send("java.version", System.getProperty("java.version"));

            trapper.send("compiler.name", "java.lang:type=Compilation", "Name");

            trapper.every(30, TimeUnit.SECONDS, "compiler.time",
                    "java.lang:type=Compilation", "TotalCompilationTime");

            // simulate lots of important work being done...
            Thread.sleep(Long.MAX_VALUE);
        } finally {
            if (trapper != null) {
                trapper.stop();
            }
        }
    }
}
