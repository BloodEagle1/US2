import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;

public class GUI {

    private JPanel panel;
    private JTextField idPreukazuVyhladanieTF;
    private JButton vyhladajPacientaButton;
    private JTextArea vypisPacientaTA;
    private JTextField menoPridanieTF;
    private JTextField priezviskoPridanieTF;
    private JTextField idPreukazuPridanieTF;
    private JTextField datumNarPridanieTF;
    private JButton pridajPacientaButton;
    private JTextField idPreukazuVymazanieTF;
    private JButton vymazPacientaButton;
    private JTextField idPreukazuHospitalizaciaTF;
    private JTextField diagnozaHospitalizaciaTF;
    private JButton hospitalizujButton;
    private JTabbedPane tabbedPane1;
    private JTextField pocetZaznamovTF;
    private JTextField pocetZaznamovPrepTF;
    private JTextField pocetSkupinTF;
    private JTextField maxHustotaTF;
    private JTextField minHustotaTF;
    private JButton vytvorButton;
    private JButton nacitajButton;
    private JButton ulozButton;
    private JTextField idPreukazuUkonciHospTF;
    private JButton ukonciHospitalizaciuButton;
    private JTextField datumAplikacieTF;
    private JButton nastavDatumButton;
    private JTextField menoEditaciaPacientaTF;
    private JTextField priezviskoEditaciaPacientaTF;
    private JTextField datumNarEditaciaPacientaTF;
    private JTextField idPreukazuEditaciaPacientaTF;
    private JButton upravPacientaButton;
    private JTextField noveIdPreukazuEditaciaPacientaTF;
    private JTextArea vypisSuboru;
    private JButton vypisButton;
    private JButton naplnButton;
    private JTextField pocetUdajovNaplnTF;
    private Aplikacia aplikacia;

    public GUI() throws IOException {
        this.aplikacia = new Aplikacia();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error setting native LAF: " + e);
        }

