import java.io.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hospitalizacia implements IZaznam {

    private LocalDate datumZacHospitalizacie;
    private LocalDate datumKonHospitalizacie;
    private String diagnoza;

    public Hospitalizacia(LocalDate datumZacHospitalizacie, String diagnoza) {
        this.datumZacHospitalizacie = datumZacHospitalizacie;
        this.diagnoza = diagnoza;
        this.datumKonHospitalizacie = LocalDate.parse("0001-01-01");
    }

    public Hospitalizacia(LocalDate datumZacHospitalizacie, LocalDate datumKonHospitalizacie, String diagnoza) {
        this.datumZacHospitalizacie = datumZacHospitalizacie;
        this.datumKonHospitalizacie = datumKonHospitalizacie;
        this.diagnoza = diagnoza;
    }

    public Hospitalizacia(Hospitalizacia hospitalizacia) {
        this(hospitalizacia.getDatumZacHospitalizacie(), hospitalizacia.getDatumKonHospitalizacie(), hospitalizacia.getDiagnoza());
    }

    public Hospitalizacia() {
        this(LocalDate.parse("0001-01-01"), "");
    }

    public LocalDate getDatumZacHospitalizacie() {
        return datumZacHospitalizacie;
    }

    public LocalDate getDatumKonHospitalizacie() {
        return datumKonHospitalizacie;
    }

    public String getDiagnoza() {
        return diagnoza;
    }

    public void setDatumKonHospitalizacie(LocalDate datumKonHospitalizacie) {
        this.datumKonHospitalizacie = datumKonHospitalizacie;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);
        int prazdneByty;

        try {
            hlpOutStream.writeLong(this.datumZacHospitalizacie.toEpochDay());
            hlpOutStream.writeLong(this.datumKonHospitalizacie.toEpochDay());
            prazdneByty = (40 - this.diagnoza.length()) * 2;
            for (int i = 0; i < prazdneByty; i++) {
                hlpOutStream.writeByte(0);
            }
            hlpOutStream.writeChars(this.diagnoza.trim());

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
            this.datumZacHospitalizacie = LocalDate.ofEpochDay(hlpInStream.readLong());
            this.datumKonHospitalizacie = LocalDate.ofEpochDay(hlpInStream.readLong());
            StringBuilder builder = new StringBuilder(40);
            for (int i = 0; i < 40; i++) {
                builder.append(hlpInStream.readChar());
            }
            String pom = builder.toString();
            this.diagnoza = pom.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Error during conversion from byte array.");
        }
    }

    @Override
    public int dajVelkost() {
        return ( 8 + 8 + (40 * 2));
    }

    @Override
    public Hospitalizacia clone() {
        return new Hospitalizacia(this);
    }

    @Override
    public int dajHash() {
        return 0;
    }

    @Override
    public boolean porovnaj(IZaznam zaznam) {
        return false;
    }

    @Override
    public String toString() {
        return "Hospitalizacia{" +
                "datumZacHospitalizacie=" + datumZacHospitalizacie +
                ", datumKonHospitalizacie=" + datumKonHospitalizacie +
                ", diagnoza='" + diagnoza + '\'' +
                '}';
    }
}
