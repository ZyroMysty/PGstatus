package tree.deku.pgstatus.utils;

import java.util.Collections;
import java.util.List;

public class Pagination {

    public static <T> List<T> getPage(List<T> list, int page, int pageSize) {
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, list.size());

        if (from >= list.size() || from < 0) {
            return Collections.emptyList();
        }

        return list.subList(from, to);
    }

    public static int getTotalPages(int size, int pageSize) {
        return (int) Math.ceil((double) size / pageSize);
    }

}