        vyhladajPacientaButton.addActionListener(e -> {
            if (!idPreukazuVyhladanieTF.getText().equals("")) {
                int idPreukazu = Integer.parseInt(idPreukazuVyhladanieTF.getText());
                try {
                    String vysledokVyhladania = this.aplikacia.vyhladajPacienta(idPreukazu);
                    this.vypisPacientaTA.setText(vysledokVyhladania);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });

        pridajPacientaButton.addActionListener(e -> {
            if (!menoPridanieTF.getText().equals("") && !priezviskoPridanieTF.getText().equals("") && !idPreukazuPridanieTF.getText().equals("") && !datumNarPridanieTF.getText().equals("")) {
                String meno = menoPridanieTF.getText();
                String priezvisko = priezviskoPridanieTF.getText();
                int idPreukazu = Integer.parseInt(idPreukazuPridanieTF.getText());
                LocalDate datumNarodenia = LocalDate.parse(datumNarPridanieTF.getText());
                try {
                    String vysledokPridania = this.aplikacia.pridajPacienta(meno, priezvisko, idPreukazu, datumNarodenia);
                    JOptionPane.showMessageDialog(panel, vysledokPridania);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });

        vymazPacientaButton.addActionListener(e -> {
            if (!idPreukazuVymazanieTF.getText().equals("")) {
                int idPreukazu = Integer.parseInt(idPreukazuVymazanieTF.getText());
                try {
                    String vysledokVymazania = this.aplikacia.vymazPacienta(idPreukazu);
                    JOptionPane.showMessageDialog(panel, vysledokVymazania);
                    this.vypisPacientaTA.setText("");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });

        hospitalizujButton.addActionListener(e -> {
            if (!idPreukazuHospitalizaciaTF.getText().equals("") && !diagnozaHospitalizaciaTF.getText().equals("")) {
                int idPreukazu = Integer.parseInt(idPreukazuHospitalizaciaTF.getText());
                String diagnoza = diagnozaHospitalizaciaTF.getText();
                try {
                    String vysledokHospitalizacie = this.aplikacia.hospitalizujPacienta(idPreukazu, diagnoza);
                    JOptionPane.showMessageDialog(panel, vysledokHospitalizacie);
                    this.vypisPacientaTA.setText(this.aplikacia.vyhladajPacienta(idPreukazu));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });
        vytvorButton.addActionListener(e -> {
            if (!pocetZaznamovTF.getText().equals("") && !pocetZaznamovPrepTF.getText().equals("") && !pocetSkupinTF.getText().equals("") && !maxHustotaTF.getText().equals("") && !minHustotaTF.getText().equals("")) {
                int pocetZaznamov = Integer.parseInt(pocetZaznamovTF.getText());
                int pocetZaznamovPrep = Integer.parseInt(pocetZaznamovPrepTF.getText());
                int pocetSkupin = Integer.parseInt(pocetSkupinTF.getText());
                double maxHustota = Double.parseDouble(maxHustotaTF.getText());
                double minHustota = Double.parseDouble(minHustotaTF.getText());
                try {
                    String vysledokVytvorenia = this.aplikacia.vytvorLinearneHesovanie(pocetZaznamov, pocetZaznamovPrep, pocetSkupin, maxHustota, minHustota);
                    JOptionPane.showMessageDialog(panel, vysledokVytvorenia);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });
        nacitajButton.addActionListener(e -> {
            String vysledokNacitania = null;
            try {
                vysledokNacitania = this.aplikacia.nacitajLinearneHesovanie();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(panel, vysledokNacitania);
        });
        ulozButton.addActionListener(e -> {
            try {
                this.aplikacia.ulozLinearneHesovanie();
                JOptionPane.showMessageDialog(panel, "Linearne hesovanie bolo ulozene");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        nastavDatumButton.addActionListener(e -> {
            if (!datumAplikacieTF.getText().equals("")) {
                LocalDate datumAplikacie = LocalDate.parse(datumAplikacieTF.getText());
                this.aplikacia.setAktualnyDatum(datumAplikacie);
                JOptionPane.showMessageDialog(panel, "Datum bol nastaveny na " + datumAplikacie);
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });
        ukonciHospitalizaciuButton.addActionListener(e -> {
            if (!idPreukazuUkonciHospTF.getText().equals("")) {
                int idPreukazu = Integer.parseInt(idPreukazuUkonciHospTF.getText());
                try {
                    String vysledokUkonceniaHosp = this.aplikacia.ukonciHospitalizaciuPacientovi(idPreukazu);
                    JOptionPane.showMessageDialog(panel, vysledokUkonceniaHosp);
                    this.vypisPacientaTA.setText(this.aplikacia.vyhladajPacienta(idPreukazu));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });
        upravPacientaButton.addActionListener(e -> {
            if (!idPreukazuEditaciaPacientaTF.getText().equals("")) {
                String meno = menoEditaciaPacientaTF.getText();
                String priezvisko = priezviskoEditaciaPacientaTF.getText();
                LocalDate datumNar;
                if (!datumNarEditaciaPacientaTF.getText().equals("")) {
                    datumNar = LocalDate.parse(datumNarEditaciaPacientaTF.getText());
                } else {
                    datumNar = null;
                }
                int noveIdPreukazu;
                if (!noveIdPreukazuEditaciaPacientaTF.getText().equals("")) {
                    noveIdPreukazu = Integer.parseInt(noveIdPreukazuEditaciaPacientaTF.getText());
                } else {
                    noveIdPreukazu = -1;
                }
                int idPreukazu = Integer.parseInt(idPreukazuEditaciaPacientaTF.getText());
                try {
                    String vysledokEditacie = this.aplikacia.editaciaPacienta(meno, priezvisko, datumNar, idPreukazu, noveIdPreukazu);
                    JOptionPane.showMessageDialog(panel, vysledokEditacie);
                    if (noveIdPreukazu != -1) {
                        this.vypisPacientaTA.setText(this.aplikacia.vyhladajPacienta(noveIdPreukazu));
                    } else {
                        this.vypisPacientaTA.setText(this.aplikacia.vyhladajPacienta(idPreukazu));
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });
        vypisButton.addActionListener(e -> {
            vypisSuboru.setText("");
            for (String item : this.aplikacia.vypisSubor()) {
                vypisSuboru.append(item);
            }
        });
        naplnButton.addActionListener(e -> {
            if (!pocetUdajovNaplnTF.getText().equals("")) {
                int pocetUdajov = Integer.parseInt(pocetUdajovNaplnTF.getText());
                try {
                    this.aplikacia.naplnDatabazu(pocetUdajov);
                    JOptionPane.showMessageDialog(panel, "Databaza bola naplnena");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Nespravne parametre");
            }
        });
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
