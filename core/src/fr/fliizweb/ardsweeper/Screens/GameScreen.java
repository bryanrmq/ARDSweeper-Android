package fr.fliizweb.ardsweeper.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;

import java.net.URISyntaxException;

import fr.fliizweb.ardsweeper.Class.Actors.Zone;
import fr.fliizweb.ardsweeper.Class.Tools;

/**
 * Created by rcdsm on 29/06/15.
 */
public class GameScreen implements Screen, GestureDetector.GestureListener {

    private Zone[][] zones; // Liste des zones dans un tableau zones[x][y]
    private Stage stage;
    private String token; // Le token du joueur qui est connecté
    private String username; // Le nom du joueur qui est connecté
    private int port; // Le port du serveur

    private JSONArray arrayMap; // La carte retourné par le serveur

    InputMultiplexer inputMultiplexer;
    private OrthographicCamera camera;
    private FitViewport vp;
    private SpriteBatch batch;
    Skin skin;


    // Zones
    private final static int ZONE_WIDTH = 90;
    private final static int ZONE_HEIGHT = 90;

    // Camera
    private final static float ZOOM_MAX = 1.75f;
    private final static float ZOOM_MIN = 1.25f;
    private final static float ZOOM_DEFAULT = 1.25f;
    private final static float MAP_MARGIN = 50;
    private final static float MAP_HEIGHT = 900;
    private final static float MAP_WIDTH = 900;
    private float origDistance, baseDistance, origZoom;

    // Form
    boolean showForm;
    boolean validForm;


    public GameScreen(){
        init();
    }

