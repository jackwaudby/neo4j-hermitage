package gsingle;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import static java.lang.Thread.sleep;

public class GSingleTransaction1 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public GSingleTransaction1(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay * 2);
                System.out.println("> MATCH (n1:Test {id:1}) RETURN n1.value AS val -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n1:Test {id:1}) RETURN n1.value AS val");
                System.out.println("> n1.value = " + result1.next().get("val") + " -- " + Thread.currentThread().getName());

                sleep(delay * 4);
                System.out.println("> MATCH (n2:Test {id:2}) RETURN n2.value AS val -- " + Thread.currentThread().getName());
                final Result result2 = tx.execute("MATCH (n2:Test {id:2}) RETURN n2.value AS val");
                System.out.println("> n2.value = " + result2.next().get("val") + " -- " + Thread.currentThread().getName());

                sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
