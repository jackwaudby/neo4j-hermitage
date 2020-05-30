package pmp;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class PMPTransaction2 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public PMPTransaction2(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }


    public void run() {
        try {
            Thread.sleep(delay);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {
                Thread.sleep(delay * 2);
                System.out.println("> CREATE (n:Test {id: 3, value: 30}) -- " + Thread.currentThread().getName());
                tx.execute("create (n:Test {id: 3, value: 30})");
                Thread.sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName() );
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}