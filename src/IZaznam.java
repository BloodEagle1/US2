public interface IZaznam {
    byte[] toByteArray();
    void fromByteArray(byte[] poleBytov);
    int dajVelkost();
    IZaznam clone();
    int dajHash();
    boolean porovnaj(IZaznam zaznam);
}
