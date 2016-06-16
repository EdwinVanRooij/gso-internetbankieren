package bankieren;

import observer.BasicPublisher;
import observer.IRemotePropertyListener;

import java.rmi.RemoteException;

class Rekening implements IRekeningInBank {

    private static final long serialVersionUID = 7221569686169173632L;
    private static final int KREDIETLIMIET = -10000;
    private int nr;
    private IKlant eigenaar;
    private Geld saldo;

    // Transient omdat deze niet verstuurd hoeft te worden over RMI
    private transient BasicPublisher publisher;
    /**
     * creatie van een bankrekening met saldo van 0.0<br>
     * de constructor heeft package-access omdat de PersistentAccount-objecten door een
     * het PersistentBank-object worden beheerd
     * @see banking.persistence.PersistentBank
     * @param number het bankrekeningnummer
     * @param klant de eigenaar van deze rekening
     * @param currency de munteenheid waarin het saldo is uitgedrukt
     */
    Rekening(int number, IKlant klant, String currency) {
        this.nr = number;
        this.eigenaar = klant;
        this.saldo = new Geld(0, currency);
        this.publisher = new BasicPublisher(new String[]{"saldo"});
    }

    /**
     * creatie van een bankrekening met saldo saldo<br>
     * de constructor heeft package-access omdat de PersistentAccount-objecten door een
     * het PersistentBank-object worden beheerd
     * @see banking.persistence.PersistentBank
     * @param number het bankrekeningnummer
     * @param name de naam van de eigenaar
     * @param city de woonplaats van de eigenaar
     * @param currency de munteenheid waarin het saldo is uitgedrukt
     */
    Rekening(int number, IKlant klant, Geld saldo) {
        this.nr = number;
        this.eigenaar = klant;
        this.saldo = saldo;
    }

    public boolean equals(Object obj) {
        return nr == ((IRekening) obj).getNr();
    }

    public int getNr() {
        return nr;
    }

    public String toString() {
        return nr + ": " + eigenaar.toString();
    }

    boolean isTransferPossible(Geld bedrag) {
        return (bedrag.getCents() + saldo.getCents() >= KREDIETLIMIET);
    }

    public IKlant getEigenaar() {
        return eigenaar;
    }

    public Geld getSaldo() {
        return saldo;
    }

    public boolean muteer(Geld bedrag) {
        if (bedrag.getCents() == 0) {
            throw new RuntimeException(" bedrag = 0 bij aanroep 'muteer'");
        }

        if (isTransferPossible(bedrag)) {
            Geld previousBalance = saldo;
            saldo = Geld.sum(saldo, bedrag);
            publisher.inform(this, "saldo", previousBalance, saldo);
            return true;
        }
        return false;
    }

    @Override
    public int getKredietLimietInCenten() {
        return KREDIETLIMIET;
    }

    @Override
    public void addListener(IRemotePropertyListener listener, String property) throws RemoteException {
        this.publisher.addListener(listener, property);
    }

    @Override
    public void removeListener(IRemotePropertyListener listener, String property) throws RemoteException {
        this.publisher.removeListener(listener, property);
    }
}
