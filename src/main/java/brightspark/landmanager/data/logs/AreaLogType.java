package brightspark.landmanager.data.logs;

import net.minecraft.util.text.TextFormatting;

public enum AreaLogType
{
    CREATE,
    DELETE,
    ALLOCATE,
    CLEAR_ALLOCATION,
    CLAIM,
    PLACE,
    BREAK;

    public String getUnlocalisedName()
    {
        return "arealog.type." + name().toLowerCase();
    }

    public TextFormatting colour()
    {
        switch(this)
        {
            case CREATE:            return TextFormatting.GREEN;
            case DELETE:            return TextFormatting.RED;
            case ALLOCATE:          return TextFormatting.DARK_AQUA;
            case CLEAR_ALLOCATION:  return TextFormatting.DARK_BLUE;
            case CLAIM:             return TextFormatting.AQUA;
            case PLACE:             return TextFormatting.YELLOW;
            case BREAK:             return TextFormatting.GOLD;
            default:                return TextFormatting.WHITE;
        }
    }
}
