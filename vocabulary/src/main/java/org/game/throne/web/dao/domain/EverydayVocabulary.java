package org.game.throne.web.dao.domain;

import java.util.Date;

/**
 * Created by lvtu on 2017/8/1.
 */
public class EverydayVocabulary {
    private Long id;
    private String date;
    private String word;
    private String wordImageName;
    private Date createtime;
    private Date updatetime;

    public static EverydayVocabulary of(String date, String word, String wordImageName) {
        EverydayVocabulary v = new EverydayVocabulary();
        v.date = date;
        v.word = word;
        v.wordImageName = wordImageName;
        return v;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordImageName() {
        return wordImageName;
    }

    public void setWordImageName(String wordImageName) {
        this.wordImageName = wordImageName;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
