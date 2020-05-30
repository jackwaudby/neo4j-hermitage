package p4;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class P4Transaction1 implements Runnable {

        private int delay;
        private GraphDatabaseService graphDb;

        public P4Transaction1(int delay, GraphDatabaseService graphDb) {
            this.delay = delay;
            this.graphDb = graphDb;
        }


        public void run() {
            try {
                Thread.sleep(delay*4);
                System.out.println("> :begin -- " + Thread.currentThread().getName());
                try (Transaction tx = graphDb.beginTx()) {
                    Thread.sleep(delay);
                    System.out.println("> MATCH (n:Test {id:1}) set n.value = n.value + 1 -- " + Thread.currentThread().getName());
                    tx.execute("MATCH (n:Test {id:1}) set n.value = n.value + 1");
                    Thread.sleep(delay*3);
                    System.out.println("> :commit -- " + Thread.currentThread().getName() );
                    tx.commit();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
}
