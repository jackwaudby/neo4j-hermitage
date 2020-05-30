package g2;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

import static java.lang.Thread.sleep;

public class G2Transaction2 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public G2Transaction2(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            sleep(delay);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {
                sleep(delay * 2);
                System.out.println("> MATCH (n:Test) WHERE n.value % 3 = 0 RETURN n.id AS id, n.value AS val -- " + Thread.currentThread().getName());
                final Result result = tx.execute("MATCH (n:Test) WHERE n.value % 3 = 0 RETURN n.id AS id, n.value AS val");
                while (result.hasNext()) {
                    Map<String, Object> res = result.next();
                    System.out.println("> id: " + res.get("id").toString() + ", value: " + res.get("val").toString() + " -- T3");
                }                sleep(delay * 2);
                System.out.println("> CREATE (n:Test {id: 4, value: 42}) -- " + Thread.currentThread().getName());
                tx.execute("CREATE (n:Test {id: 4, value: 42})");
                sleep(delay * 2);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
