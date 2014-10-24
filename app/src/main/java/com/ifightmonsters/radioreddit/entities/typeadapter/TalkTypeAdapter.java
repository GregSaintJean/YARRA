package com.ifightmonsters.radioreddit.entities.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ifightmonsters.radioreddit.entities.Status;

import java.io.IOException;

/**
 * Created by Gregory on 10/23/2014.
 */
public class TalkTypeAdapter extends TypeAdapter<Status> {
    @Override
    public void write(JsonWriter out, Status value) throws IOException {

    }

    @Override
    public Status read(JsonReader in) throws IOException {
        Status status = new Status();
        in.beginObject();
        while(in.hasNext()){
            String name = in.nextName();
            if(name.equals("online") && in.peek() != JsonToken.NULL){
                status.setOnline(in.nextString());
            } else if(name.equals("relay") && in.peek() != JsonToken.NULL) {
                status.setRelay(in.nextString());
            } else if(name.equals("listeners") && in.peek() != JsonToken.NULL) {
                status.setListeners(in.nextString());
            } else if(name.equals("all_listeners") && in.peek() != JsonToken.NULL) {
                status.setAll_listeners(in.nextString());
            } else if(name.equals("playlist") && in.peek() != JsonToken.NULL) {
                status.setPlaylist(in.nextString());
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        return status;
    }
}
