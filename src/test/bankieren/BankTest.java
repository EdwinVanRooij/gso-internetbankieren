package bankieren;

import bankieren.Bank;
import bankieren.Geld;
import bankieren.IBank;
import bankieren.Klant;
import centrale.Centrale;
import centrale.IBankTbvCentrale;
import centrale.ICentrale;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import util.NumberDoesntExistException;

import static org.junit.Assert.*;

/**
 * Created by Dennis on 14/06/16
 */
public class BankTest {
    private static final String HENK_NAAM = "Henk";
    private static final String HENK_PLAATS = "Eindhoven";

    private static final String BANK_NAAM = "Rabobank";

    public static final String PIET_NAAM = "Piet";
    public static final String PIET_PLAATS = "Veldhoven";

    private Klant henk;
    private IBank bank;
    private static ICentrale centrale;
    //test

    @BeforeClass
    public static void setUpClass() throws Exception {
        centrale = new Centrale();
    }

    @Before
    public void setUp() throws Exception {
        henk = new Klant(HENK_NAAM, HENK_PLAATS);
        bank = new Bank(BANK_NAAM, centrale);
        centrale.addBank((IBankTbvCentrale) bank);
    }

    @After
    public void tearDown() throws Exception {
        henk = null;
        centrale.removeBank(bank.getName());
        bank = null;
    }

    @Test
    public void testOpenRekeningHappyFlow() throws Exception {
        //creatie van een nieuwe bankrekening met een identificerend rekeningnummer;
        //alleen als de klant, geidentificeerd door naam en plaats, nog niet bestaat
        //wordt er ook een nieuwe klant aangemaakt

        int henkRekening = 0;
        int pietRekening = 0;
        henkRekening = bank.openRekening(HENK_NAAM, HENK_PLAATS);
        pietRekening = bank.openRekening(PIET_NAAM, PIET_PLAATS);

        assertNotEquals("henkRekening not created correctly", henkRekening, 0); //in case method did nothing
        assertNotEquals("henkRekening not created correctly", henkRekening, -1); //in case method went wrong
        assertNotEquals("pietRekening not created correctly", pietRekening, 0);
        assertNotEquals("pietRekening not created correctly", pietRekening, -1);
    }

    @Test
    public void testOpenRekeningEmptyString() throws Exception {
        //@return -1 zodra naam of plaats een lege string en anders het nummer van de
        //        gecreeerde bankrekening

        int emptyNaam = 0;
        int emptyPlaats = 0;
        int emptyEverything = 0;

        emptyNaam = bank.openRekening("", "test");
        emptyPlaats = bank.openRekening("test", "");
        emptyEverything = bank.openRekening("", "");

        assertNotEquals("emptyNaam not caught", emptyNaam, 0); //in case method did nothing
        assertEquals("emptyNaam not caught", emptyNaam, -1); // method should return -1
        assertNotEquals("emptyPlaats not caught", emptyPlaats, 0);
        assertEquals("emptyPlaats not caught", emptyPlaats, -1);
        assertNotEquals("emptyEverything not caught", emptyEverything, 0);
        assertEquals("emptyEverything not caught", emptyEverything, -1);
    }

    @Test
    public void testMaakOver() throws Exception {
        /**
         * er wordt bedrag overgemaakt van de bankrekening met nummer bron naar de
         * bankrekening met nummer bestemming, mits het afschrijven van het bedrag
         * van de rekening met nr bron niet lager wordt dan de kredietlimiet van deze
         * rekening
         *
         * @param bron
         * @param bestemming
         *            ongelijk aan bron
         * @param bedrag
         *            is groter dan 0
         * @return <b>true</b> als de overmaking is gelukt, anders <b>false</b>
         * @throws NumberDoesntExistException
         *             als een van de twee bankrekeningnummers onbekend is
         */
        Geld bedrag = new Geld(9999, Geld.EURO);

        //Happy Flow
        int bronHappy = bank.openRekening("1", "1");
        int bestemmingHappy = bank.openRekening("2", "2");
        assertTrue("maakOver happy flow did not succeed", bank.maakOver(bank.getName(), bronHappy, bestemmingHappy, bedrag));

        //Exception
        try {
            bank.maakOver("Rabobank", 9, 10, bedrag);
            fail("exception should have been thrown");
        } catch (NumberDoesntExistException e) {
            //this is supposed to happen
        }

        //Destination and Source identical
        int account = bank.openRekening("3", "3");
        try {
            bank.maakOver("Rabobank", account, account, bedrag);
            fail("exception should have been thrown");
        } catch (RuntimeException e) {
            //this is supposed to happen
        }

        //Too Much
        int bronTooMuch = bank.openRekening("4", "4");
        int bestemmingTooMuch = bank.openRekening("5", "5");
        bedrag = new Geld(10001, Geld.EURO);
        assertFalse("maakOver too much succeeded incorrectly", bank.maakOver(bank.getName(), bronTooMuch, bestemmingTooMuch, bedrag));

        //Number must be higher than 0
        int bronNegatiefBedrag = bank.openRekening("4", "4");
        int bestemmingNegatiefBedrag = bank.openRekening("5", "5");

        bedrag = new Geld(0, Geld.EURO);
        try {
            bank.maakOver("Rabobank", bronNegatiefBedrag, bestemmingNegatiefBedrag, bedrag);
            fail("exception should have been thrown");
        } catch (RuntimeException e) {
            //this is supposed to happen
        }
        bedrag = new Geld(-1, Geld.EURO);
        try {
            bank.maakOver("Rabobank", bronNegatiefBedrag, bestemmingNegatiefBedrag, bedrag);
            fail("exception should have been thrown");
        } catch (RuntimeException e) {
            //this is supposed to happen
        }

        //Bestemming moet ongelijk zijn aan bron
        int bron = bank.openRekening("7", "7");
        bedrag = new Geld(10, Geld.EURO);
        try {
            bank.maakOver("Rabobank", bron, bron, bedrag);
            fail("exception should have been thrown");
        } catch (RuntimeException e) {
            //this is supposed to happen
        }
    }

    @Test
    public void testGetRekening() throws Exception {
        /**
         * @param nr
         * @return de bankrekening met nummer nr mits bij deze bank bekend, anders null
         */

        //creating an account and getting it
        int rekeningNummer = bank.openRekening(HENK_NAAM, HENK_PLAATS);
        assertNotNull("Bank should have returned a valid rekening", bank.getRekening(rekeningNummer));

        //trying to get a non-existent account
        assertNull("Bank should not have returned a valid rekening", bank.getRekening(100994));
    }

    @Test
    public void testGetName() throws Exception {
        /**
         * @return de naam van deze bank
         */
        assertEquals("Bank naam was not gelijk while it should be", BANK_NAAM, bank.getName());
    }
}