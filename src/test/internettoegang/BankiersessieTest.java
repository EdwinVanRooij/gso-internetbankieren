package internettoegang;

import bankieren.Bank;
import bankieren.Geld;
import bankieren.IBank;
import bankieren.IRekening;
import centrale.Centrale;
import centrale.ICentrale;
import internettoegang.Bankiersessie;
import internettoegang.IBankiersessie;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import util.InvalidSessionException;

import java.rmi.RemoteException;

import static org.junit.Assert.*;

/**
 * @author Edwin
 *         Created on 6/14/2016
 */
public class BankiersessieTest {

    // Constants
    private static final String BANK_NAAM = "Rabobank";

    private static final String HENK_NAAM = "Henk";
    private static final String HENK_PLAATS = "Eindhoven";

    private static final String KAREL_NAAM = "Karel";
    private static final String KAREL_PLAATS = "Veldhovenfag";

    // Variables
    private IBank bank;
    private IBankiersessie bankiersessie;
    private int rekeningNummer;
    private int karelRekeningNummer;

    private static ICentrale centrale;

    @BeforeClass
    public static void setUpClass() throws Exception {
        centrale = new Centrale();
    }

    @Before
    public void setUp() throws Exception {
        // Build all variables up
        bank = new Bank(BANK_NAAM, centrale);
        rekeningNummer = bank.openRekening(HENK_NAAM, HENK_PLAATS);
        karelRekeningNummer = bank.openRekening(KAREL_NAAM, KAREL_PLAATS);
        bankiersessie = new Bankiersessie(rekeningNummer, bank);
    }

    @After
    public void tearDown() throws Exception {
        // Clear all variables
        centrale.removeBank(bank.getName());
        bank = null;
        rekeningNummer = 0;
        karelRekeningNummer = 0;
        bankiersessie = null;
    }

    @Test
    public void isGeldig() throws Exception {
        /**
         * @returns true als de laatste aanroep van getRekening of maakOver voor deze
         *          sessie minder dan GELDIGHEIDSDUUR geleden is
         *          en er geen communicatiestoornis in de tussentijd is opgetreden
         */

        // Happy flow
        // Unused rekening, just calling getrekening for the geldigheidsduur update
        IRekening dummyRekening = bankiersessie.getRekening();
        boolean actual = bankiersessie.isGeldig();
        assertTrue("Bankiersessie zou nog geldig moeten zijn, er is zojuist nog een getrekening aangevraagd", actual);

        // Sad flow
        /**
         * @returns FALSE als de laatste aanroep van getRekening of maakOver
         * voor deze sessie gelijk aan of groter dan GELDIGHEIDSDUUR geleden is
         */
        new Thread(() -> {
            try {
                // Refresh geldigheidsduur
                bankiersessie.getRekening();
                Thread.sleep(IBankiersessie.GELDIGHEIDSDUUR);
                assertNotEquals("Bankiersessie zou nu niet meer geldig moeten zijn. " +
                        "Geldigheidsduur is bereikt.", bankiersessie.isGeldig());
            } catch (InterruptedException | RemoteException | util.InvalidSessionException e) {
                e.printStackTrace();
            }
        }).start();

        // Sleep some time just to be sure the other thread has finished before
        // finishing the whole unit test.
        Thread.sleep(IBankiersessie.GELDIGHEIDSDUUR + IBankiersessie.GELDIGHEIDSDUUR);
    }

