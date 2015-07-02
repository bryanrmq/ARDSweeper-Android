package fr.fliizweb.ardsweeper.Screens;

/**
 * Created by rcdsm on 01/07/15.
 */

// Une interface pour se connecter à partir du package "Core" à la class BluetoothArduino se trouvant dans le package "Android"
public interface GameBluetooth {

    void send(String message);
    String receive();
    Boolean getConnected();
    //  public void timer();

}
