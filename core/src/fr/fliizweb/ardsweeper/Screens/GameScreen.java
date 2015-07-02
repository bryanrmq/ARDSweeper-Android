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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;

import java.net.URISyntaxException;

import fr.fliizweb.ardsweeper.ARDSweeper;
import fr.fliizweb.ardsweeper.Class.Actors.Zone;
import fr.fliizweb.ardsweeper.Class.Tools;

/**
 * Created by rcdsm on 29/06/15.
 */
public class GameScreen implements Screen, GestureDetector.GestureListener {

    private Zone[][] zones; // Zones list in an array zones[x][y]
    private Stage stage;
    private String token; // Player's token who is online
    private String username; // Player's username who is online
    private int port; // Port's server

    private JSONArray arrayMap; // Map return by server


    // Manage screen and his children
    InputMultiplexer inputMultiplexer;
    private OrthographicCamera camera;
    private FitViewport vp;
    private SpriteBatch batch;
    Skin skin;


    // Zones
    private final static int ZONE_WIDTH = 90;
    private final static int ZONE_HEIGHT = 90;
    private Group group;

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
    private Label countdown;


    // Arduino
    private GameBluetooth gBlue;
    private String messageArduino;
    private String code;

    // Life
    private int life;
    private Label displayLife;

    public GameScreen(){
        init();
    }

