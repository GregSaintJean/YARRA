package com.ifightmonsters.radioreddit.entities.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ifightmonsters.radioreddit.entities.Song;
import com.ifightmonsters.radioreddit.entities.Status;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gregory on 10/4/2014.
 */
public class StatusTypeAdapter extends TypeAdapter<Status>{
    @Override
    public void write(JsonWriter out, Status value) throws IOException {

    }

    @Override
    public Status read(JsonReader in) throws IOException {

        JsonToken token = in.peek();
        Status status = new Status();
        if(token.equals(JsonToken.BEGIN_OBJECT)){
            in.beginObject();
            while(!in.peek().equals(JsonToken.END_OBJECT)){
                if(in.peek().equals(JsonToken.NAME)){
                    String name =  in.nextName();
                    if(name.equals("online")){
                        status.setOnline(in.nextString());
                    } else if(name.equals("relay")) {
                        status.setRelay(in.nextString());
                    } else if(name.equals("listeners")) {
                        status.setListeners(in.nextString());
                    } else if(name.equals("all_listeners")) {
                        status.setAll_listeners(in.nextString());
                    } else if(name.equals("playlist")) {
                        status.setPlaylist(in.nextString());
                    } else if(name.equals("songs")) {
                        status.setSongs(getSongs(in));
                        in.skipValue();
                    } else {
                        in.skipValue();
                    }
                }
            }
            in.endObject();
        }

        return status;
    }

    private List<Song> getSongs(JsonReader in) throws IOException{
        List<Song> songs = new LinkedList<Song>();

        in.beginObject();
        while(!in.peek().equals(JsonToken.END_OBJECT)){
            if(in.peek().equals(JsonToken.NAME)){
                String name = in.nextName();

                if(name.equals("song")){
                    in.beginArray();
                    while(!in.peek().equals(JsonToken.END_ARRAY)){
                        songs.add(getSong(in));
                    }
                    in.endArray();
                }
            }
        }
        in.endObject();

        return songs;
    }

    private Song getSong(JsonReader in) throws IOException{

        Song song = new Song();
        in.beginObject();
        while(in.peek().equals(JsonToken.END_OBJECT)){
            if(in.peek().equals(JsonToken.NAME)){
                String name = in.nextName();

                if(name.equals("id")){
                    song.setId(in.nextString());
                } else if(name.equals("title")){
                    song.setTitle(in.nextString());
                } else if(name.equals("artist")){
                    song.setArtist(in.nextString());
                } else if(name.equals("album")){
                    song.setAlbum(in.nextString());
                } else if(name.equals("redditor")){
                    song.setRedditor(in.nextString());
                } else if(name.equals("genre")){
                    song.setGenre(in.nextString());
                } else if(name.equals("score")){
                    song.setScore(in.nextString());
                } else if(name.equals("reddit_title")){
                    song.setReddit_title(in.nextString());
                } else if(name.equals("reddit_url")){
                    song.setReddit_url(in.nextString());
                } else if(name.equals("preview_url")){
                    song.setPreview_url(in.nextString());
                } else if(name.equals("download_url")){
                    song.setDownload_url(in.nextString());
                }
            }
        }
        in.endObject();

        return song;

    }
}
