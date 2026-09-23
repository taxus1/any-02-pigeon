package com.somepro.interfaces.rest.pigeon.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 对外分页返回对象（接口层）。totalPages 作为派生字段留在接口层，
 * 不污染零框架依赖的领域 PageResult。
 */
public record PageVO<T>(List<T> content, long total, int pageNum, int pageSize, int totalPages)
        implements Serializable {
}
