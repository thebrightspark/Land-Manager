package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

//lm areas [page]
public class CommandAreas extends LMCommand
{
    @Override
    public String getName()
    {
        return "areas";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.areas.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        int page = args.length > 0 ? getPageFromArg(args[0], 0) : 0;

        //Show list of all recipes in pages
        List<Area> areas = getAllAreas(server);
        if(areas.size() == 0)
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.areas.none"));
            return;
        }

        sender.sendMessage(createListMessage(sender, areas, area -> {
                String playerName = getPlayerNameFromUuid(server, area.getAllocatedPlayer());
                if(playerName == null)
                    return "  " + area.getName();
                else
                    return String.format("  %s -> %s", area.getName(), playerName);
            }, page, "lm.command.areas.title", pageNum -> "/lm areas " + pageNum));
    }
}
