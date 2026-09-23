package com.somepro.interfaces.rest.pigeon;

import com.somepro.application.pigeon.RaceResultAppService;
import com.somepro.common.Result;
import com.somepro.interfaces.rest.pigeon.converter.PigeonVoConverter;
import com.somepro.interfaces.rest.pigeon.vo.LeaderboardRowVO;
import com.somepro.interfaces.rest.pigeon.vo.PageVO;
import com.somepro.interfaces.rest.pigeon.vo.RecalcSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 成绩与名次榜接口（用户接口层）：算分速出名次、按赛项翻榜。
 */
@RestController
@RequestMapping("/api/pigeon/result")
public class RaceResultController {

    private final RaceResultAppService raceResultAppService;

    public RaceResultController(RaceResultAppService raceResultAppService) {
        this.raceResultAppService = raceResultAppService;
    }

    /**
     * 算分速出名次：一场赛打完出一次成绩，整体覆盖旧成绩（以最新一次为准，不会算出两套）。
     * 例：POST /api/pigeon/result/recalculate?raceId=1001
     */
    @PostMapping("/recalculate")
    public Mono<Result<RecalcSummaryVO>> recalculate(@RequestParam(required = false) Long raceId) {
        return raceResultAppService.recalculate(raceId)
                .map(count -> new RecalcSummaryVO(raceId, count))
                .map(Result::ok);
    }

    /**
     * 名次榜：按赛项分页查，每行足环号/鸽主/归巢时刻/分速/名次，数据与已落库成绩一致。
     * 例：GET /api/pigeon/result/board?raceId=1001&pageNum=1&pageSize=20
     */
    @GetMapping("/board")
    public Mono<Result<PageVO<LeaderboardRowVO>>> board(@RequestParam(required = false) Long raceId,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return raceResultAppService.pageBoard(raceId, pageNum, pageSize)
                .map(PigeonVoConverter::toBoardPageVo)
                .map(Result::ok);
    }
}
