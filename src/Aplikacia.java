import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Random;

public class Aplikacia {

    private LinearneHesovanie<Pacient> linearneHesovanie;
    private File filePacienti;
    private File filePacientiPrep;
    private File fileLinPacientiConfig;
    private LocalDate aktualnyDatum;

    public Aplikacia() {
        this.filePacienti = new File("pacienti.txt");
        this.filePacientiPrep = new File("pacientiPrep.txt");
        this.fileLinPacientiConfig = new File("pacientiConfig.txt");
        this.aktualnyDatum = LocalDate.now();
    }

    public void setAktualnyDatum(LocalDate aktualnyDatum) {
        this.aktualnyDatum = aktualnyDatum;
    }

    public String vyhladajPacienta(int idPreukazu) throws IOException {
        Blok<Pacient>[] bloky = this.linearneHesovanie.najdiZaznam(new Pacient(idPreukazu));
        Blok<Pacient> blokZaznamu;
        Pacient pacient = new Pacient(idPreukazu);
        if (bloky == null) {
            return "Nenasiel sa zaznam";
        } else {
            if (bloky[1] == null && bloky[2] == null) {
                blokZaznamu = bloky[0];
            } else {
                blokZaznamu = bloky[2];
            }
            for (Pacient pacient1 : blokZaznamu.getPoleZaznamov()) {
                if (pacient.porovnaj(pacient1)) {
                    return pacient1.toString();
                }
            }
        }
        return "";
    }

    public String pridajPacienta(String meno, String priezvisko, int idPreukazu, LocalDate datumNarodenia) throws IOException {
        Pacient pacient = new Pacient(meno, priezvisko, idPreukazu, datumNarodenia);
        if (this.linearneHesovanie.vloz(pacient)) {
            return "Pacient bol vlozeny";
        } else {
            return "Pacient nebol vlozeny";
        }
    }

    public String vymazPacienta(int idPreukazu) throws IOException {
        Pacient pacient = new Pacient(idPreukazu);
        if (this.linearneHesovanie.vymazZaznam(pacient)) {
            return "Pacient bol vymazany";
        } else {
            return "Pacient neexistuje";
        }
    }

    public String hospitalizujPacienta(int idPreukazu, String diagnoza) throws IOException {
        Pacient pacient = new Pacient(idPreukazu);
        Blok<Pacient>[] bloky = this.linearneHesovanie.najdiZaznam(new Pacient(idPreukazu));
        int indexBloku = this.linearneHesovanie.indexBloku(new Pacient(idPreukazu));
        Blok<Pacient> blokZaznamu;
        if (bloky == null) {
            return "Nenasiel sa zaznam";
        } else {
            if (bloky[1] == null && bloky[2] == null) {
                blokZaznamu = bloky[0];
            } else {
                blokZaznamu = bloky[2];
            }
            for (Pacient pacient1 : blokZaznamu.getPoleZaznamov()) {
                if (pacient.porovnaj(pacient1)) {
                    pacient = pacient1;
                }
            }
        }

        Hospitalizacia[] hospitalizacie = pacient.getHospitalizacie().clone();
        if (pacient.getPocetPlatnychHospitalizacii() == 0) {
            hospitalizacie[pacient.getPocetPlatnychHospitalizacii()] = new Hospitalizacia(this.aktualnyDatum, diagnoza);
            pacient.setHospitalizacie(hospitalizacie);
            pacient.zvysPocetPlatnychHospitalizacii();
            vlozDoSuboru(bloky, indexBloku);
        } else {
            if (hospitalizacie[pacient.getPocetPlatnychHospitalizacii() - 1].getDatumKonHospitalizacie().compareTo(LocalDate.parse("0001-01-01")) == 0) {
                return "Pacient je uz aktualne hospitalizaovany";
            } else {
                hospitalizacie[pacient.getPocetPlatnychHospitalizacii()] = new Hospitalizacia(this.aktualnyDatum, diagnoza);
                pacient.setHospitalizacie(hospitalizacie);
                pacient.zvysPocetPlatnychHospitalizacii();
                vlozDoSuboru(bloky, indexBloku);
            }
        }
        return "Pacient bol hospitalizaovany";
    }

