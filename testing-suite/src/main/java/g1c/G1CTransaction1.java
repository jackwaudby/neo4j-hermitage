package g1c;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import static java.lang.Thread.sleep;

public class G1CTransaction1 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public G1CTransaction1 (int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay * 2);
                System.out.println("> MATCH (n1:Test {id:1}) SET n1.value = 11 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n1:Test {id:1}) SET n1.value = 11");


                sleep(delay * 2);
                System.out.println("> MATCH (n2:Test) WHERE n2.id = 2 RETURN n2.value AS val -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n2:Test) WHERE n2.id = 2 RETURN n2.value AS val");
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

