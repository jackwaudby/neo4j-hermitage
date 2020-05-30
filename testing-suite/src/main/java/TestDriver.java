import g0.G0Transaction1;
import g0.G0Transaction2;
import g1a.G1ATransaction1;
import g1a.G1ATransaction2;
import g1b.G1BTransaction1;
import g1b.G1BTransaction2;
import g1c.G1CTransaction1;
import g1c.G1CTransaction2;
import g2.G2Transaction1;
import g2.G2Transaction2;
import g2item.G2ItemTransaction1;
import g2item.G2ItemTransaction2;
import gsingle.GSingleTransaction1;
import gsingle.GSingleTransaction2;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;

import otv.OTVTransaction1;
import otv.OTVTransaction2;
import otv.OTVTransaction3;
import p4.P4Transaction1;
import p4.P4Transaction2;
import p4.P4Transaction3;
import pmp.PMPTransaction1;
import pmp.PMPTransaction2;

import java.io.File;

public class TestDriver {

    public static void main(String[] args) {

        String test = args[0];
        int delay = Integer.parseInt(args[1]);

        String neo4jHome = System.getenv("NEO4J_HOME");
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File(neo4jHome)).build();
        GraphDatabaseService graphDb = managementService.database("neo4j");
        registerShutdownHook(managementService);

        graphDb.isAvailable(4);
        System.out.println("# Initialise database state");

        // delete all edges
        System.out.println("# Clean database state");
        System.out.println("> MATCH (n) DETACH DELETE n");
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute("MATCH (n) DETACH DELETE n");
            tx.commit();
        }

        // add nodes
        System.out.println("# Add nodes");
        System.out.println(
                "> CREATE (n1:Test { id: 1, value: 10 })\n" +
                        "> CREATE (n2:Test { id: 2, value: 20 })"
        );
        try (Transaction tx = graphDb.beginTx()) {
            tx.execute(
                    "CREATE (n1:Test { id: 1, value: 10 })\n" +
                            "CREATE (n2:Test { id: 2, value: 20 })");
            tx.commit();
        }

        System.out.println("# Count nodes");
        System.out.println("> MATCH (n) RETURN COUNT(n)");
        long count;
        try (Transaction tx = graphDb.beginTx()) {
            final Result result = tx.execute("MATCH (n) RETURN COUNT(n) AS cn");
            count = (long) result.next().get("cn");
            tx.commit();
        }
        System.out.println("> Count: " + count);

        System.out.println(" ");
        System.out.println("# " + test + " test");

        if (test.equals("g0")) {
            Thread t1 = new Thread(new G0Transaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new G0Transaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("g1a")) {
            Thread t1 = new Thread(new G1ATransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new G1ATransaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("g1b")) {
            Thread t1 = new Thread(new G1BTransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new G1BTransaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("g1c")) {
            Thread t1 = new Thread(new G1CTransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new G1CTransaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("pmp")) {
            Thread t1 = new Thread(new PMPTransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new PMPTransaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("otv")) {
            Thread t1 = new Thread(new OTVTransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new OTVTransaction2(delay, graphDb), "T2");
            Thread t3 = new Thread(new OTVTransaction3(delay, graphDb), "T3");
            t1.start();
            t2.start();
            t3.start();
        } else if (test.equals("p4")) {
            Thread t1 = new Thread(new P4Transaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new P4Transaction2(delay, graphDb), "T2");
            Thread t3 = new Thread(new P4Transaction3(delay, graphDb), "T3");
            t1.start();
            t2.start();
            t3.start();
        } else if (test.equals("g-single")) {
            Thread t1 = new Thread(new GSingleTransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new GSingleTransaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("g2-item")) {
            Thread t1 = new Thread(new G2ItemTransaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new G2ItemTransaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        } else if (test.equals("g2")) {
            Thread t1 = new Thread(new G2Transaction1(delay, graphDb), "T1");
            Thread t2 = new Thread(new G2Transaction2(delay, graphDb), "T2");
            t1.start();
            t2.start();
        }
    }

    private static void registerShutdownHook(final DatabaseManagementService managementService) {
        Runtime.getRuntime().addShutdownHook(new Thread(managementService::shutdown));
    }
}
