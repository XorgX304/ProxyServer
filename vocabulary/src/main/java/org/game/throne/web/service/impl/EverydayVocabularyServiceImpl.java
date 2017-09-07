package org.game.throne.web.service.impl;

import org.apache.commons.io.IOUtils;
import org.game.throne.web.dao.EverydayVocabularyMapper;
import org.game.throne.web.dao.domain.EverydayVocabulary;
import org.game.throne.web.service.EverydayVocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by lvtu on 2017/8/1.
 */
@Service
public class EverydayVocabularyServiceImpl implements EverydayVocabularyService {

    private static final Logger log = LoggerFactory.getLogger(EverydayVocabularyServiceImpl.class);

    @Autowired
    private EverydayVocabularyMapper everydayVocabularyMapper;

    @Override
    @Transactional
    public void saveWord(String word, String date, MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String fullpathFileName = "/Users/lvtu/workspace/english/image/" + fileName;
            OutputStream out = new FileOutputStream(fullpathFileName);
            IOUtils.copy(file.getInputStream(), out);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(file.getInputStream());

            everydayVocabularyMapper.save(EverydayVocabulary.of(date, word, fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<EverydayVocabulary> getByDate(String date) {
        return everydayVocabularyMapper.getByDate(date);
    }
}
