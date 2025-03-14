package edu.uob;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class DBTestCases {


    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        assertTrue(sendCommandToServer("create Database XyZ;").contains("OK"));
        assertTrue(sendCommandToServer("USE xYz;").contains("OK"));
        assertTrue(sendCommandToServer("Drop DATABASE xyZ;").contains("OK"));

        String dbName = generateRandomName();
        assertTrue(sendCommandToServer("create database " + dbName + ";").contains("OK"));
        assertTrue(sendCommandToServer("USE " + dbName + ";").contains("OK"));
        assertTrue(sendCommandToServer("Create tAble Marks (Name, Mark, Pass)").contains("ERROR")); //fail
        assertTrue(sendCommandToServer("Create tAble Marks (Name, Mark, Pass);").contains("OK")); //fail
        assertTrue(sendCommandToServer("INSERT into marks Values   ('Simon', 20, FALSE);").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Marks VALUES('Sion', 30, FALSE);").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO marKs VALUES ('Rob', 40, FALSE);").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO mArks values ( 'Chris', 50, TRUE);").contains("OK"));
        assertTrue(sendCommandToServer("INSERT into marks Values ('Zack' ,  60,TRUE)  ;").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Marks VALUES ('Pete'  , 70, TRUE  ) ;").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO marKs VALUES ('Hector', 80  , TRUE  );").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO mArks values ( 'Achilles', 90   , TRUE);").contains("OK"));

        assertTrue(sendCommandToServer("Select * From Marks;").contains("OK"));
        assertTrue(sendCommandToServer("Select* From Marks;").contains("ERROR"));// fail
        assertTrue(sendCommandToServer("Select *From Marks;").contains("ERROR")); // fail
        assertTrue(sendCommandToServer("Select*From Marks;").contains("ERROR")); // fail

        assertTrue(sendCommandToServer("select * from Marks Where Name  =='Sion';").contains("OK"));
        assertTrue(sendCommandToServer("select * from Marks Where Name!='Sion';").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE pass== TRUE;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE pass!=  FALSE;").contains("OK"));

        assertTrue(sendCommandToServer("SELECT * FROM marks;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE pass == FALSE AND mark > 35;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE AND mark > 35);").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE(pass == FALSE) AND (mark > 35);").contains("ERROR"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE ((pass == FALSE) AND (mark > 35));").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE AND (mark > 35));").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE ((pass == FALSE) AND mark > 35);").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND mark > 35;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE pass == FALSE AND (mark > 35);").contains("OK"));

        assertTrue(sendCommandToServer("Create tAble Coursework (Task, Submission);").contains("OK"));
        assertTrue(sendCommandToServer("INSERT into Coursework Values ('OXO', 3);").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Coursework VALUES ('DB',1);").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO Coursework VALUES ('OXO', 4);").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO Coursework values('STAG',2);").contains("OK"));
        assertTrue(sendCommandToServer("INSERT into Coursework Values ('TOXO'  , 7);").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Coursework VALUES ('GDB',4);").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO Coursework VALUES ('BOXO', 8);").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO Coursework values ('TAG', 6);").contains("OK"));

        assertTrue(sendCommandToServer("Select * fRom courSework;").contains("OK"));
        assertTrue(sendCommandToServer("JOIN coursework AND marks ON submission AND id;").contains("OK"));
        assertTrue(sendCommandToServer("Update marks Set Mark = 38, paSS = FALSE Where Name == 'Chris';").contains("OK"));
        assertTrue(sendCommandToServer("Select * From mArks Where (Name == 'Chris');").contains("OK"));
        assertTrue(sendCommandToServer("Delete from MArks where namE == 'Sion';").contains("OK"));

        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE name LIKE 'i';").contains("OK"));
        assertTrue(sendCommandToServer("SELECT id FROM marks WHERE pass == FALSE;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT name FROM marks WHERE mark>60;").contains("OK"));
        assertTrue(sendCommandToServer("DELETE FROM marks WHERE mark<40;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks;").contains("OK"));
        assertTrue(sendCommandToServer("ALTER TABLE marks ADD age;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks;").contains("OK"));
        assertTrue(sendCommandToServer("UPDATE marks SET age = 35 WHERE name == 'Simon';").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks;").contains("OK"));
        assertTrue(sendCommandToServer("ALTER TABLE marks DROP mark;").contains("OK"));
        assertTrue(sendCommandToServer("ALTER TABLE marks DROP pass;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks").contains("ERROR"));
        assertTrue(sendCommandToServer("SELECT * FROM crew;").contains("ERROR"));
        assertTrue(sendCommandToServer("SELECT height FROM marks WHERE name == 'Chris';").contains("ERROR"));
        assertTrue(sendCommandToServer("DROP TABLE marks;").contains("OK"));
        assertTrue(sendCommandToServer("DROP DATABASE " + dbName + " ;").contains("OK"));
    }


}


