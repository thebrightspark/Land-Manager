package brightspark.landmanager.util

import kotlin.math.max

class ListView<T> private constructor(val list: List<T>, val page: Int, val pageMax: Int) {
	companion object {
		fun <T> create(list: List<T>, pageIn: Int, maxPerPageIn: Int): ListView<T> {
			var page = max(0, pageIn)
			val maxPerPage = max(1, maxPerPageIn)
			val size = list.size
			// Need to add 1 to maxPerPage so that we don't have an empty extra page when size == maxPerPage
			val pageMax = size / (maxPerPage + 1)
			// We reduce the given page number by 1 because we calculate starting from page 0, but is shown to start from page 1
			if (page > 0)
				page--
			if (page * maxPerPage > size)
				page = pageMax
			// Work out the range to get from the list
			val min = page * maxPerPage
			var max = min + maxPerPage
			if (size < max)
				max = size
			return ListView(list.subList(min, max), page, pageMax)
		}
	}
}
