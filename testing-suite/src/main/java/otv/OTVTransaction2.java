package otv;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class OTVTransaction2 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public OTVTransaction2(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }


    public void run() {
        try {
            Thread.sleep(delay);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {
                Thread.sleep(delay * 4);
                System.out.println("> MATCH (n1:Test {id:1}) SET n1.value = 12 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n1:Test {id:1}) SET n1.value = 12");
                Thread.sleep(delay * 3);
                System.out.println("> MATCH (n2:Test {id:2}) SET n2.value = 18 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n2:Test {id:2}) SET n2.value = 18");
                Thread.sleep(delay * 2);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
