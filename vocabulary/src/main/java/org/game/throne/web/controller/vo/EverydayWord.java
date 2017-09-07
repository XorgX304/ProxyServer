package org.game.throne.web.controller.vo;

import org.game.throne.web.dao.domain.EverydayVocabulary;

/**
 * Created by lvtu on 2017/7/29.
 */
public class EverydayWord {

    String date;
    String word;
    String image;

    public static EverydayWord of(String date,String word,String image){
        EverydayWord w = new EverydayWord();
        w.date = date;
        w.word = word;
        w.image = image;
        return w;
    }

    public static EverydayWord of(EverydayVocabulary v){
        EverydayWord w = new EverydayWord();
        w.date = v.getDate();
        w.word = v.getWord();
        w.image = v.getWordImageName();
        return w;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
