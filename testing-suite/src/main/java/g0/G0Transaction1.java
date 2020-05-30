package g0;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

import static java.lang.Thread.sleep;

public class G0Transaction1 implements Runnable {

    private int delay;
    private GraphDatabaseService graphDb;

    public G0Transaction1(int delay, GraphDatabaseService graphDb) {
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
                System.out.println("> MATCH (n2:Test {id:2}) SET n2.value = 21 -- " + Thread.currentThread().getName());
                tx.execute("MATCH (n2:Test {id:2}) SET n2.value = 21");

                sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }

            sleep(delay);
            System.out.println("> :begin -- " + Thread.currentThread().getName());
            try (Transaction tx = graphDb.beginTx()) {

                sleep(delay);
                System.out.println("> MATCH (n:Test) RETURN n.value AS val -- " + Thread.currentThread().getName());
                final Result result = tx.execute("MATCH (n:Test) RETURN n.id AS id, n.value AS val");
                while (result.hasNext()) {
                    Map<String, Object> res = result.next();
                    System.out.println("> id: " + res.get("id").toString() + ", value: " + res.get("val").toString() + " -- " + Thread.currentThread().getName());
                }

                sleep(delay);
                System.out.println("> :commit -- " + Thread.currentThread().getName());
                tx.commit();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
