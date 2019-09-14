package brightspark.landmanager.command;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.nonop.*;
import brightspark.landmanager.command.optional.*;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandLM extends LMCommandTree
{
    public CommandLM()
    {
        addSubcommand(new CommandArea());
        addSubcommand(new CommandAreas());
        addSubcommand(new CommandClaim());
        addSubcommand(new CommandShow());
        addSubcommand(new CommandShowOff());
        addSubcommand(new CommandMembers());
        addSubcommand(new CommandMyAreas());
        addSubcommand(new CommandSetOwner());

        if(LMConfig.permissions.passiveSpawning)
            addSubcommand(new CommandPassives());
        if(LMConfig.permissions.hostileSpawning)
            addSubcommand(new CommandHostiles());
        if(LMConfig.permissions.explosions)
            addSubcommand(new CommandExplosions());
        if(LMConfig.permissions.interactions)
            addSubcommand(new CommandInteractions());
        if(LMConfig.permissions.tool)
            addSubcommand(new CommandTool());
        if(LMConfig.permissions.rename)
            addSubcommand(new CommandRename());

        addSubcommand(new CommandOp());
        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName()
    {
        return "lm";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.usage";
    }
}
