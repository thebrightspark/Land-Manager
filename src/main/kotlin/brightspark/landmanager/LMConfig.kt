package brightspark.landmanager

import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TextFormatting.*
import net.minecraftforge.common.ForgeConfigSpec

object LMConfig {
	private val CLIENT: LMClientConfig
	val CLIENT_SPEC: ForgeConfigSpec
	private val SERVER: LMServerConfig
	val SERVER_SPEC: ForgeConfigSpec

	//---- Client
	var showAllRadius: Int = 0
	var showChatLogs: Boolean = false
	var areaNameScale: Double = 0.0
	var areaBoxAlpha: Double = 0.0
	var areaBoxEdgeThickness: Double = 0.0
	var areaBoxNearbySides: Boolean = false
	var titleOnAreaChange: Boolean = false
	lateinit var titleColourWilderness: TextFormatting
	lateinit var titleColourAreaMember: TextFormatting
	lateinit var titleColourAreaOutsider: TextFormatting

	//---- Server
	var disableClaiming: Boolean = false
	var creativeIgnoresProtection: Boolean = false
	var maxAreasCanOwn: Int = 0
	// Global Settings
	var canPassiveSpawn: Boolean = false
	var canHostileSpawn: Boolean = false
	var canExplosionsDestroyBlocks: Boolean = false
	var canPlayersInteract: Boolean = false
	var canPlayersBreakBlocks: Boolean = false
	var canPlayersPlaceBlocks: Boolean = false
	// Permissions
	var claimRequest: Boolean = false
	var passiveSpawning: Boolean = false
	var hostileSpawning: Boolean = false
	var explosions: Boolean = false
	var interactions: Boolean = false
	var tool: Boolean = false
	var rename: Boolean = false

	init {
		ForgeConfigSpec.Builder().configure { LMClientConfig(it) }.apply {
			CLIENT = left
			CLIENT_SPEC = right
		}
		ForgeConfigSpec.Builder().configure { LMServerConfig(it) }.apply {
			SERVER = left
			SERVER_SPEC = right
		}
	}

	fun bake() {
		// Client
		showAllRadius = CLIENT.showAllRadius.get()
		showChatLogs = CLIENT.showChatLogs.get()
		areaNameScale = CLIENT.areaNameScale.get()
		areaBoxAlpha = CLIENT.areaBoxAlpha.get()
		areaBoxEdgeThickness = CLIENT.areaBoxEdgeThickness.get()
		areaBoxNearbySides = CLIENT.areaBoxNearbySides.get()
		titleOnAreaChange = CLIENT.titleOnAreaChange.get()
		titleColourWilderness = CLIENT.titleColourWilderness.get()
		titleColourAreaMember = CLIENT.titleColourAreaMember.get()
		titleColourAreaOutsider = CLIENT.titleColourAreaOutsider.get()

		// Server
		disableClaiming = SERVER.disableClaiming.get()
		creativeIgnoresProtection = SERVER.creativeIgnoresProtection.get()
		maxAreasCanOwn = SERVER.maxAreasCanOwn.get()
		// Global
		canPassiveSpawn = SERVER.canPassiveSpawn.get()
		canHostileSpawn = SERVER.canHostileSpawn.get()
		canExplosionsDestroyBlocks = SERVER.canExplosionsDestroyBlocks.get()
		canPlayersInteract = SERVER.canPlayersInteract.get()
		canPlayersBreakBlocks = SERVER.canPlayersBreakBlocks.get()
		canPlayersPlaceBlocks = SERVER.canPlayersPlaceBlocks.get()
		// Permissions
		claimRequest = SERVER.claimRequest.get()
		passiveSpawning = SERVER.passiveSpawning.get()
		hostileSpawning = SERVER.hostileSpawning.get()
		explosions = SERVER.explosions.get()
		interactions = SERVER.interactions.get()
		tool = SERVER.tool.get()
		rename = SERVER.rename.get()
	}
}

class LMClientConfig(builder: ForgeConfigSpec.Builder) {
	companion object {
		private val COLOURS = listOf(
			BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA,
			RED, LIGHT_PURPLE, YELLOW, WHITE
		)
	}

