package brightspark.landmanager.command;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.op.*;
import brightspark.landmanager.command.optional.*;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandOp extends CommandTreeBase
{
    public CommandOp()
    {
        addSubcommand(new CommandDelete());
        addSubcommand(new CommandAllocate());
        addSubcommand(new CommandClearAllocation());
        addSubcommand(new CommandRequests());
        addSubcommand(new CommandApprove());
        addSubcommand(new CommandDisapprove());

        if(!LMConfig.permissions.passiveSpawning)
            addSubcommand(new CommandPassives());
        if(!LMConfig.permissions.hostileSpawning)
            addSubcommand(new CommandHostiles());
        if(!LMConfig.permissions.explosions)
            addSubcommand(new CommandExplosions());
        if(!LMConfig.permissions.interactions)
            addSubcommand(new CommandInteractions());
        if(!LMConfig.permissions.tool)
            addSubcommand(new CommandTool());
        if(!LMConfig.permissions.rename)
            addSubcommand(new CommandRename());

        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName()
    {
        return "op";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.op.usage";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
}
