package brightspark.landmanager.command.nonop;

import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.util.ListView;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

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
        int page = 0;
        if(args.length > 0)
        {
            try
            {
                page = Integer.valueOf(args[0]);
            }
            catch(NumberFormatException ignored)
            {
            }
        }

        //Show list of all recipes in pages
        List<Area> areas = getAllAreas(server);
        if(areas.size() == 0)
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.areas.none"));
            return;
        }

        ListView<Area> view = getListView(areas, page, 9);
        page = view.getPage();

        //Create the String to send to the player
        ITextComponent text = new TextComponentString(TextFormatting.YELLOW + "============= ");
        ITextComponent titleText = new TextComponentTranslation("lm.command.areas.title", (page + 1), (view.getPageMax() + 1));
        titleText.getStyle().setColor(TextFormatting.GOLD);
        text.appendSibling(titleText);
        text.appendText(TextFormatting.YELLOW + " =============");
        for(Area area : view.getList())
        {
            String playerName = getPlayerNameFromUuid(server, area.getAllocatedPlayer());
            if(playerName == null)
                text.appendText("\n  " + area.getName());
            else
                text.appendText(String.format("\n  %s -> %s", area.getName(), playerName));
        }
        sender.sendMessage(text);
    }
}
