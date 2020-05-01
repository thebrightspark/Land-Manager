package brightspark.landmanager.gui

import brightspark.landmanager.LMConfig
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.handler.ClientEventHandler
import brightspark.landmanager.message.MessageHomeActionAdd
import brightspark.landmanager.message.MessageHomeActionKickOrPass
import brightspark.landmanager.message.MessageHomeToggle
import brightspark.landmanager.util.HomeGuiActionType
import brightspark.landmanager.util.HomeGuiActionType.*
import brightspark.landmanager.util.HomeGuiToggleType
import brightspark.landmanager.util.HomeGuiToggleType.*
import brightspark.landmanager.util.areasCap
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import org.lwjgl.glfw.GLFW
import java.awt.Rectangle
import java.util.*
import kotlin.math.max

class HomeScreen(player: PlayerEntity, val pos: BlockPos) : LMScreen("Home", "gui_home", 162, 144) {
	companion object {
		private const val PLAYER_LIST_SIZE = 4
		private val ownerIcon = Rectangle(162, 110, 7, 7)
	}

	private var clientIsOp = false
	private var isOwner = false
	private val area: Area = player.world.areasCap.intersectingArea(pos)!!

	private lateinit var inputTextField: TextFieldWidget
	private var playerListStartIndex = 0
	private var owner: Pair<UUID, String>? = null
	private var members: MutableList<Pair<UUID, String>> = mutableListOf()

	private lateinit var listButtons: Array<ListButton>
	private var selectedMemberIndex = -1
	private lateinit var upButton: ArrowButton
	private lateinit var downButton: ArrowButton
	private lateinit var addButton: ActionButton
	private lateinit var kickButton: ActionButton
	private lateinit var passButton: ActionButton
	private lateinit var boundariesToggle: ToggleButton
	private lateinit var interactionsToggle: ToggleButton
	private lateinit var passivesToggle: ToggleButton
	private lateinit var hostilesToggle: ToggleButton
	private lateinit var explosionsToggle: ToggleButton

	var errorMessage: TranslationTextComponent? = null

	private fun isOwner(uuid: UUID): Boolean = owner?.first == uuid

	private fun canUseToggle(config: Boolean): Boolean = clientIsOp || (config && isOwner)

	private fun canScrollUp(): Boolean = playerListStartIndex > 0

	private fun canScrollDown(): Boolean = members.size > playerListStartIndex + PLAYER_LIST_SIZE

	private fun updatePlayerList() {
		if (members.isEmpty())
			return
		members.sortWith(Comparator.comparing(Pair<UUID, String>::second) { s1, s2 -> s1.compareTo(s2, true) })
		val size = members.size

		// Update members list
		listButtons.forEachIndexed { i, button ->
			val playerListI = playerListStartIndex + i
			button.setPlayer(if (playerListI < size) members[playerListI] else null)
			button.setSelected(selectedMemberIndex == playerListI)
		}

		// Update arrows
		upButton.active = canScrollUp()
		downButton.active = canScrollDown()
	}

	private fun updateActionButtons() {
		addButton.active = inputTextField.text.isNotBlank()
		kickButton.active = selectedMemberIndex >= 0 && !isOwner(members[selectedMemberIndex].first)
		passButton.active = kickButton.active
	}

	private fun updateToggleButtons() {
		interactionsToggle.setActive(canUseToggle(LMConfig.interactions))
		interactionsToggle.isOn = area.interactions
		passivesToggle.setActive(canUseToggle(LMConfig.passiveSpawning))
		passivesToggle.isOn = area.canPassiveSpawn
		hostilesToggle.setActive(canUseToggle(LMConfig.hostileSpawning))
		hostilesToggle.isOn = area.canHostileSpawn
		explosionsToggle.setActive(canUseToggle(LMConfig.explosions))
		explosionsToggle.isOn = area.explosions
	}

	fun clearInput() {
		inputTextField.text = ""
	}

	fun clearSelection() {
		selectedMemberIndex = -1
	}

	fun setClientIsOp() {
		clientIsOp = true
	}

	// Used by MessageOpenHomeGui to set the members data
	fun setMembersData(owner: Pair<UUID, String>?, members: List<Pair<UUID, String>>) {
		this.owner = owner
		this.members = members.toMutableList().apply { owner?.let { add(it) } }
		updatePlayerList()
	}

	fun addMember(uuid: UUID, player: String) {
		members.add(uuid to player)
		updatePlayerList()
	}

	fun removeMember(uuid: UUID) {
		members.removeIf { it.first == uuid }
		playerListStartIndex = MathHelper.clamp(playerListStartIndex, 0, max(0, members.size - PLAYER_LIST_SIZE))
		updatePlayerList()
	}

	fun setToggle(type: HomeGuiToggleType, state: Boolean) {
		when (type) {
			INTERACTIONS -> area.interactions = state
			PASSIVE_SPAWNS -> area.canPassiveSpawn = state
			HOSTILE_SPAWNS -> area.canHostileSpawn = state
			EXPLOSIONS -> area.explosions = state
			else -> Unit
		}
		updateToggleButtons()
	}

	private fun onListButtonPress(button: ListButton) {
		errorMessage = null

		listButtons.forEach {
			if (it == button) {
				it.setSelected(true)
				selectedMemberIndex = it.num + playerListStartIndex
			} else
				it.setSelected(false)
		}
		updateActionButtons()
	}

	private fun onArrowButtonPress(button: ArrowButton) {
		errorMessage = null

		playerListStartIndex += if (button.isUp) -1 else 1
		updatePlayerList()
	}

