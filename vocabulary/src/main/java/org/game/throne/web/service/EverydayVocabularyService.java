package org.game.throne.web.service;

import org.game.throne.web.dao.domain.EverydayVocabulary;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by lvtu on 2017/8/1.
 */
public interface EverydayVocabularyService {

    void saveWord(String word, String date, MultipartFile file);

    List<EverydayVocabulary> getByDate(String date);
}
