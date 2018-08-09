import java.io.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pacient implements IZaznam {

    private String meno;
    private String priezvisko;
    private int cisloPreukazu;
    private LocalDate datumNarodenia;
    private int pocetPlatnychHospitalizacii;
    private Hospitalizacia[] hospitalizacie;

    public Pacient(String meno, String priezvisko, int cisloPreukazu, LocalDate datumNarodenia) {
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.cisloPreukazu = cisloPreukazu;
        this.datumNarodenia = datumNarodenia;
        this.hospitalizacie = new Hospitalizacia[100];
        Hospitalizacia hospitalizacia = new Hospitalizacia();
        for (int i = 0; i < 100; i++) {
            this.hospitalizacie[i] = hospitalizacia.clone();
        }
    }

    public Pacient(int cisloPreukazu) {
        this.cisloPreukazu = cisloPreukazu;
    }

    public Pacient(Pacient pacient) {
        this(pacient.getMeno(), pacient.getPriezvisko(), pacient.getCisloPreukazu(), pacient.getDatumNarodenia(), pacient.getPocetPlatnychHospitalizacii(), pacient.getHospitalizacie().clone());
    }

    public Pacient() {
        this("", "", -1, LocalDate.parse("0001-01-01"));
    }

    public Pacient(String meno, String priezvisko, int cisloPreukazu, LocalDate datumNarodenia, int pocetPlatnychHospitalizacii, Hospitalizacia[] hospitalizacie) {
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.cisloPreukazu = cisloPreukazu;
        this.datumNarodenia = datumNarodenia;
        this.pocetPlatnychHospitalizacii = pocetPlatnychHospitalizacii;
        this.hospitalizacie = hospitalizacie;
    }

    public String getMeno() {
        return meno;
    }

    public String getPriezvisko() {
        return priezvisko;
    }

    public int getCisloPreukazu() {
        return cisloPreukazu;
    }

    public LocalDate getDatumNarodenia() {
        return datumNarodenia;
    }

    public int getPocetPlatnychHospitalizacii() {
        return pocetPlatnychHospitalizacii;
    }

    public void setMeno(String meno) {
        this.meno = meno;
    }

    public void setPriezvisko(String priezvisko) {
        this.priezvisko = priezvisko;
    }

    public void setDatumNarodenia(LocalDate datumNarodenia) {
        this.datumNarodenia = datumNarodenia;
    }

    public Hospitalizacia[] getHospitalizacie() {
        return hospitalizacie;
    }

    public void setPocetPlatnychHospitalizacii(int pocetPlatnychHospitalizacii) {
        this.pocetPlatnychHospitalizacii = pocetPlatnychHospitalizacii;
    }

    public void setHospitalizacie(Hospitalizacia[] hospitalizacie) {
        this.hospitalizacie = hospitalizacie;
    }

    @Override
    public int dajVelkost() {
        return ((25 * 2) + (25 * 2) + 4 + 8 + 4 + (100 * this.hospitalizacie[0].dajVelkost()));
    }

//    @Override
//    public int dajVelkost() {
//        return ((25 * 2) + (25 * 2) + 4 + 8);
//    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        int prazdneByty;

        try {
            prazdneByty = (25 - this.meno.length()) * 2;
            for (int i = 0; i < prazdneByty; i++) {
                hlpOutStream.writeByte(0);
            }
            hlpOutStream.writeChars(this.meno.trim());

            prazdneByty = (25 - this.priezvisko.length()) * 2;
            for (int i = 0; i < prazdneByty; i++) {
                hlpOutStream.writeByte(0);
            }
            hlpOutStream.writeChars(this.priezvisko.trim());
            hlpOutStream.writeInt(this.cisloPreukazu);
            hlpOutStream.writeLong(this.datumNarodenia.toEpochDay());
            hlpOutStream.writeInt(this.pocetPlatnychHospitalizacii);
            for (Hospitalizacia hospitalizacia : this.hospitalizacie) {
                byte[] hospByte = hospitalizacia.toByteArray();

                hlpOutStream.write(hospByte);
            }

        } catch (IOException ex) {
            Logger.getLogger(Pacient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hlpByteArrayOutputStream.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] poleBytov) {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(poleBytov);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);

        try {
            StringBuilder builder = new StringBuilder(25);
            for (int i = 0; i < 25; i++) {
                builder.append(hlpInStream.readChar());
            }
            String pom = builder.toString();
            this.meno = pom.trim();
            builder = new StringBuilder(25);

            for (int i = 0; i < 25; i++) {
                builder.append(hlpInStream.readChar());
            }
            String pom2 = builder.toString();
            this.priezvisko = pom2.trim();
            this.cisloPreukazu = hlpInStream.readInt();
            this.datumNarodenia = LocalDate.ofEpochDay(hlpInStream.readLong());
            this.pocetPlatnychHospitalizacii = hlpInStream.readInt();
            for (int i = 0; i < 100; i++) {
                Hospitalizacia pomHosp = new Hospitalizacia();
                byte[] temp = new byte[pomHosp.dajVelkost()];
                hlpInStream.readFully(temp);
                pomHosp.fromByteArray(temp);
                this.hospitalizacie[i] = pomHosp.clone();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public Pacient clone() {
        return new Pacient(this);
    }

    @Override
    public int dajHash() {
        return this.cisloPreukazu;
    }

    @Override
    public boolean porovnaj(IZaznam zaznam) {
        Pacient pacient = (Pacient) zaznam;
        if (this.cisloPreukazu == pacient.getCisloPreukazu()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "\nPacient{" +
                "meno='" + this.meno + '\'' +
                ", priezvisko='" + this.priezvisko + '\'' +
                ", cisloPreukazu=" + this.cisloPreukazu + '\'' +
                ", datumNarodenia=" + this.datumNarodenia + '\'' +
                ", pocetPlatnychHosp=" + this.pocetPlatnychHospitalizacii + '\'' +
                platneHospitalizacie()+
                '}';
    }

    public void zvysPocetPlatnychHospitalizacii(){
        this.pocetPlatnychHospitalizacii++;
    }

    private String platneHospitalizacie(){
        String string = "\nHospitalizacie :";
        for (int i = 0; i < this.pocetPlatnychHospitalizacii; i++) {
            string = string + "\n" + this.hospitalizacie[i].toString();
        }
        return string;
    }
}