	private fun onActionButtonPress(button: ActionButton) {
		errorMessage = null

		when (button.type) {
			KICK, PASS -> {
				if (selectedMemberIndex < 0)
					return
				val uuid = members[selectedMemberIndex].first
				if (isOwner(uuid))
					LandManager.NETWORK.sendToServer(MessageHomeActionKickOrPass(pos, button.type == PASS, uuid))
			}
			ADD -> addMember(inputTextField.text)
		}
	}

	private fun addMember(name: String) {
		if (name.isNotBlank())
			LandManager.NETWORK.sendToServer(MessageHomeActionAdd(pos, name))
	}

	private fun onToggleButtonPress(button: ToggleButton) {
		errorMessage = null

		when (button.type) {
			BOUNDARIES -> {
				button.isOn = !button.isOn
				ClientEventHandler.setRenderArea(area.name, button.isOn)
			}
			INTERACTIONS, PASSIVE_SPAWNS, HOSTILE_SPAWNS, EXPLOSIONS ->
				LandManager.NETWORK.sendToServer(MessageHomeToggle(pos, button.type))
		}
	}

	override fun init() {
		super.init()

		isOwner = area.isOwner(minecraft!!.player.uniqueID)

		inputTextField = object : TextFieldWidget(font, guiLeft + 99, guiTop + 15, 56, 10, "") {
			override fun charTyped(char: Char, keyCode: Int): Boolean {
				val result = super.charTyped(char, keyCode)
				if (result) {
					errorMessage = null
					updateActionButtons()
				} else if (keyCode == GLFW.GLFW_KEY_ENTER) {
					addMember(text)
				}
				return result
			}

			override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
				val result = super.mouseClicked(mouseX, mouseY, mouseButton)
				if (result) {
					listButtons.forEach { it.setSelected(false) }
					selectedMemberIndex = -1
					updateActionButtons()
					errorMessage = null
				} else {
					// Handle right clicking text field
					text = ""
					errorMessage = null
				}
				return result
			}
		}
		children += inputTextField

		listButtons = Array(PLAYER_LIST_SIZE) { addButton(ListButton(7, 16 + (it * 12), it)) }

		upButton = addButton(ArrowButton(97, 29, true))
		downButton = addButton(ArrowButton(97, 52, false))
		updatePlayerList()

		addButton = addButton(ActionButton(111, 29, "gui.landmanager.home.add", ADD))
		kickButton = addButton(ActionButton(111, 41, "gui.landmanager.home.kick", KICK))
		passButton = addButton(ActionButton(111, 53, "gui.landmanager.home.pass", PASS))
		updateActionButtons()

		boundariesToggle = addButton(ToggleButton(108, 70, ClientEventHandler.isAreaBeingRendered(area.name), BOUNDARIES).apply { active = true })
		interactionsToggle = addButton(ToggleButton(108, 84, area.interactions, INTERACTIONS))
		passivesToggle = addButton(ToggleButton(108, 98, area.canPassiveSpawn, PASSIVE_SPAWNS))
		hostilesToggle = addButton(ToggleButton(108, 112, area.canHostileSpawn, HOSTILE_SPAWNS))
		explosionsToggle = addButton(ToggleButton(108, 126, area.explosions, EXPLOSIONS))
		updateToggleButtons()
	}

	private inner class ListButton(x: Int, y: Int, val num: Int)
		: LMButton(x, y, 87, 11, 87, 144, "", { onListButtonPress(it as ListButton) }) {
		init {
			textOffset = 1
			drawWhenDisabled = false
			hasIcon = false
		}

		override fun getTextColour(): Int = 14737632

		fun setPlayer(player: Pair<UUID, String>?): Unit = player?.let {
			message = it.second
			active = true
			isOwner = isOwner(it.first)
			if (isOwner) {
				textOffset = 12
				setTooltip(
					TranslationTextComponent("gui.landmanager.home.owner").applyTextStyle(TextFormatting.GOLD),
					StringTextComponent(message)
				)
			} else {
				textOffset = 1
				setTooltip(StringTextComponent(message))
			}
		} ?: run {
			message = ""
			active = false
			isOwner = false
			setTooltip()
		}

		fun setSelected(selected: Boolean) {
			hasIcon = selected
		}

		override fun drawText(font: FontRenderer) {
			if (isOwner) {
				minecraft!!.textureManager.bindTexture(imageResLoc)
				blit(x + 1, y + 1, ownerIcon.x, ownerIcon.y, ownerIcon.width, ownerIcon.height)
			}
			drawStringWithMaxWidth(message, x + textOffset, y + (height - 8) / 2, 85 - textOffset, getTextColour(), true)
		}
	}

	private inner class ArrowButton(x: Int, y: Int, val isUp: Boolean)
		: LMButton(x, y, 10, 13, 162, 84, "", { onArrowButtonPress(it as ArrowButton) }) {
		override fun getIconY(): Int = if (isUp) iconY else iconY + height
	}

	private inner class ActionButton(x: Int, y: Int, text: String, val type: HomeGuiActionType)
		: LMButton(x, y, 45, 12, 162, 0, I18n.format(text), { onActionButtonPress(it as ActionButton) }) {
		init {
			textOffset = 12
		}

		override fun getIconY(): Int = iconY + (type.ordinal * height)
	}

	private inner class ToggleButton(x: Int, y: Int, var isOn: Boolean, val type: HomeGuiToggleType)
		: LMButton(x, y, 12, 12, 162, 36, "", { onToggleButtonPress(it as ToggleButton) }) {
		init {
			active = false
			drawWhenDisabled = true
		}

		override fun getIconY(): Int {
			var y = iconY
			if (!active)
				y += height * 2
			if (isOn)
				y += height
			return y
		}

		fun setActive(active: Boolean) {
			this.active = active
			if (active)
				setTooltip()
			else
				setTooltip(TranslationTextComponent("gui.landmanager.home.toggleDisabled"))
		}
	}
}