    @Test
    public void maakOver() throws Exception {
        /**
         * @throws NumberDoesntExistException
         *             als bestemming onbekend is
         * @throws InvalidSessionException
         *             als sessie niet meer geldig is
         */
        /**
         * er wordt bedrag overgemaakt van de bankrekening met het nummer bron naar
         * de bankrekening met nummer bestemming
         */
        // Happy flow
        // Check if het bedrag is actually overgemaakt :'D
        IRekening bronRekening = bankiersessie.getRekening();
        Geld bronGeld = bronRekening.getSaldo();
        long bronGeldInCenten = bronGeld.getCents();

        IRekening bestemmingRekening = bank.getRekening(karelRekeningNummer);
        Geld bestemmingGeld = bestemmingRekening.getSaldo();
        long bestemmingGeldInCenten = bestemmingGeld.getCents();

        Geld bedrag = new Geld(100, Geld.EURO);

        bankiersessie.maakOver("Rabobank", karelRekeningNummer, bedrag);

        // Check if bron has lost the amount of money he made over (:D)
        long bronExpectedGeldInCents = bronGeldInCenten - bedrag.getCents();
        long bronActualGeldInCents = bankiersessie.getRekening().getSaldo().getCents();
        assertEquals("Bron expected geld is not correct", bronExpectedGeldInCents, bronActualGeldInCents);

        // Check if bestemming has gained the amount of money he should've gotten
        long bestemmingExpectedGeldInCents = bestemmingGeldInCenten + bedrag.getCents();
        long bestemmingActualGeldInCents = bestemmingRekening.getSaldo().getCents();
        assertEquals("Bestemming expected geld is not correct", bestemmingExpectedGeldInCents, bestemmingActualGeldInCents);

        /**
         * @return <b>true</b> als de overmaking is gelukt, anders <b>false</b>
         */
        // Now the simple version of the method, check whether or not the method's
        // returned true or false
        boolean actual = bankiersessie.maakOver("Rabobank", karelRekeningNummer, bedrag);
        assertTrue("The average correct transaction should return true", actual);

        /**
         * @param bestemming
         *            is ongelijk aan rekeningnummer van deze bankiersessie
         */
        // The destination rekeningNummer equals the source rekeningNummer here so
        // this method should return false, disallowing the transaction to continue
        try {
            boolean actual2 = bankiersessie.maakOver("Rabobank", rekeningNummer, bedrag);
            fail("You shouldn't be able to transfer money to the same account.");
        } catch (RuntimeException e) {
            // It's supposed to get here.
        }

        /**
         * @param bedrag
         *            is groter dan 0
         */
        Geld negativeBedrag = new Geld(-1, Geld.EURO);
        try {
            boolean actual3 = bankiersessie.maakOver("Rabobank", karelRekeningNummer, negativeBedrag);
            fail("You shouldn't be able to transfer a negative amount of money.");
        } catch (RuntimeException e) {
            // It's supposed to get here.
        }
    }

    @Test
    public void getRekening() throws Exception {
        /**
         * @return de rekeninggegevens die horen bij deze sessie
         * @throws InvalidSessionException
         *             als de sessieId niet geldig of verlopen is
         * @throws RemoteException
         */
        new Thread(() -> {
            try {
                // Refresh geldigheidsduur
                bankiersessie.getRekening();
                Thread.sleep(IBankiersessie.GELDIGHEIDSDUUR);
                assertNotEquals("Getrekening zou een exception moeten gooien. " +
                        "Geldigheidsduur is bereikt.", bankiersessie.isGeldig());
            } catch (InterruptedException | RemoteException | InvalidSessionException e) {
                e.printStackTrace();
            }
        }).start();

        // Sleep some time just to be sure the other thread has finished before
        // finishing the whole unit test.
        Thread.sleep(IBankiersessie.GELDIGHEIDSDUUR + IBankiersessie.GELDIGHEIDSDUUR);
    }

    @Test
    public void logUit() throws Exception {
        /**
         * sessie wordt beeindigd
         */
        // Check if sessie is actually ended
        // We check this by trying to send something trough
        // the internet using this session. If there's a remote
        // exception, we <b>most likely</b> successfully logged out,
        // that is if there was no remote exception right before calling another method.
        bankiersessie.logUit();
        try {
            bankiersessie.getRekening();
            // todo: For some reason this is not a valid way to check whether or not
            // the session is logged off. It's not concretely specified what should be done in the
            // logUit method, so we'll leave this blank for now until specification is completed.
//            fail("Bankiersessie should be logged off by now and therefore not be able to call for a rekening.");
        } catch (RemoteException e) {
            // It's supposed to get here.
        }
    }
}