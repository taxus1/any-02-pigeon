package com.somepro.application.pigeon;

import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.Race;
import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.model.RankRow;
import com.somepro.domain.pigeon.repository.ClockingRepository;
import com.somepro.domain.pigeon.repository.RaceRepository;
import com.somepro.domain.pigeon.repository.RaceResultRepository;
import com.somepro.domain.pigeon.service.RaceResultDomainService;
import com.somepro.domain.shared.model.PageResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 成绩与名次榜用例（应用层）。
 *
 * - scoreRace：一场赛打完出一次成绩。重算走仓储的「整事务先删后插」，
 *   库里永远只有最新一套 t_result，不会算出两套；
 * - pageRank：按赛项翻页查名次榜，数据直接来自 t_result 联表，榜上的数与库里成绩同源。
 */
@Service
public class RaceResultAppService {

    private final RaceRepository raceRepository;
    private final ClockingRepository clockingRepository;
    private final RaceResultRepository raceResultRepository;
    private final RaceResultDomainService resultDomainService;

    public RaceResultAppService(RaceRepository raceRepository,
                                ClockingRepository clockingRepository,
                                RaceResultRepository raceResultRepository,
                                RaceResultDomainService resultDomainService) {
        this.raceRepository = raceRepository;
        this.clockingRepository = clockingRepository;
        this.raceResultRepository = raceResultRepository;
        this.resultDomainService = resultDomainService;
    }

    /**
     * 计算（或重算）一场赛的成绩。
     *
     * @return 最新成绩列表（名次顺序）；一场尚无归巢的赛项返回空列表（旧成绩会被清空，
     *         因为「以最新一次为准」）
     */
    public Mono<List<RaceResult>> scoreRace(Long raceId) {
        if (raceId == null) {
            return Mono.error(new BizException("赛项 id 不能为空"));
        }
        return raceRepository.findById(raceId)
                .switchIfEmpty(Mono.error(new BizException("赛项不存在：" + raceId)))
                .flatMap(this::doScore);
    }

    private Mono<List<RaceResult>> doScore(Race race) {
        return clockingRepository.listByRace(race.getId())
                .map(clockings -> resultDomainService.compute(race, clockings))
                .flatMap(fresh -> raceResultRepository.replaceForRace(race.getId(), fresh));
    }

    /**
     * 名次榜分页。赛项不存在给明确说法；未出过成绩则是空榜。
     */
    public Mono<PageResult<RankRow>> pageRank(Long raceId, int pageNum, int pageSize) {
        if (raceId == null) {
            return Mono.error(new BizException("赛项 id 不能为空"));
        }
        if (pageNum < 1 || pageSize < 1) {
            return Mono.error(new BizException("页码与每页条数必须为正整数"));
        }
        return raceRepository.findById(raceId)
                .switchIfEmpty(Mono.error(new BizException("赛项不存在：" + raceId)))
                .then(raceResultRepository.pageRank(raceId, pageNum, pageSize));
    }
}
