package fr.fliizweb.ardsweeper;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import fr.fliizweb.ardsweeper.Class.ARDSweeperSocket;
import fr.fliizweb.ardsweeper.Class.Tools;

public class ARDSweeper extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

    private String token;
    private String username;


	@Override
	public void create () {
        Preferences prefs = Gdx.app.getPreferences(Tools.PACKAGE_ROOT);
        token = prefs.getString(Tools.PACKAGE_ROOT + ".token", "token");
        username = prefs.getString(Tools.PACKAGE_ROOT + ".username", "username");


        Gdx.app.log("ARDSwepperLog", "username = " + username);

		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		ARDSweeperSocket socket = new ARDSweeperSocket();
		socket.connect(token, username);

        //Gdx.app.log("ARDSwepperLog", "map = " + socket.getMap());
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
}
