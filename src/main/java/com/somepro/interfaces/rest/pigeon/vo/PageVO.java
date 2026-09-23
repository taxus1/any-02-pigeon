package com.somepro.interfaces.rest.pigeon.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 对外分页返回对象（VO，用户接口层）—— 不可变 record。
 * 与 demo 模块的 PageVO 同形（比领域层 PageResult 多一个 totalPages 方便前端渲染分页器）；
 * 这里自带一份而不引用 demo 包 —— demo 是「示例，可删」模块，业务模块不应依赖它。
 */
public record PageVO<T>(List<T> content, long total, int pageNum, int pageSize, int totalPages)
        implements Serializable {
}
