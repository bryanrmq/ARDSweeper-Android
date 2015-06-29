package fr.fliizweb.ardsweeper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;

import java.net.URISyntaxException;

import fr.fliizweb.ardsweeper.Class.Actors.Zone;
import fr.fliizweb.ardsweeper.Class.Tools;

/**
 * Created by rcdsm on 28/06/15.
 */
public class ARDSweeper implements ApplicationListener {

    private Zone[][] zones; // Liste des zones dans un tableau zones[x][y]
    private Stage stage;
    private String token; // Le token du joueur qui est connecté
    private String username; // Le nom du joueur qui est connecté
    private int port; // Le port du serveur

    private JSONArray arrayMap; // La carte retourné par le serveur

    @Override
    public void create() {
        stage = new Stage();
        final TextureRegion zoneTexture = new TextureRegion(new Texture("blank.jpg")); // Texture blanche pour les zones non explorées
        final TextureRegion bombTexture = new TextureRegion(new Texture("bomb.jpg")); // Texture noire pour les zones avec une bombe dessus

        zones = new Zone[10][10]; // Taille du tableau de zones

        // On récupère les informations du joueur stockés sur le support (smartphone ou tablette)
        Preferences prefs = Gdx.app.getPreferences(Tools.PACKAGE_ROOT);
        token = prefs.getString(Tools.PACKAGE_ROOT + ".token", "token");
        username = prefs.getString(Tools.PACKAGE_ROOT + ".username", "username");
        port = prefs.getInteger(Tools.PACKAGE_ROOT + ".port");

        final Socket socket;

            try {
                socket = IO.socket(Tools.API_SERVER + ":" + port); // On se connecte au serveur
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                socket.emit("new player", username); // On connecte au serveur le joueur connecté sur l'application

                socket.emit("get full map", token); // On demande la carte du jeu
            }

        });

        socket.on("position", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int hitPosX = (Integer) args[0]; // Le premier élément retourné correspond à la position X
                int hitPosY = (Integer) args[1]; // Le second élément retourné correspond à la position Y
                int state = (Integer) args[2]; // Le troisième élément retourné correspond à l'état de la zone

                // Si la zone séléctionnée est une bombe
                if(state == -3){
                    zones[hitPosX][hitPosY] = new Zone(bombTexture, socket);

                    // Initialisation de la hauteur et la largeur de la zone.
                    zones[hitPosX][hitPosY].setHeight(70);
                    zones[hitPosX][hitPosY].setWidth(70);

                    // On met un nom à la zone qui correspond à ses coordoonées
                    zones[hitPosX][hitPosY].setName(hitPosX + "," + hitPosY);

                    // On position les zones à 70 px d'intervales les unes des autres
                    zones[hitPosX][hitPosY].setPosition(hitPosX * 70, hitPosY * 70);
                    zones[hitPosX][hitPosY].setTouchable(Touchable.disabled);

                    // On ajoute la zone au stage
                    stage.addActor(zones[hitPosX][hitPosY]);
                }
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

            }

        });

        //socket.emit("get players list");
        /*socket.on("players list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("ARDSwepperLog", "players = " + args[0]);
            }

        });*/

        // On prévient l'utilisateur qu'une bombe a été découverte et que de l'aide est requise
        socket.on("bomb", new Emitter.Listener(){
           @Override
           public void call(Object... args){
               Gdx.app.log("bombDiscovered", "A bomb was discovered");
           }
        });

        // On récupère la carte
        socket.on("full map", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                arrayMap = (JSONArray) args[0];

                // On boucle sur les lignes
                for (int i = 0; i < arrayMap.length(); i++) {
                    // On met la colonne dans un nouveau tableau
                    JSONArray cols = (JSONArray) arrayMap.get(i);

                    // On boucle sur les colonnes
                    for (int j = 0; j < cols.length(); j++) {
                        // On met les valeurs dans un nouveau tableau
                        JSONArray values = (JSONArray) cols.get(j);

                        // On instancie une nouvelle zone selon sa position
                        zones[i][j] = new Zone(zoneTexture, socket);

                        for (int k = 0; k < values.length(); k++) {
                            // On met le contenu des valeur dans un nouveau tableau
                            JSONArray valueIndividual = (JSONArray) cols.get(j);

                            // Si le premier élément du tableau "valueIndividual" est égal à -2, alors ça signifie que la zone a été visitée
                            if(Integer.valueOf(valueIndividual.get(1).toString()) == -2){
                                zones[i][j].setVisible(false);
                            } // Sinon si le premier élément du tableau "valueIndividual" est égal à -3, alors ça signifie que la zone est une bombe
                            else if (Integer.valueOf(valueIndividual.get(1).toString()) == -3){
                                zones[i][j].setTexture(bombTexture); // On remplace la texture de la zone par la texture de bombe
                                zones[i][j].setTouchable(Touchable.disabled); // On désactive le toucher sur cette zone.
                            }
                        }

                        // Initialisation de la hauteur et la largeur de la zone.
                        zones[i][j].setHeight(70);
                        zones[i][j].setWidth(70);

                        // On position les zones à 70 px d'intervales les unes des autres
                        zones[i][j].setPosition(i * 70, j * 70);

                        // On met un nom à la zone qui correspond à ses coordoonées
                        zones[i][j].setName(i + "," + j);

                        // On ajoute la zone au stage
                        stage.addActor(zones[i][j]);
                    }
                }

                socket.emit("full map client");
            }
        });

        socket.connect();

    Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
