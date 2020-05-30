package p4;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class P4Transaction3 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public P4Transaction3(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }


    public void run() {
        try {
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {
                Thread.sleep(delay);
                System.out.println("> MATCH (n:Test {id:1}) RETURN n.value AS val -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n:Test {id:1}) RETURN n.value AS val");
                System.out.println("> " + result1.next().get("val"));
                Thread.sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName() );
                tx.commit();
            }

            Thread.sleep(delay*7);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {
                Thread.sleep(delay);
                System.out.println("> MATCH (n:Test {id:1}) RETURN n.value AS val -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n:Test {id:1}) RETURN n.value AS val");
                System.out.println("> " + result1.next().get("val"));
                Thread.sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName() );
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
