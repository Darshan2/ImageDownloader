package com.darshan.android.imagedownloader.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image {

    //Ignoring received value of format.(Since it is same for each entry)
    @SerializedName("format")
    @Expose
    private String format;

    @SerializedName("width")
    @Expose
    private Integer width;

    @SerializedName("height")
    @Expose
    private Integer height;

    @SerializedName("filename")
    @Expose
    private String filename;

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("author")
    @Expose
    private String author;

    @SerializedName("author_url")
    @Expose
    private String authorUrl;

    @SerializedName("post_url")
    @Expose
    private String postUrl;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    @Override
    public String toString() {
        return "Image{" +
                "format='" + format + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", filename='" + filename + '\'' +
                ", id=" + id +
                ", author='" + author + '\'' +
                ", authorUrl='" + authorUrl + '\'' +
                ", postUrl='" + postUrl + '\'' +
                '}';
    }
}
