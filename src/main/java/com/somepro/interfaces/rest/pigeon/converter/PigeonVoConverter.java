package com.somepro.interfaces.rest.pigeon.converter;

import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.LeaderboardRow;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.interfaces.rest.pigeon.vo.ClockingVO;
import com.somepro.interfaces.rest.pigeon.vo.LeaderboardRowVO;
import com.somepro.interfaces.rest.pigeon.vo.PageVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 领域对象 → 对外 VO 转换器（用户接口层）。
 * Controller 不许直接把领域对象塞进 Result 返回，一律经这里转成 VO。
 */
public final class PigeonVoConverter {

    private PigeonVoConverter() {
    }

    public static ClockingVO toClockingVo(Clocking domain) {
        return new ClockingVO(domain.getId(), domain.getRaceId(), domain.getEntryId(),
                domain.getClockAt(), domain.getSource() == null ? null : domain.getSource().name());
    }

    public static LeaderboardRowVO toLeaderboardRowVo(LeaderboardRow row) {
        return new LeaderboardRowVO(row.rankNo(), row.bandCode(), row.ownerName(),
                row.clockAt(), row.speedMpm());
    }

    public static PageVO<LeaderboardRowVO> toBoardPageVo(PageResult<LeaderboardRow> page) {
        List<LeaderboardRowVO> content = page.content().stream()
                .map(PigeonVoConverter::toLeaderboardRowVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }
}
