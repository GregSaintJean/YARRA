package com.ifightmonsters.yarra.entities.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ifightmonsters.yarra.entities.Song;
import com.ifightmonsters.yarra.entities.Status;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gregory on 10/31/2014.
 */
public class StatusTypeAdapter extends TypeAdapter<Status> {
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
            } else if(name.equals("songs") && in.peek() != JsonToken.NULL) {
                status.setSongs(getSongs(in));
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        return status;
    }


    private List<Song> getSongs(JsonReader in) throws IOException{
        List<Song> songs = new LinkedList<Song>();

        in.beginObject();
        while(in.hasNext()){
            String name = in.nextName();

            if(name.equals("song")){
                in.beginArray();
                while(in.hasNext()){
                    songs.add(getSong(in));
                }
                in.endArray();
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        return songs;
    }

    private Song getSong(JsonReader in) throws IOException{

        Song song = new Song();
        in.beginObject();
        while(in.hasNext()){
            String name = in.nextName();

            if(name.equals("id") && in.peek() != JsonToken.NULL){
                song.setId(in.nextString());
            } else if(name.equals("title") && in.peek() != JsonToken.NULL){
                song.setTitle(in.nextString());
            } else if(name.equals("artist") && in.peek() != JsonToken.NULL){
                song.setArtist(in.nextString());
            } else if(name.equals("album") && in.peek() != JsonToken.NULL){
                song.setAlbum(in.nextString());
            } else if(name.equals("redditor") && in.peek() != JsonToken.NULL){
                song.setRedditor(in.nextString());
            } else if(name.equals("genre") && in.peek() != JsonToken.NULL){
                song.setGenre(in.nextString());
            } else if(name.equals("score") && in.peek() != JsonToken.NULL){
                song.setScore(in.nextString());
            } else if(name.equals("reddit_title") && in.peek() != JsonToken.NULL){
                song.setReddit_title(in.nextString());
            } else if(name.equals("reddit_url") && in.peek() != JsonToken.NULL){
                song.setReddit_url(in.nextString());
            } else if(name.equals("preview_url") && in.peek() != JsonToken.NULL){
                song.setPreview_url(in.nextString());
            } else if(name.equals("download_url") && in.peek() != JsonToken.NULL){
                song.setDownload_url(in.nextString());
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        return song;
    }

}
