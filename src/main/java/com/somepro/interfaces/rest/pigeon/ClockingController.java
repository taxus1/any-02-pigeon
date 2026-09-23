package com.somepro.interfaces.rest.pigeon;

import com.somepro.application.pigeon.ClockingAppService;
import com.somepro.common.Result;
import com.somepro.common.exception.BizException;
import com.somepro.interfaces.rest.pigeon.converter.PigeonVoConverter;
import com.somepro.interfaces.rest.pigeon.vo.ClockingVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 归巢报到接口（用户接口层）：只做协议适配（参数解析、时刻格式校验、VO 转换），
 * 业务规则（未集鸽/重复报到/时间窗）在应用层与领域层。
 */
@RestController
@RequestMapping("/api/pigeon")
public class ClockingController {

    private static final DateTimeFormatter CLOCK_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ClockingAppService clockingAppService;

    public ClockingController(ClockingAppService clockingAppService) {
        this.clockingAppService = clockingAppService;
    }

    /**
     * 报到录入：集过鸽的鸽子归巢后记一笔。
     * 例：POST /api/pigeon/clocking?raceId=1001&bandCode=CHN2026-A-123456&clockAt=2026-09-20 12:03:45&source=SCAN
     * 重复报到 / 早于开笼 / 晚于关门 / 未集鸽 / 来源非法 都会被打回并返回具体原因。
     */
    @PostMapping("/clocking")
    public Mono<Result<ClockingVO>> register(@RequestParam(required = false) Long raceId,
                                             @RequestParam(required = false) String bandCode,
                                             @RequestParam(required = false) String clockAt,
                                             @RequestParam(required = false) String source) {
        LocalDateTime clockTime = parseClockAt(clockAt);
        return clockingAppService.register(raceId, bandCode, clockTime, source)
                .map(PigeonVoConverter::toClockingVo)
                .map(Result::ok);
    }

    /** 归巢时刻按 yyyy-MM-dd HH:mm:ss 解析；格式不对给出明确说法，不闷头抛 500。 */
    private LocalDateTime parseClockAt(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BizException("归巢时刻不能为空（格式 yyyy-MM-dd HH:mm:ss）");
        }
        try {
            return LocalDateTime.parse(raw.trim(), CLOCK_AT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BizException("归巢时刻格式应为 yyyy-MM-dd HH:mm:ss，收到：" + raw);
        }
    }
}
