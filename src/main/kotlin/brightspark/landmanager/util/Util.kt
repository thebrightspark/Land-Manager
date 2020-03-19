package brightspark.landmanager.util

import brightspark.ksparklib.api.appendStyledText
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.HoverEvent
import org.apache.commons.lang3.StringUtils

object Util {
	private const val arrowPaddingSize = 2
	private val arrowPadding = StringUtils.repeat(' ', arrowPaddingSize)
	private const val arrowSize = 4
	private val arrowLeft = StringUtils.repeat('<', arrowSize)
	private val arrowRight = StringUtils.repeat('>', arrowSize)
	private val arrowBlank = StringUtils.repeat('-', arrowSize)

	fun <T> createListMessage(
		senderIsPlayer: Boolean,
		list: List<T>,
		pageIn: Int,
		titleLangKey: String,
		arrowsCommandToRun: (Int) -> String,
		entryToText: (T) -> ITextComponent = { StringTextComponent(it.toString()) }
	): ITextComponent {
		// Get a view of exactly what to show on the page
		val view = ListView.create(list, pageIn, 8)
		val page = view.page
		val maxPage = view.pageMax

		// Create the text component
		val text = if (senderIsPlayer)
			createPageTitle(titleLangKey, page, maxPage)
		else
		// Print on a new line in the server console for readability
			StringTextComponent("\n").appendSibling(createPageTitle(titleLangKey, page, maxPage))

		view.list.forEach { text.appendText("\n").appendSibling(entryToText(it)) }

		// Don't need to add the arrows when sending back to the server console
		if (senderIsPlayer)
			createPageArrows(page, maxPage, arrowsCommandToRun)?.let {
				text.appendText("\n").appendSibling(it)
			}
		return text
	}

	private fun createPageTitle(titleLangKey: String, page: Int, maxPage: Int): ITextComponent =
		StringTextComponent("============= ").applyTextStyle(TextFormatting.YELLOW)
			.appendSibling(TranslationTextComponent(titleLangKey, page + 1, maxPage + 1).applyTextStyle(TextFormatting.GOLD))
			.appendStyledText(" =============", TextFormatting.YELLOW)

	private fun createPageArrows(page: Int, maxPage: Int, commandToRun: (Int) -> String): ITextComponent? {
		if (page < 0 || page > maxPage)
			return null
		return StringTextComponent(arrowPadding)
			.appendSibling(if (page > 0) createArrow(true, page, commandToRun) else createBlank())
			.appendText(arrowPadding)
			.appendSibling(if (page < maxPage) createArrow(false, page, commandToRun) else createBlank())
	}

	private fun createArrow(left: Boolean, page: Int, commandToRun: (Int) -> String): ITextComponent {
		val nextPage = if (left) page else page + 2
		return StringTextComponent(if (left) arrowLeft else arrowRight).apply {
			applyTextStyles(TextFormatting.BOLD, TextFormatting.YELLOW)
			style.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TranslationTextComponent("lm.command.page", nextPage))
			style.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToRun(nextPage))
		}
	}

	private fun createBlank(): ITextComponent = StringTextComponent(arrowBlank).applyTextStyle(TextFormatting.GOLD)
}
