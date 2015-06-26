package fr.fliizweb.ardsweeper.Class;

import com.badlogic.gdx.Gdx;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;


/**
 * Created by rcdsm on 25/06/15.
 */
public class ARDSweeperSocket {

    private ArrayList<JSONObject> map;

    private Socket socket;
    {
        try {
            socket = IO.socket(Tools.API_ROOT);
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
                Gdx.app.log("ARDSwepperLog", "args = " + args[0]);


                /*JSONObject obj = new JSONObject(args);
                JSONArray json_map = obj.getJSONArray("map");

                int size = json_map.length();
                map = new ArrayList<JSONObject>();
                for (int i = 0; i < size; i++) {
                    JSONObject another_json_object = json_map.getJSONObject(i);
                    //Blah blah blah...
                    map.add(another_json_object);
                }

                //Finally
                JSONObject[] jsons = new JSONObject[map.size()];
                map.toArray(jsons);*/

                socket.emit("full map client");
            }
        });



        socket.connect();
    }


    public ArrayList<JSONObject> getMap(){
        return map;
    }

}
