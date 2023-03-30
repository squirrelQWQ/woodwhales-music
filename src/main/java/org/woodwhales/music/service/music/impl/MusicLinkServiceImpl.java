package org.woodwhales.music.service.music.impl;

import cn.woodwhales.common.business.DataTool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.woodwhales.music.config.AppConfig;
import org.woodwhales.music.entity.Music;
import org.woodwhales.music.entity.MusicInfoLink;
import org.woodwhales.music.enums.MusicLinkSourceEnum;
import org.woodwhales.music.enums.MusicLinkTypeEnum;
import org.woodwhales.music.mapper.MusicInfoLinkMapper;
import org.woodwhales.music.model.MusicInfoLinkDetailVo;

import java.util.*;

/**
 * @author woodwhales on 2023-03-28 11:50
 */
@Slf4j
@Primary
@Service
@Transactional(rollbackFor = Exception.class)
public class MusicLinkServiceImpl extends ServiceImpl<MusicInfoLinkMapper, MusicInfoLink> {

    @Autowired
    private AppConfig appConfig;

    public List<MusicInfoLink> getLinkInfoListByMusicId(Long musicId) {
        return this.list(Wrappers.<MusicInfoLink>lambdaQuery()
                .eq(MusicInfoLink::getMusicId, musicId));
    }

    public List<MusicInfoLink> getLinkInfoListByMusicIds(List<Long> musicIdList) {
        if(CollectionUtils.isEmpty(musicIdList)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<MusicInfoLink>lambdaQuery()
                .eq(MusicInfoLink::getLinkSource, appConfig.getMusicLinkSourceEnum().getCode())
                .in(MusicInfoLink::getMusicId, musicIdList));
    }

    public List<MusicInfoLinkDetailVo> getLinkDetailVoListByMusicId(Long musicId) {
        List<MusicInfoLink> list = this.list(Wrappers.<MusicInfoLink>lambdaQuery()
                .eq(MusicInfoLink::getMusicId, musicId));
        List<MusicInfoLinkDetailVo> result = new ArrayList<>();
        Map<Integer, List<MusicInfoLink>> mapping = DataTool.groupingBy(list, MusicInfoLink::getLinkSource);
        for (MusicLinkSourceEnum musicLinkSourceEnum : MusicLinkSourceEnum.values()) {
            List<MusicInfoLink> musicInfoLinkList = mapping.get(musicLinkSourceEnum.getCode());
            MusicInfoLinkDetailVo detailVo = new MusicInfoLinkDetailVo();
            detailVo.setLinkSource(musicLinkSourceEnum.getCode());
            detailVo.setLinkSourceName(musicLinkSourceEnum.getDesc());
            detailVo.setLinkMap(MusicLinkTypeEnum.buildLinkMap(musicInfoLinkList));
            result.add(detailVo);
        }
        return result;
    }

    public void createOrUpdate(Music music, List<MusicInfoLinkDetailVo> linkList) {
        List<MusicInfoLink> musicInfoLinkList = this.list(Wrappers.<MusicInfoLink>lambdaQuery()
                .eq(MusicInfoLink::getMusicId, music.getId()));
        if(CollectionUtils.isEmpty(linkList) && CollectionUtils.isNotEmpty(musicInfoLinkList)) {
            this.removeByIds(DataTool.toList(musicInfoLinkList, MusicInfoLink::getId));
            return;
        }

        Date now = new Date();
        Map<String, MusicInfoLink> mapping = DataTool.toMap(musicInfoLinkList, musicLink ->
                musicLink.getLinkSource() + "_" + MusicLinkTypeEnum.ofCode(musicLink.getLinkType()).name());
        List<MusicInfoLink> willSaveOrUpdateList = new ArrayList<>();

        for (MusicInfoLinkDetailVo detailVo : linkList) {
            Integer linkSource = detailVo.getLinkSource();
            Map<String, String> linkMap = detailVo.getLinkMap();
            linkMap.entrySet().stream().forEach(entry -> {
                String linkTypeEnumName = entry.getKey();
                String linkUrl = entry.getValue();
                String key = linkSource + "_" + linkTypeEnumName;
                MusicInfoLink musicInfoLink = mapping.get(key);
                if(Objects.isNull(musicInfoLink)) {
                    musicInfoLink = new MusicInfoLink();
                    musicInfoLink.setMusicId(music.getId());
                    musicInfoLink.setLinkType(MusicLinkTypeEnum.getMusicLinkTypeEnum(linkTypeEnumName).getCode());
                    musicInfoLink.setLinkSource(linkSource);
                    musicInfoLink.setGmtCreated(now);
                }
                musicInfoLink.setGmtModified(now);
                musicInfoLink.setLinkUrl(linkUrl);
                willSaveOrUpdateList.add(musicInfoLink);
            });
        }

        this.saveOrUpdateBatch(willSaveOrUpdateList);
    }
}
