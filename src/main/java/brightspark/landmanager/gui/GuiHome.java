package brightspark.landmanager.gui;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.handler.ClientEventHandler;
import brightspark.landmanager.message.MessageHomeActionAdd;
import brightspark.landmanager.message.MessageHomeActionKickOrPass;
import brightspark.landmanager.message.MessageHomeToggle;
import brightspark.landmanager.util.HomeGuiActionType;
import brightspark.landmanager.util.HomeGuiToggleType;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class GuiHome extends LMGui
{
	private static final int PLAYER_LIST_SIZE = 4;
	private static final Rectangle ownerIcon = new Rectangle(162, 110, 7, 7);

	private boolean clientIsOp = false;
	private boolean isOwner;
	private BlockPos pos;
	private Area area = null;
	private GuiTextField input;
	private int playerListStartIndex = 0;
	private Pair<UUID, String> owner;
	private List<Pair<UUID, String>> members;

	private List<ListButton> listButtons = new ArrayList<>(PLAYER_LIST_SIZE);
	private int selectedMemberIndex = -1;
	private ArrowButton upButton, downButton;
	private ActionButton addButton, kickButton, passButton;
	private ToggleButton boundariesToggle, interactionsToggle, passivesToggle, hostilesToggle, explosionsToggle;

	private String errorMessage = null;
	private String[] errorMessageArgs = null;

	public GuiHome(World world, BlockPos pos)
	{
		super("gui_home", 162, 144);
		this.pos = pos;
		CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
		if(cap != null)
			area = cap.intersectingArea(pos);
	}

	public void setClientIsOp()
	{
		clientIsOp = true;
		updateToggleButtons();
	}

	/**
	 * Used by {@link brightspark.landmanager.message.MessageOpenHomeGui} to set the members data
	 */
	public void setMembersData(Pair<UUID, String> owner, List<Pair<UUID, String>> members)
	{
		this.owner = owner;
		this.members = members;
		this.members.add(owner);
		updatePlayerList();
	}

	public void addMember(UUID uuid, String player)
	{
		members.add(new ImmutablePair<>(uuid, player));
		updatePlayerList();
	}

	public void removeMember(UUID player)
	{
		members.removeIf(pair -> pair.getLeft().equals(player));
		playerListStartIndex = MathHelper.clamp(playerListStartIndex, 0, Math.max(0, members.size() - PLAYER_LIST_SIZE));
		updatePlayerList();
	}

	public void setToggle(HomeGuiToggleType type, boolean state)
	{
		switch(type)
		{
			case INTERACTIONS:
				area.setInteractions(state);
				break;
			case PASSIVE_SPAWNS:
				area.setPassiveSpawning(state);
				break;
			case HOSTILE_SPAWNS:
				area.setHostileSpawning(state);
				break;
			case EXPLOSIONS:
				area.setExplosions(state);
		}
		updateToggleButtons();
	}

	public void clearInput()
	{
		input.setText("");
	}

	public void clearSelection()
	{
		selectedMemberIndex = -1;
	}

	public void setErrorMessage(String errorMessage, String[] args)
	{
		this.errorMessage = errorMessage;
		this.errorMessageArgs = args;
	}

	private void clearErrorMessage()
	{
		if(errorMessage != null)
		{
			errorMessage = null;
			errorMessageArgs = null;
		}
	}

	private boolean canUseToggle(boolean config)
	{
		return clientIsOp || (config && isOwner);
	}

	@Override
	public void initGui()
	{
		super.initGui();

		isOwner = area.isOwner(mc.player.getUniqueID());

		input = new GuiTextField(0, fontRenderer, guiLeft + 99, guiTop + 15, 56, 10);
		input.setEnableBackgroundDrawing(false);

		listButtons.clear();
		for(int i = 0; i < PLAYER_LIST_SIZE; i++)
			listButtons.add(addButton(new ListButton(7, 16 + (i * 12))));

		upButton = addButton(new ArrowButton(97, 29, true));
		downButton = addButton(new ArrowButton(97, 52, false));
		updatePlayerList();

		addButton = addButton(new ActionButton(111, 29, "gui.home.add", HomeGuiActionType.ADD));
		kickButton = addButton(new ActionButton(111, 41, "gui.home.kick", HomeGuiActionType.KICK));
		passButton = addButton(new ActionButton(111, 53, "gui.home.pass", HomeGuiActionType.PASS));
		updateActionButtons();

		boundariesToggle = addButton(new ToggleButton(HomeGuiToggleType.BOUNDARIES, 108, 70, ClientEventHandler.isAreaBeingRendered(area.getName())));
		boundariesToggle.enabled = true;
		interactionsToggle = addButton(new ToggleButton(HomeGuiToggleType.INTERACTIONS, 108, 84, area.canInteract()));
		passivesToggle = addButton(new ToggleButton(HomeGuiToggleType.PASSIVE_SPAWNS, 108, 98, area.canPassiveSpawn()));
		hostilesToggle = addButton(new ToggleButton(HomeGuiToggleType.HOSTILE_SPAWNS, 108, 112, area.canHostileSpawn()));
		explosionsToggle = addButton(new ToggleButton(HomeGuiToggleType.EXPLOSIONS, 108, 126, area.canExplosionsCauseDamage()));
		updateToggleButtons();
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void drawText()
	{
		input.drawTextBox();

		int colour = 4210752;
		drawStringWithMaxWidth(area.getName(), 6 + guiLeft, 5 + guiTop, 150, colour, false);

		int x = 7 + guiLeft;
		drawLangString("gui.home.boundaries", x, 72 + guiTop, colour, false);
		drawLangString("gui.home.interactions", x, 86 + guiTop, colour, false);
		drawLangString("gui.home.passives", x, 100 + guiTop, colour, false);
		drawLangString("gui.home.hostiles", x, 114 + guiTop, colour, false);
		drawLangString("gui.home.explosions", x, 128 + guiTop, colour, false);

		if (errorMessage != null)
			drawCenteredString(I18n.format(errorMessage, (Object[]) errorMessageArgs), width / 2, guiTop - 15, 0xFF0000);
	}

	@Override
	public void updateScreen()
	{
		if(input != null)
			input.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if(input.textboxKeyTyped(typedChar, keyCode))
		{
			clearErrorMessage();
			updateActionButtons();
		}
		else if(keyCode == Keyboard.KEY_RETURN && input.isFocused() && StringUtils.isNotBlank(input.getText()))
			LandManager.NETWORK.sendToServer(new MessageHomeActionAdd(pos, input.getText()));
		else if(keyCode == Keyboard.KEY_ESCAPE || mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
			mc.player.closeScreen();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if(input.mouseClicked(mouseX, mouseY, mouseButton))
		{
			listButtons.forEach(listButton -> listButton.setSelected(false));
			selectedMemberIndex = -1;
			updateActionButtons();
			clearErrorMessage();
		}
		if(mouseX >= input.x && mouseX < input.x + input.width && mouseY >= input.y && mouseY < input.y + input.height && mouseButton == 1)
		{
			input.setText("");
			clearErrorMessage();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		clearErrorMessage();

		if(button instanceof ListButton)
		{
			listButtons.forEach(listButton -> listButton.setSelected(false));
			ListButton listButton = (ListButton) button;
			listButton.setSelected(true);
			selectedMemberIndex = listButton.id + playerListStartIndex;
			updateActionButtons();
		}
		else if(button instanceof ArrowButton)
		{
			int change = ((ArrowButton) button).isUp() ? -1 : 1;
			playerListStartIndex += change;
			updatePlayerList();
		}
		else if(button instanceof ActionButton)
		{
			HomeGuiActionType type = ((ActionButton) button).type;
			switch(type)
			{
				case KICK:
				case PASS:
					UUID memberUuid = selectedMemberIndex >= 0 ? members.get(selectedMemberIndex).getLeft() : null;
					if(memberUuid != null && !memberUuid.equals(owner.getLeft()))
						LandManager.NETWORK.sendToServer(new MessageHomeActionKickOrPass(pos, type == HomeGuiActionType.PASS, memberUuid));
					break;
				case ADD:
					String inputText = input.getText();
					if(StringUtils.isNotBlank(inputText))
						LandManager.NETWORK.sendToServer(new MessageHomeActionAdd(pos, inputText));
			}
		}
		else if(button instanceof ToggleButton)
		{
			ToggleButton toggleButton = (ToggleButton) button;
			switch(toggleButton.type)
			{
				case BOUNDARIES:
					toggleButton.isOn = !toggleButton.isOn;
					ClientEventHandler.setRenderArea(area.getName(), toggleButton.isOn);
					break;
				case INTERACTIONS:
				case PASSIVE_SPAWNS:
				case HOSTILE_SPAWNS:
				case EXPLOSIONS:
					LandManager.NETWORK.sendToServer(new MessageHomeToggle(pos, toggleButton.type));
			}
		}
		super.actionPerformed(button);
	}

	private boolean canScrollUp()
	{
		return playerListStartIndex > 0;
	}

	private boolean canScrollDown()
	{
		return members.size() > playerListStartIndex + PLAYER_LIST_SIZE;
	}

	private void updatePlayerList()
	{
		if(members == null)
			return;

		//Update members list
		members.sort(Comparator.comparing(Pair::getRight, String::compareToIgnoreCase));
		int membersSize = members.size();
		for(int i = 0; i < PLAYER_LIST_SIZE; i++)
		{
			int playerListI = playerListStartIndex + i;
			ListButton button = listButtons.get(i);
			button.setPlayer(playerListI < membersSize ? members.get(playerListI) : null);
			button.setSelected(selectedMemberIndex == playerListI);
		}

		//Update arrows
		upButton.enabled = canScrollUp();
		downButton.enabled = canScrollDown();
	}

	private void updateActionButtons()
	{
		addButton.enabled = StringUtils.isNotBlank(input.getText());
		kickButton.enabled = passButton.enabled = selectedMemberIndex >= 0 && !members.get(selectedMemberIndex).getLeft().equals(owner.getLeft());
	}

	private void updateToggleButtons()
	{
		interactionsToggle.enabled = canUseToggle(LMConfig.permissions.interactions);
		interactionsToggle.isOn = area.canInteract();
		passivesToggle.enabled = canUseToggle(LMConfig.permissions.passiveSpawning);
		passivesToggle.isOn = area.canPassiveSpawn();
		hostilesToggle.enabled = canUseToggle(LMConfig.permissions.hostileSpawning);
		hostilesToggle.isOn = area.canHostileSpawn();
		explosionsToggle.enabled = canUseToggle(LMConfig.permissions.explosions);
		explosionsToggle.isOn = area.canExplosionsCauseDamage();
	}

	private class ListButton extends LMButton
	{
		private boolean isOwner = false;

		ListButton(int x, int y)
		{
			super(x, y, 87, 11, 87, 144, null);
			textOffset = 1;
			drawWhenDisabled = false;
			hasIcon = false;
		}

		@Override
		protected int getTextColour()
		{
			return 14737632;
		}

		public void setPlayer(Pair<UUID, String> player)
		{
			if(player == null)
			{
				displayString = null;
				enabled = false;
				isOwner = false;
				tooltip = null;
			}
			else
			{
				displayString = player.getRight();
				enabled = true;
				isOwner = player.getLeft().equals(owner.getLeft());
				textOffset = isOwner ? 12 : 1;
				tooltip = isOwner ?
					Lists.newArrayList(TextFormatting.GOLD + I18n.format("gui.home.owner"), displayString) :
					Collections.singletonList(displayString);
			}
		}

		public void setSelected(boolean selected)
		{
			hasIcon = selected;
		}

		@Override
		protected void drawText()
		{
			if(isOwner)
			{
				mc.getTextureManager().bindTexture(image);
				drawTexturedModalRect(x + 1, y + 1, ownerIcon.x, ownerIcon.y, ownerIcon.width, ownerIcon.height);
			}
			drawStringWithMaxWidth(displayString, x + textOffset, y + (height - 8) / 2, 85 - textOffset, getTextColour(), true);
		}
	}

	private class ArrowButton extends LMButton
	{
		private boolean isUp;

		ArrowButton(int x, int y, boolean isUp)
		{
			super(x, y, 10, 13, 162, 84, null);
			this.isUp = isUp;
		}

		public boolean isUp()
		{
			return isUp;
		}

		@Override
		protected int getIconY()
		{
			return isUp ? iconY : iconY + height;
		}
	}

	private class ActionButton extends LMButton
	{
		private final HomeGuiActionType type;

		ActionButton(int x, int y, String buttonText, HomeGuiActionType type)
		{
			super(x, y, 45, 12, 162, 0, I18n.format(buttonText));
			this.type = type;
			textOffset = 12;
		}

		@Override
		protected int getIconY()
		{
			return iconY + (type.ordinal() * height);
		}
	}

	private class ToggleButton extends LMButton
	{
		public final HomeGuiToggleType type;
		public boolean isOn;

		ToggleButton(HomeGuiToggleType type, int x, int y, boolean isOn)
		{
			super(x, y, 12, 12, 162, 36, null);
			this.enabled = false;
			this.type = type;
			this.isOn = isOn;
			drawWhenDisabled = true;
		}

		@Override
		protected int getIconY()
		{
			int y = iconY;
			if(!enabled)
				y += height * 2;
			if(isOn)
				y += height;
			return y;
		}
	}
}
