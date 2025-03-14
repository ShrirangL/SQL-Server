package edu.uob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void advancedTestCases() {
        String randomDatabaseName = generateRandomName();
        assertTrue(sendCommandToServer("create Database " + randomDatabaseName + ";").contains("OK"));
        assertTrue(sendCommandToServer("USE " + randomDatabaseName + ";").contains("OK"));
        assertTrue(sendCommandToServer("Create tAble Marks (Name, Mark, Pass)").contains("ERROR")); //fail ; missing
        assertTrue(sendCommandToServer("Create tAble Marks (Name, Mark, Pass);").contains("OK")); //fail
        assertTrue(sendCommandToServer("INSERT into marks Values   ('Simon', 20, FALSE);").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Marks VALUES('Sion', 30, FALSE);").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO marKs VALUES ('Rob', 40, FALSE);").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO mArks values ( 'Chris', 50, TRUE);").contains("OK"));
        assertTrue(sendCommandToServer("INSERT into marks Values ('Zack' ,  60,TRUE)  ;").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Marks VALUES ('Pete'  , 70, TRUE  ) ;").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO marKs VALUES ('Hector', 80  , TRUE  );").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO mArks values ( 'Achilles', 90   , TRUE);").contains("OK"));

        String response  = sendCommandToServer("Select * From Marks;");
        assertTrue(response.contains("OK") && response.contains("Simon") && response.contains("Sion")
                && response.contains("Rob") && response.contains("Chris") && response.contains("Zack")
                && response.contains("Pete") && response.contains("Hector") && response.contains("Achilles"));

        assertTrue(sendCommandToServer("Select* From Marks;").contains("ERROR"));// fail
        assertTrue(sendCommandToServer("Select *From Marks;").contains("ERROR")); // fail
        assertTrue(sendCommandToServer("Select*From Marks;").contains("ERROR")); // fail

        assertTrue(sendCommandToServer("select * from Marks Where Name  =='Sion';").contains("Sion"));
        assertFalse(sendCommandToServer("select * from Marks Where Name!='Sion';").contains("Sion"));
        assertFalse(sendCommandToServer("SELECT * FROM marks WHERE pass== TRUE;").contains("FALSE"));
        assertFalse(sendCommandToServer("SELECT * FROM marks WHERE pass!=  FALSE;").contains("FALSE"));

        response = sendCommandToServer("SELECT * FROM marks WHERE pass == FALSE AND mark > 35;");
        assertTrue(response.contains("OK") && response.contains("Rob") && response.contains("40")
                && response.contains("FALSE") && !response.contains("TRUE") && !response.contains("Sion") && !response.contains("Chris"));

        // where doesn't have trailing space
        assertTrue(sendCommandToServer("SELECT * FROM marks WHERE(pass == FALSE) AND (mark > 35);").contains("ERROR"));

        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE AND mark > 35);"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE ((pass == FALSE) AND (mark > 35));"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE AND (mark > 35));"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE ((pass == FALSE) AND mark > 35);"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND mark > 35;"), response);
        assertEquals(sendCommandToServer("SELECT * FROM marks WHERE pass == FALSE AND (mark > 35);"), response);

        assertTrue(sendCommandToServer("Create tAble COURSEWORK (Task, Submission);").contains("OK"));
        assertTrue(sendCommandToServer("INSERT into Coursework Values ('OXO', 3);").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Coursework VALUES ('DB',1);").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO Coursework VALUES ('OXO', 4);").contains("OK"));

        //values may not have trailing space
        assertTrue(sendCommandToServer("Insert intO Coursework values('STAG',2);").contains("OK"));

        assertTrue(sendCommandToServer("INSERT into Coursework Values ('TOXO'  , 7);").contains("OK"));
        assertTrue(sendCommandToServer("Insert into Coursework VALUES ('GDB',5);").contains("OK"));
        assertTrue(sendCommandToServer("insert INTO Coursework VALUES ('BOXO', 8);").contains("OK"));
        assertTrue(sendCommandToServer("Insert intO Coursework values ('TAG', 6);").contains("OK"));

        response = sendCommandToServer("Select * fRom courSework;");
        assertTrue(response.contains("OK") && response.contains("BOXO") && response.contains("STAG")
                && response.contains("TAG") && response.contains("OXO") && response.contains("GDB"));

        // we have columns from both table in same
        response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(response.contains("Task") && response.contains("Name") && response.contains("Mark")
                &&response.contains("Pass"));

        // We have entries from first table
        assertTrue(response.contains("OK") && response.contains("OXO") && response.contains("DB")
                && response.contains("STAG") && response.contains("TOXO") && response.contains("GDB")
                && response.contains("BOXO")  && response.contains("TAG"));

        //we have entries from second table
        assertTrue(response.contains("Simon") && response.contains("Sion")
                && response.contains("Rob") && response.contains("Chris") && response.contains("Zack")
                && response.contains("Pete") && response.contains("Hector") && response.contains("Achilles"));

        assertTrue(sendCommandToServer("Update marks Set Mark = 51, paSS = FALSE Where Name == 'Chris';").contains("OK"));

        response = sendCommandToServer("Select * From mArks Where (Name == 'Chris');");
        assertTrue(response.contains("OK") && response.contains("Chris") && response.contains("FALSE")
                && response.contains("51") && !response.contains("Rob") && !response.contains("Zack"));

        assertTrue(sendCommandToServer("Delete from MArks where namE == 'Sion';").contains("OK"));
        assertFalse(sendCommandToServer("select * from marks ;").contains("Sion"));

        response = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 'i';");
        assertTrue(response.contains("OK") && response.contains("Simon") && response.contains("Chris")
                && response.contains("Achilles"));

        response = sendCommandToServer("SELECT id FROM marks WHERE pass == FALSE;");
        assertTrue(response.contains("OK") && response.contains("1") && response.contains("3")
                && response.contains("4"));

        response = sendCommandToServer("SELECT name FROM marks WHERE mark>60;");
        assertTrue(response.contains("OK") && response.contains("Pete") && response.contains("Hector") && response.contains("Achilles"));

        assertTrue(sendCommandToServer("DELETE FROM marks WHERE mark<40;").contains("OK"));
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("OK") && !response.contains("Simon") && !response.contains("Sion"));

        assertTrue(sendCommandToServer("ALTER TABLE marks ADD age;").contains("OK"));
        assertTrue(sendCommandToServer("SELECT * FROM marks;").contains("age"));

        assertTrue(sendCommandToServer("ALTER TABLE marks DROP mark;").contains("OK"));
        assertTrue(sendCommandToServer("ALTER TABLE marks DROP pass;").contains("OK"));

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("OK") && !response.contains("Mark") && !response.contains("Pass")
                && response.contains("Name") && response.contains("age"));

        assertTrue(sendCommandToServer("SELECT * FROM crew;").contains("ERROR"));
        assertTrue(sendCommandToServer("SELECT height FROM marks WHERE name == 'Chris';").contains("ERROR"));
        assertTrue(sendCommandToServer("DROP TABLE marks;").contains("OK"));
        assertTrue(sendCommandToServer("DROP TABLE coursework;").contains("OK"));
        assertTrue(sendCommandToServer("Drop DATABASE " + randomDatabaseName + ";").contains("OK"));
    }
}