    public void init(){
        gBlue = ARDSweeper.gameBluetooth;

        // camera
        camera = new OrthographicCamera();
        camera.zoom = ZOOM_DEFAULT;
        origZoom = camera.zoom;
        camera.position.set(MAP_WIDTH / 2, MAP_HEIGHT / 2, 0); // Nous centrons la caméra par rapport à la taille de la carte
        camera.update();

        // viewport & stage
        vp = new FitViewport( 800, 450, camera );
        stage = new Stage( vp );
        stage.getViewport().setCamera(camera);
        batch = new SpriteBatch();


        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));

        skin = new Skin(Gdx.files.internal("ui/defaultskin.json"));

        // form
        countdown = new Label("0", skin);
        countdown.toBack();
        countdown.setFontScale(2);
        countdown.setPosition(20 , MAP_HEIGHT / 2 - countdown.getHeight() / 3);

        // life
        displayLife = new Label("3/3", skin);
        displayLife.setZIndex(100);
        displayLife.setZIndex(100);
        displayLife.setFontScale(2f);

        // zones
        group = new Group();
        group.setZIndex(11);
        group.toFront();


        // Texture declaration for each kind of zone
        final TextureRegion bombTexture = new TextureRegion(new Texture("zones/zone-bomb.png")); // Grey texture for zone with bomb
        final TextureRegion zoneTexture = new TextureRegion(new Texture("zones/zone.png")); // Grey texture for zone with zone no active
        final TextureRegion zone0Texture = new TextureRegion(new Texture("zones/zone-0.png")); // Grey texture for zone with 1 bomb around
        final TextureRegion zone1Texture = new TextureRegion(new Texture("zones/zone-1.png"));
        final TextureRegion zone2Texture = new TextureRegion(new Texture("zones/zone-2.png"));
        final TextureRegion zone3Texture = new TextureRegion(new Texture("zones/zone-3.png"));
        final TextureRegion zone4Texture = new TextureRegion(new Texture("zones/zone-4.png"));
        final TextureRegion zone5Texture = new TextureRegion(new Texture("zones/zone-5.png"));
        final TextureRegion zone6Texture = new TextureRegion(new Texture("zones/zone-6.png"));
        final TextureRegion zone7Texture = new TextureRegion(new Texture("zones/zone-7.png"));
        final TextureRegion zone8Texture = new TextureRegion(new Texture("zones/zone-8.png"));

        // Get player's informations
        Preferences prefs = Gdx.app.getPreferences(Tools.PACKAGE_ROOT);
        token = prefs.getString(Tools.PACKAGE_ROOT + ".token", "token");
        username = prefs.getString(Tools.PACKAGE_ROOT + ".username", "username");
        port = prefs.getInteger(Tools.PACKAGE_ROOT + ".port");

        final Socket socket;

        try {
            socket = IO.socket(Tools.API_SERVER + ":" + port); // We are connected to the server and the right port
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        /**
         * socket.on(CALL, ACTION)
         * @param CALL Event we want to get
         * @param ACTION Action to do
         */

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                socket.emit("new player", username); // Connect the player to the server

                socket.emit("get full map", token); // Ask for the game's map
            }

        });


        socket.on("restart", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });

        socket.on("restart map", new Emitter.Listener() { // Dès qu'on reçoit l'appel "restart map"
            @Override
            public void call(Object... args) {
                Gdx.app.log("restart map", "restart map life = " + displayLife);
                Gdx.app.log("restart map", "restart map life = " + displayLife.getText());
                Gdx.app.log("restart map", "restart map life = " + displayLife.getZIndex());
                socket.emit("get full map"); // Ask a new game's map
            }
        });


        socket.on("position", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int hitPosX = (Integer) args[0]; // First return element is X position
                int hitPosY = (Integer) args[1]; // Second return element is Y position
                int state = (Integer) args[2]; // Third return element is the zone's state

                // If the selected zone is a bomb
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

        // Get the life's game
        socket.on("life", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("life", "life = " + args[0]);
                life = (Integer) args[0];
                displayLife.setText("Vie : " + life + "/3");
            }
        });

        // Alert the player about a bomb was discover
        socket.on("bomb", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("bombDiscovered", "A bomb was discovered");
                code = (String) args[0];
            }
        });

        socket.on("bomb timer", new Emitter.Listener() { // Timer which is called every second until the bomb stop or explode
            @Override
            public void call(Object... args) {

                float delay = 0.5f; // seconds

                // Timer to get Arduino'sinformations if we are connected to it
                if (gBlue.getConnected()) {
                    gBlue.send(code);

                    Gdx.app.log("gameScreenLog", "connected arduino");

                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            messageArduino = gBlue.receive(); // Get what Ardino send
                        }
                    }, delay);


                    if (messageArduino != null) {
                        // If the message start with "disarmed"
                        if (messageArduino.startsWith("disarmed")) {
                            socket.emit("bomb disarmed"); // Tell the server the bomb is disarmed
                        } else if (messageArduino.startsWith("explosion")) {
                            socket.emit("explosion"); // Tell the server the bomb is explode
                        }
                    }
                }
                Gdx.app.log("gameScreenLog", "compte à rebours = " + args[0]);
                helpBomb((Integer) args[0]); // Function to show to the player a need help
            }
        });

        socket.on("bomb explode", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("bombDiscovered", "Bomb exploded");
                bombClear();
            }
        });

        socket.on("bomb disarmed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("bombDiscovered", "Bomb disarmed");
                bombClear();
            }
        });



        // Get the map
        socket.on("full map", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                arrayMap = (JSONArray) args[0];

                JSONArray colsLength = (JSONArray) arrayMap.get(0);
                zones = new Zone[arrayMap.length()][colsLength.length()]; // Array's size zone


                // Loop on the lines
                for (int i = 0; i < arrayMap.length(); i++) {
                    // Put the array on a new array
                    JSONArray cols = (JSONArray) arrayMap.get(i);

                    // Loop on the cols
                    for (int j = 0; j < cols.length(); j++) {
                        // Put value on a new array
                        JSONArray values = (JSONArray) cols.get(j);

                        // Instantiate a new zone about the position
                        zones[i][j] = new Zone(zoneTexture, socket);

                        for (int k = 0; k < values.length(); k++) {
                            // Put content value on a new array
                            JSONArray valueIndividual = (JSONArray) cols.get(j);
                            int state = Integer.valueOf(valueIndividual.get(1).toString()); // Zone's state
                            int checked = Integer.valueOf(valueIndividual.get(2).toString()); // Zone visited?

                            if(checked == 1) {
                                zones[i][j].setTouchable(Touchable.disabled); // Disable touch on this zone

                                // If zone is a bomb
                                if (state == -3) {
                                    zones[i][j].setTexture(bombTexture); // Replace texture with bomb's texture
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

                        // Init zone's height and width
                        zones[i][j].setHeight(ZONE_HEIGHT);
                        zones[i][j].setWidth(ZONE_WIDTH);
                        zones[i][j].setZIndex(10);

                        // Add zone's position
                        zones[i][j].setPosition(i * ZONE_WIDTH, j * ZONE_HEIGHT);

                        // Add name's position
                        zones[i][j].setName(i + "," + j);

                        // Add zone to group
                        group.addActor(zones[i][j]);
                    }
                }

                socket.emit("full map client");
            }
        });

        socket.connect();

    }

    /**
     * Here we add a new zone at our map.
     *
     * @param texture Texture to add at our zone
     * @param socket Main socket
     * @param hitPosX zone's position X
     * @param hitPosY zone's position Y
     */

    public void setZone(TextureRegion texture, Socket socket, int hitPosX, int hitPosY){
        zones[hitPosX][hitPosY] = new Zone(texture, socket);

        // Init zone's height and width
        zones[hitPosX][hitPosY].setHeight(ZONE_HEIGHT);
        zones[hitPosX][hitPosY].setWidth(ZONE_WIDTH);

        // Add name's position
        zones[hitPosX][hitPosY].setPosition(hitPosX * ZONE_WIDTH, hitPosY * ZONE_HEIGHT);

        // Add zone to group
        zones[hitPosX][hitPosY].setName(hitPosX + "," + hitPosY);
        zones[hitPosX][hitPosY].setTouchable(Touchable.disabled);

        zones[hitPosX][hitPosY].setZIndex(10);

        //stage.addActor(zones[hitPosX][hitPosY]);
        group.addActor(zones[hitPosX][hitPosY]);
    }


    /**
     * Ask to player to difuse the bomb
     *
     * @param time Time before bomb explode
     */

    public void helpBomb(int time){
        showForm = true;

        camera.position.set(MAP_WIDTH / 2, MAP_HEIGHT / 2, 0); // Center camera about map's size
        camera.update();


        countdown.toFront();
        countdown.setText("Temps restant : " + time + "\n Attention, un joueur est tombe sur une bombe ! \n Aidez le grace a votre peripherique Arduino");


        Gdx.app.log("gameScreenLog", " countdown helpBomb" + countdown.getText());
    }

    /**
     * Delete the message to the screen
     */

    public void bombClear(){
        countdown.setText("");
        countdown.toBack();

        // If the bomb had explode or defure, not need to display the help message
        showForm = false;
    }


    /**
     *
     * @return game's batch
     */

    public SpriteBatch getBatch(){
        return batch;
    }


    /**
     *
     * @return game's map
     */

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
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(!showForm) { // If help form is not display
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.end();

            stage.addActor(countdown);
            stage.addActor(group);

        } else {
            stage.addActor(group);
            stage.addActor(countdown);
        }

        displayLife.setPosition(camera.position.x + Gdx.graphics.getHeight() / 2 - 20, camera.position.y + 215);
        Gdx.app.log("life", "display life size = = " + displayLife.getWidth() + " | " + displayLife.getHeight());

        stage.addActor(displayLife);

        Gdx.app.log("life", "display life = " + displayLife.getZIndex());

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if(!showForm) {
            stage.getViewport().update(width, height, true);
            moveCamera(true, 0, 0);
            camera.update();
        }
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

     /* Moves */

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
        if(!showForm) { // // If help form is not display
            moveCamera(true, -deltaX, deltaY);
            camera.update();
        }

        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if(!showForm) {

            // Number finger on the screen
            /*int activeTouch = 0;
            for (int i = 0; i < 20; i++) {
                if (Gdx.app.getInput().isTouched(i)) activeTouch++;
            }*/

            // Calcul for a good camera
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
        }

        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }


    /**
     *
     * @param add Boolean to know if we want to move or not
     * @param x camera x position
     * @param y camera y position
     */

    public void moveCamera (boolean add, float x, float y) {

        float newX, newY;

        if (add) {
            newX = camera.position.x + x;
            newY = camera.position.y + y;
        } else {
            newX = x;
            newY = y;
        }

        // The camera stay in a zone on screen
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
