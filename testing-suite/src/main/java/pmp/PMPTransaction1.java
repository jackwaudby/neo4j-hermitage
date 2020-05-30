package pmp;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import static java.lang.Thread.sleep;

public class PMPTransaction1 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public PMPTransaction1(int delay, GraphDatabaseService graphDb) {
        this.delay = delay;
        this.graphDb = graphDb;
    }

    public void run() {
        try {
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay * 2);
                System.out.println("> MATCH (n:Test) WHERE n.value = 30 RETURN COUNT(n) AS cn -- " + Thread.currentThread().getName());
                final Result result1 = tx.execute("MATCH (n:Test) WHERE n.value = 30 RETURN COUNT(n) as cn");
                System.out.println("> cn = " + result1.next().get("cn") + " -- " + Thread.currentThread().getName());

                sleep(delay * 3);
                System.out.println("> MATCH (n:Test) WHERE n.value % 3 = 0 RETURN COUNT(n) AS cn -- " + Thread.currentThread().getName());
                final Result result2 = tx.execute("match (n:Test) where n.value % 3 = 0 return count(n) as cn");
                System.out.println("> cn = " + result2.next().get("cn") + " -- " + Thread.currentThread().getName());

                sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName() );
                tx.commit();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}