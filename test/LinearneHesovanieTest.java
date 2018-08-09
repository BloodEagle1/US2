import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinearneHesovanieTest {

    private LinearneHesovanie<Pacient> lh;
    private Random random;

    @Before
    public void setUp() throws IOException {

        Files.delete(Paths.get("pacientiTest.txt"));
        Files.delete(Paths.get("pacientiPrepTest.txt"));
        Files.createFile(Paths.get("pacientiTest.txt"));
        Files.createFile(Paths.get("pacientiPrepTest.txt"));
        File filePacienti = new File("pacientiTest.txt");
        File filePacientiPrep = new File("pacientiPrepTest.txt");

        this.lh = new LinearneHesovanie<>(3, 2, 2, 0.8, 0.64, filePacienti, filePacientiPrep, new Pacient());
    }

    @After
    public void zavri() {
        this.lh.uzavriRaf();
    }

    @Test
    public void vlozPrvky_ziadenPrvokSaNestratil() throws IOException {
            this.random = new Random(0);
            LinkedList<Pacient> listPacientov = new LinkedList<>();
            for (int i = 0; i < 10000; i++) {
                Pacient pacient = new Pacient("Tomas", "Bavala", random.nextInt(10000), LocalDate.now());
                if (lh.vloz(pacient)) {
                    listPacientov.add(pacient);
                }
            }

        System.out.println("zacal som hladanie");
            for (Pacient pacient : listPacientov) {
                assertNotNull(lh.najdiZaznam(pacient));
            }
        System.out.println("zacal som mazanie");
            for (Pacient pacient : listPacientov) {
                lh.vymazZaznam(pacient);
            }
        System.out.println("zacal som hladanie");
            for (Pacient pacient : listPacientov) {
                assertNull(lh.najdiZaznam(pacient));
            }
        System.out.println("zacal som vypis");
        LinkedList<String> linkedList = lh.vypisObsahSuboruDoArray();
        for (String string : linkedList) {
            System.out.println(string);
        }

    }

    @Test
    public void replikacie10vlozPrvky1000vymazVsetko_ziadenPrvokSaNestratil() throws IOException {
        for (long n = 0; n < 10; n++) {
            this.random = new Random(n);
            System.out.println(n);
            LinkedList<Pacient> listPacientov = new LinkedList<>();
            for (int i = 0; i < 1000; i++) {
                Pacient pacient = new Pacient("Tomas", "Bavala", random.nextInt(2000), LocalDate.now());
                if (lh.vloz(pacient)) {
                    listPacientov.add(pacient);
                }
            }

            for (Pacient pacient : listPacientov) {
                assertNotNull(lh.najdiZaznam(pacient));
            }
            for (Pacient pacient : listPacientov) {
                lh.vymazZaznam(pacient);
            }
            for (Pacient pacient : listPacientov) {
                assertNull(lh.najdiZaznam(pacient));
            }
//            LinkedList<String> linkedList = lh.vypisObsahSuboruDoArray();
//            for (String string : linkedList) {
//                System.out.println(string);
//            }
        }
    }

    @Test
    public void vlozPrvky10000vymaz5000nahodnych_ziadenPrvokSaNestratil() throws IOException {
        this.random = new Random(100000);
        TreeSet<Pacient> listPacientov = new TreeSet<>(Comparator.comparingInt(Pacient::getCisloPreukazu));
        for (int i = 0; i < 10000; i++) {
            Pacient pacient = new Pacient("Tomas", "Bavala", random.nextInt(10000), LocalDate.now());
            if (lh.vloz(pacient)) {
                listPacientov.add(pacient);
            }
        }
        System.out.println("hladam");
        for (Pacient pacient : listPacientov) {
            assertNotNull(lh.najdiZaznam(pacient));
        }
        System.out.println("mazem");
        Pacient pom;
        for (int i = 0; i < 5000; i++) {
            pom = new Pacient("Tomas", "Bavala", random.nextInt(10000), LocalDate.now());
            lh.vymazZaznam(pom);
            listPacientov.remove(pom);
        }
        System.out.println("hladam");
        for (Pacient pacient : listPacientov) {
            assertNotNull(lh.najdiZaznam(pacient));
        }
    }

    @Test
    public void testZPrednasky() throws IOException {
        lh.vloz(new Pacient("Tomas", "Bavala", 27, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 18, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 29, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 28, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 39, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 13, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 16, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 51, LocalDate.now()));
        lh.vloz(new Pacient("Tomas", "Bavala", 19, LocalDate.now()));
        lh.vymazZaznam(new Pacient("Tomas", "Bavala", 18, LocalDate.now()));
        lh.vymazZaznam(new Pacient("Tomas", "Bavala", 19, LocalDate.now()));
        lh.vymazZaznam(new Pacient("Tomas", "Bavala", 13, LocalDate.now()));
        lh.vymazZaznam(new Pacient("Tomas", "Bavala", 28, LocalDate.now()));
        LinkedList<String> linkedList = lh.vypisObsahSuboruDoArray();

        for (String string : linkedList) {
            System.out.println(string);
        }
    }

    @Test
    public void vlozPrvky10000nahodneMetody() throws IOException {
        this.random = new Random(100000);
        TreeSet<Pacient> listPacientov = new TreeSet<>(Comparator.comparingInt(Pacient::getCisloPreukazu));
        for (int i = 0; i < 10000; i++) {
            Pacient pacient = new Pacient("Tomas", "Bavala", random.nextInt(100000), LocalDate.now());
            if (lh.vloz(pacient)) {
                listPacientov.add(pacient);
            }
        }

        for (int i = 0; i < 5000; i++) {
            Pacient pacient = new Pacient("Tomas", "Bavala", random.nextInt(100000), LocalDate.now());
            if (random.nextDouble() < 0.5) {
                lh.vloz(pacient);
                listPacientov.add(pacient);
            } else {
                lh.vymazZaznam(pacient);
                listPacientov.remove(pacient);
            }
        }

        for (Pacient pacient : listPacientov) {
            assertNotNull(lh.najdiZaznam(pacient));
        }
    }
}