    private void vlozDoSuboru(Blok<Pacient>[] bloky, int indexSkupiny) {
        if (bloky[1] == null && bloky[2] == null) {
            this.linearneHesovanie.vlozBlokDoSuboru(bloky[0], indexSkupiny * bloky[0].dajVelkost());
        } else if (bloky[1] == null) {
            this.linearneHesovanie.vlozDoPreplnovaciehoSuboru(bloky[2], bloky[0].getPreplnAdresa());
        } else {
            this.linearneHesovanie.vlozDoPreplnovaciehoSuboru(bloky[2], bloky[1].getPreplnAdresa());
        }
    }

    public void premazSubory() throws IOException {
        Files.delete(Paths.get("pacienti.txt"));
        Files.delete(Paths.get("pacientiPrep.txt"));
        Files.delete(Paths.get("pacientiConfig.txt"));
    }

    public void vytvorSubory() throws IOException {
        Files.createFile(Paths.get("pacienti.txt"));
        Files.createFile(Paths.get("pacientiPrep.txt"));
        Files.createFile(Paths.get("pacientiConfig.txt"));
    }

    public String vytvorLinearneHesovanie(int pocetZaznamov, int pocetZaznamovPrep, int pocetSkupin, double maxHustota, double minHustota) throws IOException {
        if (this.linearneHesovanie != null)
            this.linearneHesovanie.uzavriRaf();
        premazSubory();
        vytvorSubory();
        this.linearneHesovanie = new LinearneHesovanie<>(pocetZaznamov, pocetZaznamovPrep, pocetSkupin, maxHustota, minHustota, this.filePacienti, this.filePacientiPrep, new Pacient());
        return "Linearne hesovanie bolo vytvorene";
    }

    public String nacitajLinearneHesovanie() throws IOException {
        this.linearneHesovanie = new LinearneHesovanie<>(this.filePacienti, this.filePacientiPrep, new Pacient());
        return "Linearne hesovanie bolo nacitane";
    }

    public void ulozLinearneHesovanie() throws IOException {
        this.linearneHesovanie.ulozLinearneHesovanie();
    }

    public String ukonciHospitalizaciuPacientovi(int idPreukazu) throws IOException {
        Pacient pacient = new Pacient(idPreukazu);
        Blok<Pacient>[] bloky = this.linearneHesovanie.najdiZaznam(new Pacient(idPreukazu));
        int indexBloku = this.linearneHesovanie.indexBloku(pacient);
        Blok<Pacient> blokZaznamu;
        if (bloky == null) {
            return "Nenasiel sa zaznam";
        } else {
            if (bloky[1] == null && bloky[2] == null) {
                blokZaznamu = bloky[0];
            } else {
                blokZaznamu = bloky[2];
            }
            for (Pacient pacient1 : blokZaznamu.getPoleZaznamov()) {
                if (pacient.porovnaj(pacient1)) {
                    pacient = pacient1;
                }
            }
        }

        Hospitalizacia[] hospitalizacie = pacient.getHospitalizacie();
        if (hospitalizacie[pacient.getPocetPlatnychHospitalizacii() - 1].getDatumKonHospitalizacie().compareTo(LocalDate.parse("0001-01-01")) != 0) {
            return "Pacient nie je hospitalizovny";
        } else {
            hospitalizacie[pacient.getPocetPlatnychHospitalizacii() - 1].setDatumKonHospitalizacie(this.aktualnyDatum);
            vlozDoSuboru(bloky, indexBloku);
            return "Hospitalizacia bola ukoncena";
        }
    }

