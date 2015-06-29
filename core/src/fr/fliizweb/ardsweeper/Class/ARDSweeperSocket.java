package fr.fliizweb.ardsweeper.Class;

import com.badlogic.gdx.Gdx;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;

import java.net.URISyntaxException;
import java.util.ArrayList;


/**
 * Created by rcdsm on 25/06/15.
 */
public class ARDSweeperSocket {

    private ArrayList<String> map;
    private JSONArray arrayMap;

    private Socket socket;

    public ARDSweeperSocket(int port){
        try {
            socket = IO.socket(Tools.API_SERVER + ":" + port);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public void connect(final String token, final String username){
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                socket.emit("new player", username);

                socket.emit("get full map", token);
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

        socket.on("full map", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                Gdx.app.log("ARDSwepperLog", "args[0] getname = " + args[0]);

                arrayMap = (JSONArray) args[0];

                socket.emit("full map client");
            }
        });

        socket.connect();
    }




    public JSONArray getArrayMap(){
        return arrayMap;
    }

}
