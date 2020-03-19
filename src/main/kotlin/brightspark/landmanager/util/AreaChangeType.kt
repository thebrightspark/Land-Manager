package brightspark.landmanager.util

enum class AreaChangeType {
	CREATE,
	DELETE,
	ALLOCATE,
	CLEAR_ALLOCATION,
	CLAIM;

	val unlocalisedName = "area.change.${name.toLowerCase()}"
}
