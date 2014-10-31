package com.ifightmonsters.yarra.entities;

import java.util.List;

/**
 * Created by Gregory on 10/31/2014.
 */
public class Status {

    private String online;
    private String relay;
    private String listeners;
    private String all_listeners;
    private String playlist;
    private List<Song> songs;

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getRelay() {
        return relay;
    }

    public void setRelay(String relay) {
        this.relay = relay;
    }

    public String getListeners() {
        return listeners;
    }

    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    public String getAll_listeners() {
        return all_listeners;
    }

    public void setAll_listeners(String all_listeners) {
        this.all_listeners = all_listeners;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

}
