package brightspark.landmanager.data.logs;

import java.util.ArrayList;
import java.util.List;

public class AreaLogList
{
    private List<AreaLog> logs = new ArrayList<>();
    private final int length;

    public AreaLogList(int length)
    {
        this.length = length;
    }

    public void add(AreaLog log)
    {
        logs.add(log);
        if(logs.size() > length)
            logs.remove(0);
    }

    public List<AreaLog> getLogs()
    {
        return logs;
    }

    public void clear()
    {
        logs.clear();
    }
}
