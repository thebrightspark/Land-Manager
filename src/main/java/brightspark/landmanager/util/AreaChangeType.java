package brightspark.landmanager.util;

public enum AreaChangeType {
	CREATE,
	DELETE,
	ALLOCATE,
	CLEAR_ALLOCATION,
	CLAIM;

	public String unlocalisedName = "area.change." + name().toLowerCase();
}
