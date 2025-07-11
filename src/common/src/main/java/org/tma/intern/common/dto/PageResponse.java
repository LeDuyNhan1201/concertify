package org.tma.intern.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<TData> {

    @Builder.Default
    List<TData> items = new ArrayList<>();

    @Builder.Default
    int limit = 3;

    long totalPages;

    Integer previousPage;

    Integer nextPage;

    public static <T> PageResponse<T> of(List<T> items, int pageIndex, int limit, long totalItems) {
        long totalPages = (long) Math.ceil((double) totalItems / limit);

        Integer previousPage = (pageIndex > 0) ? pageIndex - 1 : null;
        Integer nextPage = (pageIndex + 1 < totalPages) ? pageIndex + 1 : null;

        return PageResponse.<T>builder()
                .items(items)
                .limit(limit)
                .totalPages(totalPages)
                .previousPage(previousPage)
                .nextPage(nextPage)
                .build();
    }

}
