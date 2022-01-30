package brightspark.landmanager.util

class Cached<T>(private val supplier: () -> T) {
	private var value: T? = null

	fun get(): T = value ?: run(supplier).also { value = it }

	fun clear() {
		value = null
	}
}
