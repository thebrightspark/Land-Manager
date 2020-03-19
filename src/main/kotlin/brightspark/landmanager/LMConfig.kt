package brightspark.landmanager

import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TextFormatting.*
import net.minecraftforge.common.ForgeConfigSpec

object LMConfig {
	val CLIENT: LMClientConfig
	val CLIENT_SPEC: ForgeConfigSpec
	val SERVER: LMServerConfig
	val SERVER_SPEC: ForgeConfigSpec

	//---- Client
	var showAllRadius: Int = 0
	var areaNameScale: Double = 0.0
	var showChatLogs: Boolean = false
	var areaBoxAlpha: Double = 0.0
	var areaBoxEdgeThickness: Double = 0.0
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
		areaNameScale = CLIENT.areaNameScale.get()
		showChatLogs = CLIENT.showChatLogs.get()
		areaBoxAlpha = CLIENT.areaBoxAlpha.get()
		areaBoxEdgeThickness = CLIENT.areaBoxEdgeThickness.get()
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
		private val COLOURS = listOf(BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE)
	}

	val showAllRadius = builder
		.comment("The radius within which nearby areas will show when /lmShow is showing all nearby areas")
		.defineInRange("showAllRadius", 16, 0, Int.MAX_VALUE)
	val areaNameScale = builder
		.comment("The scale of the area label that's rendered")
		.defineInRange("areaNameScale", 1.0, 0.0, Double.MAX_VALUE)
	val showChatLogs = builder
		.comment("Whether OPs will see area changes in their chat")
		.define("showChatLogs", true)
	val areaBoxAlpha = builder
		.comment("The alpha for the sides of area boxes rendered in the world")
		.defineInRange("areaBoxAlpha", 0.2, 0.0, 1.0)
	val areaBoxEdgeThickness = builder
		.comment("The thickness of area box edges rendered in the world")
		.defineInRange("areaBoxEdgeThickness", 0.025, 0.0, Double.MAX_VALUE)
	val titleOnAreaChange = builder
		.comment("Whether title messages should be displayed when moving into a different area")
		.define("titleOnAreaChange", true)
	val titleColourWilderness = builder
		.comment("The colour of the area change title when you move into the Wilderness")
		.defineEnum("titleColourWilderness", GRAY, COLOURS)
	val titleColourAreaMember = builder
		.comment("The colour of the area change title when you move into an area you're a member of")
		.defineEnum("titleColourAreaMember", GREEN, COLOURS)
	val titleColourAreaOutsider = builder
		.comment("The colour of the area change title when you move into an area you're not a member of")
		.defineEnum("titleColourAreaOutsider", RED, COLOURS)
}

class LMServerConfig(builder: ForgeConfigSpec.Builder) {
	val disableClaiming = builder
		.comment("Whether non-op players can claim chunks using '/lm claim'")
		.define("disableClaiming", false)
	val creativeIgnoresProtection = builder
		.comment("Whether non-op players in creative can break/place blocks in any area")
		.define("creativeIgnoresProtection", true)
	val maxAreasCanOwn = builder
		.comment(
			"The max number of areas a player can own",
			"Use -1 for no limit"
		)
		.defineInRange("maxAreasCanOwn", -1, -1, Int.MAX_VALUE)

	// Global Settings
	val canPassiveSpawn = builder
		.comment("Can passive entities spawn in global spaces")
		.define("canPassiveSpawn", true)
	val canHostileSpawn = builder
		.comment("Can hostile entities spawn in global spaces")
		.define("canHostileSpawn", true)
	val canExplosionsDestroyBlocks = builder
		.comment("Can explosions destroy global blocks")
		.define("canExplosionsDestroyBlocks", true)
	val canPlayersInteract = builder
		.comment("Can players interact with global blocks")
		.define("canPlayersInteract", true)
	val canPlayersBreakBlocks = builder
		.comment("Can players break global blocks")
		.define("canPlayersBreakBlocks", true)
	val canPlayersPlaceBlocks = builder
		.comment("Can players place global blocks")
		.define("canPlayersPlaceBlocks", true)

	// Permissions
	val claimRequest = builder
		.comment(
			"If true then the 'claim' command will create a request rather than take instant effect",
			"An OP will then need to use the 'approve' command to accept the request"
		)
		.define("claimRequest", false)
	val passiveSpawning = builder
		.comment("If area owners can toggle whether passive entities can spawn in the area")
		.define("passiveSpawning", false)
	val hostileSpawning = builder
		.comment("If area owners can toggle whether hostile entities can spawn in the area")
		.define("hostileSpawning", false)
	val explosions = builder
		.comment("If area owners can toggle whether explosions can destroy blocks in the area")
		.define("explosions", false)
	val interactions = builder
		.comment("If area owners can toggle whether other players can interact (right click) with blocks in the area")
		.define("interactions", false)
	val tool = builder
		.comment("If non-op players can use '/lm tool' to get the admin tool for creating areas")
		.define("tool", false)
	val rename = builder
		.comment("If area owners can rename their areas")
		.define("rename", false)
}