	val showAllRadius: ForgeConfigSpec.IntValue = builder
		.comment("The radius within which nearby areas will show when /lmShow is showing all nearby areas")
		.defineInRange("showAllRadius", 16, 0, Int.MAX_VALUE)
	val showChatLogs: ForgeConfigSpec.BooleanValue = builder
		.comment("Whether OPs will see area changes in their chat")
		.define("showChatLogs", true)
	val areaNameScale: ForgeConfigSpec.DoubleValue = builder
		.comment("The scale of the area label that's rendered")
		.defineInRange("areaNameScale", 1.0, 0.0, Double.MAX_VALUE)
	val areaBoxAlpha: ForgeConfigSpec.DoubleValue = builder
		.comment("The alpha for the sides of area boxes rendered in the world")
		.defineInRange("areaBoxAlpha", 0.2, 0.0, 1.0)
	val areaBoxEdgeThickness: ForgeConfigSpec.DoubleValue = builder
		.comment("The thickness of area box edges rendered in the world")
		.defineInRange("areaBoxEdgeThickness", 0.025, 0.0, Double.MAX_VALUE)
	val areaBoxNearbySides: ForgeConfigSpec.BooleanValue = builder
		.comment("Whether to render area box sides when rendering nearby areas")
		.define("areaBoxNearbySides", true)
	val titleOnAreaChange: ForgeConfigSpec.BooleanValue = builder
		.comment("Whether title messages should be displayed when moving into a different area")
		.define("titleOnAreaChange", true)
	val titleColourWilderness: ForgeConfigSpec.EnumValue<TextFormatting> = builder
		.comment("The colour of the area change title when you move into the Wilderness")
		.defineEnum("titleColourWilderness", GRAY, COLOURS)
	val titleColourAreaMember: ForgeConfigSpec.EnumValue<TextFormatting> = builder
		.comment("The colour of the area change title when you move into an area you're a member of")
		.defineEnum("titleColourAreaMember", GREEN, COLOURS)
	val titleColourAreaOutsider: ForgeConfigSpec.EnumValue<TextFormatting> = builder
		.comment("The colour of the area change title when you move into an area you're not a member of")
		.defineEnum("titleColourAreaOutsider", RED, COLOURS)
}

class LMServerConfig(builder: ForgeConfigSpec.Builder) {
	val disableClaiming: ForgeConfigSpec.BooleanValue = builder
		.comment("Whether non-op players can claim chunks using '/lm claim'")
		.define("disableClaiming", false)
	val creativeIgnoresProtection: ForgeConfigSpec.BooleanValue = builder
		.comment("Whether non-op players in creative can break/place blocks in any area")
		.define("creativeIgnoresProtection", true)
	val maxAreasCanOwn: ForgeConfigSpec.IntValue = builder
		.comment(
			"The max number of areas a player can own",
			"Use -1 for no limit"
		)
		.defineInRange("maxAreasCanOwn", -1, -1, Int.MAX_VALUE)

	// Global Settings
	val canPassiveSpawn: ForgeConfigSpec.BooleanValue = builder
		.comment("Can passive entities spawn in global spaces")
		.define("canPassiveSpawn", true)
	val canHostileSpawn: ForgeConfigSpec.BooleanValue = builder
		.comment("Can hostile entities spawn in global spaces")
		.define("canHostileSpawn", true)
	val canExplosionsDestroyBlocks: ForgeConfigSpec.BooleanValue = builder
		.comment("Can explosions destroy global blocks")
		.define("canExplosionsDestroyBlocks", true)
	val canPlayersInteract: ForgeConfigSpec.BooleanValue = builder
		.comment("Can players interact with global blocks")
		.define("canPlayersInteract", true)
	val canPlayersBreakBlocks: ForgeConfigSpec.BooleanValue = builder
		.comment("Can players break global blocks")
		.define("canPlayersBreakBlocks", true)
	val canPlayersPlaceBlocks: ForgeConfigSpec.BooleanValue = builder
		.comment("Can players place global blocks")
		.define("canPlayersPlaceBlocks", true)

	// Permissions
	val claimRequest: ForgeConfigSpec.BooleanValue = builder
		.comment(
			"If true then the 'claim' command will create a request rather than take instant effect",
			"An OP will then need to use the 'approve' command to accept the request"
		)
		.define("claimRequest", false)
	val passiveSpawning: ForgeConfigSpec.BooleanValue = builder
		.comment("If area owners can toggle whether passive entities can spawn in the area")
		.define("passiveSpawning", false)
	val hostileSpawning: ForgeConfigSpec.BooleanValue = builder
		.comment("If area owners can toggle whether hostile entities can spawn in the area")
		.define("hostileSpawning", false)
	val explosions: ForgeConfigSpec.BooleanValue = builder
		.comment("If area owners can toggle whether explosions can destroy blocks in the area")
		.define("explosions", false)
	val interactions: ForgeConfigSpec.BooleanValue = builder
		.comment("If area owners can toggle whether other players can interact (right click) with blocks in the area")
		.define("interactions", false)
	val tool: ForgeConfigSpec.BooleanValue = builder
		.comment("If non-op players can use '/lm tool' to get the admin tool for creating areas")
		.define("tool", false)
	val rename: ForgeConfigSpec.BooleanValue = builder
		.comment("If area owners can rename their areas")
		.define("rename", false)
}
