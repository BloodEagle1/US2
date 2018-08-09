import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinearneHesovanie<T extends IZaznam> {

    private int pocetZaznamov;
    private int pocetZaznamovPrep;
    private int pocetSkupin;
    private double maxHustota;
    private double minHustota;
    private int splitPointer;
    private int uroven;
    private int pocetObsadenychMiest;
    private int pocetAlkovanychMiest;
    private File subor;
    private File preplnovaciSubor;
    private RandomAccessFile raf;
    private RandomAccessFile prepRaf;
    private LinkedList<Long> adresyVolnychPrepBlokov;
    private T typ;

    public LinearneHesovanie(int pocetZaznamov, int pocetZaznamovPrep, int pocetSkupin, double maxHustota, double minHustota, File subor, File preplnovaciSubor, T typ) {
        this.pocetZaznamov = pocetZaznamov;
        this.pocetZaznamovPrep = pocetZaznamovPrep;
        this.pocetSkupin = pocetSkupin;
        this.maxHustota = maxHustota;
        this.minHustota = minHustota;
        this.splitPointer = 0;
        this.uroven = 0;
        this.subor = subor;
        this.preplnovaciSubor = preplnovaciSubor;
        this.pocetAlkovanychMiest = pocetSkupin * pocetZaznamov;
        this.pocetObsadenychMiest = 0;
        this.adresyVolnychPrepBlokov = new LinkedList<>();
        this.typ = (T) typ.clone();


        try {
            this.raf = new RandomAccessFile(this.subor, "rw");
            this.prepRaf = new RandomAccessFile(this.preplnovaciSubor, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LinearneHesovanie.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < pocetSkupin; i++) {
            Blok<T> blok = new Blok<>(pocetZaznamov, this.typ);
            vlozBlokDoSuboru(blok, i * blok.dajVelkost());
        }
    }

    public LinearneHesovanie(File subor, File preplnovaciSubor, T typ){
        this.subor = subor;
        this.preplnovaciSubor = preplnovaciSubor;
        this.typ = typ;

        try {
            this.raf = new RandomAccessFile(this.subor, "rw");
            this.prepRaf = new RandomAccessFile(this.preplnovaciSubor, "rw");
            nacitajLinearneHesovanie();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(LinearneHesovanie.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean vloz(T zaznam) throws IOException {
//        Blok<T>[] bloky = najdi(zaznam);
        int indexBloku = indexBloku(zaznam);
        ArrayList<Blok<T>> blokySkupiny = nacitajSkupinu(indexBloku);
        int indexBlokuVSkupine = najdiBlokZaznamu(blokySkupiny, zaznam);

//        if (bloky != null) {
        if (indexBlokuVSkupine == -1) {
            Blok<T>[] bloky = new Blok[3];
            for (int i = 0; i < blokySkupiny.size(); i++) {
                if (i == 0) {
                    bloky[i] = blokySkupiny.get(i);
                } else {
                    bloky[1] = bloky[2];
                    bloky[2] = blokySkupiny.get(i);
                }
                if (blokySkupiny.get(i).getPocetPlatnychZaznamov() < blokySkupiny.get(i).getPocetZaznamov()) {
                    break;
                }
            }
            Blok<T> aktualny = bloky[0];
            if (aktualny.getPocetPlatnychZaznamov() < this.pocetZaznamov) { // aktualny je volny
                aktualny.vlozZaznam(zaznam);
                this.pocetObsadenychMiest++;
                vlozBlokDoSuboru(aktualny, indexBloku * aktualny.dajVelkost());
            } else { // aktualny je plny
                long adresaPreplnovacieho = aktualny.getPreplnAdresa();
                Blok<T> preplnovaciPosledny = null;
                if (bloky[1] != null && bloky[2] != null) { // atkualny ma viac ako jeden preplnovaci blok
                    adresaPreplnovacieho = bloky[1].getPreplnAdresa();
                    preplnovaciPosledny = bloky[2];
                } else if (bloky[1] == null) { // aktualny ma prave jeden preplnovaci blok
                    preplnovaciPosledny = bloky[2];
                } else { // aktualny nema preplnovacie bloky
                }
                try {
                    if (adresaPreplnovacieho == -1) { // preplnovaci blok neexistuje
                        if (preplnovaciPosledny != null) {
                            throw new UnsupportedOperationException("Adresa preplnovacieho bloku = -1 ale preplnovaci blok nenulovy");
                        }
                        long adresaNovehoBloku = dajAdresuVolnehoBloku();
                        aktualny.setPreplnAdresa(adresaNovehoBloku);
                        Blok<T> novyBlok = new Blok<>(this.pocetZaznamovPrep, this.typ);
                        novyBlok.vlozZaznam(zaznam);
                        this.pocetObsadenychMiest++;
                        vlozDoPreplnovaciehoSuboru(novyBlok, adresaNovehoBloku);
                        vlozBlokDoSuboru(aktualny, indexBloku * aktualny.dajVelkost());
                    } else { // preplnovaci blok existuje
                        if (preplnovaciPosledny.getPocetPlatnychZaznamov() < this.pocetZaznamovPrep) { // preplnovaci je volny
                            preplnovaciPosledny.vlozZaznam(zaznam);
                            this.pocetObsadenychMiest++;
                            vlozDoPreplnovaciehoSuboru(preplnovaciPosledny, adresaPreplnovacieho);
                        } else { // preplnovaci je plny
                            long adresaNovehoBloku = dajAdresuVolnehoBloku();
                            preplnovaciPosledny.setPreplnAdresa(adresaNovehoBloku);
                            Blok<T> novyBlok = new Blok<>(this.pocetZaznamovPrep, this.typ);
                            novyBlok.vlozZaznam(zaznam);
                            this.pocetObsadenychMiest++;
                            vlozDoPreplnovaciehoSuboru(novyBlok, adresaNovehoBloku);
                            vlozDoPreplnovaciehoSuboru(preplnovaciPosledny, adresaPreplnovacieho);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(LinearneHesovanie.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            System.out.println(hustotaSuboru());
            while (hustotaSuboru() > this.maxHustota) {
                Blok<T> novyBlokSplitu = new Blok<>(this.pocetZaznamov, this.typ);
                int indexNoveBloku = this.splitPointer + (this.pocetSkupin * (int) Math.pow(2, this.uroven));
                indexBloku = this.splitPointer;
                Blok<T> blok = nacitajBlok(indexBloku * novyBlokSplitu.dajVelkost());
                preusporiadaj(blok, novyBlokSplitu, indexBloku);
                zvysPocetAlokovanychMiest();
                vlozBlokDoSuboru(blok, indexBloku * blok.dajVelkost());
                vlozBlokDoSuboru(novyBlokSplitu, indexNoveBloku * blok.dajVelkost());
                this.splitPointer++;
                if (this.splitPointer >= (this.pocetSkupin * ((int) Math.pow(2, this.uroven)))) {
                    this.splitPointer = 0;
                    this.uroven++;
                }
            }
            skontrolujVolneBloky();
            return true;
        }
        return false;
    }

//    public Blok<T>[] najdi(T zaznam) throws IOException {
//        Blok<T>[] bloky = new Blok[3];
//        int indexBloku = indexBloku(zaznam);
//
//        Blok<T> blok = new Blok<>(this.pocetZaznamov, this.typ);
//        Blok<T> blokZaznamu = nacitajBlok((blok.dajVelkost() * indexBloku));
//        bloky[0] = blokZaznamu;
//        while (true) {
//            T[] poleZaznamov = blokZaznamu.getPoleZaznamov();
//            for (T zaznam1 : poleZaznamov) {
//                if (zaznam.porovnaj(zaznam1)) {
//                    return null;
//                }
//            }
//            if (blokZaznamu.getPreplnAdresa() != -1) {
//                Blok<T> blokPreplnovacieho = nacitajPreplnovaciBlok(blokZaznamu.getPreplnAdresa());
//                bloky[1] = bloky[2];
//                bloky[2] = blokPreplnovacieho;
//                blokZaznamu = blokPreplnovacieho;
//            } else {
//                return bloky;
//            }
//        }
//    }

    public int najdiBlokZaznamu(ArrayList<Blok<T>> blokySkupiny, T zaznam) throws IOException {
        T[] poleZaznamov;
        Blok<T> blokZaznamu;
        for (int i = 0; i < blokySkupiny.size(); i++) {
            blokZaznamu = blokySkupiny.get(i);
            poleZaznamov = blokZaznamu.getPoleZaznamov();
            for (T zaznam1 : poleZaznamov) {
                if (zaznam.porovnaj(zaznam1)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public Blok<T>[] najdiZaznam(T zaznam) throws IOException {
        Blok<T>[] bloky = new Blok[3];
        int indexBloku = indexBloku(zaznam);

        Blok<T> blok = new Blok<>(this.pocetZaznamov, this.typ);
        Blok<T> blokZaznamu = nacitajBlok((blok.dajVelkost() * indexBloku));
        bloky[0] = blokZaznamu;
        while (true) {
            T[] poleZaznamov = blokZaznamu.getPoleZaznamov();
            for (T zaznam1 : poleZaznamov) {
                if (zaznam.porovnaj(zaznam1)) {
                    return bloky;
                }
            }
            if (blokZaznamu.getPreplnAdresa() != -1) {
                Blok<T> blokPreplnovacieho = nacitajPreplnovaciBlok(blokZaznamu.getPreplnAdresa());
                bloky[1] = bloky[2];
                bloky[2] = blokPreplnovacieho;
                blokZaznamu = blokPreplnovacieho;
            } else {
                return null;
            }
        }
    }

    public boolean vymazZaznam(T zaznam) throws IOException {
        int indexBloku = indexBloku(zaznam);
        ArrayList<Blok<T>> blokySkupiny = nacitajSkupinu(indexBloku);
        int indexBlokuVSkupine = najdiBlokZaznamu(blokySkupiny, zaznam);

        if (indexBlokuVSkupine != -1) {
            vymazZaznamZBloku(blokySkupiny, indexBlokuVSkupine, indexBloku * blokySkupiny.get(0).dajVelkost(), zaznam);
            ArrayList<Blok<T>> mazanaSkupina;
            ArrayList<Blok<T>> cielovaSkupina;
            while (hustotaSuboru() < this.minHustota) {
                int indexMazanejSkupiny;
                int indexCielovejSkupiny;
                if (this.splitPointer > 0) {
                    indexMazanejSkupiny = this.splitPointer + (this.pocetSkupin * (int) Math.pow(2, this.uroven)) - 1;
                    if (indexMazanejSkupiny == indexBloku) {
                        mazanaSkupina = blokySkupiny;
                    } else {
                        mazanaSkupina = nacitajSkupinu(indexMazanejSkupiny);
                    }
                    indexCielovejSkupiny = this.splitPointer - 1;
                    if (indexCielovejSkupiny == indexBloku) {
                        cielovaSkupina = blokySkupiny;
                    } else {
                        cielovaSkupina = nacitajSkupinu(indexCielovejSkupiny);
                    }
                    presunZaznamyMazanejSkupiny(mazanaSkupina, cielovaSkupina, indexCielovejSkupiny);
                    this.raf.setLength(indexMazanejSkupiny * blokySkupiny.get(0).dajVelkost());
                    znizPocetAlokovanychMiest();
                    this.splitPointer--;
                } else if (this.splitPointer == 0 && this.uroven > 0) {
                    indexMazanejSkupiny = (this.pocetSkupin * (int) Math.pow(2, this.uroven)) - 1;
                    if (indexMazanejSkupiny == indexBloku) {
                        mazanaSkupina = blokySkupiny;
                    } else {
                        mazanaSkupina = nacitajSkupinu(indexMazanejSkupiny);
                    }
                    indexCielovejSkupiny = (this.pocetSkupin * (int) Math.pow(2, this.uroven - 1)) - 1;
                    if (indexCielovejSkupiny == indexBloku) {
                        cielovaSkupina = blokySkupiny;
                    } else {
                        cielovaSkupina = nacitajSkupinu(indexCielovejSkupiny);
                    }
                    presunZaznamyMazanejSkupiny(mazanaSkupina, cielovaSkupina, indexCielovejSkupiny);
                    this.raf.setLength(indexMazanejSkupiny * blokySkupiny.get(0).dajVelkost());
                    znizPocetAlokovanychMiest();
                    this.splitPointer = indexCielovejSkupiny;
                    this.uroven--;
                } else if (this.splitPointer == 0 && this.uroven == 0) {
                    break;
                }
            }
            skontrolujVolneBloky();
            return true;
        }
        return false;
    }

    public LinkedList<String> vypisObsahSuboruDoArray() {
        LinkedList<String> arr = new LinkedList<>();
        try {
            for (int i = 0; i < this.subor.length(); i++) {
                Blok<T> blok = new Blok<>(pocetZaznamov, this.typ);
                this.raf.seek(i * blok.dajVelkost());
                byte[] b = new byte[blok.dajVelkost()];
                this.raf.readFully(b);
                blok.fromByteArray(b);
                arr.add("\nIndex:" + i + "\nAdresa: " + (i * blok.dajVelkost()) + "\n" + blok.toString() + "\n");
                if (blok.getPreplnAdresa() != -1) {
                    do {
                        long prepAdresa = blok.getPreplnAdresa();
                        blok = nacitajPreplnovaciBlok(blok.getPreplnAdresa());
                        arr.add("\nPrepAdresa: " + (prepAdresa) + "\n" + blok.toString() + "\n");
                    } while (blok.getPreplnAdresa() != -1);
                }
                arr.add("--------------------------------------------------------------------------------------");
            }
        } catch (IOException e) {
        }
        return arr;
    }

    public void uzavriRaf() {
        try {
            this.raf.close();
            this.prepRaf.close();
        } catch (IOException ex) {
            Logger.getLogger(LinearneHesovanie.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ArrayList<Blok<T>> nacitajSkupinu(int indexBloku) throws IOException {
        ArrayList<Blok<T>> blokySkupiny = new ArrayList<>();
        Blok<T> blok = new Blok<>(this.pocetZaznamov, this.typ);
        Blok<T> blokZaznamu = nacitajBlok((blok.dajVelkost() * indexBloku));

        while (true) {
            blokySkupiny.add(blokZaznamu);
            if (blokZaznamu.getPreplnAdresa() != -1) {
                blokZaznamu = nacitajPreplnovaciBlok(blokZaznamu.getPreplnAdresa());
            } else {
                return blokySkupiny;
            }
        }
    }

    private void vymazZaznamZBloku(ArrayList<Blok<T>> blokySkupiny, int indexBlokuVSkupine, long adresaSkupiny, T zaznam) throws IOException {
        Blok<T> blokZaznamu = blokySkupiny.get(indexBlokuVSkupine);
        T[] poleZaznamov = blokZaznamu.getPoleZaznamov();
        for (int i = 0; i < blokZaznamu.getPocetPlatnychZaznamov(); i++) {
            if (poleZaznamov[i].porovnaj(zaznam)) {
                if ((i + 1) != blokZaznamu.getPocetPlatnychZaznamov()) {
                    poleZaznamov[i] = (T) poleZaznamov[blokZaznamu.getPocetPlatnychZaznamov() - 1].clone();
                }
                poleZaznamov[blokZaznamu.getPocetPlatnychZaznamov() - 1] = (T) this.typ.clone();
                blokZaznamu.setPocetPlatnychZaznamov(blokZaznamu.getPocetPlatnychZaznamov() - 1);
            }
        }

        if (blokZaznamu.getPocetPlatnychZaznamov() == 0) {
            if (indexBlokuVSkupine != 0) {
                znizPocetAlokovanychMiestPrep();
                pridajDoVolnychBlokov(blokySkupiny.get(indexBlokuVSkupine - 1));
                blokySkupiny.get(indexBlokuVSkupine - 1).setPreplnAdresa(blokZaznamu.getPreplnAdresa());
                blokySkupiny.remove(indexBlokuVSkupine);
                if (indexBlokuVSkupine - 1 != 0) {
                    vlozDoPreplnovaciehoSuboru(blokySkupiny.get(indexBlokuVSkupine - 1), blokySkupiny.get(indexBlokuVSkupine - 2).getPreplnAdresa());
                } else {
                    vlozBlokDoSuboru(blokySkupiny.get(indexBlokuVSkupine - 1), adresaSkupiny);
                }
            } else {
                spojBloky(blokySkupiny, indexBlokuVSkupine, adresaSkupiny);
            }
        } else {
            spojBloky(blokySkupiny, indexBlokuVSkupine, adresaSkupiny);
        }
        this.pocetObsadenychMiest--;
    }

    private void spojBloky(ArrayList<Blok<T>> blokySkupiny, int indexBlokuVSkupine, long adresaSkupiny) throws IOException {
        Blok<T> blokZaznamu = blokySkupiny.get(indexBlokuVSkupine);
        Blok<T> predoslyBlok;
        Blok<T> aktualnyBlok;
        boolean spajalSom = false;
        for (int i = 0; i < blokySkupiny.size(); i++) {
            if (i != indexBlokuVSkupine) {
                aktualnyBlok = blokySkupiny.get(i);
                if (aktualnyBlok.getPocetPlatnychZaznamov() <= (blokZaznamu.getPocetZaznamov() - blokZaznamu.getPocetPlatnychZaznamov()) ||
                        blokZaznamu.getPocetPlatnychZaznamov() <= (aktualnyBlok.getPocetZaznamov() - aktualnyBlok.getPocetPlatnychZaznamov()) ){

                    spajalSom = true;
                    if (i < indexBlokuVSkupine) {
                        presunZaznamyMazaneBloku(blokZaznamu, aktualnyBlok);
                        if (i == indexBlokuVSkupine - 1) {
                            predoslyBlok = aktualnyBlok;
                        } else {
                            predoslyBlok = blokySkupiny.get(indexBlokuVSkupine - 1);
                        }
                        if (i != 0) {
                            pridajDoVolnychBlokov(predoslyBlok);
                            znizPocetAlokovanychMiestPrep();
                        }
                        if (predoslyBlok.getPreplnAdresa() != this.prepRaf.length()) {
                            predoslyBlok.setPreplnAdresa(blokZaznamu.getPreplnAdresa());
                        }

                        if (i != 0) {
                            if (i != indexBlokuVSkupine - 1) {
                                vlozDoPreplnovaciehoSuboru(aktualnyBlok, blokySkupiny.get(i - 1).getPreplnAdresa());
                                vlozDoPreplnovaciehoSuboru(predoslyBlok, blokySkupiny.get(indexBlokuVSkupine - 2).getPreplnAdresa());
                            } else if (i == indexBlokuVSkupine - 1) {
                                vlozDoPreplnovaciehoSuboru(aktualnyBlok, blokySkupiny.get(i - 1).getPreplnAdresa());
                            }
                        } else {
                            if (i != indexBlokuVSkupine - 1) {
                                vlozBlokDoSuboru(aktualnyBlok, adresaSkupiny);
                                vlozDoPreplnovaciehoSuboru(predoslyBlok, blokySkupiny.get(indexBlokuVSkupine - 2).getPreplnAdresa());
                            } else if (i == indexBlokuVSkupine - 1) {
                                vlozBlokDoSuboru(aktualnyBlok, adresaSkupiny);
                            }
                        }
                        blokySkupiny.remove(indexBlokuVSkupine);
                    } else {
                        presunZaznamyMazaneBloku(aktualnyBlok, blokZaznamu);
                        if (i == indexBlokuVSkupine + 1) {
                            predoslyBlok = blokZaznamu;
                        } else {
                            predoslyBlok = blokySkupiny.get(i - 1);
                        }

                        if (i != 0) {
                            pridajDoVolnychBlokov(predoslyBlok);
                            znizPocetAlokovanychMiestPrep();
                        }
                        if (predoslyBlok.getPreplnAdresa() != this.prepRaf.length()) {
                            predoslyBlok.setPreplnAdresa(aktualnyBlok.getPreplnAdresa());
                        }
                        if (indexBlokuVSkupine != 0) {
                            if (i != indexBlokuVSkupine + 1) {
                                vlozDoPreplnovaciehoSuboru(blokZaznamu, blokySkupiny.get(indexBlokuVSkupine - 1).getPreplnAdresa());
                                vlozDoPreplnovaciehoSuboru(predoslyBlok, blokySkupiny.get(i - 2).getPreplnAdresa());
                            } else if (i == indexBlokuVSkupine + 1) {
                                vlozDoPreplnovaciehoSuboru(blokZaznamu, blokySkupiny.get(indexBlokuVSkupine - 1).getPreplnAdresa());
                            }
                        } else {
                            if (i != indexBlokuVSkupine + 1) {
                                vlozBlokDoSuboru(blokZaznamu, adresaSkupiny);
                                vlozDoPreplnovaciehoSuboru(predoslyBlok, blokySkupiny.get(i - 2).getPreplnAdresa());
                            } else if (i == indexBlokuVSkupine + 1) {
                                vlozBlokDoSuboru(blokZaznamu, adresaSkupiny);
                            }
                        }
                        blokySkupiny.remove(i);
                    }
                }
            }
        }

        if (!spajalSom) {
            if (indexBlokuVSkupine != 0) {
                vlozDoPreplnovaciehoSuboru(blokZaznamu, blokySkupiny.get(indexBlokuVSkupine - 1).getPreplnAdresa());
            } else {
                vlozBlokDoSuboru(blokZaznamu, adresaSkupiny);
            }
        }
    }

    private void presunZaznamyMazanejSkupiny(ArrayList<Blok<T>> mazanaSkupina, ArrayList<Blok<T>> cielovaSkupina, int indexCielovejSkupiny) throws IOException {
        LinkedList<T> zaznamy = new LinkedList<>();
        T[] poleZaznamov;
        for (Blok<T> mazanyBlok : mazanaSkupina) {
            poleZaznamov = mazanyBlok.getPoleZaznamov();
            for (int i = 0; i < mazanyBlok.getPocetPlatnychZaznamov(); i++) {
                zaznamy.add(poleZaznamov[i]);
            }
            if (mazanyBlok.getPreplnAdresa() != -1)
                znizPocetAlokovanychMiestPrep();
            pridajDoVolnychBlokov(mazanyBlok);
        }

        Boolean[] zmeneneBloky = new Boolean[cielovaSkupina.size()];
        int aktualnyBlok = 0;
        boolean bolVlozeny;
        Blok<T> blok;
        Blok<T> novyPreplnovaci = null;
        long adresaPrepl = -1;
        for (T zaznam : zaznamy) {
            bolVlozeny = false;
            for (int i = aktualnyBlok; i < cielovaSkupina.size(); i++) {
                blok = cielovaSkupina.get(i);
                if (blok.getPocetPlatnychZaznamov() < blok.getPocetZaznamov()) {
                    blok.vlozZaznam(zaznam);
                    bolVlozeny = true;
                    zmeneneBloky[i] = true;
                    break;
                } else {
                    if (i < cielovaSkupina.size() - 1) {
                        aktualnyBlok++;
                    } else {
                        break;
                    }
                }
            }

            if (!bolVlozeny) {
                if (novyPreplnovaci == null) {
                    adresaPrepl = dajAdresuVolnehoBloku();
                    cielovaSkupina.get(aktualnyBlok).setPreplnAdresa(adresaPrepl);
                    zmeneneBloky[aktualnyBlok] = true;
                    novyPreplnovaci = new Blok<>(this.pocetZaznamovPrep, this.typ);
                    novyPreplnovaci.vlozZaznam(zaznam);
                } else {
                    if (novyPreplnovaci.getPocetPlatnychZaznamov() < novyPreplnovaci.getPocetZaznamov()) {
                        novyPreplnovaci.vlozZaznam(zaznam);
                    } else {
                        long adresaPreplpom = dajAdresuVolnehoBloku();
                        novyPreplnovaci.setPreplnAdresa(adresaPreplpom);
                        vlozDoPreplnovaciehoSuboru(novyPreplnovaci, adresaPrepl);
                        adresaPrepl = adresaPreplpom;
                        novyPreplnovaci = new Blok<>(this.pocetZaznamovPrep, this.typ);
                        novyPreplnovaci.vlozZaznam(zaznam);
                    }
                }
            }
        }

        for (int i = 0; i < zmeneneBloky.length; i++) {
            if (zmeneneBloky[i] != null && zmeneneBloky[i]) {
                if (i > 0) {
                    vlozDoPreplnovaciehoSuboru(cielovaSkupina.get(i), cielovaSkupina.get(i - 1).getPreplnAdresa());
                } else {
                    vlozBlokDoSuboru(cielovaSkupina.get(i), indexCielovejSkupiny * cielovaSkupina.get(i).dajVelkost());
                }
            }
        }

        if (novyPreplnovaci != null)
            vlozDoPreplnovaciehoSuboru(novyPreplnovaci, adresaPrepl);

    }

    private void presunZaznamyMazaneBloku(Blok<T> mazanyBlok, Blok<T> cielovyBlok) throws IOException {
        T[] poleZaznamov = mazanyBlok.getPoleZaznamov();
        for (int j = 0; j < mazanyBlok.getPocetPlatnychZaznamov(); j++) {
            cielovyBlok.vlozZaznam((T) poleZaznamov[j].clone());
        }
    }

    private long dajAdresuVolnehoBloku() throws IOException {
        zvysPocetAlokovanychMiestPrep();
        if (this.adresyVolnychPrepBlokov.isEmpty()) {
            return this.prepRaf.length();
        }
        return this.adresyVolnychPrepBlokov.poll();
    }

    private void preusporiadaj(Blok<T> blok, Blok<T> novyBlokSplitu, int indexBloku) throws IOException {
        LinkedList<Blok<T>> listBlokov = new LinkedList<>();
        LinkedList<T> zaznamy = new LinkedList<>();
        Blok<T> pomBlok = blok.clone();
        blok.setPocetPlatnychZaznamov(0);


        long preplnAdresa = 0;
        while (preplnAdresa != -1) {
            T[] polezaznamov = pomBlok.getPoleZaznamov();
            for (int i = 0; i < pomBlok.getPocetPlatnychZaznamov(); i++) {
                zaznamy.add(polezaznamov[i]);
            }
            pomBlok.setPocetPlatnychZaznamov(0);
            preplnAdresa = pomBlok.getPreplnAdresa();
            if (pomBlok.getPreplnAdresa() != -1) {
                pomBlok = nacitajPreplnovaciBlok(pomBlok.getPreplnAdresa());
                znizPocetAlokovanychMiestPrep();
                listBlokov.add(pomBlok);
            }
        }

        T[] polezaznamovBloku = blok.getPoleZaznamov();
        for (int i = 0; i < polezaznamovBloku.length; i++) {
            polezaznamovBloku[i] = (T) this.typ.clone();
        }


        pridajDoVolnychBlokov(blok);
        blok.setPreplnAdresa(-1);

        for (Blok<T> listBlok : listBlokov) {
            pridajDoVolnychBlokov(listBlok);
            listBlok.setPreplnAdresa(-1);
        }

        Blok<T>[] poleBlokovNaUlozenieAktualneho = new Blok[2];
        Blok<T> predoslyBlokAktualneho = new Blok<>(this.pocetZaznamov, this.typ);
        Blok<T>[] poleBlokovNaUlozenieNoveho = new Blok[2];
        Blok<T> predoslyBlokNoveho = new Blok<>(this.pocetZaznamov, this.typ);
        poleBlokovNaUlozenieAktualneho[0] = predoslyBlokAktualneho;
        poleBlokovNaUlozenieAktualneho[1] = blok;
        poleBlokovNaUlozenieNoveho[0] = predoslyBlokNoveho;
        poleBlokovNaUlozenieNoveho[1] = novyBlokSplitu;

        for (T zaznam : zaznamy) {
            int hashZaznamu = zaznam.dajHash();
            int indexBlokuZaznamu = hashuj1(hashZaznamu);
            if (indexBloku == indexBlokuZaznamu) {
                poleBlokovNaUlozenieAktualneho = vlozZaznamDoBlokuPriPreusporiadani(poleBlokovNaUlozenieAktualneho, zaznam);
            } else {
                poleBlokovNaUlozenieNoveho = vlozZaznamDoBlokuPriPreusporiadani(poleBlokovNaUlozenieNoveho, zaznam);
            }
        }
        if (poleBlokovNaUlozenieAktualneho[0].getPreplnAdresa() != -1) {
            vlozDoPreplnovaciehoSuboru(poleBlokovNaUlozenieAktualneho[1], poleBlokovNaUlozenieAktualneho[0].getPreplnAdresa());
        }
        if (poleBlokovNaUlozenieNoveho[0].getPreplnAdresa() != -1) {
            vlozDoPreplnovaciehoSuboru(poleBlokovNaUlozenieNoveho[1], poleBlokovNaUlozenieNoveho[0].getPreplnAdresa());
        }
    }

    private void pridajDoVolnychBlokov(Blok<T> blok) throws IOException {
        if (blok.getPreplnAdresa() != -1) {
            if ((blok.getPreplnAdresa() + blok.dajVelkost()) != prepRaf.length()) {
                this.adresyVolnychPrepBlokov.add(blok.getPreplnAdresa());
            }
        }
    }

    private Blok<T>[] vlozZaznamDoBlokuPriPreusporiadani(Blok<T>[] poleBlokovNaUlozenie, T zaznam) throws IOException {
        if (poleBlokovNaUlozenie[1].getPocetPlatnychZaznamov() < poleBlokovNaUlozenie[1].getPocetZaznamov()) {
            poleBlokovNaUlozenie[1].vlozZaznam(zaznam);
            return poleBlokovNaUlozenie;
        } else {
            if (poleBlokovNaUlozenie[0].getPreplnAdresa() == -1) {
                poleBlokovNaUlozenie[1].setPreplnAdresa(dajAdresuVolnehoBloku());
                poleBlokovNaUlozenie[0] = poleBlokovNaUlozenie[1].clone();
                poleBlokovNaUlozenie[1] = new Blok<>(this.pocetZaznamovPrep, this.typ);
            } else {
                poleBlokovNaUlozenie[1].setPreplnAdresa(dajAdresuVolnehoBloku());
                vlozDoPreplnovaciehoSuboru(poleBlokovNaUlozenie[1], poleBlokovNaUlozenie[0].getPreplnAdresa());
                poleBlokovNaUlozenie[0] = poleBlokovNaUlozenie[1].clone();
                poleBlokovNaUlozenie[1] = new Blok<>(this.pocetZaznamovPrep, this.typ);
            }
            poleBlokovNaUlozenie[1].vlozZaznam(zaznam);
            return poleBlokovNaUlozenie;
        }

    }

    private double hustotaSuboru() {
        return ((double) (this.pocetObsadenychMiest) / (double) (this.pocetAlkovanychMiest));
    }

    public int indexBloku(T zaznam) {
        int hashZaznamu = zaznam.dajHash();
        int indexBloku = hashuj(hashZaznamu);
        if (indexBloku < this.splitPointer) {
            indexBloku = hashuj1(hashZaznamu);
        }

        return indexBloku;
    }

    private int hashuj(int hash) {
        int mod = this.pocetSkupin * (int) Math.pow(2, this.uroven);
        return (hash % mod);
    }

    private int hashuj1(int hash) {
        int mod = this.pocetSkupin * (int) Math.pow(2, this.uroven + 1);
        return (hash % mod);
    }

    private Blok<T> nacitajBlok(long adresa) {
        if (adresa == -1) { //neplatna adresa
            return null;
        }
        Blok<T> blok = new Blok<>(this.pocetZaznamov, this.typ);
        try {
            this.raf.seek(adresa);
            byte[] arr = new byte[blok.dajVelkost()];
            this.raf.readFully(arr);
            blok.fromByteArray(arr);
        } catch (IOException ex) {
            Logger.getLogger(LinearneHesovanie.class.getName()).log(Level.SEVERE, null, ex);
        }
        return blok;
    }

    private Blok<T> nacitajPreplnovaciBlok(long adresa) throws IOException {
        this.prepRaf.seek(adresa);
        Blok<T> blok = new Blok<>(this.pocetZaznamovPrep, this.typ);

        byte[] arr = new byte[blok.dajVelkost()];
        this.prepRaf.readFully(arr);
        blok.fromByteArray(arr);
        return blok;
    }

    public void vlozBlokDoSuboru(Blok<T> blok, long adresa) {
        try {
            byte[] arr;
            arr = blok.toByteArray();
            this.raf.seek(adresa);
            this.raf.write(arr);
        } catch (IOException ex) {

        }
    }

    public void vlozDoPreplnovaciehoSuboru(Blok<T> blok, long adresa) {
        try {
            byte[] arr;
            arr = blok.toByteArray();
            this.prepRaf.seek(adresa);
            this.prepRaf.write(arr);
        } catch (IOException ex) {

        }
    }

    private void zvysPocetAlokovanychMiest() {
        this.pocetAlkovanychMiest += this.pocetZaznamov;
    }

    private void znizPocetAlokovanychMiest() {
        this.pocetAlkovanychMiest -= this.pocetZaznamov;
    }

    private void zvysPocetAlokovanychMiestPrep() {
        this.pocetAlkovanychMiest += this.pocetZaznamovPrep;
    }

    private void znizPocetAlokovanychMiestPrep() {
        this.pocetAlkovanychMiest -= this.pocetZaznamovPrep;
    }

    private void skontrolujVolneBloky() throws IOException {
        if (this.adresyVolnychPrepBlokov != null){
            this.adresyVolnychPrepBlokov.sort(Long::compareTo);

            Blok<T> pomBLok = new Blok<>(pocetZaznamovPrep, this.typ);
            long potencionalnyKoniec = -1;
            int iterator = 1;
            while (!this.adresyVolnychPrepBlokov.isEmpty()) {
                if (this.adresyVolnychPrepBlokov.getLast() + (pomBLok.dajVelkost() * iterator) == this.prepRaf.length()) {
                    potencionalnyKoniec = this.adresyVolnychPrepBlokov.pollLast();
                    iterator++;
                } else {
                    break;
                }
            }
            if (potencionalnyKoniec != -1) {
                this.prepRaf.setLength(potencionalnyKoniec);
            }
        }
    }

    public void ulozLinearneHesovanie() throws IOException {
        Files.delete(Paths.get("pacientiConfig.txt"));
        File config = new File("pacientiConfig.txt");
        try (FileWriter fw = new FileWriter(config.getName(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(this.pocetZaznamov + "," + this.pocetZaznamovPrep + "," + this.pocetSkupin + "," + this.maxHustota + "," + this.minHustota + "," + this.splitPointer + "," + this.uroven +
                    "," + this.pocetObsadenychMiest + "," + this.pocetAlkovanychMiest);
            if (!this.adresyVolnychPrepBlokov.isEmpty()){
                for (long adresaVolnehoBloku : this.adresyVolnychPrepBlokov) {
                    out.println(adresaVolnehoBloku);
                }
            }
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    private void nacitajLinearneHesovanie() {
        File config = new File("pacientiConfig.txt");

        if (config.isFile()) {
            try (FileReader fr = new FileReader("pacientiConfig.txt");
                 BufferedReader br = new BufferedReader(fr)) {

                String riadok = br.readLine();
                int iterator = -1;
                while (riadok != null) {
                    iterator++;
                    if (iterator == 0) {
                        String[] split = riadok.split(",");
                        this.pocetZaznamov = Integer.parseInt(split[0]);
                        this.pocetZaznamovPrep = Integer.parseInt(split[1]);
                        this.pocetSkupin = Integer.parseInt(split[2]);
                        this.maxHustota = Double.parseDouble(split[3]);
                        this.minHustota = Double.parseDouble(split[4]);
                        this.splitPointer = Integer.parseInt(split[5]);
                        this.uroven = Integer.parseInt(split[6]);
                        this.pocetObsadenychMiest = Integer.parseInt(split[7]);
                        this.pocetAlkovanychMiest = Integer.parseInt(split[8]);
                        this.adresyVolnychPrepBlokov = new LinkedList<>();
                        this.subor = new File("pacienti.txt");
                        this.preplnovaciSubor = new File("pacientiPrep.txt");
                    }else {
                        this.adresyVolnychPrepBlokov.add(Long.parseLong(riadok));
                    }
                    riadok = br.readLine();
                }
            } catch (IOException e) {
            }
        }
    }
}

