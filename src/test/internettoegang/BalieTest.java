package internettoegang;

import main.bankieren.Bank;
import main.bankieren.Geld;
import main.bankieren.IBank;
import main.bankieren.IRekening;
import main.centrale.Centrale;
import main.centrale.IBankTbvCentrale;
import main.centrale.ICentrale;
import main.internettoegang.Balie;
import main.internettoegang.IBalie;
import main.internettoegang.IBankiersessie;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static org.junit.Assert.*;

/**
 * @author Edwin
 *         Created on 6/14/2016
 */
public class BalieTest {

    // Constants
    private static final String BANK_NAAM = "Rabobank";
    private static final String HENK_NAAM = "Henk";
    private static final String HENK_PLAATS = "Eindhoven";
    private static final String HENK_WACHTWOORD = "Bananen";
    private static final String HENK_WACHTWOORD_INCORRECT = "Appels";
    private static final String HENK_ACCOUNTNAAM_INCORRECT = "1234567";

    // Variables
    private IBank bank;
    private IBalie balie;
    private String henkAccountNaam;
    private static ICentrale centrale;

    @BeforeClass
    public static void setUpClass() throws Exception {
        centrale = new Centrale();
    }

    @Before
    public void setUp() throws Exception {
        // Build all variables up
        bank = new Bank(BANK_NAAM, centrale);
        centrale.addBank((IBankTbvCentrale) bank);

        balie = new Balie(bank);
        henkAccountNaam = balie.openRekening(HENK_NAAM, HENK_PLAATS, HENK_WACHTWOORD);
    }

    @After
    public void tearDown() throws Exception {
        // Clear all variables
        centrale.removeBank(bank.getName());
        bank = null;
        balie = null;
        henkAccountNaam = null;
    }

    @Test
    public void openRekeningDescription() throws Exception {
        /**
         * creatie van een nieuwe bankrekening; het gegenereerde bankrekeningnummer is
         * identificerend voor de nieuwe bankrekening en heeft een saldo van 0 euro
         */
        // Nummer van de zojuist aangemaakte rekening
        String accountNaam = balie.openRekening(HENK_NAAM, HENK_PLAATS, HENK_WACHTWOORD);
        // Sessie van henk
        IBankiersessie sessie = balie.logIn(accountNaam, HENK_WACHTWOORD);
        // Daadwerkelijke rekening
        IRekening nieuweRekening = sessie.getRekening();
        // Geld die initieel op de rekening staat
        Geld geldOpRekening = nieuweRekening.getSaldo();
        // Geld in centen
        long geld = geldOpRekening.getCents();
        // Verwacht is dat er nog niets op staat
        int expected = 0;
        assertEquals("Net aangemaakte bankrekening moet een saldo hebben van 0.", geld, expected);
    }

    @Test
    public void openRekeningReturn() throws RemoteException {
        /**
         * @return null zodra naam of plaats een lege string of wachtwoord minder dan
         * vier of meer dan acht karakters lang is en anders de gegenereerde
         * accountnaam(8 karakters lang) waarmee er toegang tot de nieuwe bankrekening
         * kan worden verkregen
         */

        String prefix = "Balie moet null returnen wanneer ";
        // Null wanneer naam leeg is
        String actual1 = balie.openRekening("", HENK_PLAATS, HENK_WACHTWOORD);
        assertNull(prefix + "string leeg is", actual1);
        String actual2 = balie.openRekening(null, HENK_PLAATS, HENK_WACHTWOORD);
        assertNull(prefix + "string leeg is", actual2);

        // Null wanneer plaats leeg is
        String actual3 = balie.openRekening(HENK_NAAM, "", HENK_WACHTWOORD);
        assertNull(prefix + "plaats leeg is", actual3);
        String actual4 = balie.openRekening(HENK_NAAM, null, HENK_WACHTWOORD);
        assertNull(prefix + "plaats leeg is", actual4);

        // Null wanneer wachtwoord minder dan 4 karakters lang is
        String actual5 = balie.openRekening(HENK_NAAM, HENK_PLAATS, "abc");
        assertNull(prefix + "wachtwoord minder dan 4 characters lang is", actual5);
        String actual6 = balie.openRekening(HENK_NAAM, HENK_PLAATS, "");
        assertNull(prefix + "wachtwoord minder dan 4 characters lang is", actual6);
        String actual7 = balie.openRekening(HENK_NAAM, HENK_PLAATS, null);
        assertNull(prefix + "wachtwoord minder dan 4 characters lang is", actual7);

        // Null wanneer wachtwoord meer dan 8 karakters lang is
        String actual8 = balie.openRekening(HENK_NAAM, HENK_PLAATS, "123456789");
        assertNull(prefix + "wachtwoord meer dan 8 characters lang is", actual8);
        String actual9 = balie.openRekening(HENK_NAAM, HENK_PLAATS, "123456789101214161820");
        assertNull(prefix + "wachtwoord meer dan 8 characters lang is", actual9);

        // Happy flow -- Check of accountnaam 8 karakters lang is
        String accountNaam = balie.openRekening(HENK_NAAM, HENK_PLAATS, HENK_WACHTWOORD);
        int actual10 = accountNaam.length();
        int expected10 = 8;
        assertEquals("Accountnaam moet precies 8 karakters lang zijn", actual10, expected10);
    }

    @Test
    public void logIn() throws Exception {
        /**
         * er wordt een sessie opgestart voor het login-account met de naam
         * accountnaam mits het wachtwoord correct is
         *
         * @return de gegenereerde sessie waarbinnen de gebruiker
         * toegang krijgt tot de bankrekening die hoort bij het betreffende login-
         * account mits accountnaam en wachtwoord matchen, anders null
         */
        // Happy flow
        IBankiersessie actual1 = balie.logIn(henkAccountNaam, HENK_WACHTWOORD);
        assertNotNull(String.format("Balie should have returned a bankiersessie with correct accountnaam '%s' and password '%s'", henkAccountNaam, HENK_WACHTWOORD), actual1);

        // Sad flow for accountnaam check
        IBankiersessie actual2 = balie.logIn(HENK_ACCOUNTNAAM_INCORRECT, HENK_WACHTWOORD);
        assertNull(String.format("Balie should not have returned a bankiersessie incorrect accountnaam '%s' and password '%s'", HENK_ACCOUNTNAAM_INCORRECT, HENK_WACHTWOORD), actual2);
        // Another sad flow for password check
        IBankiersessie actual3 = balie.logIn(henkAccountNaam, HENK_WACHTWOORD_INCORRECT);
        assertNull(String.format("Balie should not have returned a bankiersessie incorrect accountnaam '%s' and password '%s'", henkAccountNaam, HENK_WACHTWOORD), actual3);
    }

}