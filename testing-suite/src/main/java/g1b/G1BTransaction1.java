package g1b;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static java.lang.Thread.sleep;

public class G1BTransaction1 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public G1BTransaction1 (int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay * 2);
                System.out.println("> MATCH (n1:Test {id:1}) SET n1.value = 101 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n1:Test {id:1}) SET n1.value = 101");


                sleep(delay * 2);
                System.out.println("> MATCH (n1:Test {id:1}) SET n1.value = 11 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n1:Test {id:1}) SET n1.value = 11");


                sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
