package org.game.throne.web.controller;

import org.game.throne.common.Result;
import org.game.throne.web.controller.vo.EverydayWord;
import org.game.throne.web.dao.domain.EverydayVocabulary;
import org.game.throne.web.service.EverydayVocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lvtu on 2017/7/29.
 */
@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private EverydayVocabularyService everydayVocabularyService;

    @RequestMapping(value = "/upload")
    @ResponseBody
    Result uploadFile(String word, String date, MultipartFile file) {
        try {
            everydayVocabularyService.saveWord(word, date, file);
            return Result.createSuccessResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/everydayWords")
    @ResponseBody
    Result everydayWords(String date){
        List<EverydayVocabulary> words = everydayVocabularyService.getByDate(date);
        List<EverydayWord> wordList = words.stream().map(item->EverydayWord.of(item)).collect(Collectors.toList());
        return Result.createSuccessResult(wordList);
    }

    @RequestMapping(value = "/definition/english/")
    void dic(){

    }

}
