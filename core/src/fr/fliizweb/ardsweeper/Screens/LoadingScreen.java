package fr.fliizweb.ardsweeper.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.TimeUtils;

import fr.fliizweb.ardsweeper.ARDSweeper;

public class LoadingScreen implements Screen {
    private ARDSweeper mGame;
    private BitmapFont bf_loadProgress;
    private long progress = 0;
    private long startTime = 0;
    private ShapeRenderer mShapeRenderer;
    private OrthographicCamera camera;
    private final int screenWidth = 800, screenHeight = 480;

    private GameScreen gameScreen;

    public LoadingScreen(Game game) {
        mGame = (ARDSweeper) game;
        bf_loadProgress = new BitmapFont();

        //bf_loadProgress.setScale(2, 1);
        mShapeRenderer = new ShapeRenderer();
        startTime = TimeUtils.nanoTime();

        gameScreen = new GameScreen();

        initCamera();
    }

    private void initCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.update();

    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        showLoadProgress();
    }

    /**
     * Show progress that updates after every half second "0.5 sec"
     */
    private void showLoadProgress() {

        long currentTimeStamp = TimeUtils.nanoTime();
        if (currentTimeStamp - startTime > TimeUtils.millisToNanos(500)) {
            startTime = currentTimeStamp;
            progress = progress + 33;
        }
        // Width of progress bar on screen relevant to Screen width
        float progressBarWidth = (screenWidth / 100) * progress;


        gameScreen.getBatch().setProjectionMatrix(camera.combined);
        gameScreen.getBatch().begin();
        bf_loadProgress.draw(gameScreen.getBatch(), "Chargement de la carte en cours " + progress + " / " + 100, 10, 40);
        gameScreen.getBatch().end();

        mShapeRenderer.setProjectionMatrix(camera.combined);
        mShapeRenderer.begin(ShapeType.Filled);
        mShapeRenderer.setColor(Color.YELLOW);
        mShapeRenderer.rect(0, 10, progressBarWidth, 10);
        mShapeRenderer.end();
        Gdx.app.log("getArrayMap", "GetArrayMap = " + gameScreen.getArrayMap());
        if (gameScreen.getArrayMap() != null) {
            if (progress >= 100) {
                moveToMenuScreen();
            }
        }
    }

    /**
     * Move to menu screen after progress reaches 100%
     */
    private void moveToMenuScreen() {
        mGame.setScreen(gameScreen);
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        bf_loadProgress.dispose();
        mShapeRenderer.dispose();
    }

}