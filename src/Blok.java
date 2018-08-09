import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Blok<T extends IZaznam> {
    private int pocetZaznamov;
    private T[] poleZaznamov;
    private int pocetPlatnychZaznamov;
    private long preplnAdresa;
    private T typ;

    public Blok(int pocetZaznamov, T typ) {
        this.poleZaznamov = (T[]) Array.newInstance(typ.getClass(), pocetZaznamov);
        this.pocetZaznamov = pocetZaznamov;
        for (int i = 0; i < this.pocetZaznamov; i++) {
            this.poleZaznamov[i] = (T) typ.clone();
        }
        this.preplnAdresa = -1;
        this.pocetPlatnychZaznamov = 0;
        this.typ = (T) typ.clone();
    }

    public Blok(int pocetZaznamov, T[] poleZaznamov, int pocetPlatnychZaznamov, long preplnAdresa, T typ) {
        this.pocetZaznamov = pocetZaznamov;
        this.poleZaznamov = poleZaznamov;
        this.pocetPlatnychZaznamov = pocetPlatnychZaznamov;
        this.preplnAdresa = preplnAdresa;
        this.typ = (T) typ.clone();
    }

    public Blok(Blok<T> blok) {
        this(blok.getPocetZaznamov(), blok.getPoleZaznamov().clone(), blok.getPocetPlatnychZaznamov(), blok.getPreplnAdresa(), blok.getTyp());
    }

//    public Blok(int pocetZaznamov, Class<T[]> clazz) {
//        this.pocetZaznamov = pocetZaznamov;
//        this.poleZaznamov = clazz.cast(Array.newInstance(clazz.getComponentType(), pocetZaznamov));
//        for (int i = 0; i < this.pocetZaznamov; i++) {
//            this.poleZaznamov[i] = ;
//        }
//        this.pocetPlatnych = 0;
//    }


    public int getPocetZaznamov() {
        return pocetZaznamov;
    }

    public T[] getPoleZaznamov() {
        return poleZaznamov;
    }

    public int getPocetPlatnychZaznamov() {
        return pocetPlatnychZaznamov;
    }

    public long getPreplnAdresa() {
        return preplnAdresa;
    }

    public T getTyp() {
        return typ;
    }

    public void setPreplnAdresa(long preplnAdresa) {
        this.preplnAdresa = preplnAdresa;
    }

    public void setPocetPlatnychZaznamov(int pocetPlatnychZaznamov) {
        this.pocetPlatnychZaznamov = pocetPlatnychZaznamov;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream hlpByteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream hlpOutStream = new DataOutputStream(hlpByteArrayOutputStream);

        try {
            hlpOutStream.writeInt(this.pocetPlatnychZaznamov);
            hlpOutStream.writeLong(this.preplnAdresa);
            for (T zaznam : poleZaznamov) {
                byte[] zaznamByte = zaznam.toByteArray();

                hlpOutStream.write(zaznamByte);
            }

        } catch (IOException ex) {
            Logger.getLogger(Pacient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hlpByteArrayOutputStream.toByteArray();
    }

    public void fromByteArray(byte[] poleBytov) throws IOException {
        ByteArrayInputStream hlpByteArrayInputStream = new ByteArrayInputStream(poleBytov);
        DataInputStream hlpInStream = new DataInputStream(hlpByteArrayInputStream);

//        this.hlbkaBloku = hlpInStream.readInt();
        this.pocetPlatnychZaznamov = hlpInStream.readInt();
        this.preplnAdresa = hlpInStream.readLong();
        for (int i = 0; i < this.pocetZaznamov; i++) {
            byte[] temp = new byte[typ.dajVelkost()];
            hlpInStream.readFully(temp);
            typ.fromByteArray(temp);
            this.poleZaznamov[i] = (T) typ.clone();
        }
    }

    public int dajVelkost() {
        return (4 + 8 + (pocetZaznamov * typ.dajVelkost()));
    }

    @Override
    protected Blok<T> clone() {
        return new Blok<>(this);
    }

    public void vlozZaznam(T zaznam) {
        int index = this.pocetPlatnychZaznamov;
        this.poleZaznamov[index] = zaznam;
        this.pocetPlatnychZaznamov++;
    }

    @Override
    public String toString() {
        return "Blok{" +
                "pocetZaznamov=" + pocetZaznamov +
                ", pocetPlatnychZaznamov=" + pocetPlatnychZaznamov +
                ", preplnAdresa=" + preplnAdresa +
                ", poleZaznamov=" + Arrays.toString(poleZaznamov) +
                '}';
    }
}