    public String editaciaPacienta(String meno, String priezvisko, LocalDate datumNar, int idPreukazu, int noveIdPreukazu) throws IOException {
        Pacient pacient = new Pacient(idPreukazu);
        Blok<Pacient>[] bloky = this.linearneHesovanie.najdiZaznam(new Pacient(idPreukazu));
        int indexBloku = this.linearneHesovanie.indexBloku(pacient);
        Blok<Pacient> blokZaznamu;
        if (bloky == null) {
            return "Nenasiel sa zaznam";
        } else {
            if (bloky[1] == null && bloky[2] == null) {
                blokZaznamu = bloky[0];
            } else {
                blokZaznamu = bloky[2];
            }
            for (Pacient pacient1 : blokZaznamu.getPoleZaznamov()) {
                if (pacient.porovnaj(pacient1)) {
                    pacient = pacient1;
                }
            }
        }

        if (meno.equals(""))
            meno = pacient.getMeno();
        if (priezvisko.equals(""))
            priezvisko = pacient.getPriezvisko();
        if (datumNar == null)
            datumNar = pacient.getDatumNarodenia();

        if (noveIdPreukazu != -1) {
            Pacient novyPacient = new Pacient(meno, priezvisko, noveIdPreukazu, datumNar);
            novyPacient.setPocetPlatnychHospitalizacii(pacient.getPocetPlatnychHospitalizacii());
            novyPacient.setHospitalizacie(pacient.getHospitalizacie());
            this.linearneHesovanie.vymazZaznam(pacient);
            this.linearneHesovanie.vloz(novyPacient);
        } else {
            pacient.setMeno(meno);
            pacient.setPriezvisko(priezvisko);
            pacient.setDatumNarodenia(datumNar);
            vlozDoSuboru(bloky, indexBloku);
        }
        return "Udaje pacienta boli zmenene";
    }

    public LinkedList<String> vypisSubor() {
        return this.linearneHesovanie.vypisObsahSuboruDoArray();
    }

    public void naplnDatabazu(int pocetUdajov) throws IOException {
        Random random = new Random(1000000L);
        Random randomHospitalizacie = new Random(1000000L);
        for (int i = 0; i < pocetUdajov; i++) {
            int rnd = random.nextInt(50000);
            int rndRok = random.nextInt(49) + 50;
            int rndMesiac = random.nextInt(12) + 1;

            int rndDen;
            if (rndMesiac == 2) {
                rndDen = random.nextInt(28) + 1;
            } else {
                if (rndMesiac < 8) {
                    if (rndMesiac % 2 == 0) {
                        rndDen = random.nextInt(30) + 1;
                    } else {
                        rndDen = random.nextInt(31) + 1;
                    }
                } else {
                    if (rndMesiac % 2 == 0) {
                        rndDen = random.nextInt(31) + 1;
                    } else {
                        rndDen = random.nextInt(30) + 1;
                    }
                }
            }
            String den = (rndDen < 10 ? "0" + rndDen : rndDen + "");
            String mesiac = (rndMesiac < 10 ? "0" + rndMesiac : rndMesiac + "");
            LocalDate datNarodenia = LocalDate.parse((rndRok + 1900) + "-" + mesiac + "-" + den);
            int idPreukazu = random.nextInt(100000) + 1;
            Pacient pacient = new Pacient("Meno" + rnd, "Priezvisko" + rnd, idPreukazu, datNarodenia);

            double pravVytvoreniaHospitalizacie = randomHospitalizacie.nextDouble();
            Hospitalizacia hospitalizacia;

            LocalDate datumHosp = pacient.getDatumNarodenia().plusYears(random.nextInt(30));
            datumHosp = datumHosp.plusMonths(random.nextInt(12));
            datumHosp = datumHosp.plusDays(random.nextInt(30));
            hospitalizacia = new Hospitalizacia(datumHosp, "diagnoza" + random.nextInt(30));
            if (pravVytvoreniaHospitalizacie < 0.1) {
            } else {
                hospitalizacia.setDatumKonHospitalizacie(datumHosp.plusDays(random.nextInt(100)));
            }

            Hospitalizacia[] hospitalizacie = pacient.getHospitalizacie().clone();
            hospitalizacie[pacient.getPocetPlatnychHospitalizacii()] = hospitalizacia;
            pacient.setHospitalizacie(hospitalizacie);
            pacient.zvysPocetPlatnychHospitalizacii();

            this.linearneHesovanie.vloz(pacient);
        }
    }
}
