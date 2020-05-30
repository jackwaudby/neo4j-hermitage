package otv;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class OTVTransaction3 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;


    public OTVTransaction3(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            Thread.sleep(delay * 2);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {
                Thread.sleep(delay * 5);
                System.out.println("> MATCH (n1:Test {id:1}) RETURN n1.value AS val -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n1:Test {id:1}) RETURN n1.value AS val");
                System.out.println("> n1.value = " + result1.next().get("val") + " -- " + Thread.currentThread().getName());

                Thread.sleep(delay * 3);
                System.out.println("> MATCH (n2:Test {id:2}) RETURN n2.value AS val -- " + Thread.currentThread().getName());
                final Result result2 = tx.execute("MATCH (n2:Test {id:2}) RETURN n2.value AS val");
                System.out.println("> n2.value = " + result2.next().get("val") + " -- " + Thread.currentThread().getName());

                Thread.sleep(delay * 2);
                System.out.println("> MATCH (n2:Test {id:2}) RETURN n2.value AS val -- " + Thread.currentThread().getName());
                final Result result3 = tx.execute("MATCH (n2:Test {id:2}) RETURN n2.value AS val");
                System.out.println("> n2.value = " + result3.next().get("val") + " -- " + Thread.currentThread().getName());

                Thread.sleep(delay);
                System.out.println("> MATCH (n1:Test {id:1}) RETURN n1.value AS val -- " + Thread.currentThread().getName());
                final Result result4 = tx.execute("MATCH (n1:Test {id:1}) RETURN n1.value AS val");
                System.out.println("> n1.value = " + result4.next().get("val") + " -- " + Thread.currentThread().getName());

                Thread.sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}