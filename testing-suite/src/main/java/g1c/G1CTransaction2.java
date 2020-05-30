package g1c;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import static java.lang.Thread.sleep;

public class G1CTransaction2 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public G1CTransaction2 (int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            sleep(delay);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay * 2);
                System.out.println("> MATCH (n2:Test {id:2}) SET n2.value = 22 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n2:Test {id:2}) SET n2.value = 22");


                sleep(delay * 2);
                System.out.println("> MATCH (n1:Test) WHERE n1.id = 1 RETURN n1.value AS val-- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n1:Test) WHERE n1.id = 1 RETURN n1.value AS val");
                System.out.println("> id: 2, val: " + result1.next().get("val"));

                sleep(delay * 2);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

