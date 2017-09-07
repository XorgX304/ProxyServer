package org.game.throne.web.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.game.throne.web.dao.domain.EverydayVocabulary;

import java.util.List;

/**
 * Created by lvtu on 2017/8/1.
 */
@Mapper
public interface EverydayVocabularyMapper {

    @Insert("insert into everyday_vocabulary (date,word,word_image_name) value(#{date},#{word},#{wordImageName})")
    void save(EverydayVocabulary v);

    @Select("select * from everyday_vocabulary where date=#{date}")
    List<EverydayVocabulary> getByDate(@Param("date")String date);
}
