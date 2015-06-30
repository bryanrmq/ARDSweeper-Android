package fr.fliizweb.ardsweeper;

import com.badlogic.gdx.Game;

import fr.fliizweb.ardsweeper.Screens.GameScreen;
import fr.fliizweb.ardsweeper.Screens.LoadingScreen;

/**
 * Created by rcdsm on 28/06/15.
 */
public class ARDSweeper extends Game {

    private GameScreen gameScreen;
    private LoadingScreen loadingScreen;

    @Override
    public void create() {
        //gameScreen = new GameScreen();
        //setScreen(gameScreen);

        loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);
    }
}
