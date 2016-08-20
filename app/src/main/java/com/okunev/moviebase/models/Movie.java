package com.okunev.moviebase.models;

/**
 * Project MovieBase. Created by gwa on 8/8/16.
 */

public class Movie {
    private String name = "", time = "", part = "", season = "", series = "";
    private Integer id;

    public Movie() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return "movie " + name + " " + time + " " + part + " " + series + " " + season + "\n";
    }
}
