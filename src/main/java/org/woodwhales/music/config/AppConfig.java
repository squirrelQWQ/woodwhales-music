package org.woodwhales.music.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.woodwhales.music.enums.MusicLinkSourceEnum;

/**
 * @author woodwhales on 2023-03-28 12:37
 */
@Data
@Configuration
@Component
public class AppConfig {

    /**
     * 链接来源：0-github，1-alist
     * @see org.woodwhales.music.enums.MusicLinkSourceEnum
     */
    @Value("${music.link.source}")
    public Integer musicLinkSource;

    /**
     * github链接
     */
    @Value("${github.url:https://github.com/woodwhales/woodwhales-music}")
    public String githubUrl;

    /**
     * 是否展示 GitHub Corners
     */
    @Value("${github.show:true}")
    public boolean githubShow;

    public MusicLinkSourceEnum getMusicLinkSourceEnum() {
        return MusicLinkSourceEnum.ofCode(this.getMusicLinkSource());
    }

}