    public void init(){
        Gdx.app.log("arrayMap", "init");

        //camera
        camera = new OrthographicCamera();
        camera.zoom = ZOOM_DEFAULT;
        origZoom = camera.zoom;
        camera.position.set(MAP_WIDTH / 2, MAP_HEIGHT / 2, 0);
        camera.update();

        //viewport & stage
        vp = new FitViewport( 800, 450, camera );
        stage = new Stage( vp );
        stage.getViewport().setCamera(camera);
        batch = new SpriteBatch();

        helpBomb();

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));

        skin = new Skin(Gdx.files.internal("ui/defaultskin.json"));


        final TextureRegion bombTexture = new TextureRegion(new Texture("zones/zone-bomb.png")); // Texture noire pour les zones avec une bombe dessus
        final TextureRegion zoneTexture = new TextureRegion(new Texture("zones/zone.png")); // Texture grise pour les zones non cliquées
        final TextureRegion zone0Texture = new TextureRegion(new Texture("zones/zone-0.png")); // Texture grise pour les zones avec 1 bombes autour
        final TextureRegion zone1Texture = new TextureRegion(new Texture("zones/zone-1.png")); // Texture grise pour les zones avec 1 bombes autour
        final TextureRegion zone2Texture = new TextureRegion(new Texture("zones/zone-2.png")); // Texture grise pour les zones avec 2 bombes autour
        final TextureRegion zone3Texture = new TextureRegion(new Texture("zones/zone-3.png")); // Texture grise pour les zones avec 3 bombes autour
        final TextureRegion zone4Texture = new TextureRegion(new Texture("zones/zone-4.png")); // Texture grise pour les zones avec 4 bombes autour
        final TextureRegion zone5Texture = new TextureRegion(new Texture("zones/zone-5.png")); // Texture grise pour les zones avec 5 bombes autour
        final TextureRegion zone6Texture = new TextureRegion(new Texture("zones/zone-6.png")); // Texture grise pour les zones avec 6 bombes autour
        final TextureRegion zone7Texture = new TextureRegion(new Texture("zones/zone-7.png")); // Texture grise pour les zones avec 7 bombes autour
        final TextureRegion zone8Texture = new TextureRegion(new Texture("zones/zone-8.png")); // Texture grise pour les zones avec 8 bombes autour

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
                    setZone(bombTexture, socket, hitPosX, hitPosY);
                } else if (state == -2){
                    setZone(zoneTexture, socket, hitPosX, hitPosY);
                } else if (state == 0){
                    setZone(zone0Texture, socket, hitPosX, hitPosY);
                } else if (state == 1){
                    setZone(zone1Texture, socket, hitPosX, hitPosY);
                } else if (state == 2){
                    setZone(zone2Texture, socket, hitPosX, hitPosY);
                } else if (state == 3){
                    setZone(zone3Texture, socket, hitPosX, hitPosY);
                } else if (state == 4){
                    setZone(zone4Texture, socket, hitPosX, hitPosY);
                } else if (state == 5){
                    setZone(zone5Texture, socket, hitPosX, hitPosY);
                } else if (state == 6){
                    setZone(zone6Texture, socket, hitPosX, hitPosY);
                } else if (state == 7){
                    setZone(zone7Texture, socket, hitPosX, hitPosY);
                } else if (state == 8){
                    setZone(zone8Texture, socket, hitPosX, hitPosY);
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
        socket.on("bomb", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("bombDiscovered", "A bomb was discovered");
            }
        });

        // On récupère la carte
        socket.on("full map", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                arrayMap = (JSONArray) args[0];
                Gdx.app.log("arrayMap", "arrayMap = " + arrayMap);

                JSONArray colsLength = (JSONArray) arrayMap.get(0);
                zones = new Zone[arrayMap.length()][colsLength.length()]; // Taille du tableau de zones


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
                            int state = Integer.valueOf(valueIndividual.get(1).toString());
                            int checked = Integer.valueOf(valueIndividual.get(2).toString());

                            Gdx.app.log("gameScreenLog", "Log = " + valueIndividual);

                            if(checked == 1) {
                                zones[i][j].setTouchable(Touchable.disabled); // On désactive le toucher sur cette zone.

                                // Si le premier élément du tableau "valueIndividual" est égal à -2, alors ça signifie que la zone a été visitée
                                if (state == -2) {
                                    zones[i][j].setTexture(zone1Texture); // On remplace la texture de la zone par la texture de bombe
                                } // Sinon si le premier élément du tableau "valueIndividual" est égal à -3, alors ça signifie que la zone est une bombe
                                else if (state == -3) {
                                    zones[i][j].setTexture(bombTexture); // On remplace la texture de la zone par la texture de bombe
                                } else if (state == 0){
                                    zones[i][j].setTexture(zone0Texture);
                                } else if (state == 1) {
                                    zones[i][j].setTexture(zone1Texture);
                                } else if (state == 2) {
                                    zones[i][j].setTexture(zone2Texture);
                                } else if (state == 3) {
                                    zones[i][j].setTexture(zone3Texture);
                                } else if (state == 4) {
                                    zones[i][j].setTexture(zone4Texture);
                                } else if (state == 5) {
                                    zones[i][j].setTexture(zone5Texture);
                                } else if (state == 6) {
                                    zones[i][j].setTexture(zone6Texture);
                                } else if (state == 7) {
                                    zones[i][j].setTexture(zone7Texture);
                                } else if (state == 8) {
                                    zones[i][j].setTexture(zone8Texture);
                                }

                            } else {
                                zones[i][j].setTexture(zoneTexture);
                            }
                        }

                        // Initialisation de la hauteur et la largeur de la zone.
                        zones[i][j].setHeight(ZONE_HEIGHT);
                        zones[i][j].setWidth(ZONE_WIDTH);

                        // On position les zones à 70 px d'intervales les unes des autres
                        zones[i][j].setPosition(i * ZONE_WIDTH, j * ZONE_HEIGHT);

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

    }

    public void setZone(TextureRegion texture, Socket socket, int hitPosX, int hitPosY){
        zones[hitPosX][hitPosY] = new Zone(texture, socket);

        // Initialisation de la hauteur et la largeur de la zone.
        zones[hitPosX][hitPosY].setHeight(ZONE_HEIGHT);
        zones[hitPosX][hitPosY].setWidth(ZONE_WIDTH);

        // On position les zones à 90 px d'intervales les unes des autres
        zones[hitPosX][hitPosY].setPosition(hitPosX * ZONE_WIDTH, hitPosY * ZONE_HEIGHT);

        // On met un nom à la zone qui correspond à ses coordoonées
        zones[hitPosX][hitPosY].setName(hitPosX + "," + hitPosY);
        zones[hitPosX][hitPosY].setTouchable(Touchable.disabled);


        stage.addActor(zones[hitPosX][hitPosY]);
    }

    public void helpBomb(){
        final Table table = new Table(); // On créé une table pour mettre nos éléments du formulaire
        table.setZIndex(100);
        table.setVisible(true); // Visible à vrai (ici c'est juste un test, par défaut c'est vrai)
        table.setSize(Gdx.graphics.getWidth() - 30, Gdx.graphics.getHeight() - 30); // On met la taille de la table à celle de l'écran
        table.setPosition(30, 30);

        camera.zoom = ZOOM_MIN; // Faire un zoom lorsqu'on affiche le formulaire et le placer correctement par rapport à l'écran ? Désactiver le scroll et zoom lorsque le formulaire est affiché ?

        /*Pixmap pm1 = new Pixmap(1, 1, Pixmap.Format.RGB565);
        pm1.setColor(new Color(0f, 0f, 0f, 0.1f));
        pm1.fill();
        table.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm1))));

        TextButton close = new TextButton("Annuler", skin);
        //close.setStyle(style);
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                table.remove();
                camera.zoom = origZoom;
                showForm = false;
                validForm = false;
                Gdx.input.setOnscreenKeyboardVisible(false);
            }
        });
        close.align(Align.center);
        table.align(Align.center);
        table.add(close).width(120).height(60).padTop(30);*/

        stage.addActor(table);
    }

    public SpriteBatch getBatch(){
        return batch;
    }

    public JSONArray getArrayMap(){
        return arrayMap;
    }

    @Override
    public void show() {
        Gdx.input.isTouched();

        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f,0.5f,0.5f,0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        moveCamera(true, 0, 0);
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

     /* MOUVEMENTS */

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        moveCamera(true, -deltaX, deltaY);
        camera.update();

        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {

        // Calcul le nombre de doigts sur l'écran
        /*int activeTouch = 0;
        for (int i = 0; i < 20; i++) {
            if (Gdx.app.getInput().isTouched(i)) activeTouch++;
        }*/

        if (origDistance != initialDistance) {
            origDistance = initialDistance;
            baseDistance = initialDistance;
            origZoom = camera.zoom;
        }

        float ratio = baseDistance / distance;
        float newZoom = origZoom * ratio;

        if (newZoom >= ZOOM_MAX) {
            camera.zoom = ZOOM_MAX;
            origZoom = ZOOM_MAX;
            baseDistance = distance;
        } else if (newZoom <= ZOOM_MIN) {
            camera.zoom = (float) ZOOM_MIN;
            origZoom = (float) ZOOM_MIN;
            baseDistance = distance;
        } else {
            camera.zoom = newZoom;
        }

        moveCamera(true, 0, 0);
        camera.update();


        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    public void moveCamera (boolean add, float x, float y) {

        float newX, newY;

        if (add) {
            newX = camera.position.x + x;
            newY = camera.position.y + y;
        } else {
            newX = x;
            newY = y;
        }

        if (newX - camera.viewportWidth / 2 * camera.zoom < (Math.abs(MAP_MARGIN) * -1))
            newX = (Math.abs(MAP_MARGIN) * -1) + camera.viewportWidth / 2 * camera.zoom;
        if (newX + camera.viewportWidth / 2 * camera.zoom > MAP_WIDTH + MAP_MARGIN)
            newX = MAP_WIDTH - camera.viewportWidth / 2 * camera.zoom + MAP_MARGIN;
        if (newY + camera.viewportHeight / 2 * camera.zoom > MAP_HEIGHT + MAP_MARGIN)
            newY = MAP_HEIGHT - camera.viewportHeight / 2 * camera.zoom + MAP_MARGIN;
        if (newY - camera.viewportHeight / 2 * camera.zoom < (Math.abs(MAP_MARGIN) * -1))
            newY = (Math.abs(MAP_MARGIN) * -1) + camera.viewportHeight / 2 * camera.zoom;

        camera.position.x = newX;
        camera.position.y = newY;
    }
}
