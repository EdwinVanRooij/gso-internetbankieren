package main.bankieren;

import main.observer.IRemotePublisher;

import java.io.Serializable;

public interface IRekening extends Serializable, IRemotePublisher {
  int getNr();
  Geld getSaldo();
  IKlant getEigenaar();
  int getKredietLimietInCenten();
}

