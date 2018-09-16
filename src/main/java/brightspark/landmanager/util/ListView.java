package brightspark.landmanager.util;

import java.util.List;

/**
 * Created by bright_spark on 16/09/2018.
 */
public class ListView<T>
{
    private List<T> list;
    private int page, pageMax;

    public ListView(List<T> list, int page, int pageMax)
    {
        this.list = list;
        this.page = page;
        this.pageMax = pageMax;
    }

    public List<T> getList()
    {
        return list;
    }

    public int getPage()
    {
        return page;
    }

    public int getPageMax()
    {
        return pageMax;
    }
}
