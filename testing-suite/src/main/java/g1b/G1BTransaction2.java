package g1b;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import static java.lang.Thread.sleep;

public class G1BTransaction2 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public G1BTransaction2 (int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            sleep(delay);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay * 2);
                System.out.println("> MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val");
                System.out.println("> id: 1, val: " + result1.next().get("val"));

                sleep(delay * 3);
                System.out.println("> MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val -- " + Thread.currentThread().getName());
                final Result result2 = tx.execute("MATCH (n:Test) WHERE n.id = 1 RETURN n.value AS val");
                System.out.println("> id: 1, val: " + result2.next().get("val"));

                sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
