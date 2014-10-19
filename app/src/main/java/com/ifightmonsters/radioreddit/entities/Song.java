package com.ifightmonsters.radioreddit.entities;

/**
 * Created by Gregory on 10/4/2014.
 */
public class Song {

    private String id;
    private String title;
    private String artist;
    private String album;
    private String redditor;
    private String genre;
    private String score;
    private String reddit_title;
    private String reddit_url;
    private String preview_url;
    private String download_url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getRedditor() {
        return redditor;
    }

    public void setRedditor(String redditor) {
        this.redditor = redditor;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getReddit_title() {
        return reddit_title;
    }

    public void setReddit_title(String reddit_title) {
        this.reddit_title = reddit_title;
    }

    public String getReddit_url() {
        return reddit_url;
    }

    public void setReddit_url(String reddit_url) {
        this.reddit_url = reddit_url;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }
}
