package brightspark.landmanager.data.logs;

import java.util.LinkedList;

public class AreaLogList
{
    private LinkedList<AreaLog> logs = new LinkedList<>();
    private final int length;

    public AreaLogList(int length)
    {
        this.length = length;
    }

    public void add(AreaLog log)
    {
        logs.addLast(log);
        if(logs.size() > length)
            logs.removeFirst();
    }

    public LinkedList<AreaLog> getLogs()
    {
        return logs;
    }

    public void clear()
    {
        logs.clear();
    }
}
