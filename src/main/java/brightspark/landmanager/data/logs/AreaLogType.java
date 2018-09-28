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
    BREAK,
    SET_PASSIVES,
    SET_HOSTILES,
    SET_EXPLOSIONS,
    SET_INTERACTIONS;

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
            case SET_PASSIVES:
            case SET_HOSTILES:
            case SET_EXPLOSIONS:
            case SET_INTERACTIONS:  return TextFormatting.LIGHT_PURPLE;
            default:                return TextFormatting.WHITE;
        }
    }
}
