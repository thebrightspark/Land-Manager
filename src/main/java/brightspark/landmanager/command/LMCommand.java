package brightspark.landmanager.command;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.requests.RequestsWorldSavedData;
import brightspark.landmanager.util.ListView;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class LMCommand extends CommandBase
{
    protected CapabilityAreas getWorldCap(World world) throws CommandException
    {
        CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null)
            throw new CommandException("lm.command.data", world.provider.getDimension());
        return cap;
    }

    protected CapabilityAreas getWorldCapForPlayer(EntityPlayer player) throws CommandException
    {
        return getWorldCap(player.world);
    }

    protected Pair<CapabilityAreas, Area> getAreaAndCap(MinecraftServer server, String areaName) throws CommandException
    {
        if(areaName == null)
            throw new WrongUsageException("lm.command.areaName");
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap == null)
                continue;
            Area area = cap.getArea(areaName);
            if(area != null)
                return new ImmutablePair<>(cap, area);
        }
        throw new CommandException("lm.command.none", areaName);
    }

    protected Pair<CapabilityAreas, Area> getAreaAndCapNoException(MinecraftServer server, @Nonnull String areaName)
    {
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap == null)
                continue;
            Area area = cap.getArea(areaName);
            if(area != null)
                return new ImmutablePair<>(cap, area);
        }
        return null;
    }

    protected RequestsWorldSavedData getRequestsData(MinecraftServer server) throws CommandException
    {
        RequestsWorldSavedData data = RequestsWorldSavedData.get(server.getEntityWorld());
        if(data == null)
            throw new CommandException("lm.command.reqdata");
        return data;
    }

    protected List<Area> getAllAreas(MinecraftServer server)
    {
        List<Area> areas = new LinkedList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null)
                areas.addAll(cap.getAllAreas());
        }
        areas.sort(Comparator.comparing(Area::getName));
        return areas;
    }

    protected List<Area> getAreasByNameRegex(MinecraftServer server, String regex)
    {
        Pattern pattern = Pattern.compile(regex);
        List<Area> areas = new LinkedList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null)
                for(Area area : cap.getAllAreas())
                    if(pattern.matcher(area.getName()).matches())
                        areas.add(area);
        }
        areas.sort(Comparator.comparing(Area::getName));
        return areas;
    }

    protected List<String> getAllAreaNames(MinecraftServer server)
    {
        List<String> areaNames = new LinkedList<>();
        for(WorldServer world : server.worlds)
        {
            CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
            if(cap != null)
                areaNames.addAll(cap.getAllAreaNames());
        }
        areaNames.sort(Comparator.naturalOrder());
        return areaNames;
    }

    protected String getPlayerNameFromUuid(MinecraftServer server, UUID uuid)
    {
        String playerName = null;
        if(uuid != null)
        {
            GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
            if(profile != null)
                playerName = profile.getName();
        }
        return playerName;
    }

    protected UUID getUuidFromPlayerName(MinecraftServer server, String playerName) throws CommandException
    {
        GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        if(profile != null)
            return profile.getId();
        throw new CommandException("lm.command.noplayer", playerName);
    }

    protected String posToString(BlockPos pos)
    {
        return String.format("%sX: %s%s, %sY: %s%s, %sZ: %s%s", TextFormatting.YELLOW, TextFormatting.RESET, pos.getX(), TextFormatting.YELLOW, TextFormatting.RESET, pos.getY(), TextFormatting.YELLOW, TextFormatting.RESET, pos.getZ());
    }

    protected String argsToString(String[] args)
    {
        return argsToString(args, 0);
    }

    protected String argsToString(String[] args, int startIndex)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = startIndex; i < args.length; i++)
            sb.append(args[i]).append(" ");
        return sb.toString().trim();
    }

    protected ITextComponent goldTextComponent(String text, Object... args)
    {
        return textComponentWithColour(TextFormatting.GOLD, text, args);
    }

    protected ITextComponent textComponentWithColour(TextFormatting colour, String text, Object... args)
    {
        ITextComponent textComponent = new TextComponentTranslation(text, args);
        textComponent.getStyle().setColor(colour);
        return textComponent;
    }

    protected TextComponentTranslation booleanToText(boolean bool)
    {
        return new TextComponentTranslation(bool ? "message.misc.true" : "message.misc.false");
    }

    protected void throwWrongUsage(ICommandSender sender) throws WrongUsageException
    {
        throw new WrongUsageException(getUsage(sender));
    }

    protected boolean isOP(MinecraftServer server, ICommandSender sender)
    {
        if(!(sender instanceof EntityPlayer))
            return false;
        UserListOpsEntry op = server.getPlayerList().getOppedPlayers().getEntry(((EntityPlayer) sender).getGameProfile());
        return op != null;
    }

    protected void checkCanEditArea(MinecraftServer server, ICommandSender sender, Area area) throws CommandException
    {
        if(!isOP(server, sender) && !area.isOwner(((EntityPlayer) sender).getUniqueID()))
            throw new CommandException("lm.command.noPerm", area.getName());
    }

    protected void validateSenderIsPlayer(ICommandSender sender) throws CommandException
    {
        if(!(sender instanceof EntityPlayer))
            throw new CommandException("lm.command.player");
    }

    protected Integer parseIntWithDefault(String arg)
    {
        return parseIntWithDefault(arg, null);
    }

    protected Integer parseIntWithDefault(String arg, Integer pageDefault)
    {
        try
        {
            return Integer.valueOf(arg);
        }
        catch(NumberFormatException e)
		{
			return pageDefault;
		}
    }

    //----====|||||||||||||====----//
    //----==== Paged Lists ====----//
    //----====|||||||||||||====----//

    protected <T> ITextComponent createListMessage(ICommandSender sender, List<T> list, @Nullable Function<T, String> entryToString, int page, String titleKey, Function<Integer, String> arrowsCommandToRun)
    {
        if(entryToString == null)
            entryToString = T::toString;
        Function<T, String> finalEntryToString = entryToString;

        //Get the list of exactly what to show
        ListView<T> view = getListView(list, page, 8);
        page = view.getPage();
        int maxPage = view.getPageMax();

        boolean isPlayer = sender instanceof EntityPlayer;

        //Create the text to send back
        ITextComponent text;
        if(isPlayer)
            text = createPageTitle(titleKey, page, maxPage);
        else
        {
            //Print on a new line in the server console for readability
            text = new TextComponentString("\n");
            text.appendSibling(createPageTitle(titleKey, page, maxPage));
        }

        view.getList().forEach(entry -> text.appendText("\n").appendText(finalEntryToString.apply(entry)));

        //Don't need to add the arrows when sending back to a server console
        if(isPlayer)
        {
            ITextComponent arrows = createPageArrows(page, maxPage, arrowsCommandToRun);
            if(arrows != null)
                text.appendText("\n").appendSibling(arrows);
        }
        return text;
    }

    private <T> ListView<T> getListView(List<T> list, int page, int maxPerPage)
    {
        page = Math.max(0, page);
        maxPerPage = Math.max(1, maxPerPage);
        int size = list.size();
        //Need to add 1 to maxPerPage so that we don't have an empty extra page when size == maxPerPage
        int pageMax = size / (maxPerPage + 1);
        //We reduce the given page number by 1, because we calculate starting from page 0, but is shown to start from page 1.
        if(page > 0)
            page--;
        if(page * maxPerPage > size)
            page = pageMax;
        //Work out the range to get from the list
        int min = page * maxPerPage;
        int max = min + maxPerPage;
        if(size < max)
            max = size;

        return new ListView<>(list.subList(min, max), page, pageMax);
    }

    private ITextComponent createPageTitle(String titleTranslationKey, int curPage, int maxPage)
    {
        ITextComponent text = new TextComponentString(TextFormatting.YELLOW + "============= ");
        ITextComponent titleText = new TextComponentTranslation(titleTranslationKey, (curPage + 1), (maxPage + 1));
        titleText.getStyle().setColor(TextFormatting.GOLD);
        text.appendSibling(titleText);
        text.appendText(TextFormatting.YELLOW + " =============");
        return text;
    }

    private static final int paddingAmount = 2;
    private static final String padding = StringUtils.repeat(' ', paddingAmount);
    private static final int arrowSize = 4;
    private static final String arrowLeft = StringUtils.repeat('<', arrowSize);
    private static final String arrowRight = StringUtils.repeat('>', arrowSize);
    private static final String blank = StringUtils.repeat('-', arrowSize);

    private ITextComponent createPageArrows(int curPage, int pageMax, Function<Integer, String> commandToRun)
    {
        ITextComponent text = null;
        if(curPage > 0 || curPage < pageMax)
        {
            //Add arrows at the bottom so the player can easily click between pages
            text = new TextComponentString(padding);
            if(curPage > 0)
                //Left arrow
                text.appendSibling(createArrow(true, curPage, commandToRun));
            else
                text.appendSibling(createBlank());

            text.appendText(padding);

            if(curPage < pageMax)
                //Right arrow
                text.appendSibling(createArrow(false, curPage, commandToRun));
            else
                text.appendSibling(createBlank());
        }
        return text;
    }

    private ITextComponent createArrow(boolean left, int curPage, Function<Integer, String> commandToRun)
    {
        int nextPage = left ? curPage : curPage + 2;
        ITextComponent arrow = new TextComponentString(left ? arrowLeft : arrowRight);
        arrow.getStyle()
            .setBold(true)
            .setColor(TextFormatting.YELLOW)
            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("lm.command.page", nextPage)))
            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToRun.apply(nextPage)));
        return arrow;
    }

    private ITextComponent createBlank()
    {
        ITextComponent text = new TextComponentString(blank);
        text.getStyle().setColor(TextFormatting.GOLD);
        return text;
    }
}
